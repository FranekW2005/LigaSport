package com.example.ligasport

data class Team(
    val id: String = "",
    val name: String = "",
    val players: List<Player> = emptyList()
)