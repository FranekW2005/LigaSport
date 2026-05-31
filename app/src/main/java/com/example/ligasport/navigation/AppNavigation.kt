package com.example.ligasport.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ligasport.ui.auth.LoginScreen
import com.example.ligasport.ui.home.HomeScreen
import com.example.ligasport.ui.leagues.LeaguesScreen
import com.example.ligasport.ui.leagueDetail.LeagueDetailScreen
import com.google.firebase.auth.FirebaseAuth

/**
 * Główna nawigacja aplikacji używająca NavHost.
 *
 * Trasy:
 * - "login" → ekran logowania
 * - "home" → ekran główny z bottom barem
 * - "leagues" → lista lig (osobny ekran)
 * - "leagueDetail/{leagueId}" → szczegóły ligi
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Sprawdź czy użytkownik jest zalogowany
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "home"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Ekran logowania
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }  // Usuń login ze stosu
                    }
                }
            )
        }

        // Ekran główny z bottom barem
        composable("home") {
            HomeScreen(
                onLeagueClick = { leagueId ->
                    navController.navigate("leagueDetail/$leagueId")
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }  // Wyczyść cały stos
                    }
                }
            )
        }

        // Lista lig (osobny ekran)
        composable("leagues") {
            LeaguesScreen(
                onLeagueClick = { leagueId ->
                    navController.navigate("leagueDetail/$leagueId")
                },
                onBack = {
                    navController.popBackStack()  // Wróć do poprzedniego ekranu
                }
            )
        }

        // Szczegóły ligi (z parametrem leagueId)
        composable("leagueDetail/{leagueId}") { backStackEntry ->
            val leagueId = backStackEntry.arguments?.getString("leagueId") ?: ""
            LeagueDetailScreen(
                leagueId = leagueId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}