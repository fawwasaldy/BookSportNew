package com.example.booksportnew

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class SportViewModel : ViewModel() {
    private val _venues = MutableStateFlow(sampleVenues)
    val venues: StateFlow<List<SportVenue>> = _venues

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    private var nextBookingId = 0L

    fun addBooking(venue: SportVenue, dateTime: LocalDateTime, sportType: String) {
        try {
            val booking = Booking(
                id = nextBookingId++,
                venue = venue,
                dateTime = dateTime,
                sportType = sportType
            )
            _bookings.update { it + booking }
        } catch (e: Exception) {
            Log.e("SportViewModel", "Error creating booking: ${e.message}", e)
            throw e
        }
    }

    companion object {
        private val sampleVenues = listOf(
            SportVenue(1, "Futsal Arena", R.drawable.futsal_court, "Jakarta Pusat"),
            SportVenue(2, "Badminton Court", R.drawable.badminton_court, "Jakarta Selatan"),
            SportVenue(3, "Basketball Hall", R.drawable.basketball_hall, "Jakarta Barat")
        )
    }
}