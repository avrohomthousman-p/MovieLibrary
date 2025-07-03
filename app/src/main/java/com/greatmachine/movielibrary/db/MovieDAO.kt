package com.greatmachine.movielibrary.db

import androidx.room.*

@Dao
interface MovieDao {
    @Query("SELECT * FROM Movies")
    suspend fun getAllMovies(): List<Movie>

    @Query("SELECT * FROM Movies WHERE favorited = 1")
    suspend fun getAllFavorites(): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovie(movie: Movie)

    @Update
    suspend fun updateMovie(movie: Movie)

    @Delete
    suspend fun removeFavorite(movie: Movie)
}