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
import com.retrotv.app.data.LibraryRepository
import com.retrotv.app.data.SettingsRepository
import com.retrotv.app.data.db.ChannelEntity
import com.retrotv.app.data.db.RetroTvDatabase
import com.retrotv.app.data.schedule.ChannelPlaylistBuilder
import com.retrotv.app.data.schedule.ChannelScheduleCalculator
import com.retrotv.app.data.schedule.ScheduleItem
import com.retrotv.app.ui.*
import com.retrotv.app.ui.theme.RetroTVTheme
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.util.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class AppScreen {
    LOADING, NEEDS_PERMISSION, PICKING_FOLDER, NEEDS_FOLDER, MAIN_MENU, SETTINGS, LIBRARY, PLAYER, CHANNELS
}

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var libraryRepository: LibraryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)
        libraryRepository = LibraryRepository(RetroTvDatabase.getInstance(applicationContext))
        enableEdgeToEdge()
        setContent {
            RetroTVTheme {
                RetroTVApp(
                    settingsRepository = settingsRepository,
                    libraryRepository = libraryRepository
                )
            }
        }
    }
}

@Composable
private fun RetroTVApp(
    settingsRepository: SettingsRepository,
    libraryRepository: LibraryRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var screen by remember { mutableStateOf(AppScreen.LOADING) }
    var folderStatus by remember { mutableStateOf(FolderStatus.NOT_SELECTED) }
    var folderPath by remember { mutableStateOf<String?>(null) }

    var playerChannelName by remember { mutableStateOf("") }
    var playerItems by remember { mutableStateOf<List<ScheduleItem>>(emptyList()) }
    var playerStartIndex by remember { mutableStateOf(0) }
    var playerStartOffsetMs by remember { mutableStateOf(0L) }

    var channelList by remember { mutableStateOf<List<ChannelEntity>>(emptyList()) }
    var currentChannelIndex by remember { mutableStateOf(0) }

    fun refreshFromStorage(path: String?) {
        folderPath = path
        screen = if (StorageUtils.isFolderAccessible(path)) {
            AppScreen.MAIN_MENU
        } else {
            folderStatus = if (path == null) FolderStatus.NOT_SELECTED else FolderStatus.NOT_FOUND
            if (StorageUtils.hasAllFilesAccess()) AppScreen.NEEDS_FOLDER else AppScreen.NEEDS_PERMISSION
        }
    }

    /**
     * Tunes into the first channel found (walking outward from [startIndex] in
     * [direction]) that actually has episodes, wrapping around the list.
     * Updates the player state (channel name/playlist/live start position)
     * and [currentChannelIndex] on success. Returns false if no channel in
     * [channels] has any playable content.
     */
    suspend fun tunePlayableChannel(channels: List<ChannelEntity>, startIndex: Int, direction: Int): Boolean {
        if (channels.isEmpty()) return false
        var index = startIndex
        repeat(channels.size) {
            val normalizedIndex = ((index % channels.size) + channels.size) % channels.size
            val channel = channels[normalizedIndex]

            val playlist = withContext(Dispatchers.IO) {
                val episodes = libraryRepository.getEpisodesForChannel(channel.id)
                if (episodes.isEmpty()) {
                    null
                } else {
                    val ads = libraryRepository.getAdsOnce()
                    val jingles = libraryRepository.getJinglesOnce()
                    ChannelPlaylistBuilder.build(episodes, ads, jingles)
                }
            }

            if (playlist != null) {
                val program = ChannelScheduleCalculator.currentProgram(playlist)
                if (program != null) {
                    playerChannelName = channel.name
                    playerItems = playlist
                    playerStartIndex = program.itemIndex
                    playerStartOffsetMs = program.offsetMs
                    currentChannelIndex = normalizedIndex
                    return true
                }
            }
            index += direction
        }
        return false
    }

    fun startWatchingLiveChannel() {
        scope.launch {
            val channels = withContext(Dispatchers.IO) { libraryRepository.observeChannels().first() }
            channelList = channels
            val found = tunePlayableChannel(channels, 0, 1)
            if (found) screen = AppScreen.PLAYER
            // else: no playable content found yet — stays on the main menu.
        }
    }

    fun switchChannel(direction: Int) {
        if (channelList.isEmpty()) return
        scope.launch {
            tunePlayableChannel(channelList, currentChannelIndex + direction, direction)
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
                    MainMenuItem.CHANNELS -> screen = AppScreen.CHANNELS
                    MainMenuItem.WATCH_TV -> startWatchingLiveChannel()
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

        AppScreen.CHANNELS -> ChannelsScreen(
            repository = libraryRepository,
            rootPath = folderPath ?: "",
            onBack = { screen = AppScreen.MAIN_MENU }
        )

        AppScreen.PLAYER -> PlayerScreen(
            channelName = playerChannelName,
            items = playerItems,
            startIndex = playerStartIndex,
            startOffsetMs = playerStartOffsetMs,
            onEpisodeProgress = { episodeId, positionMs, watched ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        libraryRepository.updateEpisodeProgress(episodeId, positionMs, watched)
                    }
                }
            },
            onChannelUp = { switchChannel(1) },
            onChannelDown = { switchChannel(-1) },
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
