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
import java.util.concurrent.TimeUnit
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.Image
import coil.compose.rememberImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.absendulu_uts.viewmodel.AbsenViewModel

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
    val absenViewModel: AbsenViewModel = viewModel() // Get ViewModel instance
    var isAbsenMasuk by remember { mutableStateOf(true) }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var outputUri by remember { mutableStateOf<Uri?>(null) }
    var selectedScreen by remember { mutableStateOf<Screen?>(null) }

    when (selectedScreen) {
        Screen.Selfie -> SelfieInputScreen(
            absenViewModel = absenViewModel,
            onCaptureClick = {
                takePhoto(context, lifecycleOwner, cameraProviderFuture, { uri ->
                    outputUri = uri
                    onImageCaptured(uri)
                }, onError)
            },
            onBackClick = { selectedScreen = null },
            onSaveClick = { /* Handle save action */ },
            isAbsenMasuk = isAbsenMasuk, // Pass the correct value
            onNavigateBack = { selectedScreen = null }
        )
        Screen.Perizinan -> PerizinanInputScreen(
            absenViewModel = absenViewModel,
            onCaptureClick = {
                takePhoto(context, lifecycleOwner, cameraProviderFuture, { uri ->
                    outputUri = uri
                    onImageCaptured(uri)
                }, onError)
            },
            onBackClick = { selectedScreen = null },
            onSaveClick = { /* Handle save action */ },
            onNavigateBack = { selectedScreen = null }
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
                onClick = { selectedScreen = Screen.Selfie; isAbsenMasuk = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AbsenOptionCard(
                title = "Absen Keluar",
                color = Color.Red,
                iconResId = R.drawable.ic_masuk,
                onClick = { selectedScreen = Screen.Selfie; isAbsenMasuk = false }
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

fun saveAbsenMasuk(viewModel: AbsenViewModel, timestamp: String, notes: String, photoUri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val absenData = AbsenData(
        id = db.collection("absen").document().id,
        type = "Absen Masuk",
        nama = viewModel.name.value ?: "",
        timestamp = timestamp,
        notes = notes,
        photoUri = photoUri.toString()
    )
    db.collection("absen")
        .document(absenData.id)
        .set(absenData)
        .addOnSuccessListener {
            viewModel.addAbsenData(absenData) // Update ViewModel
            onSuccess()
        }
        .addOnFailureListener { e -> onFailure(e) }
}

fun saveAbsenKeluar(viewModel: AbsenViewModel, timestamp: String, notes: String, photoUri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val absenData = AbsenData(
        id = db.collection("absen").document().id,
        type = "Absen Keluar",
        nama = viewModel.name.value ?: "",
        timestamp = timestamp,
        notes = notes,
        photoUri = photoUri.toString()
    )
    db.collection("absen")
        .document(absenData.id)
        .set(absenData)
        .addOnSuccessListener {
            viewModel.addAbsenData(absenData) // Update ViewModel
            onSuccess()
        }
        .addOnFailureListener { e -> onFailure(e) }
}

fun savePerizinan(viewModel: AbsenViewModel, startDate: String, endDate: String, notes: String, photoUri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val izinData = IzinData(
        id = db.collection("izin").document().id,
        type = "Perizinan",
        nama = viewModel.name.value ?: "",
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        photoUri = photoUri.toString()
    )
    db.collection("izin")
        .document(izinData.id)
        .set(izinData)
        .addOnSuccessListener {
            viewModel.addIzinData(izinData) // Update ViewModel
            onSuccess()
        }
        .addOnFailureListener { e -> onFailure(e) }
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
            val cameraProvider = cameraProviderFuture.get(10, TimeUnit.SECONDS) // Increase timeout to 10 seconds
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
                    Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                    onError(exception)
                }
            }
        )
    } catch (e: Exception) {
        Log.e("CameraScreen", "Failed to capture photo: ${e.message}", e)
        onError(e)
    }
}

