package com.furkan.diagnosights.controller

import com.furkan.diagnosights.model.LabRecord
import com.furkan.diagnosights.model.MatchInfo
import com.furkan.diagnosights.service.LabRecordsService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


@RestController
@RequestMapping("/")
class LabRecordsController {


    @Autowired
    private lateinit var labRecordsService: LabRecordsService

    @PostMapping(value = ["/uploadLabRecords"], consumes = ["multipart/form-data"])
    suspend fun uploadLabRecords(@RequestPart("files") filePartFlux: Flux<FilePart>) {
        labRecordsService.parseFileStreamToExcel(filePartFlux)
    }

    //read list of records with pagination

    @GetMapping("/records", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getRecords(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Long?,
        @RequestParam(required = false) timeIntervalStart: String?,
        @RequestParam(required = false) timeIntervalEnd: String?
    ): List<Pair<LabRecord, MatchInfo?>> {

        val timeIntervalStartTime =
            timeIntervalStart?.let { OffsetDateTime.parse(timeIntervalStart, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
        val timeIntervalEndTime =
            timeIntervalEnd?.let { OffsetDateTime.parse(timeIntervalEnd, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }

        return labRecordsService.getRecords(limit, offset, timeIntervalStartTime, timeIntervalEndTime)
    }

    //delete

    @DeleteMapping("/records/{barcodeId}")
    suspend fun deleteRecord(@PathVariable barcodeId: String) {
        labRecordsService.deleteRecord(barcodeId)
    }

    @PostMapping("/records/batchDelete")
    suspend fun batchDelete(@RequestBody barcodeIds: List<String>) {
        return labRecordsService.deleteRecords(barcodeIds)
    }


}