package com.example.ligasport.ui.leagueDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligasport.data.models.Team
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel dla ekranu szczegółów ligi.
 *
 * Odpowiada za:
 * - Drużyny przypisane do konkretnej ligi
 * - Dodawanie/usuwanie drużyn z ligi
 * - Dostępne drużyny globalne (do wyboru)
 * - Sprawdzanie uprawnień admina
 */
class LeagueDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- Stan danych ---

    /** Drużyny, które już są przypisane do tej ligi */
    private val _teamsInLeague = MutableStateFlow<List<Team>>(emptyList())
    val teamsInLeague: StateFlow<List<Team>> = _teamsInLeague

    /** Wszystkie drużyny globalne (do wyboru przy dodawaniu) */
    private val _globalTeams = MutableStateFlow<List<Team>>(emptyList())
    val globalTeams: StateFlow<List<Team>> = _globalTeams

    // --- Stan UI ---

    /** Czy aktualnie zalogowany user jest adminem tej konkretnej ligi? */
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    /** Czy trwa ładowanie */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Komunikat błędu */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Pobiera dane o lidze i sprawdza uprawnienia.
     */
    fun checkIfAdmin(leagueId: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("leagues").document(leagueId).get().await()
                val adminId = doc.getString("adminId") ?: ""
                _isAdmin.value = adminId == auth.currentUser?.uid
            } catch (e: Exception) {
                _isAdmin.value = false
            }
        }
    }

    /**
     * Pobiera drużyny z podkolekcji ligi.
     * Struktura: leagues/{leagueId}/teams/{teamId}
     */
    fun loadTeamsInLeague(leagueId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .get()
                    .await()

                _teamsInLeague.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Team::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd ładowania drużyn: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Pobiera wszystkie Twoje drużyny z "global_teams", żeby pokazać je w liście do wyboru.
     */
    fun loadGlobalTeams() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("global_teams")
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
     * Dodaje drużynę globalną do ligi.
     */
    fun addTeamToLeague(leagueId: String, team: Team) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .document(team.id)
                    .set(team)
                    .await()
                loadTeamsInLeague(leagueId) // Odświeżamy widok
            } catch (e: Exception) {
                _errorMessage.value = "Błąd dodawania: ${e.message}"
            }
        }
    }

    /**
     * Usuwa drużynę z ligi.
     */
    fun deleteTeamFromLeague(leagueId: String, teamId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues")
                    .document(leagueId)
                    .collection("teams")
                    .document(teamId)
                    .delete()
                    .await()
                loadTeamsInLeague(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania: ${e.message}"
            }
        }
    }
}
