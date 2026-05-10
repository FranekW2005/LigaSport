package com.example.ligasport

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

// Ikony dla bottom bar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person

// === ZAKŁADKI BOTTOM BARA ===
// Każda zakładka ma: nazwę, ikonę i treść

enum class HomeTab(val title: String) {
    HOME("Główna"),
    TEAM("Drużyna"),
    CALENDAR("Kalendarz"),
    PROFILE("Profil")
}

@Composable
fun HomeScreen(
    onNavigateToLeagues: () -> Unit,
    onLogout: () -> Unit
) {
    // Która zakładka jest aktywna (domyślnie HOME)
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    // Pobierz dane użytkownika
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: ""
    val userName = userEmail.split("@").firstOrNull() ?: "Użytkowniku"

    // Scaffold z bottom barem
    Scaffold(
        modifier = Modifier.fillMaxSize(),

        // === DOLNY PASEK NAWIGACJI ===
        bottomBar = {
            NavigationBar {
                // Zakładka HOME
                NavigationBarItem(
                    selected = selectedTab == HomeTab.HOME,
                    onClick = { selectedTab = HomeTab.HOME },
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Główna"
                        )
                    },
                    label = { Text("Główna") }
                )

                // Zakładka DRUŻYNA
                NavigationBarItem(
                    selected = selectedTab == HomeTab.TEAM,
                    onClick = { selectedTab = HomeTab.TEAM },
                    icon = {
                        Icon(
                            Icons.Filled.Groups,
                            contentDescription = "Drużyna"
                        )
                    },
                    label = { Text("Drużyna") }
                )

                // Zakładka KALENDARZ
                NavigationBarItem(
                    selected = selectedTab == HomeTab.CALENDAR,
                    onClick = { selectedTab = HomeTab.CALENDAR },
                    icon = {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = "Kalendarz"
                        )
                    },
                    label = { Text("Kalendarz") }
                )

                // Zakładka PROFIL
                NavigationBarItem(
                    selected = selectedTab == HomeTab.PROFILE,
                    onClick = { selectedTab = HomeTab.PROFILE },
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profil"
                        )
                    },
                    label = { Text("Profil") }
                )
            }
        }
    ) { innerPadding ->
        // === ZAWARTOŚĆ AKTYWNEJ ZAKŁADKI ===
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                HomeTab.HOME -> {
                    // Zawartość zakładki Główna
                    HomeTabContent(
                        userName = userName,
                        onNavigateToLeagues = onNavigateToLeagues
                    )
                }

                HomeTab.TEAM -> {
                    // Zawartość zakładki Drużyna
                    TeamTabContent()
                }

                HomeTab.CALENDAR -> {
                    // Zawartość zakładki Kalendarz
                    CalendarTabContent()
                }

                HomeTab.PROFILE -> {
                    // Zawartość zakładki Profil
                    ProfileTabContent(
                        userEmail = userEmail,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

// === ZAWARTOŚĆ ZAKŁADKI GŁÓWNA ===
@Composable
fun HomeTabContent(
    userName: String,
    onNavigateToLeagues: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Powitanie
        Text(
            text = "Witaj, $userName!",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Karta "Twoja Liga"
        Text(
            text = "Twoja Liga",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToLeagues  // Kliknięcie → przejdź do lig
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Liga Podwórkowa 2024", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "4 drużyny • 6 meczów",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sekcja "Najbliższe mecze"
        Text(
            text = "Najbliższe Mecze",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Przykładowy mecz
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FC Orły", fontSize = 16.sp)
                Text("vs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("KS Sport", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Drugi przykładowy mecz
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FC Orły", fontSize = 16.sp)
                Text("vs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Old Boys", fontSize = 16.sp)
            }
        }
    }
}

// === ZAWARTOŚĆ ZAKŁADKI DRUŻYNA ===
@Composable
fun TeamTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Moja Drużyna",
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu będzie widok Twojej drużyny i zawodników",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

// === ZAWARTOŚĆ ZAKŁADKI KALENDARZ ===
@Composable
fun CalendarTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kalendarz Meczów",
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu będzie kalendarz z nadchodzącymi meczami",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

// === ZAWARTOŚĆ ZAKŁADKI PROFIL ===
@Composable
fun ProfileTabContent(
    userEmail: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mój Profil",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Karta z danymi
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Email:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(userEmail, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Przycisk wylogowania
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wyloguj się")
        }
    }
}