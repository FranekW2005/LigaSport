package com.example.ligasport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

//Modele danych

/**
 * Reprezentuje ligę w systemie.
 * @param id - identyfikator ligi
 * @param name - nazwa ligi
 * @param adminId - identyfikator użytkownika, który utworzył ligę
 */
data class League(val id: String = "", val name: String = "", val adminId: String = "")


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
/**
 * LeagueViewModel zarządza wszystkimi danymi aplikacji:
 * - Ligami (tworzenie, usuwanie, sprawdzanie admina)
 * - Drużynami globalnymi (niezależne od ligi)
 * - Drużynami w konkretnej lidze
 * - Meczami
 */
class LeagueViewModel : ViewModel() {
    // baza danych Firebase
    private val firestore = FirebaseFirestore.getInstance()
    // sprawdzanie kto jest zalogowany
    private val auth = FirebaseAuth.getInstance()

    /** Lista wszystkich lig dla zalogowanego użytkownika */
    private val _leagues = MutableStateFlow<List<League>>(emptyList())
    val leagues: StateFlow<List<League>> = _leagues

    /** Lista drużyn globalnych (niezależnych od ligi) */
    private val _globalTeams = MutableStateFlow<List<Team>>(emptyList())
    val globalTeams: StateFlow<List<Team>> = _globalTeams

    /** Lista drużyn przypisanych do konkretnej ligi */
    private val _teamsInLeague = MutableStateFlow<List<Team>>(emptyList())
    val teamsInLeague: StateFlow<List<Team>> = _teamsInLeague

    /** Lista meczów w wybranej lidze */
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

    /** Czy trwa ładowanie danych*/
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Komunikat błędu do wyświetlenia (null = brak błędu) */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Inicjalizacja
    init {
        loadLeagues()       // Pobierz wszystkie ligi
        loadGlobalTeams()   // Pobierz wszystkie drużyny globalne
    }

    // Operacje na ligach
    /**
     * Pobiera wszystkie ligi dla zalogowanego użytkownika.
     */
    fun loadLeagues() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val snapshot = firestore.collection("leagues")
                    .get()
                    .await()

