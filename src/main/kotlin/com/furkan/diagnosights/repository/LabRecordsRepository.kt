package com.furkan.diagnosights.repository

import com.furkan.diagnosights.model.LabRecord
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.awaitSingleOrNull
import lombok.extern.log4j.Log4j2
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Log4j2
@Repository
class LabRecordsRepository @Autowired constructor(val template: ReactiveMongoTemplate) {

    private val logger = LogManager.getLogger(LabRecordsRepository::class.java)

    init {
        if (template.converter is MappingMongoConverter) {
            (template.converter as MappingMongoConverter).setMapKeyDotReplacement("-DOT")
        }
    }


    suspend fun upsertLabRecords(labRecords: List<LabRecord>) {

        var bulkOps = template.bulkOps(BulkOperations.BulkMode.ORDERED, LabRecord::class.java)

        for (labRecord in labRecords) {
            bulkOps.upsert(
                Query.query(where("_id").`is`(labRecord.barcodeId)),
                Update()
                    .set("uploadDate", labRecord.uploadDate)
                    .addToSet("resistantAntibiotics").each(*labRecord.resistantAntibiotics)
                    .addToSet("susceptibleAntibiotics").each(*labRecord.susceptibleAntibiotics)
                    .addToSet("susceptibleAtHighDoseAntibiotics").each(*labRecord.susceptibleAtHighDoseAntibiotics)
                    .setOnInsert("organismName", labRecord.organismName)
                    .setOnInsert("barcodeId", labRecord.barcodeId)
                    .setOnInsert("patientNameSurname", labRecord.patientNameSurname)
                    .setOnInsert("department", labRecord.department)
                    .setOnInsert("requestDate", labRecord.requestDate)
                    .setOnInsert("barcodeDate", labRecord.barcodeDate)
            )
        }
        val insertedRecords = bulkOps.execute().awaitLast()
        logger.info("Upserted ${insertedRecords?.upserts?.size} lab records")

    }

    suspend fun getRecords(limit: Int?, offset: Long?, timeIntervalStart: OffsetDateTime?, timeIntervalEnd: OffsetDateTime?): List<LabRecord> {

        val query = Query()

        if (timeIntervalStart != null) {
            query.addCriteria(where("barcodeDate").gte(timeIntervalStart))
        }
        if (timeIntervalEnd != null) {
            query.addCriteria(where("barcodeDate").lte(timeIntervalEnd))
        }

        query.with(Sort.by(Sort.Direction.DESC, "barcodeDate"))
        query.limit(limit?:20)
        query.skip(offset?:0)

        return template.find(query, LabRecord::class.java).collectList().awaitSingleOrNull() ?: emptyList()

    }

    suspend fun getRecordByBarcodeId(barcodeId: String): LabRecord? {

        return template.findOne(Query.query(where("_id").`is`(barcodeId)), LabRecord::class.java).awaitSingleOrNull()
    }

    suspend fun deleteRecord(barcodeId: String) {

        template.remove(Query.query(where("_id").`is`(barcodeId)), LabRecord::class.java).awaitSingleOrNull()
    }

    suspend fun deleteRecords(barcodeIds: List<String>) {

        template.remove(Query.query(where("_id").`in`(barcodeIds)), LabRecord::class.java).awaitSingleOrNull()
    }


}