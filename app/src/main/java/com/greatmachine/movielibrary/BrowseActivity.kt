package com.greatmachine.movielibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BrowseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Temporary data until I set up the API
        val movies = listOf(
            Movie("Inception", "", false),
            Movie("The Matrix", "", false),
            Movie("Interstellar", "", false)
        )

        setContent {
            Column (
                modifier = Modifier.fillMaxSize()
            ){
                Text(
                    "Discover New Movies",
                    fontSize = 24.sp,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
                MovieList(movies)
            }
        }
    }


    @Composable
    fun MovieList(movies: List<Movie>) {
        LazyColumn (
            modifier = Modifier.fillMaxSize().background(Color.LightGray),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            items(movies) { movie ->
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = movie.title,
                        fontSize = 20.sp,
                    )
                    Image(
                        painter = painterResource(id = R.drawable.movie_pic), //temp pic
                        contentDescription = "Movie poster",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}