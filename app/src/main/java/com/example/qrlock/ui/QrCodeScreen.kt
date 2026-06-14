package com.example.qrlock.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.qrlock.data.LockRepository
import com.example.qrlock.util.QrUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QrCodeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val token by LockRepository.token.collectAsState()
    var showRegenDialog by remember { mutableStateOf(false) }

    val bitmap by produceState<Bitmap?>(initialValue = null, token) {
        value = withContext(Dispatchers.Default) { QrUtils.encodeToBitmap(token, 720) }
    }

    Scaffold(topBar = { TopBar("My QR key", onBack = onBack) }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Print this code and keep it somewhere safe. Scanning it from the app toggles your protected apps between locked and unlocked.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val bmp = bitmap
                if (bmp == null) {
                    CircularProgressIndicator()
                } else {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Your QR key",
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { bitmap?.let { shareQr(context, it) } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Share / Print") }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showRegenDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Generate a new code") }

            Spacer(Modifier.height(16.dp))
            Text(
                "Keep this code private — anyone who scans it can lock or unlock your apps.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (showRegenDialog) {
        AlertDialog(
            onDismissRequest = { showRegenDialog = false },
            title = { Text("Generate a new code?") },
            text = { Text("Your current printed code will stop working. You'll need to print the new one.") },
            confirmButton = {
                TextButton(onClick = {
                    LockRepository.regenerateToken()
                    showRegenDialog = false
                }) { Text("Replace") }
            },
            dismissButton = {
                TextButton(onClick = { showRegenDialog = false }) { Text("Cancel") }
            },
        )
    }
}

private fun shareQr(context: android.content.Context, bitmap: Bitmap) {
    val uri = QrUtils.saveForSharing(context, bitmap)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share QR key"))
}
