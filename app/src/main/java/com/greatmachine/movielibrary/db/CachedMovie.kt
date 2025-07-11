package com.greatmachine.movielibrary.db
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.greatmachine.movielibrary.utils.MovieData


/**
 * Movie object that is saved only because it is being cached as part of a API query.
 */
@Entity(tableName = "CachedMovies")
data class CachedMovie(
    @PrimaryKey override val id: Int,
    override val title: String,
    override val imgURL: String,
    val position: Int
) : BasicMovie {


    fun convertToFavoritedMovie() :FavoritedMovie {
        return FavoritedMovie(id, title, imgURL)
    }


    fun convertToMovieData() : MovieData {
        return MovieData(convertToFavoritedMovie(), false)
    }
}

