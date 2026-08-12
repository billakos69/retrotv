package com.retrotv.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChannelEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        AdEntity::class,
        JingleEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RetroTvDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun adDao(): AdDao
    abstract fun jingleDao(): JingleDao

    companion object {
        @Volatile private var INSTANCE: RetroTvDatabase? = null

        fun getInstance(context: Context): RetroTvDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RetroTvDatabase::class.java,
                    "retrotv.db"
                )
                    // Schema is still evolving during development, and all of this
                    // data is just a cache of what's on disk (RESCAN LIBRARY
                    // rebuilds it) apart from watch progress, so destructively
                    // wiping on a version bump is an acceptable tradeoff for now.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
