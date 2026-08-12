package com.retrotv.app.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.data.db.EpisodeEntity
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.io.File

/**
 * Plays [episodes] as a continuous, looping channel, starting at [startIndex]
 * and [startOffsetMs] into that episode — i.e. wherever
 * [com.retrotv.app.data.schedule.ChannelScheduleCalculator] says the "live"
 * channel currently is, so opening a channel behaves like tuning into a real
 * broadcast already in progress.
 *
 * [onProgress] is called to persist watch state to the database: on a
 * 5-second checkpoint timer, whenever an episode finishes naturally, and once
 * more when the screen is closed — so per-episode position/watched state
 * (used for future catch-up/VOD features) survives navigating away or the
 * app being force-closed mid-episode.
 */
@Composable
fun PlayerScreen(
    channelName: String,
    episodes: List<EpisodeEntity>,
    startIndex: Int,
    startOffsetMs: Long,
    onProgress: (episodeId: Long, positionMs: Long, watched: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var currentIndex by remember { mutableStateOf(startIndex.coerceIn(0, (episodes.size - 1).coerceAtLeast(0))) }

    BackHandler(onBack = onBack)

    DisposableEffect(episodes, startIndex, startOffsetMs) {
        val mediaItems = episodes.map { MediaItem.fromUri(Uri.fromFile(File(it.filePath))) }
        val safeStartIndex = startIndex.coerceIn(0, (mediaItems.size - 1).coerceAtLeast(0))

        // A real channel never "ends" — loop the playlist so it behaves like
        // a continuous broadcast instead of stopping after the last episode.
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.setMediaItems(mediaItems, safeStartIndex, startOffsetMs.coerceAtLeast(0L))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val previousIndex = currentIndex
                currentIndex = exoPlayer.currentMediaItemIndex
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && previousIndex in episodes.indices) {
                    // The previous episode played through to the end naturally.
                    onProgress(episodes[previousIndex].id, 0L, true)
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            // Remember exactly where playback was left, in case this episode
            // gets resumed later.
            if (currentIndex in episodes.indices) {
                onProgress(episodes[currentIndex].id, exoPlayer.currentPosition, false)
            }
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Periodic checkpoint so progress survives a crash/force-close, not just
    // a clean back-navigation.
    LaunchedEffect(episodes, startIndex, startOffsetMs) {
        while (true) {
            delay(5000)
            if (currentIndex in episodes.indices) {
                onProgress(episodes[currentIndex].id, exoPlayer.currentPosition, false)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
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

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        ) {
            Text(
                text = channelName.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = TvAccentGreen
            )
            if (episodes.isNotEmpty() && currentIndex < episodes.size) {
                Text(
                    text = episodes[currentIndex].title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TvTextSecondary
                )
            }
        }
    }
}
