package com.example.absendulu_uts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absendulu_uts.screens.AbsenData
import com.google.firebase.firestore.FirebaseFirestore

class AbsenViewModel : ViewModel() {
    private val _absenList = MutableLiveData<List<AbsenData>>()
    val absenList: LiveData<List<AbsenData>> = _absenList

    init {
        fetchAbsenData()
    }

    private fun fetchAbsenData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("absen")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { document -> document.toObject(AbsenData::class.java) }
                _absenList.value = list
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }
}