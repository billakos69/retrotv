package com.retrotv.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.data.LibraryRepository
import com.retrotv.app.data.db.ChannelEntity
import com.retrotv.app.ui.components.RetroButton
import com.retrotv.app.ui.theme.TvAccentAmber
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvSurface
import com.retrotv.app.ui.theme.TvTextPrimary
import com.retrotv.app.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChannelsScreen(
    repository: LibraryRepository,
    rootPath: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val backFocusRequester = remember { FocusRequester() }
    val channels by repository.observeChannels().collectAsState(initial = emptyList())
    var isSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        backFocusRequester.requestFocus()
    }

    fun rescan() {
        if (rootPath.isBlank()) return
        scope.launch {
            isSyncing = true
            withContext(Dispatchers.IO) { repository.syncFromDisk(rootPath) }
            isSyncing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 56.dp, vertical = 40.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                RetroButton(onClick = onBack, focusRequester = backFocusRequester) {
                    Text(text = "< BACK", style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    text = "CHANNELS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TvAccentGreen,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Box(modifier = Modifier.padding(top = 20.dp)) {
                RetroButton(onClick = { rescan() }) {
                    Text(
                        text = if (isSyncing) "SCANNING..." else "RESCAN LIBRARY",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Box(modifier = Modifier.padding(top = 28.dp)) {
                if (channels.isEmpty()) {
                    Text(
                        text = if (isSyncing) "Scanning $rootPath ..." else "No channels yet. Tap RESCAN LIBRARY to scan $rootPath.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TvTextSecondary
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(channels) { index, channel ->
                            ChannelRow(
                                channel = channel,
                                isFirst = index == 0,
                                isLast = index == channels.lastIndex,
                                onMoveUp = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            repository.moveChannel(channels, channel, -1)
                                        }
                                    }
                                },
                                onMoveDown = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            repository.moveChannel(channels, channel, +1)
                                        }
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            repository.deleteChannel(channel)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channel: ChannelEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmingDelete by remember(channel.id) { mutableStateOf(false) }

    val onDeleteButtonClick: () -> Unit = {
        if (confirmingDelete) {
            confirmingDelete = false
            onDelete()
        } else {
            confirmingDelete = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TvSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChannelLogo(logoPath = channel.logoPath)
            Text(
                text = channel.name,
                style = MaterialTheme.typography.titleMedium,
                color = TvTextPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!isFirst) {
                RetroButton(onClick = onMoveUp) {
                    Text(text = "UP", style = MaterialTheme.typography.labelLarge)
                }
            }
            if (!isLast) {
                RetroButton(onClick = onMoveDown) {
                    Text(text = "DOWN", style = MaterialTheme.typography.labelLarge)
                }
            }
            RetroButton(
                onClick = onDeleteButtonClick,
                accentColor = if (confirmingDelete) TvAccentAmber else TvAccentGreen
            ) {
                Text(
                    text = if (confirmingDelete) "CONFIRM?" else "DELETE",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ChannelLogo(logoPath: String?) {
    var bitmap by remember(logoPath) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(logoPath) {
        bitmap = if (logoPath != null) {
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(logoPath)?.asImageBitmap() }.getOrNull()
            }
        } else null
    }

    Box(
        modifier = Modifier.size(48.dp).background(TvBackground),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(bitmap = bmp, contentDescription = null, modifier = Modifier.fillMaxSize())
        } else {
            Text(text = "?", color = TvAccentAmber, style = MaterialTheme.typography.labelLarge)
        }
    }
}
