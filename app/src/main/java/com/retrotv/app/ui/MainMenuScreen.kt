package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.ui.components.RetroButton
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvTextSecondary

enum class MainMenuItem(val label: String) {
    WATCH_TV("WATCH TV"),
    CHANNELS("CHANNELS"),
    TV_GUIDE("TV GUIDE"),
    SCHEDULE("SCHEDULE"),
    LIBRARY("LIBRARY"),
    ADVERTISEMENTS("ADVERTISEMENTS"),
    SETTINGS("SETTINGS")
}

@Composable
fun MainMenuScreen(
    onItemSelected: (MainMenuItem) -> Unit = {}
) {
    val firstItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        firstItemFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 56.dp, vertical = 48.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RETROTV",
                style = MaterialTheme.typography.headlineLarge,
                color = TvAccentGreen
            )
            Text(
                text = "your own 24/7 local television network",
                style = MaterialTheme.typography.bodyLarge,
                color = TvTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(MainMenuItem.values().toList()) { index, item ->
                    MenuButton(
                        index = index,
                        item = item,
                        onClick = { onItemSelected(item) },
                        focusRequester = if (index == 0) firstItemFocusRequester else null
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuButton(
    index: Int,
    item: MainMenuItem,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null
) {
    RetroButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.4f),
        focusRequester = focusRequester
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%02d".format(index + 1),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(40.dp)
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
