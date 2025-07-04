package com.greatmachine.movielibrary.utils

import com.greatmachine.movielibrary.db.FavoritedMovie


/**
 * Class that stores a movie along with any other data that is needed but
 * isn't in the database.
 */
data class MovieData(
    val movie: FavoritedMovie,
    var isFavorited: Boolean
)
