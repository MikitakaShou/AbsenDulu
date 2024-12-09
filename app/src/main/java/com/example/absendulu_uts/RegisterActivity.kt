package com.example.absendulu_uts

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.*

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var faceBitmap by mutableStateOf<Bitmap?>(null)
    private var isDataLocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            RegisterScreen(
                onCaptureClick = { checkCameraPermission() },
                onUploadClick = { selectImageFromGallery() },
                onRegisterClick = { name, nim, email, password -> showConfirmationDialog(name, nim, email, password) },
                onVerifyClick = { sendVerificationEmail() },
                faceBitmap = faceBitmap,
                isDataLocked = isDataLocked
            )
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            faceBitmap = imageBitmap
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = getBitmapFromUri(it)
            faceBitmap = bitmap
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun selectImageFromGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun checkCameraPermission() {
        val permission = android.Manifest.permission.CAMERA
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(name: String, nim: String, email: String, password: String) {
        if (name.isEmpty() || nim.isEmpty() || email.isEmpty() || password.isEmpty() || faceBitmap == null) {
            Toast.makeText(this, "Please fill all fields and provide a face image", Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Data yang telah diisi tidak dapat diubah kembali.")
            .setPositiveButton("OK") { _, _ ->
                isDataLocked = true
                registerUser(name, nim, email, password)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun registerUser(name: String, nim: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(name, nim, email, faceBitmap!!)
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(name: String, nim: String, email: String, faceBitmap: Bitmap) {
        val user = auth.currentUser
        val userId = user?.uid ?: UUID.randomUUID().toString()
        val userData = hashMapOf(
            "name" to name,
            "nim" to nim,
            "email" to email,
            "faceImage" to bitmapToBase64(faceBitmap)
        )

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
                // Redirect to profile after email verification
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
}

@Composable
fun RegisterScreen(
    onCaptureClick: () -> Unit,
    onUploadClick: () -> Unit,
    onRegisterClick: (String, String, String, String) -> Unit,
    onVerifyClick: () -> Unit,
    faceBitmap: Bitmap?,
    isDataLocked: Boolean
) {
    var name by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isDataLocked) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nim,
                onValueChange = { nim = it },
                label = { Text("NIM") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            faceBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCaptureClick) {
                Text("Capture Face")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onUploadClick) {
                Text("Upload from Gallery")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onRegisterClick(name, nim, email, password) }) {
                Text("Register")
            }
        } else {
            Button(onClick = onVerifyClick) {
                Text("Verify Email")
            }
        }
    }
}
