package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
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
            .padding(48.dp)
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(MainMenuItem.values().toList()) { index, item ->
                    MenuButton(
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
    item: MainMenuItem,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    var modifier = Modifier
        .fillMaxWidth(0.35f)
        .onFocusChanged { focusState -> isFocused = focusState.isFocused }
        .border(
            width = if (isFocused) 3.dp else 0.dp,
            color = TvAccentGreen
        )

    if (focusRequester != null) {
        modifier = modifier.focusRequester(focusRequester)
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF1A1A1A),
            contentColor = Color(0xFFECECEC),
            focusedContainerColor = Color(0xFF232323),
            focusedContentColor = TvAccentGreen
        )
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
