package com.greatmachine.movielibrary.utils

import com.greatmachine.movielibrary.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale


/**
 * Runs the API query to get detailed information about a specific movie.
 */
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


/**
 * Changes some of the data in the API response to a form that makes it
 * easier to display on the front end.
 */
fun cleanJsonData(data: JSONObject){
    replaceUrl(data)

    convertGenresToTextDisplay(data)

    replaceAdultWithTextDisplay(data)

    convertBudgetToTextDisplay(data)
}


/**
 * Replaces the poster_path partial image url in the API response with
 * a proper full url that the front end can use.
 */
private fun replaceUrl(data: JSONObject) {
    val imgPath = data.getString("poster_path")
    data.put("image_url", BASE_URL + imgPath)
}


/**
 * Converts the genre data provided by the API into a much more
 * usable format that can be easily displayed on the screen.
 */
private fun convertGenresToTextDisplay(data: JSONObject) {
    val genreData = data.getJSONArray("genres")
    val genreNames: StringBuilder = StringBuilder()

    for (i in 0 until genreData.length()) {
        val genre = genreData.getJSONObject(i)
        val name: String = genre.getString("name")
        genreNames.append(name)
        genreNames.append(", ")
    }

    //remove trailing comma
    genreNames.deleteCharAt(genreNames.length - 1)
    genreNames.deleteCharAt(genreNames.length - 1)

    data.put("genres", genreNames.toString())
}


/**
 * Replaces the boolean value (for adult content) provided by the API
 * with text to display on the front end.
 */
private fun replaceAdultWithTextDisplay(data: JSONObject) {
    val isAdult = data.getBoolean("adult")
    if (isAdult) {
        data.put("adult", "✅ Yes")
    } else {
        data.put("adult", "❌ No")
    }
}


/**
 * Formats the movie budget (given as an Int by the APT) into nicer output
 * text with a dollar sign and commas.
 */
private fun convertBudgetToTextDisplay(data: JSONObject){
    val budget = data.getInt("budget")

    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val budgetDisplay = formatter.format(budget)


    data.put("budget", budgetDisplay)
}
