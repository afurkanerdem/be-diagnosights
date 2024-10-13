package com.furkan.diagnosights.service

import com.furkan.diagnosights.model.LabRecord
import com.furkan.diagnosights.model.MatchInfo
import com.furkan.diagnosights.repository.MatchInfoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MatchInfoService {

    @Autowired
    private lateinit var matchInfoRepository: MatchInfoRepository

    suspend fun updateMatchInfo(labRecords: List<LabRecord>) {
        val derivedMatches = labRecords.map {  deriveMatchInfo(it) }
        matchInfoRepository.upsertMatchInfo(derivedMatches)
    }

    suspend fun deleteRecord(barcodeId: String) {
        matchInfoRepository.deleteRecord(barcodeId)
    }

    suspend fun getMatchInfo(labRecord: LabRecord) : MatchInfo? {
        val matchInfo = deriveMatchInfo(labRecord)
        return matchInfoRepository.getMatchInfo(matchInfo)
    }

    private fun deriveMatchInfo(labRecord: LabRecord): MatchInfo {

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



    private fun hashArrayIrrespectiveOrder(values: Array<String>) =
        hashSetOf(*values).hashCode().toString()
}