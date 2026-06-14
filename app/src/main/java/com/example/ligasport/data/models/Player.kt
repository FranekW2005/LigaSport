package com.example.ligasport.data.models

/**
 * Model danych dla zawodnika. 
 * Przechowujemy tu podstawowe info, które wpisujemy przy dodawaniu gracza do drużyny.
 */
data class Player(
    val id: String = "",         // Unikalne ID (zazwyczaj z Firestore)
    val name: String = "",       // Imię i nazwisko
    val position: String = "",   // Pozycja na boisku (np. Napastnik, Bramkarz)
    val age: String = "",        // Wiek zawodnika
    val height: String = "",     // Wzrost w cm
    val weight: String = ""      // Waga w kg
)