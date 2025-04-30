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

    fun addBooking(
        venue: SportVenue,
        dateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        sportType: String,
        fullName: String,
        email: String,
        phone: String
    ): Long {
        return try {
            val booking = Booking(
                id = nextBookingId++,
                venue = venue,
                dateTime = dateTime,
                endDateTime = endDateTime,
                sportType = sportType,
                fullName = fullName,
                email = email,
                phone = phone
            )
            _bookings.update { it + booking }
            booking.id
        } catch (e: Exception) {
            Log.e("SportViewModel", "Error creating booking: ${e.message}", e)
            throw e
        }
    }

    companion object {
        private val sampleVenues = listOf(
            SportVenue(
                id = 1,
                name = "Futsal Arena",
                imageRes = R.drawable.futsal_court,
                location = "Jakarta Pusat",
                address = "Jl. Kebon Sirih No. 23, Jakarta Pusat",
                pricePerHour = 150
            ),
            SportVenue(
                id = 2,
                name = "Badminton Court",
                imageRes = R.drawable.badminton_court,
                location = "Jakarta Selatan",
                address = "Jl. Sisingamangaraja No. 45, Kebayoran Baru, Jakarta Selatan",
                pricePerHour = 100
            ),
            SportVenue(
                id = 3,
                name = "Basketball Hall",
                imageRes = R.drawable.basketball_hall,
                location = "Jakarta Barat",
                address = "Jl. Panjang No. 102, Kedoya, Jakarta Barat",
                pricePerHour = 200
            ),
            SportVenue(
                id = 4,
                name = "Tennis Court",
                imageRes = R.drawable.tennis_court,
                location = "Jakarta Utara",
                address = "Jl. Pluit Raya No. 77, Pluit, Jakarta Utara",
                pricePerHour = 180
            ),
            SportVenue(
                id = 5,
                name = "Swimming Pool",
                imageRes = R.drawable.swimming_pool,
                location = "Jakarta Timur",
                address = "Jl. Raya Bogor Km. 25, Ciracas, Jakarta Timur",
                pricePerHour = 75
            )
        )
    }
}