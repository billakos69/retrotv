package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.ui.components.RetroButton
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvSurface
import com.retrotv.app.ui.theme.TvTextSecondary

@Composable
fun SettingsScreen(
    currentFolderPath: String,
    onChangeFolder: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(56.dp)
    ) {
        Column(modifier = Modifier.widthIn(max = 800.dp)) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                color = TvAccentGreen
            )

            Column(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurface)
                    .padding(24.dp)
            ) {
                Text(
                    text = "STORAGE",
                    style = MaterialTheme.typography.labelLarge,
                    color = TvAccentGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = currentFolderPath,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TvTextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                RetroButton(
                    onClick = onChangeFolder,
                    focusRequester = focusRequester
                ) {
                    Text(
                        text = "CHANGE FOLDER",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}
