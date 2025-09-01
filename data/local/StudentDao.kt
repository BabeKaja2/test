package com.babetech.ucb_admin_access.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE matricule = :matricule LIMIT 1")
    suspend fun getByMatricule(matricule: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(student: StudentEntity)

    @Query("DELETE FROM students")
    suspend fun clearAll()

    @Query("SELECT * FROM students")
    fun getAllEntities(): Flow<List<StudentEntity>>

}
