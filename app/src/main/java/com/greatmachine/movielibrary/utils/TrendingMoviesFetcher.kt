package com.greatmachine.movielibrary.utils

import android.content.Context
import com.greatmachine.movielibrary.BuildConfig
import com.greatmachine.movielibrary.db.CachedMovie
import com.greatmachine.movielibrary.db.FavoritedMovie
import com.greatmachine.movielibrary.db.MovieDatabaseInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


const val BASE_URL = "https://image.tmdb.org/t/p/w500/"
const val TIMESTAMP_KEY = "cache_updated_at"
const val PREFS_KEY = "cache_data"


/**
 * Loads trending movies - either from the cache or the API whichever is appropriate.
 */
suspend fun discoverMovies(applicationContext: Context): List<MovieData>? = withContext(Dispatchers.IO){
    val db = MovieDatabaseInstance.getInstance(applicationContext)

    if (isCacheStale(applicationContext)){
        //Load from the API and replace the cache
        val results: List<MovieData>? = queryAPIForMovies(applicationContext)

        if (results != null){
            val asCachedMovie = results.mapIndexed { index, item ->
                CachedMovie(item.movie.id, item.movie.title, item.movie.imgURL, index)
            }

            db.cachedMovieDao().clearCache()
            db.cachedMovieDao().cacheMovies(asCachedMovie)
            updateTimeStamp(applicationContext)
        }

        return@withContext results
    }
    else {
        //Load from the cache
        val cachedMovies = db.cachedMovieDao().getAllCached()
        val asMovieData = cachedMovies.map { it.convertToMovieData() }
        updateFavoritedValueFromDB(applicationContext, asMovieData)
        return@withContext asMovieData
    }
}


/**
 * Sets the timestamp (for tracking when the cache was last set) to now.
 */
private fun updateTimeStamp(applicationContext: Context){
    val prefs = applicationContext.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    prefs.edit().putLong(TIMESTAMP_KEY, System.currentTimeMillis()).apply()
}


/**
 * Checks if the cache is outdated and needs to be replaced with newer data.
 */
private fun isCacheStale(applicationContext: Context): Boolean {
    val prefs = applicationContext.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    val lastCacheTime = prefs.getLong(TIMESTAMP_KEY, 0L)
    val cacheValidityDuration = 24 * 60 * 60 * 1000L // 24 hours in ms
    return System.currentTimeMillis() - lastCacheTime > cacheValidityDuration
}


/**
 * Runs the API query to get trending movies.
 */
private suspend fun queryAPIForMovies(applicationContext: Context): List<MovieData>? = withContext(Dispatchers.IO)  {
    val movieList: List<MovieData>?
    val response = StringBuilder()

    val url = URL("https://api.themoviedb.org/3/trending/movie/day?language=en-US")
    val connection = url.openConnection() as HttpURLConnection
    connection.setRequestProperty("accept", "application/json")
    connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.ACCESS_TOKEN}")
    connection.requestMethod = "GET"

    try {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }

        reader.close()
        connection.disconnect()

        movieList = compileJSONToListOfMovies(response.toString())
        updateFavoritedValueFromDB(applicationContext, movieList)


    } catch (e: Exception) {
        e.printStackTrace()
        response.clear()
        response.append("ERROR: ")
        response.append(e.message)
        connection.disconnect()
        return@withContext null
    }

    return@withContext movieList
}


/**
 * Compiles the JSON results of the API query into actual MovieData instances
 * with a default value of false for isFavorited.
 */
fun compileJSONToListOfMovies(response: String): List<MovieData>? {
    if (response.startsWith("ERROR")){
        return null
    }

    val movies = ArrayList<MovieData>()


    val jsonData = JSONObject(response).getJSONArray("results")
    for (i in 0 until jsonData.length()){
        val data = jsonData.getJSONObject(i)

        val url = BASE_URL + data.getString("poster_path")
        val movie = FavoritedMovie(data.getInt("id"), data.getString("title"), url)

        movies.add(MovieData(movie, false))
    }

    return movies
}


/**
 * Takes a list of MovieData objects and ensures that they all have correct values for isFavorited.
 * When movies are retrieved from the cache or API, we cant know if they are favorited or not without
 * checking to see if they are in the favorites table.
 */
suspend fun updateFavoritedValueFromDB(applicationContext: Context, movies: List<MovieData>?){
    if (movies.isNullOrEmpty())
        return


    //Load correct favorited values from the DB
    val movieIds = movies.map { it.movie.id }
    val allFavorites = MovieDatabaseInstance
                .getInstance(applicationContext)
                .favoriteMovieDao()
                .getFavoritesFromMovieList(movieIds)
                .map { it.id }
                .toSet()



    //Copy them over to the existing movies
    for (movieWrapper in movies){
        movieWrapper.isFavorited = allFavorites.contains(movieWrapper.movie.id)
    }
}
