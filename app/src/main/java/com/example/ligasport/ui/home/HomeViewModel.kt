package com.example.ligasport.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligasport.data.models.League
import com.example.ligasport.data.models.Match
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel dla ekranu głównego (HomeScreen).
 *
 * Odpowiada za:
 * - Listę wszystkich lig
 * - Mecze w wybranej lidze
 * - Sprawdzanie czy użytkownik jest adminem
 * - Dodawanie meczów
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

    // Pobierz dane przy starcie
    init {
        loadLeagues()
        loadUserName()
    }

    /**
     * Pobiera nazwę użytkownika z Firestore.
     */
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

    /**
     * Pobiera wszystkie ligi z Firestore.
     */
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

    /**
     * Pobiera mecze dla wybranej ligi, sortuje po dacie.
     */
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

    /**
     * Dodaje nowy mecz do ligi.
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

    /**
     * Sprawdza czy zalogowany użytkownik jest adminem ligi.
     */
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
    }

    /** Czyści błąd */
    fun clearError() {
        _errorMessage.value = null
    }
}