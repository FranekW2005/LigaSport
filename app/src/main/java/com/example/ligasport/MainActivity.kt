package com.example.ligasport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ligasport.ui.theme.LigaSportTheme
import com.example.ligasport.navigation.AppNavigation

/**
 * Główny punkt wejścia do aplikacji. 
 * Tutaj ustawiamy motyw i odpalamy nawigację.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Rozciągnięcie apki na cały ekran (pod pasek statusu)
        enableEdgeToEdge()
        
        setContent {
            // Nasz własny motyw LigaSport
            LigaSportTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Startujemy główny graf nawigacji
                    AppNavigation()
                }
            }
        }
    }
}