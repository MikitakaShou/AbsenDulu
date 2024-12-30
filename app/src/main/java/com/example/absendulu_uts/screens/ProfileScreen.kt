package com.example.absendulu_uts.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.absendulu_uts.R
import com.example.absendulu_uts.viewmodel.AbsenViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import androidx.compose.ui.geometry.Offset
import com.example.absendulu_uts.LoginActivity
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(absenViewModel: AbsenViewModel = viewModel()) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }
    var nim by remember { mutableStateOf("Loading...") }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                val resizedBitmap = resizeBitmap(originalBitmap, 200, 200) // Resize for Firestore storage
                val base64String = encodeToBase64(resizedBitmap)
                updateProfilePictureInFirestore(
                    base64String,
                    db,
                    auth,
                    onSuccess = {
                        profileBitmap = resizedBitmap
                        Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name = document.getString("name") ?: "Unknown"
                        email = document.getString("email") ?: "Unknown"
                        nim = document.getString("nim") ?: "Unknown"

                        absenViewModel.setName(name) // Set name in ViewModel

                        val base64Image = document.getString("profileImageBase64")
                        profileBitmap = base64Image?.let { decodeBase64(it) }
                    } else {
                        name = "No data found"
                        email = "-"
                        nim = "-"
                    }
                    isVisible = true // Trigger the animation
                }
                .addOnFailureListener {
                    name = "Error fetching data"
                    email = "-"
                    nim = "-"
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)) // Rounded bottom corners
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
            Text(
                text = "Profile",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Default Profile Picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Name",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = ":",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(10.dp)
                            )
                            Text(
                                text = name,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Email",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = ":",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(10.dp)
                            )
                            Text(
                                text = email,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "NIM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = ":",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(10.dp)
                            )
                            Text(
                                text = nim,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // New content in the center of the white box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // 70% of the screen height
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome $name",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)

                    )
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd-MM-yyyy")),
                        fontSize = 18.sp,
                    )
                    val currentTime = remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH : mm : ss"))) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1000L)
                            currentTime.value = LocalTime.now().format(DateTimeFormatter.ofPattern("HH : mm : ss"))
                        }
                    }
                    Text(
                        text = currentTime.value,
                        fontSize = 18.sp
                    )
                }
            }
        }

        val context = LocalContext.current

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





// Resize the image for Firestore storage
fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

// Convert Bitmap to Base64 string
fun encodeToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Compress to reduce size
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Decode Base64 string back to Bitmap
fun decodeBase64(base64String: String): Bitmap {
    val byteArray = Base64.decode(base64String, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

// Update profile picture in Firestore
fun updateProfilePictureInFirestore(
    base64String: String,
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val userId = auth.currentUser?.uid ?: return
    db.collection("users").document(userId)
        .update("profileImageBase64", base64String)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onError() }
}