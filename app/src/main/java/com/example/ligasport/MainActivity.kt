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
    // Sprawdź czy użytkownik jest zalogowany PRZY STARCIE
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    // Ustaw ekran startowy w zależności czy jest zalogowany
    var currentScreen by remember {
        mutableStateOf(if (isLoggedIn) "home" else "login")
    }

    // ID wybranej ligi
    var selectedLeagueId by remember { mutableStateOf("") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { currentScreen = "home" }
        )

        "home" -> HomeScreen(
            onNavigateToLeagues = { currentScreen = "leagues" },
            onLeagueClick = { leagueId ->
                selectedLeagueId = leagueId
                currentScreen = "leagueDetail"
            },
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                currentScreen = "login"
            }
        )

        "leagues" -> LeaguesScreen(
            onLeagueClick = { leagueId ->
                selectedLeagueId = leagueId
                currentScreen = "leagueDetail"
            },
            onBack = { currentScreen = "home" }
        )

        "leagueDetail" -> LeagueDetailScreen(
            leagueId = selectedLeagueId,
            onBack = { currentScreen = "leagues" }
        )
    }
}
