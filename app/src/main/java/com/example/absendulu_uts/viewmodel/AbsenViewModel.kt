package com.example.absendulu_uts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absendulu_uts.screens.AbsenData
import com.example.absendulu_uts.screens.HistoryData
import com.example.absendulu_uts.screens.IzinData
import com.google.firebase.firestore.FirebaseFirestore

class AbsenViewModel : ViewModel() {
    private val _absenList = MutableLiveData<List<AbsenData>>()
    val absenList: LiveData<List<AbsenData>> = _absenList

    private val _izinList = MutableLiveData<List<IzinData>>()
    val izinList: LiveData<List<IzinData>> = _izinList

    private val _historyList = MutableLiveData<List<HistoryData>>()
    val historyList: LiveData<List<HistoryData>> = _historyList

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    fun setName(name: String) {
        _name.value = name
    }

    init {
        fetchAbsenData()
        fetchIzinData()
    }

    fun addAbsenData(absenData: AbsenData) {
        val currentList = _absenList.value?.toMutableList() ?: mutableListOf()
        currentList.add(absenData)
        _absenList.value = currentList
        updateHistoryList()
    }

    fun addIzinData(izinData: IzinData) {
        val currentList = _izinList.value?.toMutableList() ?: mutableListOf()
        currentList.add(izinData)
        _izinList.value = currentList
        updateHistoryList()
    }

    fun fetchAbsenData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("absen")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { document ->
                    val absenData = document.toObject(AbsenData::class.java)
                    Log.d("AbsenViewModel", "Fetched document ID: ${document.id}")
                    absenData
                }
                _absenList.value = list
                updateHistoryList()
            }
            .addOnFailureListener { exception ->
                Log.e("AbsenViewModel", "Failed to fetch absen data: ${exception.message}")
            }
    }

    fun fetchIzinData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("izin")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { document ->
                    val izinData = document.toObject(IzinData::class.java)
                    Log.d("AbsenViewModel", "Fetched document ID: ${document.id}")
                    izinData
                }
                _izinList.value = list
                updateHistoryList()
            }
            .addOnFailureListener { exception ->
                Log.e("AbsenViewModel", "Failed to fetch izin data: ${exception.message}")
            }
    }

    private fun updateHistoryList() {
        val absenHistory = _absenList.value?.map { HistoryData.Absen(it) } ?: emptyList()
        val izinHistory = _izinList.value?.map { HistoryData.Izin(it) } ?: emptyList()
        _historyList.value = absenHistory + izinHistory
        Log.d("AbsenViewModel", "History list updated: ${_historyList.value?.size} items")
    }

    fun deleteHistoryData(historyData: HistoryData) {
        val db = FirebaseFirestore.getInstance()
        when (historyData) {
            is HistoryData.Absen -> {
                db.collection("absen").document(historyData.data.id) // Use the correct document ID
                    .delete()
                    .addOnSuccessListener {
                        val updatedList = _absenList.value?.filter { it != historyData.data } ?: emptyList()
                        _absenList.value = updatedList
                        updateHistoryList()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AbsenViewModel", "Failed to delete absen data: ${exception.message}")
                    }
            }
            is HistoryData.Izin -> {
                db.collection("izin").document(historyData.data.id) // Use the correct document ID
                    .delete()
                    .addOnSuccessListener {
                        val updatedList = _izinList.value?.filter { it != historyData.data } ?: emptyList()
                        _izinList.value = updatedList
                        updateHistoryList()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AbsenViewModel", "Failed to delete izin data: ${exception.message}")
                    }
            }
        }
    }
}