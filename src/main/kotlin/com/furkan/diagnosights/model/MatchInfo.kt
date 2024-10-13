package com.furkan.diagnosights.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("match_info")
@CompoundIndexes(value = [
    CompoundIndex(name = "hash_index", def = "{'resistantAntibioticsHashValue' : 1, 'susceptibleAntibioticsHashValue': 1, 'susceptibleAtHighDoseAntibioticsHashValue': 1}", unique = true),
])data class MatchInfo(
    @Id
    val id:String? = null,
    @Indexed
    val resistantAntibioticsHashValue: String,
    @Indexed
    val susceptibleAntibioticsHashValue: String,
    @Indexed
    val susceptibleAtHighDoseAntibioticsHashValue: String,
    val matchRecords:Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatchInfo

        if (id != other.id) return false
        if (resistantAntibioticsHashValue != other.resistantAntibioticsHashValue) return false
        if (susceptibleAntibioticsHashValue != other.susceptibleAntibioticsHashValue) return false
        if (susceptibleAtHighDoseAntibioticsHashValue != other.susceptibleAtHighDoseAntibioticsHashValue) return false
        if (!matchRecords.contentEquals(other.matchRecords)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + resistantAntibioticsHashValue.hashCode()
        result = 31 * result + susceptibleAntibioticsHashValue.hashCode()
        result = 31 * result + susceptibleAtHighDoseAntibioticsHashValue.hashCode()
        result = 31 * result + matchRecords.contentHashCode()
        return result
    }
}