# QR AppLock

An Android app that locks and unlocks a set of apps you choose **by scanning a QR code you print out**.

Scan the printed code once to lock your chosen apps; scan it again to unlock them. While locked, opening any
protected app shows a full-screen lock screen until you scan the code.

> Everything runs **on-device**. No accounts, no network, nothing leaves your phone.

---

## How it works

| Piece | Role |
|-------|------|
| **QR key** | A secret token generated on first launch and rendered as a QR code you print. Scanning a QR whose payload matches the token toggles the lock. |
| **Accessibility service** | Detects when a protected app is opened so the lock screen can be shown. |
| **Overlay (display over other apps)** | Draws the lock screen on top of the protected app. |
| **Camera + ML Kit** | Reads the printed QR code (on-device barcode scanning). |

## First-time setup (on your phone)

1. Install the APK (see below) and open **QR AppLock**.
2. Open **Permissions & setup** and grant:
   - **Accessibility access** — so it can tell when a protected app opens.
   - **Display over other apps** — so the lock screen can cover them.
3. Open **Choose apps to protect** and toggle on the apps you want behind the lock.
4. Open **My QR key**, tap **Share / Print**, and print the QR code (or save it). Keep it private —
   anyone who scans it can lock/unlock your apps.
5. Scan the printed code from the home screen to lock. Scan again to unlock.

## Building the APK

You don't need Android Studio — every push builds a debug APK in CI:

1. Go to the repo's **Actions** tab → **Build APK** workflow → latest run.
2. Download the **`qr-app-lock-debug-apk`** artifact.
3. Transfer `app-debug.apk` to your phone and install it (enable "install from unknown sources" when prompted).

To build locally instead (with the Android SDK installed):

```bash
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

## Tech

- Kotlin + Jetpack Compose (Material 3)
- CameraX + ML Kit barcode scanning (QR reading)
- ZXing (QR generation)
- AccessibilityService + SYSTEM_ALERT_WINDOW overlay (the lock mechanism)
- minSdk 26 (Android 8.0+), targetSdk 34, compileSdk 35

## Notes & limitations

This is a base app meant to be built on:

- The lock is a **deterrent**, not hardened security. A determined user with the printed code (or who
  disables the accessibility service in system settings) can get around it.
- One QR code toggles **all** selected apps together.
- Tested targets are recent Android versions; OEM battery/permission managers may need the app exempted
  from being killed so the accessibility service keeps running.
