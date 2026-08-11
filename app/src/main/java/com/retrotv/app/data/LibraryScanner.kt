package com.retrotv.app.data

import android.media.MediaMetadataRetriever
import com.retrotv.app.data.model.Channel
import com.retrotv.app.data.model.Episode
import com.retrotv.app.data.model.MediaClip
import com.retrotv.app.data.model.ScannedLibrary
import com.retrotv.app.data.model.Series
import com.retrotv.app.util.naturalSortedBy
import java.io.File

/**
 * Scans the user's chosen root folder (plain java.io.File, no SAF) for the
 * Channels/, Ads/, Jingles/ and Logos/ sub-folders and builds an in-memory
 * snapshot of the library. Pure I/O + data mapping, no Compose/UI code here.
 */
object LibraryScanner {

    private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "webm", "m4v", "ts")
    private val LOGO_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "bmp")

    fun scan(rootPath: String): ScannedLibrary {
        val root = File(rootPath)
        val channelsDir = File(root, "Channels")
        val adsDir = File(root, "Ads")
        val jinglesDir = File(root, "Jingles")
        val logosDir = File(root, "Logos")

        val logoFiles = if (logosDir.isDirectory) {
            logosDir.listFiles { f -> f.isFile && f.extension.lowercase() in LOGO_EXTENSIONS } ?: emptyArray()
        } else emptyArray()

        val channels = if (channelsDir.isDirectory) {
            (channelsDir.listFiles { f -> f.isDirectory } ?: emptyArray())
                .toList()
                .naturalSortedBy { it.name }
                .map { scanChannel(it, logoFiles) }
        } else emptyList()

        return ScannedLibrary(
            channels = channels,
            ads = scanClips(adsDir),
            jingles = scanClips(jinglesDir)
        )
    }

    private fun scanChannel(channelDir: File, logoFiles: Array<File>): Channel {
        val logo = logoFiles.firstOrNull {
            it.nameWithoutExtension.equals(channelDir.name, ignoreCase = true)
        }

        val subFolders = (channelDir.listFiles { f -> f.isDirectory } ?: emptyArray())
            .toList()
            .naturalSortedBy { it.name }

        val series = if (subFolders.isNotEmpty()) {
            subFolders.map { scanSeries(it) }
        } else {
            // No series sub-folders: treat episodes directly under the channel
            // folder as one implicit series named after the channel.
            val directEpisodes = scanEpisodes(channelDir)
            if (directEpisodes.isNotEmpty()) {
                listOf(Series(name = channelDir.name, folder = channelDir, episodes = directEpisodes))
            } else {
                emptyList()
            }
        }

        return Channel(
            name = channelDir.name,
            folder = channelDir,
            logoFile = logo,
            series = series
        )
    }

    private fun scanSeries(seriesDir: File): Series {
        return Series(
            name = seriesDir.name,
            folder = seriesDir,
            episodes = scanEpisodes(seriesDir)
        )
    }

    private fun scanEpisodes(dir: File): List<Episode> {
        val files = (dir.listFiles { f -> f.isFile && f.extension.lowercase() in VIDEO_EXTENSIONS } ?: emptyArray())
            .toList()
            .naturalSortedBy { it.name }
        return files.map { Episode(title = it.nameWithoutExtension, file = it, durationMs = extractDurationMs(it)) }
    }

    private fun scanClips(dir: File): List<MediaClip> {
        if (!dir.isDirectory) return emptyList()
        val files = (dir.listFiles { f -> f.isFile && f.extension.lowercase() in VIDEO_EXTENSIONS } ?: emptyArray())
            .toList()
            .naturalSortedBy { it.name }
        return files.map { MediaClip(title = it.nameWithoutExtension, file = it) }
    }

    /**
     * Reads the duration of a video file via MediaMetadataRetriever. Returns 0
     * if the file can't be read (corrupt file, unsupported codec, etc.) so a
     * bad file doesn't crash the whole scan — it'll just be treated as
     * zero-length by the scheduler later.
     */
    private fun extractDurationMs(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            retriever.release()
        }
    }
}
