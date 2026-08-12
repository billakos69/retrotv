package com.retrotv.app.data.schedule

import com.retrotv.app.data.db.AdEntity
import com.retrotv.app.data.db.EpisodeEntity
import com.retrotv.app.data.db.JingleEntity

/**
 * Builds the flat, repeating "block schedule" for a channel: each episode is
 * followed by a jingle (station ID) and then an ad (commercial), the way a
 * real local TV channel breaks up its programming. Ads and jingles are cycled
 * round-robin across the channel's shared Ads/ and Jingles/ pools, so the
 * same clip doesn't play after every single episode.
 *
 * If a channel has no ads and/or no jingles available, those slots are simply
 * skipped — the channel still plays fine, just without breaks.
 */
object ChannelPlaylistBuilder {

    fun build(
        episodes: List<EpisodeEntity>,
        ads: List<AdEntity>,
        jingles: List<JingleEntity>
    ): List<ScheduleItem> {
        if (episodes.isEmpty()) return emptyList()

        val items = mutableListOf<ScheduleItem>()
        episodes.forEachIndexed { index, episode ->
            items += ScheduleItem.EpisodeItem(
                episodeId = episode.id,
                title = episode.title,
                filePath = episode.filePath,
                durationMs = episode.durationMs
            )

            if (jingles.isNotEmpty()) {
                val jingle = jingles[index % jingles.size]
                items += ScheduleItem.JingleItem(
                    title = jingle.title,
                    filePath = jingle.filePath,
                    durationMs = jingle.durationMs
                )
            }

            if (ads.isNotEmpty()) {
                val ad = ads[index % ads.size]
                items += ScheduleItem.AdItem(
                    title = ad.title,
                    filePath = ad.filePath,
                    durationMs = ad.durationMs
                )
            }
        }
        return items
    }
}
