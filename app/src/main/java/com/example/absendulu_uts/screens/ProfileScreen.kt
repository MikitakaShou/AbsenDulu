package com.example.absendulu_uts.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.absendulu_uts.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }
    var nim by remember { mutableStateOf("Loading...") }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

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

                        val base64Image = document.getString("profileImageBase64")
                        profileBitmap = base64Image?.let { decodeBase64(it) }
                    } else {
                        name = "No data found"
                        email = "-"
                        nim = "-"
                    }
                }
                .addOnFailureListener {
                    name = "Error fetching data"
                    email = "-"
                    nim = "-"
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (profileBitmap != null) {
                Image(
                    bitmap = profileBitmap!!.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp)
                        .clip(CircleShape)
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
                        .padding(16.dp)
                        .clip(CircleShape)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Name: $name", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Email: $email", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "NIM: $nim", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                auth.signOut() // Logout
                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_logout), contentDescription = "Logout")
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
