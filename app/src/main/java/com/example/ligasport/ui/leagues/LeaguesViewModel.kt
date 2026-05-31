package com.example.ligasport.ui.leagues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligasport.data.models.League
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel dla ekranu listy lig.
 *
 * Odpowiada za:
 * - Tworzenie nowej ligi
 * - Usuwanie ligi
 * - Ładowanie listy lig użytkownika
 */
class LeaguesViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** Lista lig */
    private val _leagues = MutableStateFlow<List<League>>(emptyList())
    val leagues: StateFlow<List<League>> = _leagues

    /** Czy trwa ładowanie */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Komunikat błędu */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadLeagues()
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
     * Tworzy nową ligę.
     * Twórca automatycznie staje się adminem.
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
     * Usuwa ligę po ID.
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
}