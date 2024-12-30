package com.example.absendulu_uts.screens

data class AbsenData(
    val id: String = "",
    val nama: String = "",
    val type: String = "",
    val timestamp: String = "",
    val notes: String = "",
    val photoUri: String = ""
){
    // Konstruktor tanpa argumen untuk Firestore
    constructor() : this("", "", "", "", "", "")
}

