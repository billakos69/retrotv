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
    version = 1,
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
                ).build().also { INSTANCE = it }
            }
        }
    }
}
