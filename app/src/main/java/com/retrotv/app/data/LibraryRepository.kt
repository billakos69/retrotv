package com.retrotv.app.data

import androidx.room.withTransaction
import com.retrotv.app.data.db.AdEntity
import com.retrotv.app.data.db.ChannelEntity
import com.retrotv.app.data.db.EpisodeEntity
import com.retrotv.app.data.db.JingleEntity
import com.retrotv.app.data.db.RetroTvDatabase
import com.retrotv.app.data.db.SeriesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LibraryRepository(private val db: RetroTvDatabase) {

    fun observeChannels(): Flow<List<ChannelEntity>> = db.channelDao().getAll()

    suspend fun getEpisodesForChannel(channelId: Long): List<EpisodeEntity> {
        val seriesList: List<SeriesEntity> = db.seriesDao().getForChannel(channelId).first()
        return seriesList.flatMap { series ->
            db.episodeDao().getForSeries(series.id).first()
        }
    }

    suspend fun getAdsOnce(): List<AdEntity> = db.adDao().getAll().first()

    suspend fun getJinglesOnce(): List<JingleEntity> = db.jingleDao().getAll().first()

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
                                watched = watched,
                                durationMs = episode.durationMs
                            )
                        )
                    }
                }
            }

            library.ads.forEach { ad ->
                db.adDao().insert(
                    AdEntity(title = ad.title, filePath = ad.file.absolutePath, durationMs = ad.durationMs)
                )
            }
            library.jingles.forEach { jingle ->
                db.jingleDao().insert(
                    JingleEntity(title = jingle.title, filePath = jingle.file.absolutePath, durationMs = jingle.durationMs)
                )
            }
        }
    }

    suspend fun deleteChannel(channel: ChannelEntity) {
        db.channelDao().delete(channel)
    }

    suspend fun moveChannel(currentOrder: List<ChannelEntity>, channel: ChannelEntity, delta: Int) {
        val index = currentOrder.indexOfFirst { it.id == channel.id }
        if (index < 0) return
        val targetIndex = index + delta
        if (targetIndex < 0 || targetIndex >= currentOrder.size) return

        val target = currentOrder[targetIndex]
        db.withTransaction {
            db.channelDao().update(channel.copy(sortOrder = target.sortOrder))
            db.channelDao().update(target.copy(sortOrder = channel.sortOrder))
        }
    }

    suspend fun updateEpisodeProgress(episodeId: Long, positionMs: Long, watched: Boolean) {
        db.episodeDao().updateProgress(episodeId, positionMs, watched)
    }

    // Renames a channel in place (its DB row only — the on-disk folder name
    // is untouched, so a future RESCAN LIBRARY will bring the folder name
    // back unless the disk folder itself is also renamed).
    suspend fun renameChannel(channel: ChannelEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        db.channelDao().update(channel.copy(name = trimmed))
    }
}