@Composable
fun FullScreenCamera(
    onImageCaptured: (Uri) -> Unit,
    onBackClick: () -> Unit,
    onError: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        LaunchedEffect(Unit) {
            bindCameraUseCases(context, lifecycleOwner, previewView!!) {
                onError(it)
            }
        }
        Button(
            onClick = {
                takePhoto(context, lifecycleOwner, cameraProviderFuture, onImageCaptured, onError)
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            Text(text = "Capture")
        }
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
        }
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
    absenViewModel: AbsenViewModel,
    onCaptureClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isCameraActive by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val previewView = remember { PreviewView(context) }
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("EEEE, yyyy-MM-dd", Locale.getDefault())

    val showDatePickerDialog = { dateSetter: (String) -> Unit ->
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = dateFormatter.format(calendar.time)
                dateSetter(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (isCameraActive) {
        FullScreenCamera(
            onImageCaptured = { uri ->
                capturedImageUri = uri
                isCameraActive = false
            },
            onBackClick = { isCameraActive = false },
            onError = { Log.e("CameraError", it.message ?: "Unknown error") }
        )
    } else {
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
                    .aspectRatio(16f / 9f)
                    .background(Color.Gray)
                    .clickable { isCameraActive = true },
                contentAlignment = Alignment.Center
            ) {
                capturedImageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text(text = "Add Photo")
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

            Button(onClick = {
                if (notes.text.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || capturedImageUri == null) {
                    showWarningDialog = true
                } else {
                    showConfirmationDialog = true
                }
            }) {
                Text(text = "Done")
            }

            if (showWarningDialog) {
                AlertDialog(
                    onDismissRequest = { showWarningDialog = false },
                    title = { Text(text = "Warning") },
                    text = { Text(text = "Please fill in all required fields.") },
                    confirmButton = {
                        Button(onClick = { showWarningDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text(text = "Confirmation") },
                    text = { Text(text = "Data yang telah diisi tidak dapat diubah kembali.") },
                    confirmButton = {
                        Button(onClick = {
                            showConfirmationDialog = false
                            isLoading = true
                            val notesText = notes.text
                            val photoUri = capturedImageUri!!
                            savePerizinan(absenViewModel, startDate, endDate, notesText, photoUri, {
                                isLoading = false
                                onSaveClick()
                                onNavigateBack()
                            }, { e ->
                                isLoading = false
                                Log.e("FirestoreError", "Failed to save perizinan: ${e.message}", e)
                            })
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showConfirmationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun SelfieInputScreen(
    absenViewModel: AbsenViewModel,
    onCaptureClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isAbsenMasuk: Boolean,
    onNavigateBack: () -> Unit
) {
    var isCameraActive by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val previewView = remember { PreviewView(context) }
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    val currentDateTime = remember { SimpleDateFormat("EEEE, yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) }
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    if (isCameraActive) {
        FullScreenCamera(
            onImageCaptured = { uri ->
                capturedImageUri = uri
                isCameraActive = false
            },
            onBackClick = { isCameraActive = false },
            onError = { Log.e("CameraError", it.message ?: "Unknown error") }
        )
    } else {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Gray)
                    .clickable { isCameraActive = true },
                contentAlignment = Alignment.Center
            ) {
                capturedImageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text(text = "Add Photo")
            }

            OutlinedTextField(
                value = currentDateTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date and Time") },
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

            Button(onClick = {
                if (notes.text.isEmpty() || capturedImageUri == null) {
                    showWarningDialog = true
                } else {
                    showConfirmationDialog = true
                }
            }) {
                Text(text = "Done")
            }

            if (showWarningDialog) {
                AlertDialog(
                    onDismissRequest = { showWarningDialog = false },
                    title = { Text(text = "Warning") },
                    text = { Text(text = "Please fill in all required fields.") },
                    confirmButton = {
                        Button(onClick = { showWarningDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text(text = "Confirmation") },
                    text = { Text(text = "Data yang telah diisi tidak dapat diubah kembali.") },
                    confirmButton = {
                        Button(onClick = {
                            showConfirmationDialog = false
                            isLoading = true
                            val timestamp = currentDateTime
                            val notesText = notes.text
                            val photoUri = capturedImageUri!!
                            if (isAbsenMasuk) {
                                saveAbsenMasuk(absenViewModel, timestamp, notesText, photoUri, {
                                    isLoading = false
                                    onSaveClick()
                                    onNavigateBack()
                                }, { e ->
                                    isLoading = false
                                    Log.e("FirestoreError", "Failed to save absen masuk: ${e.message}", e)
                                })
                            } else {
                                saveAbsenKeluar(absenViewModel, timestamp, notesText, photoUri, {
                                    isLoading = false
                                    onSaveClick()
                                    onNavigateBack()
                                }, { e ->
                                    isLoading = false
                                    Log.e("FirestoreError", "Failed to save absen keluar: ${e.message}", e)
                                })
                            }
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showConfirmationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
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