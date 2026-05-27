package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StartupReport
import kotlinx.coroutines.flow.Flow

@Dao
interface StartupReportDao {
    @Query("SELECT * FROM startup_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<StartupReport>>

    @Query("SELECT * FROM startup_reports WHERE id = :id")
    suspend fun getReportById(id: Long): StartupReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: StartupReport): Long

    @Update
    suspend fun updateReport(report: StartupReport)

    @Query("DELETE FROM startup_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Delete
    suspend fun deleteReport(report: StartupReport)
}
