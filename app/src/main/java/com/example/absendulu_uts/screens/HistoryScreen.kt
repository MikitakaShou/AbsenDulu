package com.example.absendulu_uts.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.absendulu_uts.R

data class AbsenData(
    val jenisAbsen: String,
    val nama: String,
    val nim: String,
    val foto: Int, // Assuming the photo is a drawable resource ID
    val tanggalWaktu: String,
    val lokasi: String,
    val notes: String
)

@Composable
fun HistoryScreen(absenList: List<AbsenData>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        absenList.forEach { absen ->
            AbsenCard(absen)
        }
    }
}

@Composable
fun AbsenCard(absen: AbsenData) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White,
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = absen.jenisAbsen, fontSize = 20.sp, color = Color.Black)
            Text(text = "Nama: ${absen.nama}", fontSize = 16.sp, color = Color.Gray)
            Text(text = "NIM: ${absen.nim}", fontSize = 16.sp, color = Color.Gray)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable { showDialog = true }
            ) {
                Image(
                    painter = painterResource(id = absen.foto),
                    contentDescription = "Foto Absen",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(text = "Tanggal & Waktu: ${absen.tanggalWaktu}", fontSize = 16.sp, color = Color.Gray)
            Text(text = "Lokasi: ${absen.lokasi}", fontSize = 16.sp, color = Color.Gray)
            Text(text = "Notes: ${absen.notes}", fontSize = 16.sp, color = Color.Gray)
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = absen.foto),
                        contentDescription = "Foto Absen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showDialog = false }) {
                        Text(text = "Close")
                    }
                }
            }
        }
    }
}