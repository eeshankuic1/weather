package com.example.qrlock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrlock.data.LockRepository
import com.example.qrlock.util.Permissions

@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onChooseApps: () -> Unit,
    onShowQr: () -> Unit,
    onPermissions: () -> Unit,
) {
    val context = LocalContext.current
    val locked by LockRepository.locked.collectAsState()
    val protectedApps by LockRepository.protectedPackages.collectAsState()

    val overlayOk = rememberRefreshedOnResume { Permissions.canDrawOverlays(context) }
    val accessibilityOk = rememberRefreshedOnResume { Permissions.isAccessibilityServiceEnabled(context) }
    val setupComplete = overlayOk && accessibilityOk
    val hasApps = protectedApps.isNotEmpty()

    Scaffold(topBar = { TopBar("QR AppLock") }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            StatusCard(locked = locked, protectedCount = protectedApps.size)

            if (!setupComplete || !hasApps) {
                Spacer(Modifier.height(16.dp))
                SetupBanner(
                    setupComplete = setupComplete,
                    hasApps = hasApps,
                    onPermissions = onPermissions,
                    onChooseApps = onChooseApps,
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Scan QR code  •  ${if (locked) "Unlock" else "Lock"}", fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))
            HomeAction(Icons.Filled.Apps, "Choose apps to protect", onChooseApps)
            Spacer(Modifier.height(12.dp))
            HomeAction(Icons.Filled.QrCode2, "Show / print my QR code", onShowQr)
            Spacer(Modifier.height(12.dp))
            HomeAction(Icons.Filled.Settings, "Permissions & setup", onPermissions)
        }
    }
}

@Composable
private fun StatusCard(locked: Boolean, protectedCount: Int) {
    val color = if (locked) MaterialTheme.colorScheme.primary else Color(0xFF2D6A4F)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = if (locked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.height(56.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (locked) "Apps are LOCKED" else "Apps are UNLOCKED",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$protectedCount app${if (protectedCount == 1) "" else "s"} protected",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun SetupBanner(
    setupComplete: Boolean,
    hasApps: Boolean,
    onPermissions: () -> Unit,
    onChooseApps: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFB36B00))
            Spacer(Modifier.height(4.dp))
            Text("Finish setup", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            if (!setupComplete) {
                Text(
                    "Grant the overlay + accessibility permissions so the lock can actually block apps.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                )
                OutlinedButton(onClick = onPermissions) { Text("Open permissions") }
            }
            if (!hasApps) {
                Text(
                    "Pick at least one app to protect.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                )
                OutlinedButton(onClick = onChooseApps) { Text("Choose apps") }
            }
        }
    }
}

@Composable
private fun HomeAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(),
    ) {
        Icon(icon, contentDescription = null)
        Text("   $label", fontSize = 15.sp)
    }
}
