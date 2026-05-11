package com.example.ligasport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.ligasport.ui.theme.LigaSportTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LigaSportTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("login") }
    var selectedLeagueId by remember { mutableStateOf("") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { currentScreen = "home" }
        )

        "home" -> HomeScreen(
            onNavigateToLeagues = { /* HomeScreen obsługuje to teraz wewnętrznie */ },
            onLeagueClick = { leagueId ->
                selectedLeagueId = leagueId
                currentScreen = "leagueDetail"
            },
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                currentScreen = "login"
            }
        )

        "leagueDetail" -> LeagueDetailScreen(
            leagueId = selectedLeagueId,
            onBack = { currentScreen = "home" } // Powrót do ekranu głównego (który pamięta zakładkę Ligi)
        )
    }
}
