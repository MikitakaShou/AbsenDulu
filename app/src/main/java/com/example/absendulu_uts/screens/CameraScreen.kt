package com.example.absendulu_uts.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.absendulu_uts.BuildConfig
import com.example.absendulu_uts.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_IMAGE_CAPTURE = 1

private fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

@Composable
fun CameraScreen(
    context: Context,
    onCaptureClick: (Uri) -> Unit
) {
    val activity = context as Activity
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageUri?.let {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        Button(onClick = {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile: File? = try {
                createImageFile(context)
            } catch (ex: Exception) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    it
                )
                imageUri = photoURI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                onCaptureClick(photoURI)
            }
        }) {
            Text(text = "Capture Selfie")
        }
    }
}

@Composable
fun PerizinanInputScreen(
    context: Context,
    onCaptureClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val showDatePickerDialog = { dateSetter: (String) -> Unit ->
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$year-${(month + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
                dateSetter(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
        }

        Text(
            text = "Surat izin jika ada.",
            color = Color.Red,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Camera Preview")
        }

        Button(
            onClick = { showDatePickerDialog { date -> startDate = date } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (startDate.isEmpty()) "Select Start Date" else startDate)
        }

        Button(
            onClick = { showDatePickerDialog { date -> endDate = date } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (endDate.isEmpty()) "Select End Date" else endDate)
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { newValue -> notes = newValue },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White)
                .padding(8.dp)
        )

        Button(onClick = onCaptureClick) {
            Text(text = "Capture Selfie")
        }

        Button(onClick = {
            onSaveClick()
            showDialog = true
        }) {
            Text(text = "Done")
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
                        Text(text = "Data tidak dapat dirubah", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showDialog = false }) {
                            Text(text = "OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelfieInputScreen(
    onCaptureClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    val currentDateTime = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) }
    val currentLocation = "Current Location (Placeholder)"
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
        }

        Text(
            text = "Please ensure your face is visible in the selfie.",
            color = Color.Red,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Camera Preview")
        }

        OutlinedTextField(
            value = currentDateTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date and Time") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currentLocation,
            onValueChange = {},
            readOnly = true,
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { newValue -> notes = newValue },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White)
                .padding(8.dp)
        )

        Button(onClick = onCaptureClick) {
            Text(text = "Capture Selfie")
        }

        Button(onClick = {
            onSaveClick()
            showDialog = true
        }) {
            Text(text = "Done")
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
                        Text(text = "Data tidak dapat dirubah", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showDialog = false }) {
                            Text(text = "OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AbsenOptionCard(
    title: String,
    color: Color,
    iconResId: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        backgroundColor = color,
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}