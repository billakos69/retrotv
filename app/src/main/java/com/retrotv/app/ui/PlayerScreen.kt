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
import com.retrotv.app.data.model.Episode
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvTextSecondary

@Composable
fun PlayerScreen(
    channelName: String,
    episodes: List<Episode>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var currentIndex by remember { mutableStateOf(0) }

    BackHandler(onBack = onBack)

    DisposableEffect(episodes) {
        val mediaItems = episodes.map { MediaItem.fromUri(Uri.fromFile(it.file)) }
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentIndex = exoPlayer.currentMediaItemIndex
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
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
