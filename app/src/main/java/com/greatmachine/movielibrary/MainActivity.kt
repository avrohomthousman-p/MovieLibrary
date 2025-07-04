package com.greatmachine.movielibrary
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text("What would you like to do today?", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(24.dp))
                BrowseButton()
                Spacer(modifier = Modifier.height(16.dp))
                FavoritesButton()
            }
        }
    }


    @Composable
    fun FavoritesButton() {
        Button(onClick = {
            val intent = Intent(this, BrowseActivity::class.java)
            intent.putExtra(BROWSE_FAVS_KEY, true)
            startActivity(intent)
        }) {
            Text("Browse Favourites")
        }
    }


    @Composable
    fun BrowseButton() {
        Button(onClick = {
            val intent = Intent(this, BrowseActivity::class.java)
            intent.putExtra(BROWSE_FAVS_KEY, false)
            startActivity(intent)
        }) {
            Text("Browse All")
        }
    }
}