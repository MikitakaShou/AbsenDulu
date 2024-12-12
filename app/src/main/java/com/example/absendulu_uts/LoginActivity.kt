package com.example.absendulu_uts

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            LoginScreen(
                onLoginClick = { email, password -> loginUser(email, password) },
                onRegisterClick = { startActivity(Intent(this, RegisterActivity::class.java)) }
            )
        }
    }

    private fun loginUser(email: String, password: String) {
        if (!isValidInput(email, password)) {
            Toast.makeText(this, "Please fill all fields with valid data", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isValidInput(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6
    }
}

@Composable
fun LoginScreen(onLoginClick: (String, String) -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Input untuk Email dengan KeyEvent untuk Tab dan Enter
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { event: KeyEvent ->
                    when (event.key) {
                        Key.Tab -> {
                            focusManager.moveFocus(FocusDirection.Down) // Pindahkan fokus ke Password field
                            true
                        }
                        Key.Enter -> {
                            focusManager.moveFocus(FocusDirection.Down) // Pindahkan fokus ke tombol Login
                            true
                        }
                        else -> false
                    }
                }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input untuk Password dengan KeyEvent untuk Enter
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { event: KeyEvent ->
                    if (event.key == Key.Enter) {
                        onLoginClick(email, password) // Login jika Enter ditekan
                        true
                    } else {
                        false
                    }
                }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Login
        Button(onClick = { onLoginClick(email, password) }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Register
        Button(onClick = onRegisterClick) {
            Text("Register")
        }
    }
}
