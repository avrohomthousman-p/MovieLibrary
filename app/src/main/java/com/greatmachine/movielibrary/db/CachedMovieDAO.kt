package com.greatmachine.movielibrary.db
import androidx.room.*

@Dao
interface CachedMovieDAO {
    @Query("SELECT * FROM CachedMovies")
    suspend fun getAllCached(): List<CachedMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheMovies(movies: List<CachedMovie>)

    @Query("DELETE FROM CachedMovies")
    suspend fun clearCache()
}
