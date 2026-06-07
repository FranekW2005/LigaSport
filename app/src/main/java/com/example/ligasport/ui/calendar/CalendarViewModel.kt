package com.example.ligasport.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligasport.data.models.Match
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel dla zakładki Kalendarz.
 * Obsługuje pobieranie wszystkich meczów i ich usuwanie.
 */
class CalendarViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _allMatches = MutableStateFlow<List<Match>>(emptyList())
    val allMatches: StateFlow<List<Match>> = _allMatches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadAllMatches()
    }

    /**
     * Pobiera wszystkie mecze ze wszystkich lig.
     */
    fun loadAllMatches() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("matches")
                    .get()
                    .await()
                
                _allMatches.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Match::class.java)?.copy(id = doc.id)
                }.sortedBy { it.date }
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Błąd ładowania meczów: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Usuwa mecz po ID.
     */
    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("matches").document(matchId).delete().await()
                loadAllMatches()
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Błąd usuwania meczu: ${e.message}")
            }
        }
    }
}
