package com.greatmachine.movielibrary.db

import androidx.room.*

@Dao
interface MovieDao {
    @Query("SELECT * FROM Movies")
    suspend fun getAllFavorites(): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(movie: Movie)

    @Delete
    suspend fun removeFavorite(movie: Movie)
}