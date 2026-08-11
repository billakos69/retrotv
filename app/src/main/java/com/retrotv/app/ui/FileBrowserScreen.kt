package com.retrotv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.retrotv.app.ui.components.RetroButton
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

            if (currentDir != null) {
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    NavButton(text = "◀ BACK") {
                        val parent = currentDir!!.parentFile
                        currentDir = if (parent != null && roots.any {
                                it.second.absolutePath.startsWith(parent.absolutePath) ||
                                    parent.absolutePath.startsWith(it.second.absolutePath)
                            }
                        ) {
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

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
    RetroButton(onClick = onClick) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun FolderRow(name: String, onClick: () -> Unit) {
    RetroButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "\uD83D\uDCC1", modifier = Modifier.width(36.dp))
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
