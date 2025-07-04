package com.greatmachine.movielibrary.db
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CachedMovies")
data class CachedMovie(
    @PrimaryKey override val id: Int,
    override val title: String,
    override val imgURL: String
) : BasicMovie

