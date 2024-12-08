package com.furkan.diagnosights.service

import com.furkan.diagnosights.model.LabRecord
import com.furkan.diagnosights.model.MatchInfo
import com.furkan.diagnosights.repository.MatchInfoRepository
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MatchInfoService {

    @Autowired
    private lateinit var matchInfoRepository: MatchInfoRepository

    private val cache: Cache<String, MatchInfo> = Caffeine.newBuilder()
        .maximumSize(1000)
        .build()

    suspend fun updateMatchInfo(labRecords: List<LabRecord>) {

        val derivedMatches = labRecords.map { deriveMatchInfo(it) }

        derivedMatches.forEach {
            it?.let {
                val matchKey = generateCacheKeyOfMatchInfo(it)
                cache.invalidate(matchKey)
            }
        }

        matchInfoRepository.upsertMatchInfo(derivedMatches.filterNotNull())
    }

    suspend fun deleteRecord(barcodeId: String) {
        matchInfoRepository.deleteRecord(barcodeId)
    }

    suspend fun getMatchInfo(labRecord: LabRecord): MatchInfo? {

        val matchInfo = deriveMatchInfo(labRecord)
        return matchInfo?.let { derivedMatch ->

            val matchKey = generateCacheKeyOfMatchInfo(derivedMatch)
            val cachedMatchedInfo: MatchInfo? = cache.getIfPresent(matchKey)

            if (cachedMatchedInfo != null) return cachedMatchedInfo

            return matchInfoRepository.getMatchInfo(matchInfo)
                .also { retrievedMatch -> retrievedMatch?.let { cache.put(matchKey, it) } }
        }
    }

    private fun deriveMatchInfo(labRecord: LabRecord): MatchInfo? {

        if (labRecord.resistantAntibiotics.isEmpty() && labRecord.susceptibleAntibiotics.isEmpty() && labRecord.susceptibleAtHighDoseAntibiotics.isEmpty()) return null

        val resistantAntibioticsHash = hashArrayIrrespectiveOrder(labRecord.resistantAntibiotics)
        val susceptibleAntibioticsHash = hashArrayIrrespectiveOrder(labRecord.susceptibleAntibiotics)
        val susceptibleAtHighDoseAntibioticsHash =
            hashArrayIrrespectiveOrder(labRecord.susceptibleAtHighDoseAntibiotics)

        return MatchInfo(
            resistantAntibioticsHashValue = resistantAntibioticsHash,
            susceptibleAntibioticsHashValue = susceptibleAntibioticsHash,
            susceptibleAtHighDoseAntibioticsHashValue = susceptibleAtHighDoseAntibioticsHash,
            matchRecords = arrayOf(labRecord.barcodeId)
        )
    }


    fun hashArrayIrrespectiveOrder(values: Array<String>) =
        hashSetOf(*values).hashCode().toString()


     fun generateCacheKeyOfMatchInfo(matchInfo: MatchInfo): String {
        return "${matchInfo.resistantAntibioticsHashValue}_${matchInfo.susceptibleAntibioticsHashValue}_${matchInfo.susceptibleAtHighDoseAntibioticsHashValue}"
    }

}


