package com.greatmachine.movielibrary.db
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Movies")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val imgURL: String,
    val favorited: Boolean
)