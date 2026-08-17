package com.abutorab.resultentry.data

import androidx.room.Entity

enum class SyncState { DRAFT, SYNCED, ERROR }

@Entity(primaryKeys = ["roll", "section"], tableName = "roster")
data class Roster(
    val roll: Int,
    val section: String,
    val name: String
)

@Entity(primaryKeys = ["roll", "section"], tableName = "entry")
data class Entry(
    val roll: Int,
    val section: String,
    val totalMarks: Int,
    val failedCount: Int,
    val gpa: Double?,
    val syncState: SyncState,
    val localUpdatedAt: Long
)

data class StudentResult(
    val roster: Roster,
    val entry: Entry?
) {
    val status: String
        get() = when (entry?.syncState) {
            null -> "Not started"
            SyncState.DRAFT -> "Draft"
            SyncState.ERROR -> "Draft (Error)"
            SyncState.SYNCED -> "Synced"
        }
}
