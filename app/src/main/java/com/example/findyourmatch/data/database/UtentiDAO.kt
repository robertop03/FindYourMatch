package com.example.findyourmatch.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UtentiDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(utente: Utenti)

    @Delete
    suspend fun delete(utente: Utenti)

    @Query("SELECT * FROM Utenti")
    suspend fun getAll(): List<Utenti>

    @Query("SELECT * FROM Utenti WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): Utenti?
}