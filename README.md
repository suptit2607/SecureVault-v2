# SecureVault

SecureVault is a modern Android starter app for protecting:

- Photos and videos
- Documents and other files
- Audio recordings
- Passwords and sensitive notes

## What is included

- Jetpack Compose UI with a current-generation dashboard design
- Local-first architecture for private vault data
- Android Keystore-backed AES/GCM encryption helper
- Biometric gate entry flow
- Real Android picker imports for photos, videos, files, and audio
- Encrypted local file storage inside the app sandbox
- Persisted password vault with encrypted secrets at rest
- Vault overview, encrypted item browser, password vault, and security center screens
- Security-focused app notes for the next implementation steps

## Security direction

This starter keeps the architecture aligned with strong Android security practices:

- `BiometricPrompt` is used to unlock the app with biometrics or device credentials.
- `CryptoManager` generates and uses a hardware-backed Android Keystore key when available.
- Imported files are encrypted into private app storage before metadata is saved.
- Password secrets are encrypted before they are persisted.
- Files are decrypted only into cache when the user explicitly opens them.
- The UI avoids rendering raw passwords until the user explicitly reveals them.
- Backup is disabled in the manifest to reduce accidental data export.

## Recommended next steps

1. Add encrypted Room or DataStore persistence for all vault categories.
2. Add secure thumbnail generation for imported media.
3. Add per-folder locking, decoy mode, secure deletion, and cloud backup with end-to-end encryption.
4. Add search, tags, and bulk actions for large vaults.
5. Add export/import recovery guarded by a recovery key.

## Build note

This repository contains the Android project source. If your machine does not already have an Android Gradle wrapper available, open the project in Android Studio and let it generate or sync the wrapper for you.
