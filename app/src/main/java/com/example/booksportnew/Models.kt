package com.example.booksportnew

import androidx.annotation.DrawableRes
import java.time.LocalDateTime

data class SportVenue(
    val id: Long,
    val name: String,
    @DrawableRes val imageRes: Int,
    val location: String
)

data class Booking(
    val id: Long,
    val venue: SportVenue,
    val dateTime: LocalDateTime,
    val sportType: String
)