package com.retrotv.app.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.data.schedule.ScheduleItem
import com.retrotv.app.ui.theme.TvAccentAmber
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvSurface
import com.retrotv.app.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Plays [items] (episodes interleaved with ads/jingles, as built by
 * ChannelPlaylistBuilder) as a continuous, looping channel, starting at
 * [startIndex] and [startOffsetMs] — wherever ChannelScheduleCalculator says
 * the "live" channel currently is.
 *
 * [onEpisodeProgress] is only invoked for episode items (ads/jingles have no
 * watch-progress to persist).
 *
 * [onChannelUp] / [onChannelDown] are triggered by the remote's dedicated
 * CHANNEL UP/DOWN keys and, as a fallback, by D-pad UP/DOWN.
 */
@Composable
fun PlayerScreen(
    channelName: String,
    logoPath: String?,
    items: List<ScheduleItem>,
    startIndex: Int,
    startOffsetMs: Long,
    onEpisodeProgress: (episodeId: Long, positionMs: Long, watched: Boolean) -> Unit,
    onChannelUp: () -> Unit,
    onChannelDown: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val rootFocusRequester = remember { FocusRequester() }
    var currentIndex by remember { mutableStateOf(startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))) }

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    fun reportProgressIfEpisode(index: Int, positionMs: Long, watched: Boolean) {
        val item = items.getOrNull(index) as? ScheduleItem.EpisodeItem ?: return
        onEpisodeProgress(item.episodeId, positionMs, watched)
    }

    DisposableEffect(items, startIndex, startOffsetMs) {
        val mediaItems = items.map { MediaItem.fromUri(Uri.fromFile(File(it.filePath))) }
        val safeStartIndex = startIndex.coerceIn(0, (mediaItems.size - 1).coerceAtLeast(0))

        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.setMediaItems(mediaItems, safeStartIndex, startOffsetMs.coerceAtLeast(0L))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val previousIndex = currentIndex
                currentIndex = exoPlayer.currentMediaItemIndex
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    reportProgressIfEpisode(previousIndex, 0L, true)
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            reportProgressIfEpisode(currentIndex, exoPlayer.currentPosition, false)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(items, startIndex, startOffsetMs) {
        while (true) {
            delay(5000)
            reportProgressIfEpisode(currentIndex, exoPlayer.currentPosition, false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.ChannelUp, Key.DirectionUp -> {
                        onChannelUp()
                        true
                    }
                    Key.ChannelDown, Key.DirectionDown -> {
                        onChannelDown()
                        true
                    }
                    else -> false
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    controllerShowTimeoutMs = 3000
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerLogo(logoPath = logoPath)

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = channelName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TvAccentGreen
                )
                val current = items.getOrNull(currentIndex)
                if (current != null) {
                    Text(
                        text = labelFor(current),
                        style = MaterialTheme.typography.labelLarge,
                        color = TvAccentAmber
                    )
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TvTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerLogo(logoPath: String?) {
    if (logoPath == null) return

    var bitmap by remember(logoPath) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(logoPath) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(logoPath)?.asImageBitmap() }.getOrNull()
        }
    }

    val bmp = bitmap ?: return
    Box(
        modifier = Modifier.size(56.dp).background(TvSurface),
        contentAlignment = Alignment.Center
    ) {
        Image(bitmap = bmp, contentDescription = null, modifier = Modifier.fillMaxSize())
    }
}

private fun labelFor(item: ScheduleItem): String = when (item) {
    is ScheduleItem.EpisodeItem -> "NOW PLAYING"
    is ScheduleItem.AdItem -> "ADVERTISEMENT"
    is ScheduleItem.JingleItem -> "STATION ID"
}
