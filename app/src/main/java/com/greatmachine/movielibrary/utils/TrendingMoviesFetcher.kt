package com.greatmachine.movielibrary.utils

import android.content.Context
import com.greatmachine.movielibrary.BuildConfig
import com.greatmachine.movielibrary.db.Movie
import com.greatmachine.movielibrary.db.MovieDatabaseInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


const val BASE_URL = "https://image.tmdb.org/t/p/w500/"


suspend fun discoverMovies(applicationContext: Context): List<Movie>? = withContext(Dispatchers.IO)  {
    val movieList: List<Movie>?
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

        movieList = compileResponseToListOfMovies(response.toString())
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


fun compileResponseToListOfMovies(response: String): List<Movie>? {
    if (response.startsWith("ERROR")){
        return null
    }

    val movies = ArrayList<Movie>()


    val jsonData = JSONObject(response).getJSONArray("results")
    for (i in 0 until jsonData.length()){
        val data = jsonData.getJSONObject(i)

        val url = BASE_URL + data.getString("poster_path")
        val movie = Movie(data.getInt("id"), data.getString("title"), url,false)

        movies.add(movie)
    }

    return movies
}


suspend fun updateFavoritedValueFromDB(applicationContext: Context, movies: List<Movie>?){
    if (movies.isNullOrEmpty())
        return


    //Load correct favorited values from the DB
    val movieIds = movies.map { it.id }
    val correctFavValues = MovieDatabaseInstance
                .getInstance(applicationContext)
                .movieDao()
                .getFavoritesFromMovieList(movieIds)
                .associate { it.id to it.favorited }


    //Copy them over to the existing movies
    for (movie in movies){
        if (correctFavValues.containsKey(movie.id)){
            movie.favorited = correctFavValues[movie.id]!!
        }
    }
}
