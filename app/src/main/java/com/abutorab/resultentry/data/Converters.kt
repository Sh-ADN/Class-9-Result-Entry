package com.abutorab.resultentry.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toSyncState(value: String?): SyncState = try {
        if (value != null) SyncState.valueOf(value) else SyncState.DRAFT
    } catch (e: Exception) {
        SyncState.DRAFT
    }

    @TypeConverter
    fun fromSyncState(value: SyncState?): String = (value ?: SyncState.DRAFT).name
}
