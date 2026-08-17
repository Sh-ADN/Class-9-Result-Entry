package com.abutorab.resultentry.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toSyncState(value: String): SyncState = enumValueOf(value)

    @TypeConverter
    fun fromSyncState(value: SyncState): String = value.name
}
