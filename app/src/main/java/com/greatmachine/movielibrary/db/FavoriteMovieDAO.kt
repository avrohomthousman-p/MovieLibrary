package com.greatmachine.movielibrary.db

import androidx.room.*

@Dao
interface FavoriteMovieDAO {
    @Query("SELECT * FROM FavoriteMovies")
    suspend fun getAllFavorites(): List<Movie>

    @Query("SELECT * FROM FavoriteMovies WHERE id in (:movieIds) ORDER BY id")
    suspend fun getFavoritesFromMovieList(movieIds: List<Int>): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovie(movie: Movie)

    @Delete
    suspend fun removeFavorite(movie: Movie)
}