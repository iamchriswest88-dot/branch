package com.example.branch.data.repository

import com.example.branch.data.dao.DoneDao
import com.example.branch.data.model.DoneLog
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DoneRepository(private val dao: DoneDao) {
    fun getDoneDates(category: String): Flow<List<String>> = dao.getDatesByCategory(category)
    suspend fun getDoneDatesSync(category: String): List<String> = dao.getDatesByCategorySync(category)

    suspend fun logDone(category: String, dateKey: String) {
        if (dao.find(category, dateKey) == null) {
            dao.insert(DoneLog(UUID.randomUUID().toString(), category, dateKey))
        }
    }

    suspend fun removeDone(category: String, dateKey: String) = dao.delete(category, dateKey)
}
