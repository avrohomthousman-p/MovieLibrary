package com.greatmachine.movielibrary

import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.greatmachine.movielibrary.utils.getMovieData
import kotlinx.coroutines.launch
import org.json.JSONObject


const val MOVIE_ID_KEY = "movie_id"

class MovieDetailsActivity : ComponentActivity() {
    private var uiState: DetailsQueryState by mutableStateOf(DetailsQueryState.Loading)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val movieId = intent.getIntExtra(MOVIE_ID_KEY, -1)
        if (movieId == -1){
            throw IllegalArgumentException("no movie ID provided")
        }

        lifecycleScope.launch {
            val results: JSONObject = getMovieData(movieId)
            if (results.has("error")){
                uiState = DetailsQueryState.Error
            }
            else{
                uiState = DetailsQueryState.Success(results)
            }
        }


        setContent{
            MainContent(uiState)
        }
    }


    @Composable
    fun MainContent(state: DetailsQueryState){
        when (state) {
            is DetailsQueryState.Error -> DisplayError()
            is DetailsQueryState.Success -> DisplayContent(state.movieData)
            DetailsQueryState.Loading -> Text("Loading...")
        }

    }


    @Composable
    fun DisplayError(){
        Text("Error. Unable to load movie details")
    }


    @Composable
    fun DisplayContent(movieData: JSONObject){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = movieData.getString("title"),
                    fontSize = 24.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                AsyncImage(
                    model = movieData.getString("image_url"),
                    contentDescription = movieData.getString("title"),
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = movieData.getString("tagline"),
                    fontSize = 20.sp,
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Overview",
                    fontSize = 22.sp,
                )

                Text(
                    text = movieData.getString("overview"),
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Genres: " + movieData.getString("genres"),
                    fontSize = 14.sp,
                    color = Color.Green
                )

                Spacer(modifier = Modifier.height(18.dp))

                InfoRow("Adult Content: ", movieData.getString("adult"))
                InfoRow("Movie Length: ", movieData.getString("runtime") + " minutes")
                InfoRow("Movie Budget: ", movieData.getString("budget"))
                InfoRow("Release Date", movieData.getString("release_date"))
            }
        }
    }


    @Composable
    fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontWeight = FontWeight.Bold)
            Text(text = value)
        }
    }

}


sealed interface DetailsQueryState {
    object Loading : DetailsQueryState
    data class Success(val movieData: JSONObject) : DetailsQueryState
    object Error : DetailsQueryState
}
