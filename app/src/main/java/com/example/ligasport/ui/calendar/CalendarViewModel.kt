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
 * ViewModel obsługujący widok kalendarza. 
 * Pobiera absolutnie wszystkie mecze (ze wszystkich lig), żebyśmy mogli je pokazać w jednym widoku.
 */
class CalendarViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    /** Lista wszystkich meczów pobranych z Firestore */
    private val _allMatches = MutableStateFlow<List<Match>>(emptyList())
    val allMatches: StateFlow<List<Match>> = _allMatches

    /** Flaga ładowania danych */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadAllMatches()
    }

    /**
     * Pobiera całą kolekcję "matches". 
     * Wyświetlamy je potem w kalendarzu, filtrując po dacie już w UI.
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
     * Usuwanie meczu bezpośrednio z poziomu kalendarza.
     */
    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("matches").document(matchId).delete().await()
                loadAllMatches() // Odświeżamy listę, żeby mecz zniknął z widoku
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Błąd usuwania meczu: ${e.message}")
            }
        }
    }
}
