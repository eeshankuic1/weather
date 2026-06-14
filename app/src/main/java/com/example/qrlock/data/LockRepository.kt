package com.example.qrlock.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Single source of truth for the app's state, shared between the UI ([com.example.qrlock.MainActivity])
 * and the guard ([com.example.qrlock.service.AppLockAccessibilityService]). Both run in the same
 * process, so a process-wide singleton backed by [SharedPreferences] keeps them in sync.
 *
 * State:
 *  - [token]: the secret string encoded in the printable QR code. Scanning a QR whose payload
 *    matches this token toggles [locked].
 *  - [protectedPackages]: the package names of the apps the user chose to protect.
 *  - [locked]: when true, opening any protected app shows the lock screen.
 */
object LockRepository {

    private const val PREFS = "qr_app_lock_prefs"
    private const val KEY_TOKEN = "qr_token"
    private const val KEY_PACKAGES = "protected_packages"
    private const val KEY_LOCKED = "locked"

    private lateinit var prefs: SharedPreferences

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val _protectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val protectedPackages: StateFlow<Set<String>> = _protectedPackages.asStateFlow()

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    /** Must be called once from [com.example.qrlock.LockApplication.onCreate]. */
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val existingToken = prefs.getString(KEY_TOKEN, null) ?: newToken().also {
            prefs.edit().putString(KEY_TOKEN, it).apply()
        }
        _token.value = existingToken
        _protectedPackages.value = prefs.getStringSet(KEY_PACKAGES, emptySet())!!.toSet()
        _locked.value = prefs.getBoolean(KEY_LOCKED, false)
    }

    fun setProtectedPackages(packages: Set<String>) {
        _protectedPackages.value = packages
        prefs.edit().putStringSet(KEY_PACKAGES, packages).apply()
    }

    fun setLocked(value: Boolean) {
        _locked.value = value
        prefs.edit().putBoolean(KEY_LOCKED, value).apply()
    }

    /** Flips the lock state and returns the new value. Called when the correct QR is scanned. */
    fun toggleLocked(): Boolean {
        val next = !_locked.value
        setLocked(next)
        return next
    }

    /** Generates a brand-new secret. The old printed QR code stops working after this. */
    fun regenerateToken(): String {
        val token = newToken()
        _token.value = token
        prefs.edit().putString(KEY_TOKEN, token).apply()
        return token
    }

    fun isProtected(packageName: String?): Boolean =
        packageName != null && _protectedPackages.value.contains(packageName)

    private fun newToken(): String = "qrapplock:" + UUID.randomUUID().toString()
}
