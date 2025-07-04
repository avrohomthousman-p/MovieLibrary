package com.greatmachine.movielibrary.db
import androidx.room.*


/**
 * Class defining some simple database queries for the CachedMovie table.
 */
@Dao
interface CachedMovieDAO {
    @Query("SELECT * FROM CachedMovies ORDER BY position ASC")
    suspend fun getAllCached(): List<CachedMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheMovies(movies: List<CachedMovie>)

    @Query("DELETE FROM CachedMovies")
    suspend fun clearCache()
}
