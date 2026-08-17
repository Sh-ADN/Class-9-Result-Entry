package com.abutorab.resultentry.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ResultRepository(
    private val resultDao: ResultDao,
    private val networkManager: NetworkManager
) {
    fun getStudentResults(section: String): Flow<List<StudentResult>> {
        return combine(
            resultDao.getRosterFlow(section),
            resultDao.getEntriesFlow(section)
        ) { rosterList, entryList ->
            val entryMap = entryList.associateBy { it.roll }
            rosterList.map { roster ->
                StudentResult(roster, entryMap[roster.roll])
            }
        }
    }

    suspend fun refreshRoster(section: String) {
        val remoteRoster = networkManager.getRoster(section)
        resultDao.insertRoster(remoteRoster)
    }

    suspend fun refreshEntries(section: String) {
        val remoteEntries = networkManager.getEntries(section)
        val localDrafts = resultDao.getEntriesBySyncState(SyncState.DRAFT).associateBy { it.roll }
        val toInsert = remoteEntries.map { remote ->
            if (localDrafts.containsKey(remote.roll) && localDrafts[remote.roll]!!.section == section) {
                localDrafts[remote.roll]!! // Keep local draft
            } else {
                remote
            }
        }
        resultDao.insertEntries(toInsert)
    }
    
    suspend fun saveEntryLocally(entry: Entry) {
        resultDao.insertEntry(entry)
    }
    
    suspend fun syncDrafts() {
        val drafts = resultDao.getEntriesBySyncState(SyncState.DRAFT)
        val errors = resultDao.getEntriesBySyncState(SyncState.ERROR)
        val toSync = drafts + errors
        
        for (entry in toSync) {
            try {
                val success = networkManager.saveEntry(entry)
                if (success) {
                    resultDao.insertEntry(entry.copy(syncState = SyncState.SYNCED))
                } else {
                    resultDao.insertEntry(entry.copy(syncState = SyncState.ERROR))
                }
            } catch (e: Exception) {
                resultDao.insertEntry(entry.copy(syncState = SyncState.ERROR))
            }
        }
    }
}
