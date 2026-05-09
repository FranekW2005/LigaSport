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

/**
 * Przechowuje dane i logikę dla lig
  */


class LeagueViewModel : ViewModel() {

    /**
     * **Baza danych Firestore.**
     */
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * **Sprawdza kto jest zalogowany.**
     */
    private val auth = FirebaseAuth.getInstance()

    // Stan listy lig
    private val _leagues = MutableStateFlow<List<League>>(emptyList()) // SteteFlow - strumien danych który emituje aktualną wartość
    val leagues: StateFlow<List<League>> = _leagues

    /**
     * Czy trwa ładowanie.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Komunikat błędu.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Stan drużyn dla wybranej ligi
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams

    // Inicjalizacja
    init { // wywołuje się gdy ViewModel jest tworzony
        loadLeagues()
    }

    fun loadLeagues() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                /**
                 * Zapytanie do Firestore:
                 *
                 * Pobierz wszystkie ligi gdzie adminId = obecny użytkownik
                 */
                val snapshot = firestore.collection("leagues")
                    .whereEqualTo("adminId", auth.currentUser?.uid)
                    .get()
                    .await() // poczekaj na wynik (korutyna)

                /**
                 * Przekształć dokumenty Firestore na liste League
                 */
                val leagueList = snapshot.documents.mapNotNull { document ->
                    val name = document.getString("name") ?: return@mapNotNull null
                    League(
                        id = document.id,
                        name = name
                    )
                }

                _leagues.value = leagueList

            } catch (e: Exception) {
                _errorMessage.value = "Błąd ładowania lig: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Tworzenie nowej ligi
     */
    fun createLeague(name: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {

                /**
                 * Dane ligi do zapisania
                 */
                val leagueData = hashMapOf(
                    "name" to name,
                    "adminId" to (auth.currentUser?.uid ?: ""),
                    "createdAt" to System.currentTimeMillis()
                )

                // Dodaj do Firestore
                firestore.collection("leagues")
                    .add(leagueData) // tworzy nowy dokument z automatycznmym ID
                    .await()

                loadLeagues() // Odśwież listę lig
            } catch (e: Exception) {
                _errorMessage.value = "Błąd tworzenia ligi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Usuwanie ligi
     */
    fun deleteLeague(leagueId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues")
                    .document(leagueId)
                    .delete()
                    .await()

                loadLeagues()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania: ${e.message}"
            }
        }
    }

    /**
     * Pobieranie drużyn dla konkretnej ligi
     */
    fun loadTeams(leagueId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .get()
                    .await()

                val teamList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Team::class.java)?.copy(id = doc.id)
                }
                _teams.value = teamList
            } catch (e: Exception) {
                _errorMessage.value = "Błąd ładowania drużyn: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Dodawanie drużyny do ligi
     */
    fun addTeam(leagueId: String, name: String) {
        viewModelScope.launch {
            try {
                val teamData = Team(id = "", name = name, players = emptyList())
                firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .add(teamData)
                    .await()
                loadTeams(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd dodawania drużyny: ${e.message}"
            }
        }
    }

    /**
     * Usuwanie drużyny
     */
    fun deleteTeam(leagueId: String, teamId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .document(teamId)
                    .delete()
                    .await()
                loadTeams(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania drużyny: ${e.message}"
            }
        }
    }

    /**
     * Dodawanie zawodnika do drużyny
     */
    fun addPlayer(leagueId: String, teamId: String, playerName: String) {
        viewModelScope.launch {
            try {
                val newPlayer = Player(id = UUID.randomUUID().toString(), name = playerName)
                firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .document(teamId)
                    .update("players", FieldValue.arrayUnion(newPlayer))
                    .await()
                loadTeams(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd dodawania zawodnika: ${e.message}"
            }
        }
    }

    /**
     * Usuwanie zawodnika z drużyny
     */
    fun deletePlayer(leagueId: String, teamId: String, player: Player) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .document(teamId)
                    .update("players", FieldValue.arrayRemove(player))
                    .await()
                loadTeams(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania zawodnika: ${e.message}"
            }
        }
    }
}

data class League(
    val id: String = "",
    val name: String = ""
)