package com.example.ligasport

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onNavigateToLeagues: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LigaSport",
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Główny przycisk do lig
        Button(
            onClick = onNavigateToLeagues,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Moje Ligi", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Miejsce na przyszłe funkcje
        OutlinedButton(
            onClick = { /* Tu w przyszłości inna funkcja */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = false // Na razie wyłączony
        ) {
            Text(text = "Inne funkcje (wkrótce)", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onLogout) {
            Text("Wyloguj się", color = MaterialTheme.colorScheme.error)
        }
    }
}
