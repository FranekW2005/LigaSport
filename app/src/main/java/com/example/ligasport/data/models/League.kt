package com.example.ligasport.data.models

/**
 * Reprezentuje ligę w systemie.
 * @param id - identyfikator ligi
 * @param name - nazwa ligi
 * @param adminId - identyfikator użytkownika, który utworzył ligę
 */
data class League(
    val id: String = "",       // Unikalny identyfikator dokumentu w Firestore
    val name: String = "",     // Wyświetlana nazwa ligi
    val adminId: String = ""   // ID użytkownika, który ma uprawnienia do edycji tej ligi
)