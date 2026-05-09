package com.example.ligasport

import android.os.Bundle                                // Obiekt do przechowywania danych
import androidx.activity.ComponentActivity              // Klasa bazowa dla Activity używających Compose
import androidx.activity.compose.setContent             // Rozszerza ComponentActivity
import androidx.activity.enableEdgeToEdge               // Sprawia że aplikacja jest pod paskiem statusu
import androidx.compose.foundation.layout.fillMaxSize   // Modyfikator rozmiaru
import androidx.compose.material3.MaterialTheme         // Dostęp do kolorów i stylów Material3
import androidx.compose.material3.Surface               // Kontener w Material3
import com.example.ligasport.ui.theme.LigaSportTheme    // Własny motyw
import androidx.compose.runtime.*                       // Wszystko z pakietu runtime
import androidx.compose.ui.Modifier


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // fullscreen
        setContent {       // rysowanie UI w Compose
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { // kontener z tłem
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Funckja obsługująca nawigację w aplikacji.
 */
@Composable
fun AppNavigation() {
    /**
     * **Zmienna sterująca który ekran pokazać.**
     *
     * * "login" - ekran startowy*
     *
     * * "leagues" - widok lig*
     *
     * * "leagueDetail" - wgląd w ligę"*
     */
    var currentScreen by remember {mutableStateOf("login")}

    /**
     * **ID wybranej ligi.**
     */
    var selectedLeagueId by remember {mutableStateOf("")}

    when (currentScreen){
        "login" -> LoginScreen (
            onLoginSuccess = {currentScreen = "leagues"}
        )

        "leagues" -> LeaguesScreen (
            onLeagueClick = {leagueID ->
                selectedLeagueId = leagueID
                currentScreen = "leagueDetail"
            }
        )

        "leagueDetail" -> LeagueDetailScreen(
            leagueId = selectedLeagueId,
            onBack = { currentScreen = "leagues" }
        )
    }
}
