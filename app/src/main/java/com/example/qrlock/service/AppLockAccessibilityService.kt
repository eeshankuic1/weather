package com.example.qrlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.qrlock.MainActivity
import com.example.qrlock.data.LockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * The "guard". The system delivers a window-state-changed event whenever a new app comes to the
 * foreground. If that app is one the user chose to protect and the lock is currently engaged, we
 * draw a full-screen overlay on top of it so it can't be used until the user scans their QR code.
 *
 * Uses a [WindowManager] overlay (SYSTEM_ALERT_WINDOW) rather than launching an Activity, which is
 * the reliable way to cover another app on modern Android.
 */
class AppLockAccessibilityService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private var overlay: View? = null
    private var currentPackage: String? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Re-evaluate whenever the lock state or the protected-app set changes (e.g. the user
        // just scanned their QR code), even if no new window event has arrived.
        scope.launch {
            combine(LockRepository.locked, LockRepository.protectedPackages) { _, _ -> }
                .collect { evaluate() }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            // Ignore our own windows (including the overlay) and system UI noise.
            if (pkg == packageName) {
                hideOverlay()
                currentPackage = pkg
                return
            }
            currentPackage = pkg
            evaluate()
        }
    }

    private fun evaluate() {
        val pkg = currentPackage
        val shouldLock = LockRepository.locked.value &&
            pkg != null &&
            pkg != packageName &&
            LockRepository.isProtected(pkg)

        if (shouldLock) showOverlay() else hideOverlay()
    }

    private fun showOverlay() {
        if (overlay != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE,
        )

        val view = buildOverlayView()
        runCatching { windowManager.addView(view, params) }
            .onSuccess { overlay = view }
    }

    private fun hideOverlay() {
        overlay?.let { runCatching { windowManager.removeView(it) } }
        overlay = null
    }

    private fun buildOverlayView(): View {
        val teal = Color.parseColor("#0D4F4F")
        val sand = Color.parseColor("#F5F0E8")
        val copper = Color.parseColor("#C17F59")
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(teal)
            setPadding(dp(32), dp(32), dp(32), dp(32))
            // Swallow stray taps on the background so nothing leaks to the app underneath.
            isClickable = true

            addView(TextView(context).apply {
                text = "🔒"
                textSize = 64f
            })
            addView(TextView(context).apply {
                text = "Locked"
                setTextColor(sand)
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, dp(16), 0, 0)
            })
            addView(TextView(context).apply {
                text = "Scan your QR code to unlock your apps."
                setTextColor(sand)
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, dp(28))
            })
            addView(Button(context).apply {
                text = "Scan QR to unlock"
                setBackgroundColor(copper)
                setTextColor(Color.WHITE)
                setOnClickListener { openScanner() }
            })
            addView(Button(context).apply {
                text = "Go to home screen"
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(sand)
                setOnClickListener { goHome() }
            })
        }
    }

    private fun openScanner() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_OPEN_SCANNER, true)
        }
        startActivity(intent)
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        hideOverlay()
        scope.cancel()
        return super.onUnbind(intent)
    }
}
