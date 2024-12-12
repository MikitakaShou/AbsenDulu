package com.example.absendulu_uts

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var isResendEnabled by mutableStateOf(true)
    private var timerText by mutableStateOf("")

    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            VerificationScreen(
                onBackClick = { finish() }, // Menambahkan aksi back
                onVerifyClick = { sendVerificationEmail() },
                isResendEnabled = isResendEnabled,
                timerText = timerText
            )
        }
    }

    private fun sendVerificationEmail() {
        // Segera update status UI sebelum proses pengiriman dimulai
        isResendEnabled = false
        timerText = "Please wait..."

        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
                startResendCooldown() // Mulai countdown setelah berhasil mengirim email
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                isResendEnabled = true // Aktifkan tombol kembali jika gagal
            }
        }
    }

    private fun startResendCooldown() {
        val cooldownTime = 60000L // 60 detik
        resendTimer = object : CountDownTimer(cooldownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText = "Resend available in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                isResendEnabled = true
                timerText = "" // Kosongkan teks timer setelah selesai
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel() // Hentikan timer saat activity dihancurkan
    }
}


@Composable
fun VerificationScreen(onBackClick: () -> Unit, onVerifyClick: () -> Unit, isResendEnabled: Boolean, timerText: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tombol Back di pojok kiri atas
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
        }

        Text("Please verify your email to continue.")
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVerifyClick,
            enabled = isResendEnabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isResendEnabled) Color.Blue else Color.Gray, // Warna tombol
                contentColor = Color.White
            )
        ) {
            Text(if (isResendEnabled) "Send Verification" else "Please wait...") // Teks tombol
        }

        // Menampilkan timer jika ada teks timer
        if (timerText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(timerText)
        }
    }
}

