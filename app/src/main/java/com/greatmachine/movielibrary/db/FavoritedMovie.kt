package com.greatmachine.movielibrary.db
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Represents a movie that has been favorited by the user. Any movie in this table
 * is favorited by the user, and should be removed if the user un-favorites it.
 */
@Entity(tableName = "FavoriteMovies")
data class FavoritedMovie(
    @PrimaryKey override val id: Int,
    override val title: String,
    override val imgURL: String,
) : BasicMovie