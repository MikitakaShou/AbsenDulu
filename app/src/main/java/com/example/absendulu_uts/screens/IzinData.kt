package com.example.absendulu_uts.screens

data class IzinData(
    val id: String = "",
    val nama: String = "",
    val type: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val notes: String = "",
    val photoUri: String = ""
) {
    // Konstruktor tanpa argumen untuk Firestore
    constructor() : this("", "", "", "", "", "", "")
}