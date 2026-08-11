package com.retrotv.app.util

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import java.io.File

object StorageUtils {

    fun hasAllFilesAccess(): Boolean {
        return Environment.isExternalStorageManager()
    }

    /** Returns top-level storage roots: internal storage + any USB/SD volumes. */
    fun getStorageRoots(context: Context): List<Pair<String, File>> {
        val roots = mutableListOf<Pair<String, File>>()
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        for (volume in storageManager.storageVolumes) {
            val dir = volume.directory ?: continue
            val label = if (volume.isPrimary) "Internal Storage" else (volume.getDescription(context) ?: "USB Storage")
            roots.add(label to dir)
        }
        return roots
    }

    fun isFolderAccessible(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val f = File(path)
        return f.exists() && f.isDirectory && f.canRead()
    }
}
