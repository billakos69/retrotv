package com.retrotv.app.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object StorageUtils {

    fun isTreeAccessible(context: Context, uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        return try {
            val uri = Uri.parse(uriString)
            val doc = DocumentFile.fromTreeUri(context, uri)
            doc != null && doc.exists() && doc.isDirectory && doc.canRead()
        } catch (e: Exception) {
            false
        }
    }
}
