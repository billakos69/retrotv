package com.retrotv.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "retrotv_settings")

private object SettingsKeys {
    val ROOT_FOLDER_URI = stringPreferencesKey("root_folder_uri")
}

class SettingsRepository(private val context: Context) {

    val rootFolderUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.ROOT_FOLDER_URI]
    }

    suspend fun setRootFolderUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.ROOT_FOLDER_URI] = uri
        }
    }

    suspend fun clearRootFolderUri() {
        context.dataStore.edit { prefs ->
            prefs.remove(SettingsKeys.ROOT_FOLDER_URI)
        }
    }
}
