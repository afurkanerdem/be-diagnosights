package com.furkan.diagnosights.service

import com.furkan.diagnosights.config.RecordColumn
import com.furkan.diagnosights.config.TestResult
import com.furkan.diagnosights.model.LabRecord
import com.furkan.diagnosights.model.MatchInfo
import com.furkan.diagnosights.repository.LabRecordsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime


@Service
class LabRecordsService {

    @Autowired
    private lateinit var labRecordsRepository: LabRecordsRepository

    @Autowired
    private lateinit var matchInfoService: MatchInfoService

    @OptIn(ExperimentalCoroutinesApi::class)
    public suspend fun parseFileStreamToExcel(filePartFlux: Flux<FilePart>) {

        val fileNameToContentMap: Map<String, ByteArrayOutputStream> = filePartFlux.asFlow().map {
            it.content().asFlow().map { df -> it.filename() to df }
        }.flattenConcat().fold(HashMap<String, ByteArrayOutputStream>()) { acc, it ->

            val fileName = it.first
            val dataBuffer = it.second

            val bytes = ByteArray(dataBuffer.readableByteCount())
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);

            acc[fileName] = (acc[fileName] ?: ByteArrayOutputStream()).use { os -> os.write(bytes); return@use os }

            return@fold acc
        }

        fileNameToContentMap.forEach { (fileName, content) ->

            val fileStream: InputStream = ByteArrayInputStream(content.toByteArray())

            val workbook: Workbook = HSSFWorkbook(fileStream)

            for (sheet in workbook) {

                processSheet(sheet)
            }
        }
    }

    private suspend fun processSheet(sheet: Sheet) {

        val indexToColName = mutableMapOf<Int, RecordColumn>()
        val rowsIterator = sheet.rowIterator()
        val headerRow = rowsIterator.next()

        for (i in 0 until headerRow.lastCellNum) {
            val headerValue = headerRow.getCell(i).toString().lowercase()
            val column = getColumnByHeaderValue(headerValue)
            column?.let { indexToColName[i] = it }
        }

        val listOfRows: MutableList<Map<RecordColumn, String>> = ArrayList()

        for (row in rowsIterator) {

            val rowMap = mutableMapOf<RecordColumn, String>()

            for (i in 0 until row.lastCellNum) {
                indexToColName[i]?.let { rowMap[it] = row.getCell(i).toString() }
            }

            listOfRows.add(rowMap)
        }

        val groupedByBarcode = listOfRows.groupBy { it[RecordColumn.BARCODE_ID] }

        val labRecords: List<LabRecord> = groupedByBarcode.entries.filter { it.value.isNotEmpty() }.mapNotNull { it ->

            val recordsWithSameBarcode = it.value
            var resistantAntibiotics: MutableSet<String> = HashSet()
            var susceptibleAntibiotics: MutableSet<String> = HashSet()
            var susceptibleAtHighDoseAntibiotics: MutableSet<String> = HashSet()

            for (rec in recordsWithSameBarcode) {

                val antibioticName = rec[RecordColumn.ANTIBIOTIC_NAME]!!

                if (antibioticName.isEmpty()) continue

                when (getTestResultByValue(rec[RecordColumn.RESULT]!!.lowercase())) {
                    TestResult.RESISTANT-> resistantAntibiotics.add(antibioticName)
                    TestResult.SUSCEPTIBLE -> susceptibleAntibiotics.add(antibioticName)
                    TestResult.SUSCEPTIBLE_AT_HIGH_DOSE -> susceptibleAtHighDoseAntibiotics.add(antibioticName)
                    else -> continue
                }
            }

            return@mapNotNull populateLabRecord(
                recordsWithSameBarcode[0],
                resistantAntibiotics.toTypedArray(),
                susceptibleAntibiotics.toTypedArray(),
                susceptibleAtHighDoseAntibiotics.toTypedArray()
            )
        }

        labRecordsRepository.upsertLabRecords(labRecords)

        matchInfoService.updateMatchInfo(labRecords)
    }

    private fun populateLabRecord(
        recordMap: Map<RecordColumn, String>,
        resistantAntibiotics: Array<String>,
        susceptibleAntibiotics: Array<String>,
        susceptibleAtHighDoseAntibiotics: Array<String>
    ): LabRecord? {

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss") // 05.07.2024 10:32:00
        try {
            val barcodeDate: Instant =
                dateFormat.parse(recordMap[RecordColumn.BARCODE_DATE]!!.trim().replace("\n", " ")).toInstant()
            val requestDate: Instant =
                dateFormat.parse(recordMap[RecordColumn.REQUEST_DATE]!!.trim().replace("\n", " ")).toInstant()

            return LabRecord(
                recordMap[RecordColumn.BARCODE_ID]!!,
                recordMap[RecordColumn.PATIENT_NAME_SURNAME]!!,
                recordMap[RecordColumn.DEPARTMENT]!!,
                recordMap[RecordColumn.ORGANISM_NAME]!!,
                Instant.now(),
                barcodeDate,
                requestDate,
                resistantAntibiotics,
                susceptibleAntibiotics,
                susceptibleAtHighDoseAntibiotics
            )
        } catch (e: Exception) {
            println("row with error $recordMap")
        }


        return null;

    }


    fun getColumnByHeaderValue(header: String): RecordColumn? {
        return RecordColumn.entries.map { it }.map { it to FuzzySearch.ratio(it.header, header) }
            .filter { it.second > 80 }.maxByOrNull { it.second }?.first
    }

    fun getTestResultByValue(value: String): TestResult? {
        return TestResult.entries.map { it }.map { it to FuzzySearch.ratio(it.value, value) }
            .filter { it.second > 80 }.maxByOrNull { it.second }?.first
    }

    suspend fun getRecords(limit: Int?, offset: Long?, timeIntervalStart: OffsetDateTime?, timeIntervalEnd: OffsetDateTime?): List<Pair<LabRecord, MatchInfo?>> {

        return labRecordsRepository.getRecords(limit, offset, timeIntervalStart, timeIntervalEnd)
            .map { rec -> rec to matchInfoService.getMatchInfo(rec) }
    }

    suspend fun deleteRecord(barcodeId: String) {
        labRecordsRepository.deleteRecord(barcodeId)
        matchInfoService.deleteRecord(barcodeId)
    }

   suspend fun deleteRecords(barcodeIds: List<String>) {
        labRecordsRepository.deleteRecords(barcodeIds)
    }


}
