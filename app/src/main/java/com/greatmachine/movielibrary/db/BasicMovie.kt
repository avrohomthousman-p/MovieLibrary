package com.greatmachine.movielibrary.db

/**
 * Parent class for both movie objects.
 */
interface BasicMovie {
    val id: Int
    val title: String
    val imgURL: String
}
