package com.example.ligasport.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligasport.data.models.League
import com.example.ligasport.data.models.Match
import com.example.ligasport.data.models.Team
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel dla ekranu głównego (HomeScreen).
 */
class HomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** Lista wszystkich lig */
    private val _leagues = MutableStateFlow<List<League>>(emptyList())
    val leagues: StateFlow<List<League>> = _leagues

    /** Lista meczów w wybranej lidze */
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

    /** Drużyny w wybranej lidze (do tworzenia meczu) */
    private val _teamsInLeague = MutableStateFlow<List<Team>>(emptyList())
    val teamsInLeague: StateFlow<List<Team>> = _teamsInLeague

    /** Czy trwa ładowanie */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Komunikat błędu */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** Nazwa zalogowanego użytkownika */
    private val _userName = MutableStateFlow("Użytkowniku")
    val userName: StateFlow<String> = _userName

    /** Która zakładka jest aktywna */
    private val _selectedTab = MutableStateFlow("HOME")
    val selectedTab: StateFlow<String> = _selectedTab

    /** Wybrana liga */
    private val _selectedLeagueId = MutableStateFlow("")
    val selectedLeagueId: StateFlow<String> = _selectedLeagueId

    private val _selectedLeagueName = MutableStateFlow("Wybierz ligę")
    val selectedLeagueName: StateFlow<String> = _selectedLeagueName

    init {
        loadLeagues()
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val doc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                _userName.value = doc.getString("userName") ?: "Użytkowniku"
            } catch (e: Exception) {
                _userName.value = "Użytkowniku"
            }
        }
    }

    fun loadLeagues() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
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

    fun loadMatches(leagueId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("matches")
                    .whereEqualTo("leagueId", leagueId)
                    .get()
                    .await()

                _matches.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Match::class.java)?.copy(id = doc.id)
                }.sortedBy { it.date }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd ładowania meczów: ${e.message}"
            }
        }
    }

    /** Pobiera drużyny przypisane do ligi */
    fun loadTeamsInLeague(leagueId: String) {
        viewModelScope.launch {
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
                _errorMessage.value = "Błąd ładowania drużyn ligi: ${e.message}"
            }
        }
    }

    fun addMatch(leagueId: String, homeTeam: String, awayTeam: String, date: String, time: String) {
        viewModelScope.launch {
            try {
                val match = hashMapOf(
                    "leagueId" to leagueId,
                    "homeTeam" to homeTeam,
                    "awayTeam" to awayTeam,
                    "date" to date,
                    "time" to time,
                    "homeScore" to null,
                    "awayScore" to null
                )
                firestore.collection("matches").add(match).await()
                loadMatches(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd dodawania meczu: ${e.message}"
            }
        }
    }

    fun deleteMatch(matchId: String, leagueId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("matches")
                    .document(matchId)
                    .delete()
                    .await()
                loadMatches(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania meczu: ${e.message}"
            }
        }
    }

    fun isUserAdmin(leagueId: String): Boolean {
        val league = _leagues.value.find { it.id == leagueId }
        return league?.adminId == auth.currentUser?.uid
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setSelectedLeague(id: String, name: String) {
        _selectedLeagueId.value = id
        _selectedLeagueName.value = name
        if (id.isNotEmpty()) {
            loadMatches(id)
            loadTeamsInLeague(id)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Aktualizuje wynik meczu w Firestore.
     *
     * @param matchId - ID meczu do zaktualizowania
     * @param homeScore - bramki gospodarzy
     * @param awayScore - bramki gości
     * @param leagueId - ID ligi (do odświeżenia listy meczów)
     */
    fun updateMatchResult(matchId: String, homeScore: Int, awayScore: Int, leagueId: String) {
        viewModelScope.launch {
            try {
                // Aktualizuj dokument meczu w Firestore
                firestore.collection("matches")
                    .document(matchId)
                    .update(
                        mapOf(
                            "homeScore" to homeScore,
                            "awayScore" to awayScore
                        )
                    )
                    .await()

                // Odśwież listę meczów żeby zobaczyć wynik
                loadMatches(leagueId)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd zapisu wyniku: ${e.message}"
            }
        }
    }
} // Koniec klasy
