package com.example.ligasport.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Ekran logowania i rejestracji. 
 * Jeden ekran, który zmienia się w zależności od tego, czy chcemy się zalogować, czy założyć konto.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // Lokalne stany dla pól tekstowych
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Obserwujemy stan z ViewModelu
    val isRegistering by viewModel.isRegistering.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tytuł - zmienia się dynamicznie
        Text(
            text = if (isRegistering) "Rejestracja" else "Logowanie",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Pole nazwy użytkownika pojawia się tylko przy rejestracji
        if (isRegistering) {
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Nazwa użytkownika") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Standardowe pola email i hasło
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Wyświetlanie błędu, jeśli coś poszło nie tak
        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Główny przycisk akcji
        Button(
            onClick = {
                if (isRegistering) {
                    viewModel.register(email, password, userName, onLoginSuccess)
                } else {
                    viewModel.login(email, password, onLoginSuccess)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                // Małe kółeczko ładowania wewnątrz przycisku
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    if (isRegistering) "Zarejestruj się"
                    else "Zaloguj się"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Przycisk do przełączania trybu (Logowanie <-> Rejestracja)
        TextButton(
            onClick = {
                viewModel.toggleRegistering()
                viewModel.clearError()
            }
        ) {
            Text(
                if (isRegistering) "Masz już konto? Zaloguj się"
                else "Nie masz konta? Zarejestruj się"
            )
        }
    }
}