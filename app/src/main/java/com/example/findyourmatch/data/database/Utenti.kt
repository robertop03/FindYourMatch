package com.example.findyourmatch.data.database

import java.time.LocalDate
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Utenti (
    @PrimaryKey val email: String,
    val nome: String,
    val cognome: String,
    val dataNascita: LocalDate,
    val password: String,
    val salt: String,
    val sesso: String,
    val dataIscrizione: LocalDate,
    val telefono: String
    )