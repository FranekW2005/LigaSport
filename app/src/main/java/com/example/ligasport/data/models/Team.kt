package com.example.ligasport.data.models

/**
 * Model drużyny. 
 * Zawiera nazwę i listę przypisanych do niej zawodników.
 */
data class Team(
    val id: String = "",               // ID drużyny w Firestore
    val name: String = "",             // Nazwa (np. "FC Kopacze")
    val players: List<Player> = emptyList() // Lista obiektów Player
)