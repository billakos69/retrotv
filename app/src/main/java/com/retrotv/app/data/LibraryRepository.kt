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

/**
 * Bridges the on-disk LibraryScanner with the Room database.
 */
class LibraryRepository(private val db: RetroTvDatabase) {

    fun observeChannels(): Flow<List<ChannelEntity>> = db.channelDao().getAll()

    /**
     * Flat, ordered list of every episode belonging to [channelId], across
     * all of its series — series in name order, episodes within a series in
     * their scanned (natural-sorted) order. This is the "channel playlist"
     * that [com.retrotv.app.data.schedule.ChannelScheduleCalculator] runs
     * against to work out what's on right now.
     */
    suspend fun getEpisodesForChannel(channelId: Long): List<EpisodeEntity> {
        val seriesList: List<SeriesEntity> = db.seriesDao().getForChannel(channelId).first()
        return seriesList.flatMap { series ->
            db.episodeDao().getForSeries(series.id).first()
        }
    }

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
                                watched = watched,
                                durationMs = episode.durationMs
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

    /**
     * Deletes a single channel (and, via FK cascade, its series/episodes).
     */
    suspend fun deleteChannel(channel: ChannelEntity) {
        db.channelDao().delete(channel)
    }

    /**
     * Moves [channel] by [delta] positions within [currentOrder] (the currently
     * displayed, sortOrder-sorted list) by swapping sortOrder with the neighbor
     * at the target index. delta = -1 moves up, +1 moves down. No-op if the
     * channel is already at that edge of the list.
     */
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

    /**
     * Persists playback progress for a single episode — called from
     * PlayerScreen both periodically (checkpointing) and when an episode
     * finishes or the player is closed.
     */
    suspend fun updateEpisodeProgress(episodeId: Long, positionMs: Long, watched: Boolean) {
        db.episodeDao().updateProgress(episodeId, positionMs, watched)
    }
}
