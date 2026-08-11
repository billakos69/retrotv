package com.retrotv.app.ui

import androidx.compose.foundation.background
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
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvTextSecondary

@Composable
fun PermissionRequestScreen(
    onGrantPermission: () -> Unit,
    onRecheck: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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
            Text(text = "RETROTV", style = MaterialTheme.typography.headlineLarge, color = TvAccentGreen)
            Text(
                text = "RetroTV needs storage access",
                style = MaterialTheme.typography.titleLarge,
                color = TvTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = "Grant 'Allow access to manage all files' on the next screen, then press Back to return here.",
                style = MaterialTheme.typography.bodyLarge,
                color = TvTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )
            RetroButton(
                onClick = { onGrantPermission(); onRecheck() },
                focusRequester = focusRequester
            ) {
                Text(
                    text = "GRANT ACCESS",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
                )
            }
        }
    }
}
