package com.furkan.diagnosights.repository

import com.furkan.diagnosights.model.LabRecord
import com.furkan.diagnosights.model.MatchInfo
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
class MatchInfoRepository @Autowired constructor(val template: ReactiveMongoTemplate) {

    private val logger = LogManager.getLogger(MatchInfoRepository::class.java)

    init {
        if (template.converter is MappingMongoConverter) {
            (template.converter as MappingMongoConverter).setMapKeyDotReplacement("-DOT")
        }
    }


    suspend fun upsertMatchInfo(matches: List<MatchInfo>) {

        var bulkOps = template.bulkOps(BulkOperations.BulkMode.ORDERED, MatchInfo::class.java)

        for (match in matches) {

            bulkOps.updateOne(
                Query.query(where("matchRecords").`is`(match.matchRecords[0])),
                Update().pull("matchRecords", match.matchRecords[0]))


            bulkOps.upsert(
                Query.query(
                    where("resistantAntibioticsHashValue").`is`(match.resistantAntibioticsHashValue)
                        .and("susceptibleAntibioticsHashValue").`is`(match.susceptibleAntibioticsHashValue)
                        .and("susceptibleAtHighDoseAntibioticsHashValue")
                        .`is`(match.susceptibleAtHighDoseAntibioticsHashValue)
                ),
                Update()
                    .addToSet("matchRecords").each(match.matchRecords)

            )
        }

        bulkOps.execute().awaitLast()
        logger.info("Upserted ${matches.size} match records")


    }

    suspend fun deleteRecord(barcodeId: String) {
        template.updateFirst(
            Query.query(where("matchRecords").`is`(barcodeId)),
            Update().pull("matchRecords", barcodeId),
            MatchInfo::class.java
        ).awaitSingleOrNull()
    }

    suspend fun getMatchInfo(matchInfo: MatchInfo): MatchInfo? {
        return template.findOne(
            Query.query(
                where("resistantAntibioticsHashValue").`is`(matchInfo.resistantAntibioticsHashValue)
                    .and("susceptibleAntibioticsHashValue").`is`(matchInfo.susceptibleAntibioticsHashValue)
                    .and("susceptibleAtHighDoseAntibioticsHashValue").`is`(matchInfo.susceptibleAtHighDoseAntibioticsHashValue)
                    .and("matchRecords.1").exists(true)
            ), MatchInfo::class.java
        ).awaitSingleOrNull()
    }


}