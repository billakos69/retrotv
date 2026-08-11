package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

@Composable
fun SettingsScreen(
    currentFolderPath: String,
    onChangeFolder: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(48.dp)
    ) {
        Column(modifier = Modifier.widthIn(max = 800.dp)) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                color = TvAccentGreen
            )

            Text(
                text = "Storage",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFECECEC),
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
            )

            Text(
                text = currentFolderPath,
                style = MaterialTheme.typography.bodyLarge,
                color = TvTextSecondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onChangeFolder,
                modifier = Modifier
                    .onFocusChanged { isFocused = it.isFocused }
                    .border(
                        width = if (isFocused) 3.dp else 0.dp,
                        color = TvAccentGreen
                    )
                    .focusRequester(focusRequester),
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color(0xFFECECEC),
                    focusedContainerColor = Color(0xFF232323),
                    focusedContentColor = TvAccentGreen
                )
            ) {
                Text(
                    text = "CHANGE FOLDER",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
