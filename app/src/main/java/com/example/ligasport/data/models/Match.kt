package com.example.ligasport.data.models

/**
 * Reprezentuje mecz między dwiema drużynami.
 * @param id - identyfikator
 * @param leagueId - ID ligi do której należy mecz
 * @param homeTeam - nazwa drużyny gospodarzy
 * @param awayTeam - nazwa drużyny gości
 * @param date - data meczu (format "YYYY-MM-DD")
 * @param time - godzina meczu (format "HH:MM")
 * @param homeScore - bramki gospodarzy (null = mecz się nie odbył)
 * @param awayScore - bramki gości (null = mecz się nie odbył)
 */
data class Match(
    val id: String = "",          // ID dokumentu w bazie
    val leagueId: String = "",    // Do której ligi przypisany jest mecz
    val homeTeam: String = "",    // Nazwa gospodarzy
    val awayTeam: String = "",    // Nazwa gości
    val date: String = "",        // Data (np. "2023-12-01")
    val time: String = "",        // Godzina (np. "18:00")
    val homeScore: Int? = null,   // Bramki gospodarzy (null = brak wyniku)
    val awayScore: Int? = null    // Bramki gości (null = brak wyniku)
)