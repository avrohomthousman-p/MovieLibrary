package com.greatmachine.movielibrary.db

import androidx.room.*

@Dao
interface FavoriteMovieDAO {
    @Query("SELECT * FROM FavoriteMovies")
    suspend fun getAllFavorites(): List<FavoritedMovie>

    @Query("SELECT * FROM FavoriteMovies WHERE id in (:movieIds) ORDER BY id")
    suspend fun getFavoritesFromMovieList(movieIds: List<Int>): List<FavoritedMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovie(movie: FavoritedMovie)

    @Delete
    suspend fun removeFavorite(movie: FavoritedMovie)
}