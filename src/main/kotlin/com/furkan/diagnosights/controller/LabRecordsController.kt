package com.furkan.diagnosights.controller

import com.furkan.diagnosights.model.LabRecord
import com.furkan.diagnosights.model.MatchInfo
import com.furkan.diagnosights.service.LabRecordsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.time.LocalDate
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
        @RequestParam(required = false) timeIntervalEnd: String?,
        @RequestParam(required = false) patientNameSurname: String?
    ): ApiResponse {

        val timeIntervalStartTime =
            timeIntervalStart?.let { LocalDate.parse(timeIntervalStart) }
        val timeIntervalEndTime =
            timeIntervalEnd?.let { LocalDate.parse(timeIntervalEnd) }

        return labRecordsService.getRecords(limit, offset, timeIntervalStartTime, timeIntervalEndTime, patientNameSurname)?.let {
            ApiResponse(it.second, it.first)
        } ?: ApiResponse(0, emptyList())
    }

    @PostMapping("/batchRecords", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getBatchRecords(
        @RequestBody barcodeIds: List<String>
    ): List<LabRecord>?{
       return labRecordsService.getBatchRecords(barcodeIds) ?: emptyList()
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

    class ApiResponse(val totalCount: Long, val records: List<LabRecordsService.LabRecordWithMatchInfo>)
}