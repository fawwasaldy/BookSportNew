package com.example.booksportnew

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException


@Composable
fun VenueListScreen(venues: List<SportVenue>, onSelect: (SportVenue) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pilih Tempat Olahraga", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(venues) { venue ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onSelect(venue) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = venue.imageRes,
                            contentDescription = venue.name,
                            modifier = Modifier.size(80.dp).padding(8.dp),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.ic_error_image)
                        )
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(venue.name, style = MaterialTheme.typography.bodyLarge)
                            Text(venue.location, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SportApp(vm: SportViewModel = viewModel()) {
    val navController = rememberNavController()
    val venues by vm.venues.collectAsState()
    val bookings by vm.bookings.collectAsState()
    val context = LocalContext.current // <-- Tambahkan ini

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            VenueListScreen(venues = venues) { venue ->
                navController.navigate("form/${venue.id}")
            }
        }
        composable(
            "form/{venueId}",
            arguments = listOf(navArgument("venueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("venueId")?.toLongOrNull()
            val venue = venues.find { it.id == id }
            venue?.let {
                BookingFormScreen(
                    venue = it,
                    onBook = { date, time, sport ->
                        try {
                            val dt = LocalDateTime.parse("${date}T${time}")
                            val bookingId = vm.addBooking(it, dt, sport)
                            navController.navigate("confirm/$bookingId")
                        } catch (e: Exception) {
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
        composable(
            "confirm/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bid = backStackEntry.arguments?.getString("bookingId")?.toLongOrNull()
            val booking = bookings.firstOrNull { it.id == bid }
            if (booking != null) {
                ConfirmationScreen(
                    booking = booking,
                    onDismiss = { navController.popBackStack("list", inclusive = false) }
                )
            } else {
                ErrorScreen("Pemesanan tidak ditemukan")
            }
        }
    }
}
@Composable
fun ConfirmationScreen(booking: Booking, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "✅ Pemesanan Berhasil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detail Pemesanan", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                DetailItem("Venue", booking.venue.name)
                DetailItem("Lokasi", booking.venue.location)
                DetailItem("Olahraga", booking.sportType)
                DetailItem(
                    "Waktu",
                    booking.dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali ke Daftar Venue")
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun BookingFormScreen(
    venue: SportVenue,
    onBook: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var sportError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pesan: ${venue.name}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        // Field Tanggal
        OutlinedTextField(
            value = date,
            onValueChange = { date = it; dateError = null },
            label = { Text("Tanggal (YYYY-MM-DD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = dateError != null,
            supportingText = { dateError?.let { Text(text = it) } }
        )

        // Field Waktu
        OutlinedTextField(
            value = time,
            onValueChange = { time = it; timeError = null },
            label = { Text("Waktu (HH:MM:SS)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = timeError != null,
            supportingText = { timeError?.let { Text(text = it) } }
        )

        // Field Olahraga
        OutlinedTextField(
            value = sport,
            onValueChange = { sport = it; sportError = null },
            label = { Text("Jenis Olahraga") },
            modifier = Modifier.fillMaxWidth(),
            isError = sportError != null,
            supportingText = { sportError?.let { Text(text = it) } }
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    // Reset error
                    dateError = null
                    timeError = null
                    sportError = null

                    var hasError = false

                    // Validasi Tanggal
                    if (date.isBlank()) {
                        dateError = "Wajib diisi"
                        hasError = true
                    } else {
                        try {
                            val inputDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
                            if (inputDate.isBefore(LocalDate.now())) {
                                dateError = "Tidak boleh di masa lalu"
                                hasError = true
                            }
                        } catch (e: DateTimeParseException) {
                            dateError = "Format tidak valid"
                            hasError = true
                        }
                    }

                    // Validasi Waktu
                    if (time.isBlank()) {
                        timeError = "Wajib diisi"
                        hasError = true
                    } else {
                        try {
                            LocalTime.parse(time, DateTimeFormatter.ISO_TIME)
                        } catch (e: DateTimeParseException) {
                            timeError = "Format tidak valid"
                            hasError = true
                        }
                    }

                    // Validasi Olahraga
                    if (sport.isBlank()) {
                        sportError = "Wajib diisi"
                        hasError = true
                    }

                    if (hasError) return@Button

                    try {
                        val dt = LocalDateTime.parse("${date}T${time}")
                        if (dt.isBefore(LocalDateTime.now())) {
                            Toast.makeText(context, "Waktu tidak boleh di masa lalu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onBook(date, time, sport)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Waktu tidak valid", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Pesan")
            }

            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Batal")
            }
        }
    }
}