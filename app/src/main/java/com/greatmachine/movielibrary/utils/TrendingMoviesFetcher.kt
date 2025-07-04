package com.greatmachine.movielibrary.utils

import android.content.Context
import com.greatmachine.movielibrary.BuildConfig
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


suspend fun discoverMovies(applicationContext: Context): List<MovieData>? = withContext(Dispatchers.IO){
    val db = MovieDatabaseInstance.getInstance(applicationContext)

    if (isCacheStale(applicationContext)){
        //Load from the API and replace the cache
        val results: List<MovieData>? = queryAPIForMovies(applicationContext)
        if (results != null){
            val asCachedMovie = results.map { it.convertToCachedMovie() }
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


private fun updateTimeStamp(applicationContext: Context){
    val prefs = applicationContext.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    prefs.edit().putLong(TIMESTAMP_KEY, System.currentTimeMillis()).apply()
}


private fun isCacheStale(applicationContext: Context): Boolean {
    val prefs = applicationContext.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    val lastCacheTime = prefs.getLong(TIMESTAMP_KEY, 0L)
    val cacheValidityDuration = 24 * 60 * 60 * 1000L // 24 hours in ms
    return System.currentTimeMillis() - lastCacheTime > cacheValidityDuration
}


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
