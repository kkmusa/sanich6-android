package com.example.sanich

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanich.data.HistoryDao
import com.example.sanich.data.HistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryEntry(
    val id: Long = 0,
    val title: String,
    val method: String,
    val result: String,
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val time: String
        get() = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(createdAt))
}

class CalculationHistoryViewModel(
    private val dao: HistoryDao
) : ViewModel() {
    private val insertMutex = Mutex()
    private val sessionIds = mutableMapOf<String, Long>()
    val entries: StateFlow<List<HistoryEntry>> = dao.observeAll()
        .map { rows ->
            rows.map { row ->
                HistoryEntry(row.id, row.title, row.method, row.result, row.details, row.createdAt)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addIfNew(entry: HistoryEntry) {
        viewModelScope.launch {
            insertMutex.withLock {
                val latest = dao.latest()
                val duplicate = latest?.let {
                    it.title == entry.title &&
                        it.method == entry.method &&
                        it.result == entry.result &&
                        it.details == entry.details
                } == true
                if (!duplicate) {
                    dao.insert(
                        HistoryEntity(
                            title = entry.title,
                            method = entry.method,
                            result = entry.result,
                            details = entry.details,
                            createdAt = entry.createdAt
                        )
                    )
                }
            }
        }
    }

    fun saveSession(sessionKey: String, entry: HistoryEntry) {
        viewModelScope.launch {
            insertMutex.withLock {
                val existingId = sessionIds[sessionKey]
                if (existingId == null) {
                    val id = dao.insert(
                        HistoryEntity(
                            title = entry.title,
                            method = entry.method,
                            result = entry.result,
                            details = entry.details,
                            createdAt = entry.createdAt
                        )
                    )
                    if (id > 0L) sessionIds[sessionKey] = id
                } else {
                    dao.updateById(
                        id = existingId,
                        title = entry.title,
                        method = entry.method,
                        result = entry.result,
                        details = entry.details,
                        createdAt = entry.createdAt
                    )
                }
            }
        }
    }

    fun delete(entry: HistoryEntry) {
        viewModelScope.launch {
            dao.delete(HistoryEntity(entry.id, entry.title, entry.method, entry.result, entry.details, entry.createdAt))
        }
    }

    fun clear() {
        viewModelScope.launch { dao.clear() }
    }
}

class CalculationHistoryViewModelFactory(
    private val dao: HistoryDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculationHistoryViewModel::class.java)) {
            return CalculationHistoryViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
