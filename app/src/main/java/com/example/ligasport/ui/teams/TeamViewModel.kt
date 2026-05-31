package com.example.ligasport.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligasport.data.models.Player
import com.example.ligasport.data.models.Team
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * ViewModel dla zakładki Drużyna.
 *
 * Odpowiada za:
 * - Listę drużyn globalnych użytkownika
 * - Tworzenie/usuwanie drużyn
 * - Dodawanie/usuwanie/edycję zawodników
 */
class TeamViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** Lista drużyn globalnych */
    private val _globalTeams = MutableStateFlow<List<Team>>(emptyList())
    val globalTeams: StateFlow<List<Team>> = _globalTeams

    /** Czy trwa ładowanie */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Komunikat błędu */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadGlobalTeams()
    }

    /**
     * Pobiera wszystkie drużyny globalne należące do użytkownika.
     */
    fun loadGlobalTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("global_teams")
                    .whereEqualTo("ownerId", auth.currentUser?.uid)
                    .get()
                    .await()

                _globalTeams.value = snapshot.documents.mapNotNull { doc ->
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
                firestore.collection("global_teams")
                    .document(id)
                    .delete()
                    .await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania: ${e.message}"
            }
        }
    }

    /**
     * Dodaje zawodnika do drużyny globalnej.
     * Generuje unikalne UUID dla zawodnika.
     */
    fun addPlayerToGlobalTeam(teamId: String, player: Player) {
        viewModelScope.launch {
            try {
                val newPlayer = player.copy(id = UUID.randomUUID().toString())
                firestore.collection("global_teams")
                    .document(teamId)
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
     */
    fun deletePlayerFromGlobalTeam(teamId: String, player: Player) {
        viewModelScope.launch {
            try {
                firestore.collection("global_teams")
                    .document(teamId)
                    .update("players", FieldValue.arrayRemove(player))
                    .await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania zawodnika: ${e.message}"
            }
        }
    }

    /**
     * Aktualizuje dane zawodnika w drużynie.
     * Znajduje starego zawodnika po ID i zastępuje nowym.
     */
    fun updatePlayerInGlobalTeam(teamId: String, oldPlayer: Player, newPlayer: Player) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("global_teams")
                    .document(teamId)
                    .get()
                    .await()

                val players = (doc.get("players") as? List<Map<String, Any>>)?.mapNotNull { map ->
                    val player = map.toPlayer()
                    if (player?.id == oldPlayer.id) newPlayer else player
                } ?: return@launch

                firestore.collection("global_teams")
                    .document(teamId)
                    .update("players", players)
                    .await()
                loadGlobalTeams()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd aktualizacji: ${e.message}"
            }
        }
    }
}

/**
 * Funkcja pomocnicza - konwertuje Map<String, Any> na Player.
 * Potrzebna przy pobieraniu listy zawodników z Firestore.
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
        null
    }
}