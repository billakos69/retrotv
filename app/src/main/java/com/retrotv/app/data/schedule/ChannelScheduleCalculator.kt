package com.retrotv.app.data.schedule

import com.retrotv.app.data.db.EpisodeEntity

/**
 * Given an ordered, flat list of episodes for a channel, works out which
 * episode should be playing *right now* and at what offset — the way a real
 * linear TV channel behaves. The schedule is a continuous loop of the
 * channel's episodes anchored to the Unix epoch, so "what's on now" is a
 * pure function of wall-clock time: the same input always gives the same
 * answer, and it doesn't matter when the app was last opened (Section 14 of
 * the spec — join mid-episode, don't restart from the beginning).
 *
 * Episodes with an unknown/zero duration (e.g. a corrupt file that
 * MediaMetadataRetriever couldn't read) are treated as
 * [FALLBACK_DURATION_MS] long, so they don't collapse the schedule to a
 * single instant.
 *
 * Pure Kotlin, no Android framework dependency beyond the EpisodeEntity data
 * class itself — easy to reason about and safe to call from anywhere.
 */
object ChannelScheduleCalculator {

    const val FALLBACK_DURATION_MS = 3 * 60 * 1000L // 3 minutes

    data class CurrentProgram(
        val episodeIndex: Int,
        val episode: EpisodeEntity,
        val offsetMs: Long,
        val nextEpisode: EpisodeEntity?
    )

    fun effectiveDuration(episode: EpisodeEntity): Long =
        if (episode.durationMs > 0) episode.durationMs else FALLBACK_DURATION_MS

    /**
     * Returns null if [episodes] is empty — nothing to schedule.
     */
    fun currentProgram(
        episodes: List<EpisodeEntity>,
        nowMs: Long = System.currentTimeMillis()
    ): CurrentProgram? {
        if (episodes.isEmpty()) return null

        val totalDurationMs = episodes.sumOf { effectiveDuration(it) }
        if (totalDurationMs <= 0) return null

        var elapsed = nowMs % totalDurationMs
        if (elapsed < 0) elapsed += totalDurationMs

        for ((index, episode) in episodes.withIndex()) {
            val duration = effectiveDuration(episode)
            if (elapsed < duration) {
                val next = episodes.getOrNull((index + 1) % episodes.size)
                return CurrentProgram(
                    episodeIndex = index,
                    episode = episode,
                    offsetMs = elapsed,
                    nextEpisode = next
                )
            }
            elapsed -= duration
        }

        return CurrentProgram(
            episodeIndex = 0,
            episode = episodes.first(),
            offsetMs = 0L,
            nextEpisode = episodes.getOrNull(1)
        )
    }
}
