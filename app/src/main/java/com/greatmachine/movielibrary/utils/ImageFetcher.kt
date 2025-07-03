package com.greatmachine.movielibrary.utils

import com.greatmachine.movielibrary.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


const val BASE_URL = "https://image.tmdb.org/t/p/w500/"


suspend fun discoverMovies(): List<Movie>? = withContext(Dispatchers.IO)  {
    val response = StringBuilder()

    val url = URL("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US")
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
    } catch (e: Exception) {
        e.printStackTrace()
        response.clear()
        response.append("ERROR: ")
        response.append(e.message)
    } finally {
        connection.disconnect()
    }

    return@withContext compileResponseToListOfMovies(response.toString())
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
        val movie = Movie(data.getString("title"), url, false)

        movies.add(movie)
    }

    return movies
}
