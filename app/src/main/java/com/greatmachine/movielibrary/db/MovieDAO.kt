package com.greatmachine.movielibrary.db

import androidx.room.*

@Dao
interface MovieDao {
    @Query("SELECT * FROM Movies")
    suspend fun getAllFavorites(): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovie(movie: Movie)

    @Update
    suspend fun updateMovie(movie: Movie)

    @Delete
    suspend fun removeFavorite(movie: Movie)
}