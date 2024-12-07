package com.furkan.diagnosights.model

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

import org.springframework.data.annotation.Id
import java.time.Instant
import java.util.*

@Document("lab_records")
data class LabRecord(
    @Id
    val barcodeId: String,
    @Indexed
    val patientNameSurname: String,
    val department: String,
    @Indexed
    val organismName: String,
    val uploadDate: Instant = Instant.now(),
    val requestDate: Instant,
    val barcodeDate: Instant,
    @Indexed
    val resistantAntibiotics: Array<String>,
    @Indexed
    val susceptibleAntibiotics: Array<String>,
    @Indexed
    val susceptibleAtHighDoseAntibiotics: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LabRecord

        if (barcodeId != other.barcodeId) return false
        if (patientNameSurname != other.patientNameSurname) return false
        if (department != other.department) return false
        if (organismName != other.organismName) return false
        if (uploadDate != other.uploadDate) return false
        if (requestDate != other.requestDate) return false
        if (barcodeDate != other.barcodeDate) return false
        if (!resistantAntibiotics.contentEquals(other.resistantAntibiotics)) return false
        if (!susceptibleAntibiotics.contentEquals(other.susceptibleAntibiotics)) return false
        if (!susceptibleAtHighDoseAntibiotics.contentEquals(other.susceptibleAtHighDoseAntibiotics)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = barcodeId.hashCode()
        result = 31 * result + patientNameSurname.hashCode()
        result = 31 * result + department.hashCode()
        result = 31 * result + organismName.hashCode()
        result = 31 * result + uploadDate.hashCode()
        result = 31 * result + requestDate.hashCode()
        result = 31 * result + barcodeDate.hashCode()
        result = 31 * result + resistantAntibiotics.contentHashCode()
        result = 31 * result + susceptibleAntibiotics.contentHashCode()
        result = 31 * result + susceptibleAtHighDoseAntibiotics.contentHashCode()
        return result
    }
}
