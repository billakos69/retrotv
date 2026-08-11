package com.retrotv.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY sortOrder, name")
    fun getAll(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getById(id: Long): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: ChannelEntity): Long

    @Update
    suspend fun update(channel: ChannelEntity)

    @Delete
    suspend fun delete(channel: ChannelEntity)
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series WHERE channelId = :channelId ORDER BY name")
    fun getForChannel(channelId: Long): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(series: SeriesEntity): Long

    @Delete
    suspend fun delete(series: SeriesEntity)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY sortOrder")
    fun getForSeries(seriesId: Long): Flow<List<EpisodeEntity>>

    @Query("UPDATE episodes SET lastPositionMs = :positionMs, watched = :watched WHERE id = :id")
    suspend fun updateProgress(id: Long, positionMs: Long, watched: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: EpisodeEntity): Long

    @Delete
    suspend fun delete(episode: EpisodeEntity)
}

@Dao
interface AdDao {
    @Query("SELECT * FROM ads")
    fun getAll(): Flow<List<AdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ad: AdEntity): Long

    @Delete
    suspend fun delete(ad: AdEntity)
}

@Dao
interface JingleDao {
    @Query("SELECT * FROM jingles")
    fun getAll(): Flow<List<JingleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jingle: JingleEntity): Long

    @Delete
    suspend fun delete(jingle: JingleEntity)
}
