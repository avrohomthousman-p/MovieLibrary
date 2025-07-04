package com.greatmachine.movielibrary.db
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "FavoriteMovies")
data class FavoritedMovie(
    @PrimaryKey val id: Int,
    val title: String,
    val imgURL: String,
)