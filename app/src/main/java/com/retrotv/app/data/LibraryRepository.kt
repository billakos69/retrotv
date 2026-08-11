package com.retrotv.app.data

import androidx.room.withTransaction
import com.retrotv.app.data.db.AdEntity
import com.retrotv.app.data.db.ChannelEntity
import com.retrotv.app.data.db.EpisodeEntity
import com.retrotv.app.data.db.JingleEntity
import com.retrotv.app.data.db.RetroTvDatabase
import com.retrotv.app.data.db.SeriesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Bridges the on-disk LibraryScanner with the Room database.
 */
class LibraryRepository(private val db: RetroTvDatabase) {

    fun observeChannels(): Flow<List<ChannelEntity>> = db.channelDao().getAll()

    /**
     * Re-scans [rootPath] on disk and replaces the database contents with what
     * is found there. Per-episode watch progress (lastPositionMs / watched) is
     * preserved across a re-scan by matching on the episode's absolute file
     * path, so re-scanning doesn't reset "resume watching" state.
     */
    suspend fun syncFromDisk(rootPath: String) {
        val library = LibraryScanner.scan(rootPath)
        val previousEpisodes = db.episodeDao().getAllOnce()
        val progressByPath: Map<String, Pair<Long, Boolean>> =
            previousEpisodes.associate { it.filePath to (it.lastPositionMs to it.watched) }

        db.withTransaction {
            db.channelDao().deleteAll()
            db.adDao().deleteAll()
            db.jingleDao().deleteAll()

            library.channels.forEachIndexed { channelIndex, channel ->
                val channelId = db.channelDao().insert(
                    ChannelEntity(
                        name = channel.name,
                        folderPath = channel.folder.absolutePath,
                        logoPath = channel.logoFile?.absolutePath,
                        sortOrder = channelIndex
                    )
                )

                channel.series.forEach { series ->
                    val seriesId = db.seriesDao().insert(
                        SeriesEntity(
                            channelId = channelId,
                            name = series.name,
                            folderPath = series.folder.absolutePath
                        )
                    )

                    series.episodes.forEachIndexed { episodeIndex, episode ->
                        val path = episode.file.absolutePath
                        val (lastPositionMs, watched) = progressByPath[path] ?: (0L to false)
                        db.episodeDao().insert(
                            EpisodeEntity(
                                seriesId = seriesId,
                                title = episode.title,
                                filePath = path,
                                sortOrder = episodeIndex,
                                lastPositionMs = lastPositionMs,
                                watched = watched
                            )
                        )
                    }
                }
            }

            library.ads.forEach { ad ->
                db.adDao().insert(AdEntity(title = ad.title, filePath = ad.file.absolutePath))
            }
            library.jingles.forEach { jingle ->
                db.jingleDao().insert(JingleEntity(title = jingle.title, filePath = jingle.file.absolutePath))
            }
        }
    }
}
