package com.abutorab.resultentry.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Query("SELECT * FROM roster WHERE section = :section ORDER BY roll ASC")
    fun getRosterFlow(section: String): Flow<List<Roster>>
    
    @Query("SELECT * FROM roster WHERE section = :section ORDER BY roll ASC")
    suspend fun getRoster(section: String): List<Roster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoster(roster: List<Roster>)

    @Query("SELECT * FROM entry WHERE section = :section")
    fun getEntriesFlow(section: String): Flow<List<Entry>>
    
    @Query("SELECT * FROM entry WHERE section = :section")
    suspend fun getEntries(section: String): List<Entry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<Entry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: Entry)

    @Query("SELECT * FROM entry WHERE syncState = :syncState")
    suspend fun getEntriesBySyncState(syncState: SyncState): List<Entry>
}
