package com.example.ligasport.data.models

data class Team(
    val id: String = "",
    val name: String = "",
    val players: List<Player> = emptyList()
)