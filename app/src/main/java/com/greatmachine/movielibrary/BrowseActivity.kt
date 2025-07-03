package com.greatmachine.movielibrary

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.greatmachine.movielibrary.utils.Movie
import com.greatmachine.movielibrary.utils.discoverMovies
import kotlinx.coroutines.launch

class BrowseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val result: List<Movie>? = discoverMovies()

            if (result.isNullOrEmpty()){
                runOnUiThread { displayLoadingError() }
            }
            else {
                runOnUiThread {
                    setContent{
                        LoadedContent(result)
                    }
                }
            }
        }


        setContent {
            Text("Loading. Please wait...",
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }


    fun displayLoadingError(){
        setContent {
            Text(
                "Unable to connect",
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }


    @Composable
    fun LoadedContent(movies: List<Movie>) {
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
                    AsyncImage(
                        model = movie.imgURL,
                        contentDescription = "${movie.title} cover picture",
                        modifier = Modifier.padding(top = 8.dp).height(300.dp)
                    )
                    //TODO: add a favorite button
                }
            }
        }
    }
}