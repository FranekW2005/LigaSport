package com.example.ligasport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                    // .whereEqualTo("adminId", auth.currentUser?.uid) // odkomentuj później
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
}

data class League(
    val id: String = "",
    val name: String = ""
)