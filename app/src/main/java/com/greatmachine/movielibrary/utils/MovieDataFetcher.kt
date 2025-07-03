package com.greatmachine.movielibrary.utils

import com.greatmachine.movielibrary.BuildConfig
import com.greatmachine.movielibrary.db.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


suspend fun getMovieData(movieId: Int): JSONObject = withContext(Dispatchers.IO) {
    val response = StringBuilder()

    val url = URL("https://api.themoviedb.org/3/movie/${movieId}")
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

    } catch (e: Exception) {
        e.printStackTrace()
        connection.disconnect()
        return@withContext JSONObject("{\"error\": \"failed to connect to API\"}")
    }


    val jsonData = JSONObject(response.toString())
    cleanJsonData(jsonData)
    return@withContext jsonData
}


fun cleanJsonData(data: JSONObject){
    val imgPath = data.getString("poster_path")
    data.put("image_url1", BASE_URL + imgPath)
}
