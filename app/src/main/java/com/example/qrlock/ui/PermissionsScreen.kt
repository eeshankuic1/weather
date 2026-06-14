package com.example.qrlock.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrlock.util.Permissions

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val overlayOk = rememberRefreshedOnResume { Permissions.canDrawOverlays(context) }
    val accessibilityOk = rememberRefreshedOnResume { Permissions.isAccessibilityServiceEnabled(context) }

    Scaffold(topBar = { TopBar("Permissions & setup", onBack = onBack) }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text(
                "QR AppLock needs two permissions to block apps. Everything runs on your device — nothing is uploaded.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(20.dp))

            PermissionCard(
                granted = accessibilityOk,
                title = "Accessibility access",
                description = "Lets the app notice when a protected app opens so it can show the lock screen.",
                buttonText = "Open accessibility settings",
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
            )
            Spacer(Modifier.height(12.dp))
            PermissionCard(
                granted = overlayOk,
                title = "Display over other apps",
                description = "Lets the lock screen appear on top of the app you're protecting.",
                buttonText = "Open overlay settings",
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        ),
                    )
                },
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "Camera access is requested the first time you open the scanner.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun PermissionCard(
    granted: Boolean,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (granted) Color(0xFF2D6A4F) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (granted) "Granted" else "Not granted yet",
                fontSize = 13.sp,
                color = if (granted) Color(0xFF2D6A4F) else Color(0xFFB36B00),
                fontWeight = FontWeight.Medium,
            )
            if (!granted) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onClick) { Text(buttonText) }
            }
        }
    }
}
