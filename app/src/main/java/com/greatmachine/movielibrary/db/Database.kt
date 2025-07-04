package com.greatmachine.movielibrary.db
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import android.content.Context


/**
 * Singleton database reference object.
 */
object MovieDatabaseInstance {
    @Volatile
    private var INSTANCE: MovieDatabase? = null

    fun getInstance(context: Context): MovieDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                MovieDatabase::class.java,
                "movie-db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}


@Database(entities = [FavoritedMovie::class, CachedMovie::class], version = 1, exportSchema = true)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoritedMovieDAO
    abstract fun cachedMovieDao(): CachedMovieDAO
}
