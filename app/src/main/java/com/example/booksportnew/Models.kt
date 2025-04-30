package com.example.booksportnew

import androidx.annotation.DrawableRes
import java.time.LocalDateTime

data class SportVenue(
    val id: Long,
    val name: String,
    @DrawableRes val imageRes: Int,
    val location: String,
    val address: String,
    val pricePerHour: Int
)

data class Booking(
    val id: Long,
    val venue: SportVenue,
    val dateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val sportType: String,
    val fullName: String,
    val email: String,
    val phone: String
)