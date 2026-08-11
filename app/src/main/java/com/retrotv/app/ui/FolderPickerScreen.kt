package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.ui.components.RetroButton
import com.retrotv.app.ui.theme.TvAccentAmber
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvTextSecondary

enum class FolderStatus {
    NOT_SELECTED,
    NOT_FOUND
}

@Composable
fun FolderPickerScreen(
    status: FolderStatus,
    onSelectFolder: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 700.dp)
        ) {
            Text(
                text = "RETROTV",
                style = MaterialTheme.typography.headlineLarge,
                color = TvAccentGreen
            )

            Text(
                text = if (status == FolderStatus.NOT_SELECTED) {
                    "Select your RetroTV folder to get started"
                } else {
                    "RetroTV storage not found"
                },
                style = MaterialTheme.typography.titleLarge,
                color = if (status == FolderStatus.NOT_SELECTED) TvTextSecondary else TvAccentAmber,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Text(
                text = if (status == FolderStatus.NOT_SELECTED) {
                    "Connect your USB HDD and choose the RetroTV folder on it. " +
                        "This only needs to be done once."
                } else {
                    "The USB HDD with your RetroTV folder isn't connected right now. " +
                        "Reconnect it, or choose a different folder below."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = TvTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            RetroButton(
                onClick = onSelectFolder,
                focusRequester = focusRequester
            ) {
                Text(
                    text = if (status == FolderStatus.NOT_SELECTED) {
                        "SELECT RETROTV FOLDER"
                    } else {
                        "CHOOSE FOLDER AGAIN"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
                )
            }
        }
    }
}
