package com.example.acronodroid.db

import androidx.room.*
import com.example.acronodroid.models.Acronym
import kotlinx.coroutines.flow.Flow

@Dao
interface AcronymDao {
    @Query("SELECT * FROM acronyms ORDER BY short")
    fun getAll(): Flow<List<Acronym>>

    @Query("SELECT * FROM acronyms WHERE short LIKE :query OR full LIKE :query OR explanation LIKE :query ORDER BY short")
    fun search(query: String): Flow<List<Acronym>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(acronym: Acronym)

    @Delete
    suspend fun delete(acronym: Acronym)

    @Query("DELETE FROM acronyms")
    suspend fun clearAll()
}
