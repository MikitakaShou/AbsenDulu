package com.example.absendulu_uts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.absendulu_uts.screens.CameraScreen
import com.example.absendulu_uts.screens.HistoryScreen
import com.example.absendulu_uts.screens.ProfileScreen
import com.example.absendulu_uts.viewmodel.AbsenViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
        }

        setContent {
            MainScreen()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with camera access
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imageUri?.let { uri ->
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                // Handle the captured image bitmap here
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf(Screen.Profile) }
    val viewModel: AbsenViewModel = viewModel()
    val absenList by viewModel.absenList.observeAsState(emptyList())

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(painterResource(R.drawable.ic_profile), contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedScreen == Screen.Profile,
                    onClick = { selectedScreen = Screen.Profile }
                )
                BottomNavigationItem(
                    icon = { Icon(painterResource(R.drawable.ic_camera), contentDescription = "Camera") },
                    label = { Text("Camera") },
                    selected = selectedScreen == Screen.Camera,
                    onClick = { selectedScreen = Screen.Camera }
                )
                BottomNavigationItem(
                    icon = { Icon(painterResource(R.drawable.ic_history), contentDescription = "History") },
                    label = { Text("History") },
                    selected = selectedScreen == Screen.History,
                    onClick = { selectedScreen = Screen.History }
                )
            }
        },
        content = { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                when (selectedScreen) {
                    Screen.Profile -> ProfileScreen()
                    Screen.Camera -> CameraScreen(
                        onImageCaptured = { uri: Uri ->
                            // Handle Camera capture click
                        },
                        onError = { throwable: Throwable ->
                            // Handle error
                        }
                    )
                    Screen.History -> HistoryScreen(absenList = absenList)
                }
            }
        }
    )
}