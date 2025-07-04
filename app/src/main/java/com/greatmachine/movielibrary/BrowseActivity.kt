package com.greatmachine.movielibrary

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.greatmachine.movielibrary.utils.discoverMovies
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.greatmachine.movielibrary.db.MovieDatabaseInstance
import com.greatmachine.movielibrary.utils.MovieData

const val BROWSE_FAVS_KEY = "browseFavs"

class BrowseActivity : ComponentActivity() {
    private var uiState: MoviesQueryState by mutableStateOf(MoviesQueryState.Loading)
    private var browsingFavrourites: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        browsingFavrourites = intent.getBooleanExtra(BROWSE_FAVS_KEY, false)

        if (browsingFavrourites){
            getMoviesFromDatabase()
        }
        else {
            getMoviesFromAPI()
        }


        setContent{
            MainDisplay(uiState)
        }
    }


    fun getMoviesFromAPI() {
        lifecycleScope.launch {
            val result: List<MovieData>? = discoverMovies(applicationContext)

            if (result.isNullOrEmpty()){
                uiState = MoviesQueryState.Error
            }
            else {
                uiState = MoviesQueryState.Success(result)
            }
        }
    }


    fun getMoviesFromDatabase() {
        lifecycleScope.launch{
            try{
                val movies = MovieDatabaseInstance
                                .getInstance(applicationContext)
                                .favoriteMovieDao()
                                .getAllFavorites()
                                .map {
                                    movie -> MovieData(movie = movie, isFavorited = true)
                                }

                uiState = MoviesQueryState.Success(movies)
            }
            catch (e: Exception){
                uiState = MoviesQueryState.Error
                Log.e("failure", "Failed to fetch favorite movies", e)
            }
        }

    }


    @Composable
    fun MainDisplay(state: MoviesQueryState){
        when (state) {
            MoviesQueryState.Loading -> LoadingScreen()
            MoviesQueryState.Error   -> ErrorScreen()
            is MoviesQueryState.Success -> LoadedContent(state.movies)
        }
    }


    @Composable
    fun LoadingScreen(){
        Text("Loading. Please wait...",
            fontSize = 24.sp,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            textAlign = TextAlign.Center
        )
    }


    @Composable
    fun ErrorScreen(){
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
    fun LoadedContent(movies: List<MovieData>) {
        val bannerText = if (browsingFavrourites) "My Favorurites" else "Discover New Movies"

        Column (
            modifier = Modifier.fillMaxSize()
        ){
            Text(
                bannerText,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center
            )
            MovieList(movies)
        }
    }


    @Composable
    fun MovieList(movies: List<MovieData>) {
        LazyColumn (
            modifier = Modifier.fillMaxSize().background(Color.LightGray),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            items(movies) { movie ->
                MovieItem(movie)
            }
        }
    }


    @Composable
    fun MovieItem(movieWrapper: MovieData) {
        var isFavorited by remember { mutableStateOf(movieWrapper.isFavorited) }


        val contentDescription = if (isFavorited) "Un-Favorite" else "Favorite"
        val backgroundColor = if (isFavorited) Color.Red else Color.White

        if (!isFavorited and browsingFavrourites){
            return
        }


        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = movieWrapper.movie.imgURL,
                        contentDescription = "${movieWrapper.movie.title} cover",
                        modifier = Modifier.fillMaxSize()
                    )

                    //Favorites Button
                    IconButton(
                        onClick = {
                            isFavorited = !isFavorited
                            toggleFavorite(movieWrapper)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(backgroundColor, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = contentDescription
                        )
                    }

                    //Info button
                    IconButton(
                        onClick = {
                            val intent = Intent(applicationContext, MovieDetailsActivity::class.java)
                            intent.putExtra(MOVIE_ID_KEY, movieWrapper.movie.id)
                            startActivity(intent)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = contentDescription
                        )
                    }
                }

                Text(
                    text = movieWrapper.movie.title,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }


    fun toggleFavorite(movieWrapper: MovieData) : Unit {
        lifecycleScope.launch {
            val dbTable = MovieDatabaseInstance.getInstance(applicationContext).favoriteMovieDao()

            movieWrapper.isFavorited = !movieWrapper.isFavorited
            if(movieWrapper.isFavorited){
                dbTable.saveMovie(movieWrapper.movie)
            }
            else {
                dbTable.removeFavorite(movieWrapper.movie)
            }
        }
    }
}


sealed interface MoviesQueryState {
    object Loading : MoviesQueryState
    data class Success(val movies: List<MovieData>) : MoviesQueryState
    object Error : MoviesQueryState
}

