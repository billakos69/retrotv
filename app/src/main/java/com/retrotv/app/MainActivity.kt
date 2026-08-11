package com.retrotv.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.data.SettingsRepository
import com.retrotv.app.ui.*
import com.retrotv.app.ui.theme.RetroTVTheme
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.util.StorageUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

private enum class AppScreen {
    LOADING, NEEDS_PERMISSION, PICKING_FOLDER, NEEDS_FOLDER, MAIN_MENU, SETTINGS, LIBRARY
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
    var folderPath by remember { mutableStateOf<String?>(null) }

    fun refreshFromStorage(path: String?) {
        folderPath = path
        screen = if (StorageUtils.isFolderAccessible(path)) {
            AppScreen.MAIN_MENU
        } else {
            folderStatus = if (path == null) FolderStatus.NOT_SELECTED else FolderStatus.NOT_FOUND
            if (StorageUtils.hasAllFilesAccess()) AppScreen.NEEDS_FOLDER else AppScreen.NEEDS_PERMISSION
        }
    }

    LaunchedEffect(Unit) {
        val stored = settingsRepository.rootFolderUri.first()
        refreshFromStorage(stored)
    }

    when (screen) {
        AppScreen.LOADING -> LoadingScreen()

        AppScreen.NEEDS_PERMISSION -> PermissionRequestScreen(
            onGrantPermission = {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            },
            onRecheck = {
                scope.launch {
                    val stored = settingsRepository.rootFolderUri.first()
                    refreshFromStorage(stored)
                }
            }
        )

        AppScreen.NEEDS_FOLDER -> FolderPickerScreen(
            status = folderStatus,
            onSelectFolder = { screen = AppScreen.PICKING_FOLDER }
        )

        AppScreen.PICKING_FOLDER -> FileBrowserScreen(
            onFolderChosen = { dir: File ->
                scope.launch {
                    settingsRepository.setRootFolderUri(dir.absolutePath)
                    refreshFromStorage(dir.absolutePath)
                }
            }
        )

        AppScreen.MAIN_MENU -> MainMenuScreen(
            onItemSelected = { item ->
                when (item) {
                    MainMenuItem.SETTINGS -> screen = AppScreen.SETTINGS
                    MainMenuItem.LIBRARY -> screen = AppScreen.LIBRARY
                    else -> { /* not wired up yet */ }
                }
            }
        )

        AppScreen.SETTINGS -> SettingsScreen(
            currentFolderPath = folderPath ?: "Not set",
            onChangeFolder = { screen = AppScreen.PICKING_FOLDER }
        )

        AppScreen.LIBRARY -> LibraryScreen(
            rootPath = folderPath ?: "",
            onBack = { screen = AppScreen.MAIN_MENU }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(TvBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "RETROTV", style = MaterialTheme.typography.headlineLarge, color = TvAccentGreen)
    }
}
