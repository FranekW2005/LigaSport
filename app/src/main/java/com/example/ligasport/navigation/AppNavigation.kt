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
        // --- EKRAN LOGOWANIA ---
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Po udanym logowaniu czyścimy stos, żeby user nie wrócił do logowania przyciskiem "back"
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- EKRAN GŁÓWNY (z zakładkami) ---
        composable("home") {
            HomeScreen(
                onLeagueClick = { leagueId ->
                    navController.navigate("leagueDetail/$leagueId")
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    // Przy wylogowaniu czyścimy wszystko i wracamy do logowania
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- LISTA LIG ---
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

        // --- SZCZEGÓŁY KONKRETNEJ LIGI ---
        // Przekazujemy leagueId w URL, żeby wiedzieć, które dane pobrać
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