package com.retrotv.app.data.model

import java.io.File

data class Episode(
    val title: String,
    val file: File
)

data class Series(
    val name: String,
    val folder: File,
    val episodes: List<Episode>
)

data class Channel(
    val name: String,
    val folder: File,
    val logoFile: File?,
    val series: List<Series>
)

data class MediaClip(
    val title: String,
    val file: File
)

data class ScannedLibrary(
    val channels: List<Channel>,
    val ads: List<MediaClip>,
    val jingles: List<MediaClip>
)
