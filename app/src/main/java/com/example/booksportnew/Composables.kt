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

@Composable
fun SportApp(vm: SportViewModel = viewModel()) {
    val navController = rememberNavController()
    val venues by vm.venues.collectAsState()
    val bookings by vm.bookings.collectAsState()

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
                BookingFormScreen(venue = it, onBook = { date, time, sport ->
                    try {
                        val dt = LocalDateTime.parse("${date}T${time}", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        vm.addBooking(it, dt, sport)
                        navController.navigate("confirm/${bookings.size - 1}")
                    } catch (e: Exception) {
                        Log.e("SportApp", "Date parsing error: ${e.message}", e)
                        // Show error to user
                    }
                }, onCancel = { navController.popBackStack() })
            }
        }
        composable(
            "confirm/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bid = backStackEntry.arguments?.getString("bookingId")?.toIntOrNull()
            val booking = bookings.getOrNull(bid ?: -1)
            booking?.let {
                ConfirmationScreen(booking = it)
            }
        }
    }
}

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
fun BookingFormScreen(
    venue: SportVenue,
    onBook: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pesan: ${venue.name}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Tanggal (YYYY-MM-DD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Waktu (HH:MM:SS)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = sport,
            onValueChange = { sport = it },
            label = { Text("Jenis Olahraga") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (date.isBlank() || time.isBlank() || sport.isBlank()) {
                    Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        // Validate date format
                        if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            Toast.makeText(context, "Format tanggal harus YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Validate time format
                        if (!time.matches(Regex("\\d{2}:\\d{2}:\\d{2}"))) {
                            Toast.makeText(context, "Format waktu harus HH:MM:SS", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onBook(date, time, sport)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Format tanggal/waktu tidak valid: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }, modifier = Modifier.weight(1f)) {
                Text("Pesan")
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Batal")
            }
        }
    }
}

@Composable
fun ConfirmationScreen(booking: Booking) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Konfirmasi Pemesanan", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text("Venue: ${booking.venue.name}")
        Text("Lokasi: ${booking.venue.location}")
        Text("Olahraga: ${booking.sportType}")
        Text("Waktu: ${booking.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        Spacer(Modifier.height(24.dp))
        Text("Pemesanan berhasil!", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
