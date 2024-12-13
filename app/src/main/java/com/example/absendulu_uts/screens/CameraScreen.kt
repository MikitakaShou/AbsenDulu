package com.example.absendulu_uts.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.example.absendulu_uts.R
import android.app.DatePickerDialog
import androidx.compose.ui.viewinterop.AndroidView
import com.google.common.util.concurrent.ListenableFuture

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var outputUri by remember { mutableStateOf<Uri?>(null) }
    var selectedScreen by remember { mutableStateOf<Screen?>(null) }

    when (selectedScreen) {
        Screen.Selfie -> SelfieInputScreen(
            onCaptureClick = {
                takePhoto(context, lifecycleOwner, cameraProviderFuture, { uri ->
                    outputUri = uri
                    onImageCaptured(uri)
                }, onError)
            },
            onBackClick = { selectedScreen = null },
            onSaveClick = { /* Handle save action */ }
        )
        Screen.Perizinan -> PerizinanInputScreen(
            context = context,
            onCaptureClick = {
                takePhoto(context, lifecycleOwner, cameraProviderFuture, { uri ->
                    outputUri = uri
                    onImageCaptured(uri)
                }, onError)
            },
            onBackClick = { selectedScreen = null },
            onSaveClick = { /* Handle save action */ }
        )
        else -> Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AbsenOptionCard(
                title = "Absen Masuk",
                color = Color.Green,
                iconResId = R.drawable.ic_masuk,
                onClick = { selectedScreen = Screen.Selfie }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AbsenOptionCard(
                title = "Absen Keluar",
                color = Color.Red,
                iconResId = R.drawable.ic_masuk,
                onClick = { selectedScreen = Screen.Selfie }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AbsenOptionCard(
                title = "Perizinan",
                color = Color.Blue,
                iconResId = R.drawable.ic_masuk,
                onClick = { selectedScreen = Screen.Perizinan }
            )
        }
    }
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onError: (Throwable) -> Unit
) {
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
        } catch (e: Exception) {
            onError(e)
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun takePhoto(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    onImageCaptured: (Uri) -> Unit,
    onError: (Throwable) -> Unit
) {
    val outputDirectory = getOutputDirectory(context)
    val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis()) + ".jpg"
    val photoFile = File(outputDirectory, fileName)

    val imageCapture = ImageCapture.Builder().build()

    try {
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            imageCapture
        )

        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                    Toast.makeText(context, "Photo Saved: $savedUri", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    } catch (e: Exception) {
        onError(e)
    }
}

private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, "CameraXPhotos").apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
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

enum class Screen {
    Selfie,
    Perizinan
}