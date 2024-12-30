package com.example.absendulu_uts.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.absendulu_uts.R
import com.example.absendulu_uts.viewmodel.AbsenViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import com.example.absendulu_uts.LoginActivity


sealed class HistoryData {
    data class Absen(val data: AbsenData) : HistoryData()
    data class Izin(val data: IzinData) : HistoryData()
}

@Composable
fun HistoryScreen(viewModel: AbsenViewModel) {
    val historyList by viewModel.historyList.observeAsState(emptyList())
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Blue, Color(0xFF00BFA5)), // Blue and Teal600
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "History",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // History List
            LazyColumn {
                items(historyList) { historyData ->
                    when (historyData) {
                        is HistoryData.Absen -> AbsenCard(absenData = historyData.data, onDelete = {
                            viewModel.deleteHistoryData(historyData)
                        })
                        is HistoryData.Izin -> IzinCard(izinData = historyData.data, onDelete = {
                            viewModel.deleteHistoryData(historyData)
                        })
                    }
                }
            }
        }

        // Logout Button
        IconButton(
            onClick = {
                // Proses logout
                auth.signOut()
                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

                // Navigasi ke LoginActivity
                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_logout),
                contentDescription = "Logout",
                tint = Color.White // Set icon color to white
            )
        }
    }
}

@Composable
fun AbsenCard(absenData: AbsenData, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirmation") },
            text = { Text(text = "Data will be deleted from the database. Are you sure?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    isVisible = false
                    onDelete()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImageDialog) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Image(
                painter = rememberImagePainter(data = absenData.photoUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showImageDialog = false }
            )
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberImagePainter(data = absenData.photoUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                            .clickable { showImageDialog = true }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = absenData.type, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "Name", modifier = Modifier.width(60.dp))
                            Text(text = ":", modifier = Modifier.width(10.dp))
                            Text(text = absenData.nama)

                        }
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "Time", modifier = Modifier.width(60.dp))
                            Text(text = ":", modifier = Modifier.width(10.dp))
                            Text(text = absenData.timestamp)
                        }
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "Notes", modifier = Modifier.width(60.dp))
                            Text(text = ":", modifier = Modifier.width(10.dp))
                            Text(text = absenData.notes)
                        }
                    }
                }
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun IzinCard(izinData: IzinData, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirmation") },
            text = { Text(text = "Data will be deleted from the database. Are you sure?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    isVisible = false
                    onDelete()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImageDialog) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Image(
                painter = rememberImagePainter(data = izinData.photoUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showImageDialog = false }
            )
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberImagePainter(data = izinData.photoUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                            .clickable { showImageDialog = true }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = izinData.type, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "Name", modifier = Modifier.width(60.dp))
                            Text(text = ":", modifier = Modifier.width(10.dp))
                            Text(text = izinData.nama)

                        }
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "Start", modifier = Modifier.width(60.dp))
                            Text(text = ":", modifier = Modifier.width(10.dp))
                            Text(text = izinData.startDate)
                        }
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "End", modifier = Modifier.width(60.dp))
                            Text(text = ":", modifier = Modifier.width(10.dp))
                            Text(text = izinData.endDate)
                        }
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "Notes", modifier = Modifier.width(60.dp))
                            Text(text = ":", modifier = Modifier.width(10.dp))
                            Text(text = izinData.notes)
                        }
                    }
                }
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Delete")
                }
            }
        }
    }
}