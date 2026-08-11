package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.ui.theme.TvAccentGreen
import com.retrotv.app.ui.theme.TvBackground
import com.retrotv.app.ui.theme.TvTextSecondary

@Composable
fun PermissionRequestScreen(
    onGrantPermission: () -> Unit,
    onRecheck: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier.fillMaxSize().background(TvBackground).padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 700.dp)) {
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
            Button(
                onClick = { onGrantPermission(); onRecheck() },
                modifier = Modifier
                    .onFocusChanged { isFocused = it.isFocused }
                    .border(width = if (isFocused) 3.dp else 0.dp, color = TvAccentGreen)
                    .focusRequester(focusRequester),
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color(0xFFECECEC),
                    focusedContainerColor = Color(0xFF232323),
                    focusedContentColor = TvAccentGreen
                )
            ) {
                Text(text = "GRANT ACCESS", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}
