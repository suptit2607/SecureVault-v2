package com.alish.securevault

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alish.securevault.data.PasswordVaultRepository
import com.alish.securevault.data.VaultRepository
import com.alish.securevault.security.CryptoManager
import com.alish.securevault.ui.SecureVaultApp
import com.alish.securevault.ui.SecureVaultViewModel
import com.alish.securevault.ui.theme.SecureVaultTheme
import kotlinx.coroutines.launch

@Composable
fun SecureVaultRoot(
    onBiometricUnlockRequest: (onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appContext = context.applicationContext
    val cryptoManager = remember { CryptoManager() }
    val passwordRepository = remember { PasswordVaultRepository(appContext, cryptoManager) }
    val vaultRepository = remember { VaultRepository(appContext, cryptoManager) }
    val viewModel: SecureVaultViewModel = viewModel(
        factory = SecureVaultViewModel.factory(
            vaultRepository = vaultRepository,
            passwordVaultRepository = passwordRepository
        )
    )

    val visualPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(15)
    ) { uris ->
        viewModel.importUris(uris)
    }
    val filesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.importUris(uris)
    }
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.importUris(uris)
    }

    SecureVaultTheme {
        SecureVaultApp(
            viewModel = viewModel,
            onUnlockRequest = onBiometricUnlockRequest,
            onImportMedia = {
                visualPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onImportFiles = {
                filesPicker.launch(arrayOf("*/*"))
            },
            onImportAudio = {
                audioPicker.launch(arrayOf("audio/*"))
            },
            onOpenVaultItem = { itemId ->
                coroutineScope.launch {
                    val asset = viewModel.prepareOpenAsset(itemId) ?: return@launch
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(asset.contentUri.toUri(), asset.mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open ${asset.fileName}"))
                }
            }
        )
    }
}
