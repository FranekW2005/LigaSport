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
    val id: String = "",
    val leagueId: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val date: String = "",
    val time: String = "",
    val homeScore: Int? = null,
    val awayScore: Int? = null
)