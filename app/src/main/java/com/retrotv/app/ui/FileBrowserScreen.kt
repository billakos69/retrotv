package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
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
import com.retrotv.app.util.StorageUtils
import java.io.File

@Composable
fun FileBrowserScreen(
    onFolderChosen: (File) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val roots = remember { StorageUtils.getStorageRoots(context) }

    // currentDir == null means we're at the "pick a storage root" level
    var currentDir by remember { mutableStateOf<File?>(null) }

    val entries: List<File> = remember(currentDir) {
        currentDir?.listFiles { f -> f.isDirectory }?.sortedBy { it.name.lowercase() } ?: emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(48.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "SELECT RETROTV FOLDER",
                style = MaterialTheme.typography.headlineLarge,
                color = TvAccentGreen
            )
            Text(
                text = currentDir?.absolutePath ?: "Choose a storage device",
                style = MaterialTheme.typography.bodyLarge,
                color = TvTextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                if (currentDir != null) {
                    NavButton(text = "◀ BACK") {
                        val parent = currentDir!!.parentFile
                        currentDir = if (parent != null && roots.any { it.second.absolutePath.startsWith(parent.absolutePath) || parent.absolutePath.startsWith(it.second.absolutePath) }) {
                            parent
                        } else {
                            null
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    NavButton(text = "USE THIS FOLDER") {
                        onFolderChosen(currentDir!!)
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (currentDir == null) {
                    items(roots) { (label, dir) ->
                        FolderRow(name = label, onClick = { currentDir = dir })
                    }
                } else {
                    items(entries) { dir ->
                        FolderRow(name = dir.name, onClick = { currentDir = dir })
                    }
                }
            }
        }
    }
}

@Composable
private fun NavButton(text: String, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Button(
        onClick = onClick,
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .border(width = if (isFocused) 3.dp else 0.dp, color = TvAccentGreen),
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF1A1A1A),
            contentColor = Color(0xFFECECEC),
            focusedContainerColor = Color(0xFF232323),
            focusedContentColor = TvAccentGreen
        )
    ) {
        Text(text = text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}

@Composable
private fun FolderRow(name: String, onClick: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "\uD83D\uDCC1  $name")
        }
    }
}
