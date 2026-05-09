package com.example.ligasport

import android.widget.Space
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.proto.Mutation

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    //Zmienne Stanu
    /**
     * To co użytkownik wpisał w pole email.
     */
    var email by remember { mutableStateOf("") }

    /**
     * To co użytkownik wpisał w pole hasło.
     */
    var password by remember { mutableStateOf("") }

    /**
     * * **false** - tryb logowania
     * * **true** - tryb rejestracji
     */
    var isRegistering by remember { mutableStateOf(false) }

    /**
     * String? Znaczy że może być null
     * * **null** - nie ma błędu
     * * **jakiś tekst** - pokaż komunikat błędu
     */
    var errorMessage by remember { mutableStateOf<String?>(null) }

    /**
     * * **true** - trwa łączenie z Firebase
     * * **false** - nic się nie ładuje
     */
    var isLoading by remember { mutableStateOf(false) }

    //Firebase
    /**
     * **Pobieramy instancję FirebaseAuth. Nasze połączenie z Firebase.**
     */
    val auth = FirebaseAuth.getInstance()

    // Interfejs użytkownika
    Column( // Układa elementy pionowo
        modifier = Modifier
            .fillMaxSize() // Cały ekran
            .padding(32.dp), // odstęp 32dp od każdej krawędzi
        horizontalAlignment = Alignment.CenterHorizontally, // Wyśrodkowanie w poziomie

        verticalArrangement = Arrangement.Center //Wyśrodkowanie w pionie
    ){
        // Nagłówek
        Text(
            text = if (isRegistering) "Rejestracja" else "Logowanie",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp)) // Odstęp 32dp między nagłówkiem a polami

        // Pole email
        OutlinedTextField( // Pole tekstowe z obramowaniem
            value = email, // Co wyświetla
            onValueChange = { email = it }, // Co robi gdy użytkownik pisze. it to nowy tekst

            label = { Text("Email") }, // Etykieta
            singleLine = true, // Jedna linia tekstu
            modifier = Modifier.fillMaxWidth() // Pole na całą szerokość
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pole hasło
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(), // Ukrywa wpisywane znaki, pokazuje kropki
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Komunikat błędu
        errorMessage?.let { error -> // Wyświetli się tylko jeśli errorMessage nie jest null
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Główny przycisk
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) { // Sprawdza czy pola nie są puste
                    errorMessage = "Wypełnij wszystkie pola"
                    return@Button // Zatrzymaj się tutaj
                }

                isLoading = true // Rozpoczęcie ładowania
                errorMessage = null // Wyczyszczenie starych błędów

                if (isRegistering){
                    //Rejestracja
                    // createUserWithEmailAndPassword() tworzy nowe konto
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            // To się wykona jak Firebase odpowie
                            isLoading = false // Przestań ładować

                            if (task.isSuccessful) {
                                onLoginSuccess() // Do głównego ekranu
                            }
                            else { // błąd przy rejestracji
                                errorMessage = task.exception?.message
                                    ?: "Błąd rejestacji"
                            }
                        }
                }

                else {
                    // Logowanie
                    // signInWithEmailAndPassword() loguje istniejące konto
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false

                            if (task.isSuccessful) {
                                onLoginSuccess()
                            }
                            else {
                                errorMessage = "Nieprawidłowy email lub hasło"
                            }
                        }
                }
            },

            //Przycisk nieaktywny podczas ładowania
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Zawartość przycisku
            if (isLoading) {
                // Kółko ładowania
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            else {
                // Tekst na przycisku
                Text(
                    if (isRegistering) "Zarejstruj się"
                    else "Zaloguj się"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Przełącznik logowanie/rejestracja
        TextButton(
            onClick = {
                isRegistering = !isRegistering // Zmiana trybu na przeciwny
                errorMessage = null // Wyczyszczenie błędów
            }
        ) {
            Text(
                if(isRegistering) "Masz już konto? Zaloguj się"
                else "Nie masz konta? Zarejestruj się"
            )
        }
    }
}