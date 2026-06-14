package com.example.qrlock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.qrlock.ui.AppPickerScreen
import com.example.qrlock.ui.HomeScreen
import com.example.qrlock.ui.PermissionsScreen
import com.example.qrlock.ui.QrCodeScreen
import com.example.qrlock.ui.QrScannerScreen
import com.example.qrlock.ui.Screen

class MainActivity : ComponentActivity() {

    // A single mutable screen pointer drives the lightweight in-app navigation. It lives on the
    // Activity so onNewIntent (e.g. the overlay's "Scan to unlock" button) can redirect it.
    private var startScreen by mutableStateOf(Screen.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent.getBooleanExtra(EXTRA_OPEN_SCANNER, false)) startScreen = Screen.SCANNER

        setContent {
            QrAppLockTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var screen by remember { mutableStateOf(startScreen) }
                    // React to onNewIntent updating startScreen.
                    androidx.compose.runtime.LaunchedEffect(startScreen) { screen = startScreen }

                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            onScan = { screen = Screen.SCANNER },
                            onChooseApps = { screen = Screen.APPS },
                            onShowQr = { screen = Screen.QR },
                            onPermissions = { screen = Screen.PERMISSIONS },
                        )
                        Screen.SCANNER -> QrScannerScreen(onDone = { screen = Screen.HOME })
                        Screen.APPS -> AppPickerScreen(onBack = { screen = Screen.HOME })
                        Screen.QR -> QrCodeScreen(onBack = { screen = Screen.HOME })
                        Screen.PERMISSIONS -> PermissionsScreen(onBack = { screen = Screen.HOME })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startScreen = if (intent.getBooleanExtra(EXTRA_OPEN_SCANNER, false)) Screen.SCANNER else Screen.HOME
    }

    companion object {
        const val EXTRA_OPEN_SCANNER = "open_scanner"
    }
}

private val BrandColors = lightColorScheme(
    primary = Color(0xFF0D4F4F),
    onPrimary = Color.White,
    secondary = Color(0xFFC17F59),
    onSecondary = Color.White,
    background = Color(0xFFFAFAF8),
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
)

private val BrandDarkColors = darkColorScheme(
    primary = Color(0xFF1A7A7A),
    secondary = Color(0xFFC17F59),
)

@androidx.compose.runtime.Composable
fun QrAppLockTheme(content: @androidx.compose.runtime.Composable () -> Unit) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) BrandDarkColors else BrandColors,
        content = content,
    )
}
