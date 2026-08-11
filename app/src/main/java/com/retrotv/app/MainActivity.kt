package com.retrotv.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.data.SettingsRepository
import com.retrotv.app.ui.FolderPickerScreen
import com.retrotv.app.ui.FolderStatus
import com.retrotv.app.ui.MainMenuItem
import com.retrotv.app.ui.MainMenuScreen
import com.retrotv.app.ui.SettingsScreen
import com.retrotv.app.ui.theme.RetroTVTheme
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.util.StorageUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private enum class AppScreen {
    LOADING,
    NEEDS_FOLDER,
    MAIN_MENU,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)
        enableEdgeToEdge()
        setContent {
            RetroTVTheme {
                RetroTVApp(settingsRepository = settingsRepository)
            }
        }
    }
}

@Composable
private fun RetroTVApp(settingsRepository: SettingsRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var screen by remember { mutableStateOf(AppScreen.LOADING) }
    var folderStatus by remember { mutableStateOf(FolderStatus.NOT_SELECTED) }
    var folderUriString by remember { mutableStateOf<String?>(null) }

    fun refreshFromStorage(uriString: String?) {
        folderUriString = uriString
        screen = if (StorageUtils.isTreeAccessible(context, uriString)) {
            AppScreen.MAIN_MENU
        } else {
            folderStatus = if (uriString == null) FolderStatus.NOT_SELECTED else FolderStatus.NOT_FOUND
            AppScreen.NEEDS_FOLDER
        }
    }

    LaunchedEffect(Unit) {
        val stored = settingsRepository.rootFolderUri.first()
        refreshFromStorage(stored)
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            scope.launch {
                settingsRepository.setRootFolderUri(uri.toString())
                refreshFromStorage(uri.toString())
            }
        }
    }

    when (screen) {
        AppScreen.LOADING -> LoadingScreen()

        AppScreen.NEEDS_FOLDER -> FolderPickerScreen(
            status = folderStatus,
            onSelectFolder = { folderPickerLauncher.launch(null) }
        )

        AppScreen.MAIN_MENU -> MainMenuScreen(
            onItemSelected = { item ->
                if (item == MainMenuItem.SETTINGS) {
                    screen = AppScreen.SETTINGS
                }
                // Other destinations (Channels, TV Guide, Schedule, Library,
                // Advertisements, Watch TV) are implemented in later stages.
            }
        )

        AppScreen.SETTINGS -> SettingsScreen(
            currentFolderPath = folderUriString?.let { Uri.parse(it).path ?: it } ?: "Not set",
            onChangeFolder = { folderPickerLauncher.launch(null) }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RETROTV",
            style = MaterialTheme.typography.headlineLarge,
            color = TvAccentGreen
        )
    }
}
