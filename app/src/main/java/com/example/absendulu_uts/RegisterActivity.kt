package com.example.absendulu_uts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            RegisterScreen(
                onRegisterClick = { name, nim, email, password -> showConfirmationDialog(name, nim, email, password) }
            )
        }
    }

    private fun showConfirmationDialog(name: String, nim: String, email: String, password: String) {
        if (!isValidInput(name, nim, email, password)) {
            Toast.makeText(this, "Please fill all fields with valid data", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Data yang telah diisi tidak dapat diubah kembali.")
            .setPositiveButton("OK") { _, _ ->
                registerUser(name, nim, email, password)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun isValidInput(name: String, nim: String, email: String, password: String): Boolean {
        return name.isNotEmpty() && nim.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6
    }

    private fun registerUser(name: String, nim: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(name, nim, email)
                } else {
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("RegisterActivity", "Registration failed: $errorMessage")
                }
            }
    }

    private fun saveUserToDatabase(name: String, nim: String, email: String) {
        val user = auth.currentUser
        val userId = user?.uid ?: java.util.UUID.randomUUID().toString()
        val userData = hashMapOf(
            "name" to name,
            "nim" to nim,
            "email" to email
        )

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully.", Toast.LENGTH_SHORT).show()
                Log.d("RegisterActivity", "Data saved successfully.")
                startActivity(Intent(this, VerificationActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                Log.e("RegisterActivity", "Failed to save data: ${e.message}")
            }
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        Button(onClick = {
            isLoading = true
            onRegisterClick(name, nim, email, password)
        }) {
            Text("Register")
        }
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}