                _leagues.value = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val adminId = doc.getString("adminId") ?: ""
                    League(id = doc.id, name = name, adminId = adminId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd ładowania lig: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Tworzy nową ligę w Firestore.
     * Twórca automatycznie staje się adminem (adminId = currentUser.uid).
     */
    fun createLeague(name: String) {
        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "name" to name,
                    "adminId" to (auth.currentUser?.uid ?: "")
                )
                firestore.collection("leagues").add(data).await()
                loadLeagues()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd tworzenia ligi: ${e.message}"
            }
        }
    }

    /**
     * Usuwa ligę z Firestore po jej ID.
     */
    fun deleteLeague(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues")
                    .document(id)
                    .delete()
                    .await()
                loadLeagues()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania: ${e.message}"
            }
        }
    }

    /**
     * Sprawdza czy zalogowany użytkownik jest adminem danej ligi.
     * Admin może: dodawać/usuwać drużyny, zarządzać meczami.
     *
     * @return true jeśli currentUser.uid == liga.adminId
     */
    fun isUserAdmin(leagueId: String): Boolean {
        val league = _leagues.value.find { it.id == leagueId }
        return league?.adminId == auth.currentUser?.uid
    }

    // Operacje na globalnych drużynach
    /**
     * Pobiera wszystkie drużyny globalne należące do użytkownika.
     */
    fun loadGlobalTeams() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("global_teams")
                    .whereEqualTo("ownerId", auth.currentUser?.uid)
                    .get()
                    .await()
                _globalTeams.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Team::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd drużyn: ${e.message}"
            }
        }
    }

    /**
     * Tworzy nową drużynę globalną.
     */
    fun createGlobalTeam(name: String) {
        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "name" to name,
                    "ownerId" to (auth.currentUser?.uid ?: ""),
                    "players" to emptyList<Player>()
                )
                firestore.collection("global_teams").add(data).await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd tworzenia drużyny: ${e.message}"
            }
        }
    }

    /**
     * Usuwa drużynę globalną.
     */
    fun deleteGlobalTeam(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("global_teams").document(id).delete().await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania drużyny: ${e.message}"
            }
        }
    }

    /**
     * Dodaje zawodnika do drużyny globalnej.
     *
     * @param teamId - ID drużyny do której dodajemy
     * @param player - obiekt zawodnika (bez ID, zostanie wygenerowane)
     */
    fun addPlayerToGlobalTeam(teamId: String, player: Player) {
        viewModelScope.launch {
            try {
                // Generuje unikalne ID dla zawodnika
                val newPlayer = player.copy(id = UUID.randomUUID().toString())
                // FieldValue.arrayUnion() = dodaj do tablicy bez duplikacji
                firestore.collection("global_teams").document(teamId)
                    .update("players", FieldValue.arrayUnion(newPlayer))
                    .await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd dodawania zawodnika: ${e.message}"
            }
        }
    }

    /**
     * Usuwa zawodnika z drużyny globalnej.
     * FieldValue.arrayRemove() = usuń konkretny element z tablicy.
     */
    fun deletePlayerFromGlobalTeam(teamId: String, player: Player) {
        viewModelScope.launch {
            try {
                firestore.collection("global_teams").document(teamId)
                    .update("players", FieldValue.arrayRemove(player))
                    .await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania zawodnika: ${e.message}"
            }
        }
    }

    /**
     * Aktualizuje dane zawodnika w drużynie globalnej.
     */
    fun updatePlayerInGlobalTeam(teamId: String, oldPlayer: Player, newPlayer: Player) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("global_teams").document(teamId).get().await()
                val players = (doc.get("players") as? List<Map<String, Any>>)?.mapNotNull {
                    val player = it.toPlayer()
                    if (player?.id == oldPlayer.id) newPlayer else player
                } ?: return@launch

                firestore.collection("global_teams").document(teamId)
                    .update("players", players).await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd aktualizacji: ${e.message}"
            }
        }
    }

    // Operacje na drużynach w lidze

    /**
     * Pobiera drużyny przypisane do konkretnej ligi.
     *
     * Struktura w Firestore:
     * leagues/{leagueId}/teams/{teamId}
     */
    fun loadTeamsInLeague(leagueId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("leagues").document(leagueId)
                    .collection("teams")  // Podkolekcja!
                    .get().await()
                _teamsInLeague.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Team::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd drużyn ligi: ${e.message}"
            }
        }
    }

    /**
     * Dodaje istniejącą drużynę globalną do ligi.
     * Kopiuje drużynę do podkolekcji leagues/{leagueId}/teams/
     */
    fun addTeamToLeague(leagueId: String, team: Team) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues").document(leagueId)
                    .collection("teams").document(team.id)
                    .set(team)
                    .await()
                loadTeamsInLeague(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd dodawania drużyny: ${e.message}"
            }
        }
    }

    /**
     * Usuwa drużynę z ligi (nie usuwa drużyny globalnej).
     */
    fun deleteTeamFromLeague(leagueId: String, teamId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues").document(leagueId)
                    .collection("teams").document(teamId)
                    .delete().await()
                loadTeamsInLeague(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania: ${e.message}"
            }
        }
    }

    // Operacje na meczach

    /**
     * Pobiera mecze dla wybranej ligi, posortowane po dacie.
     */
    fun loadMatches(leagueId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("matches")
                    .whereEqualTo("leagueId", leagueId)  // Tylko mecze z tej ligi
                    .get().await()
                _matches.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Match::class.java)?.copy(id = doc.id)
                }.sortedBy { it.date }  // Sortuj rosnąco po dacie
            } catch (e: Exception) {
                _errorMessage.value = "Błąd meczów: ${e.message}"
            }
        }
    }

    /**
     * Dodaje nowy mecz do ligi.
     * Wynik jest null (mecz jeszcze się nie odbył).
     */
    fun addMatch(leagueId: String, homeTeam: String, awayTeam: String, date: String, time: String) {
        viewModelScope.launch {
            try {
                val match = hashMapOf(
                    "leagueId" to leagueId,
                    "homeTeam" to homeTeam,
                    "awayTeam" to awayTeam,
                    "date" to date,
                    "time" to time,
                    "homeScore" to null,  // null = mecz nierozegrany
                    "awayScore" to null
                )
                firestore.collection("matches").add(match).await()
                loadMatches(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd dodawania meczu: ${e.message}"
            }
        }
    }
}

// Funkcja pomocnicza
/**
 * Rozszerzenie dla Map<String, Any> które konwertuje mapę z Firestore
 * na obiekt Player.
 */
fun Map<String, Any>.toPlayer(): Player? {
    return try {
        Player(
            id = this["id"] as? String ?: "",
            name = this["name"] as? String ?: return null,
            position = this["position"] as? String ?: "Napastnik",
            age = this["age"] as? String ?: "",
            height = this["height"] as? String ?: "",
            weight = this["weight"] as? String ?: ""
        )
    } catch (e: Exception) {
        null  // Jeśli konwersja się nie uda, pomiń tego zawodnika
    }
}


