package com.retrotv.app.data.schedule

/**
 * One playable slot in a channel's on-air schedule — an episode, an ad, or a
 * jingle (station ID bumper). ChannelPlaylistBuilder produces a flat list of
 * these; ChannelScheduleCalculator works out which one is "on now" purely
 * from their durations; PlayerScreen just plays the list in order.
 */
sealed class ScheduleItem {
    abstract val title: String
    abstract val filePath: String
    abstract val durationMs: Long

    data class EpisodeItem(
        val episodeId: Long,
        override val title: String,
        override val filePath: String,
        override val durationMs: Long
    ) : ScheduleItem()

    data class AdItem(
        override val title: String,
        override val filePath: String,
        override val durationMs: Long
    ) : ScheduleItem()

    data class JingleItem(
        override val title: String,
        override val filePath: String,
        override val durationMs: Long
    ) : ScheduleItem()
}
