package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.data.LibraryScanner
import com.retrotv.app.data.model.Channel
import com.retrotv.app.data.model.ScannedLibrary
import com.retrotv.app.ui.components.RetroButton
import com.retrotv.app.ui.theme.TvAccentAmber
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvSurface
import com.retrotv.app.ui.theme.TvTextPrimary
import com.retrotv.app.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LibraryScreen(
    rootPath: String,
    onBack: () -> Unit
) {
    val backFocusRequester = remember { FocusRequester() }
    var isLoading by remember { mutableStateOf(true) }
    var library by remember { mutableStateOf<ScannedLibrary?>(null) }

    LaunchedEffect(rootPath) {
        isLoading = true
        library = withContext(Dispatchers.IO) { LibraryScanner.scan(rootPath) }
        isLoading = false
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) backFocusRequester.requestFocus()
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
                    text = "LIBRARY",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TvAccentGreen,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer()

            when {
                isLoading -> Text(
                    text = "Scanning $rootPath ...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TvTextSecondary
                )

                library == null -> Text(
                    text = "Scan failed.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TvTextSecondary
                )

                else -> LibraryResults(library!!)
            }
        }
    }
}

@Composable
private fun Spacer() {
    Box(modifier = Modifier.padding(top = 28.dp))
}

@Composable
private fun LibraryResults(library: ScannedLibrary) {
    val totalEpisodes = library.channels.sumOf { channel -> channel.series.sumOf { it.episodes.size } }

    Column {
        Text(
            text = "${library.channels.size} channels  ·  $totalEpisodes episodes  ·  " +
                "${library.ads.size} ads  ·  ${library.jingles.size} jingles",
            style = MaterialTheme.typography.bodyLarge,
            color = TvTextSecondary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (library.channels.isEmpty()) {
            Text(
                text = "No channels found under Channels/. Check the folder structure.",
                style = MaterialTheme.typography.bodyLarge,
                color = TvAccentAmber
            )
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(library.channels) { channel -> ChannelRow(channel) }
        }
    }
}

@Composable
private fun ChannelRow(channel: Channel) {
    val episodeCount = channel.series.sumOf { it.episodes.size }
    val logoStatus = if (channel.logoFile != null) "logo ✓" else "logo missing"
    val logoColor = if (channel.logoFile != null) TvAccentGreen else TvAccentAmber

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TvSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.titleMedium,
                color = TvTextPrimary
            )
            Text(
                text = logoStatus,
                style = MaterialTheme.typography.labelLarge,
                color = logoColor
            )
        }
        Text(
            text = "${channel.series.size} series  ·  $episodeCount episodes",
            style = MaterialTheme.typography.bodyLarge,
            color = TvTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
