package com.example.absendulu_uts

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.absendulu_uts.screens.CameraScreen
import com.example.absendulu_uts.screens.HistoryScreen
import com.example.absendulu_uts.screens.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf(Screen.Profile) }

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
                    Screen.Camera -> CameraScreen(onCaptureClick = { /* Handle Camera */ }, faceBitmap = null)
                    Screen.History -> HistoryScreen()
                }
            }
        }
    )
}
