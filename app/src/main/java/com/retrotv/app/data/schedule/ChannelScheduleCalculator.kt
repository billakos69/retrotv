package com.retrotv.app.data.schedule

/**
 * Given an ordered, flat list of ScheduleItems (episodes + ads + jingles, as
 * built by ChannelPlaylistBuilder), works out which item should be playing
 * *right now* and at what offset — the way a real linear TV channel behaves.
 * The schedule is a continuous loop anchored to the Unix epoch, so "what's on
 * now" is a pure function of wall-clock time.
 *
 * Items with an unknown/zero duration are treated as [FALLBACK_DURATION_MS]
 * long, so they don't collapse the schedule to a single instant.
 */
object ChannelScheduleCalculator {

    const val FALLBACK_DURATION_MS = 3 * 60 * 1000L // 3 minutes

    data class CurrentProgram(
        val itemIndex: Int,
        val item: ScheduleItem,
        val offsetMs: Long
    )

    fun effectiveDuration(item: ScheduleItem): Long =
        if (item.durationMs > 0) item.durationMs else FALLBACK_DURATION_MS

    /**
     * Returns null if [items] is empty — nothing to schedule.
     */
    fun currentProgram(
        items: List<ScheduleItem>,
        nowMs: Long = System.currentTimeMillis()
    ): CurrentProgram? {
        if (items.isEmpty()) return null

        val totalDurationMs = items.sumOf { effectiveDuration(it) }
        if (totalDurationMs <= 0) return null

        var elapsed = nowMs % totalDurationMs
        if (elapsed < 0) elapsed += totalDurationMs

        for ((index, item) in items.withIndex()) {
            val duration = effectiveDuration(item)
            if (elapsed < duration) {
                return CurrentProgram(itemIndex = index, item = item, offsetMs = elapsed)
            }
            elapsed -= duration
        }

        return CurrentProgram(itemIndex = 0, item = items.first(), offsetMs = 0L)
    }
}
