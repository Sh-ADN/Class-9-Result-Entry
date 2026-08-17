package com.abutorab.resultentry.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.abutorab.resultentry.MyApplication
import com.abutorab.resultentry.data.Entry
import com.abutorab.resultentry.data.ResultRepository
import com.abutorab.resultentry.data.StudentResult
import com.abutorab.resultentry.data.SyncState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ResultRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _section = MutableStateFlow<String?>(sharedPrefs.getString("section", null))
    val section: StateFlow<String?> = _section.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val studentResults: StateFlow<List<StudentResult>> = _section
        .flatMapLatest { sec ->
            if (sec != null) repository.getStudentResults(sec) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    init {
        if (_section.value != null) {
            refreshData()
            syncNow()
        }
    }
    
    fun setSection(newSection: String) {
        sharedPrefs.edit().putString("section", newSection).apply()
        _section.value = newSection
        refreshData()
    }
    
    fun refreshData() {
        val sec = _section.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshRoster(sec)
                repository.refreshEntries(sec)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.syncDrafts()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }
    
    fun saveEntry(roll: Int, totalMarks: Int, failedCount: Int, gpa: Double?) {
        val sec = _section.value ?: return
        viewModelScope.launch {
            val entry = Entry(
                roll = roll,
                section = sec,
                totalMarks = totalMarks,
                failedCount = failedCount,
                gpa = gpa,
                syncState = SyncState.DRAFT,
                localUpdatedAt = System.currentTimeMillis()
            )
            repository.saveEntryLocally(entry)
            syncNow()
        }
    }
}

val MainViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as MyApplication
        val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        return MainViewModel(application.container.resultRepository, prefs) as T
    }
}
