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
                name = "GBK Futsal Center",
                imageRes = R.drawable.futsal_court,
                location = "Jl. Gelora Bung Karno, Senayan, Jakarta",
                address = "Gedung Pusat Olahraga Lt. 3, Komplek GBK",
                pricePerHour = 150
            ),
            SportVenue(
                id = 2,
                name = "Champions Badminton Hall",
                imageRes = R.drawable.badminton_court,
                location = "Jl. HR Rasuna Said Kav. X-1, Jakarta Selatan",
                address = "Kuningan City Mall Lt. 5, Tower A",
                pricePerHour = 80
            ),
            SportVenue(
                id = 3,
                name = "NBA Standard Basketball Court",
                imageRes = R.drawable.basketball_hall,
                location = "Jl. Jend. Sudirman Kav. 52-53, Jakarta",
                address = "Pacific Place Mall Lt. Roof Top",
                pricePerHour = 250
            ),
            SportVenue(
                id = 4,
                name = "Royal Tennis Club",
                imageRes = R.drawable.tennis_court,
                location = "Jl. Metro Pondok Indah, Jakarta Selatan",
                address = "Blok III-B No. 15, Komplek Pondok Indah Sport Club",
                pricePerHour = 200
            ),
            SportVenue(
                id = 5,
                name = "Olympic Size Swimming Pool",
                imageRes = R.drawable.swimming_pool,
                location = "Jl. Gelora Bung Karno, Senayan, Jakarta",
                address = "Komplek Senayan Aquatic Center Lt. 1",
                pricePerHour = 100
            ),
            SportVenue(
                id = 6,
                name = "Voli Pantai Sentul",
                imageRes = R.drawable.volleyball_court,
                location = "Jl. Raya Sentul No. 88, Bogor",
                address = "Komplek Sentul City Blok B-12, Bogor",
                pricePerHour = 120
            ),
            SportVenue(
                id = 7,
                name = "Lapangan Futsal Ultima",
                imageRes = R.drawable.futsal_court1,
                location = "Jl. Gatot Subroto Kav. 21, Jakarta Selatan",
                address = "Gedung Olahraga Ultima Lantai 3",
                pricePerHour = 180
            ),
            SportVenue(
                id = 8,
                name = "GOR Badminton Champions",
                    imageRes = R.drawable.badminton_court1,
                location = "Jl. MH Thamrin No. 10, Jakarta Pusat",
                address = "Plaza Senayan Lt. 5",
                pricePerHour = 150
            ),
            SportVenue(
                id = 9,
                name = "Arena Panjat Tebing Xtreme",
                imageRes = R.drawable.climbing_wall,
                location = "Jl. Sudirman Kav. 52-53, Jakarta",
                address = "SCBD Lot 28",
                pricePerHour = 200
            ),
            SportVenue(
                id = 10,
                name = "Lapangan Golf Mini Putt-Putt",
                imageRes = R.drawable.golf_course,
                location = "Jl. Kemang Raya No. 15, Jakarta Selatan",
                address = "Kemang Village Lt. 2",
                pricePerHour = 80
            ),
            SportVenue(
                id = 11,
                name = "Stadion Atletik Bung Karno",
                imageRes = R.drawable.athletic_field,
                location = "Gelora Bung Karno, Jakarta",
                address = "Komplek Gelora Bung Karno Senayan",
                pricePerHour = 75
            ),
            SportVenue(
                id = 12,
                name = "Arena Sepak Takraw",
                imageRes = R.drawable.sepak_takraw,
                location = "Jl. Ciputat Raya No. 5, Jakarta Selatan",
                address = "Pondok Indah Sport Center",
                pricePerHour = 90
            ),
            SportVenue(
                id = 13,
                name = "Baku Aquastic Swimming Palace",
                imageRes = R.drawable.swimming_pool1,
                location = "Jl. Asia Afrika No. 8, Bandung",
                address = "Gedung Sate Complex",
                pricePerHour = 100
            ),
            SportVenue(
                id = 14,
                name = "Lapangan Baseball Sluggers",
                imageRes = R.drawable.baseball_field,
                location = "Jl. Pantai Indah Kapuk No. 1, Jakarta Utara",
                address = "Pantai Indah Kapuk Blok M",
                pricePerHour = 220
            ),
            SportVenue(
                id = 15,
                name = "Arena Squash Velocity",
                imageRes = R.drawable.squash_court,
                location = "Jl. Prof. DR. Satrio No. 3, Jakarta Selatan",
                address = "Casco Tower Lt. 7",
                pricePerHour = 170
            ),
            SportVenue(
                id = 16,
                name = "Aquaswim Sport Center",
                imageRes = R.drawable.swimming_pool2,
                location = "Jl. Raya Bekasi Km 22, Jakarta Timur",
                address = "Komplek Cibubur Sport Arena Blok D-5",
                pricePerHour = 120
            )
        )
    }
}