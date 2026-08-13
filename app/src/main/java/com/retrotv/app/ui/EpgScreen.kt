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
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.retrotv.app.data.schedule.ChannelPlaylistBuilder
import com.retrotv.app.data.schedule.ChannelScheduleCalculator
import com.retrotv.app.data.schedule.ScheduleItem
import com.retrotv.app.ui.components.RetroButton
import com.retrotv.app.ui.theme.TvAccentAmber
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvSurface
import com.retrotv.app.ui.theme.TvTextPrimary
import com.retrotv.app.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private data class NowPlayingInfo(val label: String, val title: String)

/**
 * Program guide: every channel with its logo and what's on right now,
 * recomputed every 30s while this screen is open so it doesn't go stale.
 * Selecting a channel tunes straight into it, at its live position.
 */
@Composable
fun EpgScreen(
    repository: LibraryRepository,
    onSelectChannel: (ChannelEntity) -> Unit,
    onBack: () -> Unit
) {
    val backFocusRequester = remember { FocusRequester() }
    val channels by repository.observeChannels().collectAsState(initial = emptyList())
    var nowPlaying by remember { mutableStateOf<Map<Long, NowPlayingInfo>>(emptyMap()) }

    LaunchedEffect(Unit) {
        backFocusRequester.requestFocus()
    }

    LaunchedEffect(channels) {
        while (true) {
            val updated = withContext(Dispatchers.IO) {
                val ads = repository.getAdsOnce()
                val jingles = repository.getJinglesOnce()
                channels.associate { channel ->
                    val episodes = repository.getEpisodesForChannel(channel.id)
                    val info = if (episodes.isEmpty()) {
                        NowPlayingInfo(label = "OFF AIR", title = "No content scanned yet")
                    } else {
                        val playlist = ChannelPlaylistBuilder.build(episodes, ads, jingles)
                        val program = ChannelScheduleCalculator.currentProgram(playlist)
                        if (program != null) {
                            NowPlayingInfo(label = labelFor(program.item), title = program.item.title)
                        } else {
                            NowPlayingInfo(label = "OFF AIR", title = "No content scanned yet")
                        }
                    }
                    channel.id to info
                }
            }
            nowPlaying = updated
            delay(30_000)
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
                    text = "TV GUIDE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TvAccentGreen,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Box(modifier = Modifier.padding(top = 28.dp)) {
                if (channels.isEmpty()) {
                    Text(
                        text = "No channels yet. Scan your library from CHANNELS first.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TvTextSecondary
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(channels, key = { it.id }) { channel ->
                            EpgRow(
                                channel = channel,
                                nowPlaying = nowPlaying[channel.id],
                                onClick = { onSelectChannel(channel) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpgRow(
    channel: ChannelEntity,
    nowPlaying: NowPlayingInfo?,
    onClick: () -> Unit
) {
    RetroButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EpgLogo(logoPath = channel.logoPath)

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TvTextPrimary
                )
                if (nowPlaying != null) {
                    Text(
                        text = nowPlaying.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = TvAccentAmber
                    )
                    Text(
                        text = nowPlaying.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TvTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun EpgLogo(logoPath: String?) {
    var bitmap by remember(logoPath) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(logoPath) {
        bitmap = if (logoPath != null) {
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(logoPath)?.asImageBitmap() }.getOrNull()
            }
        } else null
    }

    Box(
        modifier = Modifier.size(48.dp).background(TvSurface),
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

private fun labelFor(item: ScheduleItem): String = when (item) {
    is ScheduleItem.EpisodeItem -> "NOW PLAYING"
    is ScheduleItem.AdItem -> "ADVERTISEMENT"
    is ScheduleItem.JingleItem -> "STATION ID"
}
