package com.furkan.diagnosights.config

import com.furkan.diagnosights.config.LabRecordsConstants.Companion.ANTIBIOTIC_NAME_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.BARCODE_DATE_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.BARCODE_ID_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.DEPARTMENT_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.ORGANISM_NAME_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.PATIENT_NAME_SURNAME_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.REQUEST_DATE_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.RESULT_COLUMN_HEADER
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.RESULT_RESISTANT_VALUE
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.RESULT_SUSCEPTIBLE_AT_HIGH_DOSE_VALUE
import com.furkan.diagnosights.config.LabRecordsConstants.Companion.RESULT_SUSCEPTIBLE_VALUE
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import java.time.Instant

class LabRecordsConstants {

    companion object {

        const val BARCODE_ID_COLUMN_HEADER = "barkod no"
        const val PATIENT_NAME_SURNAME_COLUMN_HEADER = "hasta adi soyadi"
        const val DEPARTMENT_COLUMN_HEADER = "birim"
        const val ORGANISM_NAME_COLUMN_HEADER = "organizma adı"
        const val REQUEST_DATE_COLUMN_HEADER = "istem tarihi"
        const val BARCODE_DATE_COLUMN_HEADER = "barkod tarihi"
        const val ANTIBIOTIC_NAME_COLUMN_HEADER = "antibiyotik adı"
        const val RESULT_COLUMN_HEADER = "sonuç şablon"
        const val RESULT_RESISTANT_VALUE = "dirençli"
        const val RESULT_SUSCEPTIBLE_VALUE = "duyarlı"
        const val RESULT_SUSCEPTIBLE_AT_HIGH_DOSE_VALUE = "yüksek dozda duyarlı"

    }


}


enum class RecordColumn(val header: String) {
    BARCODE_ID(BARCODE_ID_COLUMN_HEADER),
    PATIENT_NAME_SURNAME(PATIENT_NAME_SURNAME_COLUMN_HEADER),
    DEPARTMENT(DEPARTMENT_COLUMN_HEADER),
    ORGANISM_NAME(ORGANISM_NAME_COLUMN_HEADER),
    REQUEST_DATE(REQUEST_DATE_COLUMN_HEADER),
    BARCODE_DATE(BARCODE_DATE_COLUMN_HEADER),
    ANTIBIOTIC_NAME(ANTIBIOTIC_NAME_COLUMN_HEADER),
    RESULT(RESULT_COLUMN_HEADER);
}

enum class TestResult(val value: String) {
    RESISTANT(RESULT_RESISTANT_VALUE),
    SUSCEPTIBLE(RESULT_SUSCEPTIBLE_VALUE),
    SUSCEPTIBLE_AT_HIGH_DOSE(RESULT_SUSCEPTIBLE_AT_HIGH_DOSE_VALUE)
}