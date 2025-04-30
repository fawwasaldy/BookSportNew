package com.example.booksportnew

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

sealed class Screen(val route: String, val title: String) {
    object Book : Screen("book", "Book")
    object History : Screen("history", "History")
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
fun SportApp(vm: SportViewModel = viewModel()) {
    val navController = rememberNavController()
    val venues by vm.venues.collectAsState()
    val bookings by vm.bookings.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.Book,
        Screen.History
    )

    Scaffold(
        bottomBar = {
            val showBottomBar = currentRoute in listOf("list", "history")

            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.Book -> Icons.Filled.Home
                                        Screen.History -> Icons.Filled.List
                                    },
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = when (screen) {
                                Screen.Book -> currentRoute == "list"
                                Screen.History -> currentRoute == "history"
                            },
                            onClick = {
                                when (screen) {
                                    Screen.Book -> {
                                        if (currentRoute != "list") {
                                            navController.navigate("list") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    Screen.History -> {
                                        if (currentRoute != "history") {
                                            navController.navigate("history") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(innerPadding)
        ) {
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
                        onBook = { date, startTime, endTime, sport, fullName, email, phone ->
                            try {
                                val startDateTime = LocalDateTime.parse("${date}T${startTime}")
                                val endDateTime = LocalDateTime.parse("${date}T${endTime}")
                                val bookingId = vm.addBooking(
                                    venue = it,
                                    dateTime = startDateTime,
                                    endDateTime = endDateTime,
                                    sportType = sport,
                                    fullName = fullName,
                                    email = email,
                                    phone = phone
                                )
                                navController.navigate("confirm/$bookingId")
                            } catch (e: Exception) {
                                Log.e("SportApp", "Error during booking: ${e.message}", e)
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

            composable("history") {
                BookingHistoryScreen(bookings = bookings)
            }
        }
    }
}

@Composable
fun BookingHistoryScreen(bookings: List<Booking>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Riwayat Pemesanan",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada pemesanan",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(bookings.sortedByDescending { it.dateTime }) { booking ->
                    BookingHistoryItem(booking)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun BookingHistoryItem(booking: Booking) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = booking.venue.imageRes,
                contentDescription = booking.venue.name,
                modifier = Modifier.size(80.dp).padding(8.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_error_image)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    booking.venue.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    booking.sportType,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Lokasi: ${booking.venue.location}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Tanggal: ${booking.dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Waktu: ${booking.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))} to ${booking.endDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Nama: ${booking.fullName}",
                    style = MaterialTheme.typography.bodySmall
                )
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
            "Pemesanan Berhasil",
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
    onBook: (String, String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    // Booking information
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }

    // Personal information
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Error states
    var dateError by remember { mutableStateOf<String?>(null) }
    var startTimeError by remember { mutableStateOf<String?>(null) }
    var endTimeError by remember { mutableStateOf<String?>(null) }
    var sportError by remember { mutableStateOf<String?>(null) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pesan: ${venue.name}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Personal Information Section
        Text(
            "Personal Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; fullNameError = null },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = fullNameError != null,
            supportingText = { fullNameError?.let { Text(text = it) } }
        )
        Spacer(Modifier.height(8.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = { Text("Email Address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = { emailError?.let { Text(text = it) } }
        )
        Spacer(Modifier.height(8.dp))

        // Phone Field
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = null },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            isError = phoneError != null,
            supportingText = { phoneError?.let { Text(text = it) } }
        )
        Spacer(Modifier.height(16.dp))

        // Booking Information Section
        Text(
            "Booking Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        // Date Field
        OutlinedTextField(
            value = date,
            onValueChange = { date = it; dateError = null },
            label = { Text("Select Date (YYYY-MM-DD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = dateError != null,
            supportingText = { dateError?.let { Text(text = it) } }
        )
        Spacer(Modifier.height(8.dp))

        // Time Fields (Start and End)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it; startTimeError = null },
                label = { Text("Start Time (HH:MM:SS)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                isError = startTimeError != null,
                supportingText = { startTimeError?.let { Text(text = it) } }
            )

            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it; endTimeError = null },
                label = { Text("End Time (HH:MM:SS)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                isError = endTimeError != null,
                supportingText = { endTimeError?.let { Text(text = it) } }
            )
        }
        Spacer(Modifier.height(8.dp))

        // Sport Type Field
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
                    // Reset all errors
                    dateError = null
                    startTimeError = null
                    endTimeError = null
                    sportError = null
                    fullNameError = null
                    emailError = null
                    phoneError = null

                    var hasError = false

                    // Validate Personal Information
                    if (fullName.isBlank()) {
                        fullNameError = "Wajib diisi"
                        hasError = true
                    }

                    if (email.isBlank()) {
                        emailError = "Wajib diisi"
                        hasError = true
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Format email tidak valid"
                        hasError = true
                    }

                    if (phone.isBlank()) {
                        phoneError = "Wajib diisi"
                        hasError = true
                    }

                    // Validate Date
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

                    // Validate Start Time
                    if (startTime.isBlank()) {
                        startTimeError = "Wajib diisi"
                        hasError = true
                    } else {
                        try {
                            LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME)
                        } catch (e: DateTimeParseException) {
                            startTimeError = "Format tidak valid"
                            hasError = true
                        }
                    }

                    // Validate End Time
                    if (endTime.isBlank()) {
                        endTimeError = "Wajib diisi"
                        hasError = true
                    } else {
                        try {
                            val startLocalTime = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME)
                            val endLocalTime = LocalTime.parse(endTime, DateTimeFormatter.ISO_TIME)

                            if (!endLocalTime.isAfter(startLocalTime)) {
                                endTimeError = "Harus setelah waktu mulai"
                                hasError = true
                            }
                        } catch (e: DateTimeParseException) {
                            endTimeError = "Format tidak valid"
                            hasError = true
                        }
                    }

                    // Validate Sport Type
                    if (sport.isBlank()) {
                        sportError = "Wajib diisi"
                        hasError = true
                    }

                    if (hasError) return@Button

                    try {
                        val startDateTime = LocalDateTime.parse("${date}T${startTime}")
                        val endDateTime = LocalDateTime.parse("${date}T${endTime}")

                        if (startDateTime.isBefore(LocalDateTime.now())) {
                            Toast.makeText(context, "Waktu tidak boleh di masa lalu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onBook(date, startTime, endTime, sport, fullName, email, phone)
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