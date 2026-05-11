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

class LeagueViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _leagues = MutableStateFlow<List<League>>(emptyList())
    val leagues: StateFlow<List<League>> = _leagues

    private val _globalTeams = MutableStateFlow<List<Team>>(emptyList())
    val globalTeams: StateFlow<List<Team>> = _globalTeams

    private val _teamsInLeague = MutableStateFlow<List<Team>>(emptyList())
    val teamsInLeague: StateFlow<List<Team>> = _teamsInLeague

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadLeagues()
        loadGlobalTeams()
    }

    fun loadLeagues() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("leagues")
                    .whereEqualTo("adminId", auth.currentUser?.uid)
                    .get().await()
                _leagues.value = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    League(id = doc.id, name = name)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun createLeague(name: String) {
        viewModelScope.launch {
            try {
                val data = hashMapOf("name" to name, "adminId" to (auth.currentUser?.uid ?: ""))
                firestore.collection("leagues").add(data).await()
                loadLeagues()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun deleteLeague(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues").document(id).delete().await()
                loadLeagues()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun loadGlobalTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("global_teams")
                    .whereEqualTo("ownerId", auth.currentUser?.uid)
                    .get().await()
                _globalTeams.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Team::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

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
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun deleteGlobalTeam(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("global_teams").document(id).delete().await()
                loadGlobalTeams()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun addPlayerToGlobalTeam(teamId: String, player: Player) {
        viewModelScope.launch {
            try {
                val newPlayer = if (player.id.isEmpty()) player.copy(id = UUID.randomUUID().toString()) else player
                firestore.collection("global_teams").document(teamId)
                    .update("players", FieldValue.arrayUnion(newPlayer)).await()
                loadGlobalTeams()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun deletePlayerFromGlobalTeam(teamId: String, player: Player) {
        viewModelScope.launch {
            try {
                firestore.collection("global_teams").document(teamId)
                    .update("players", FieldValue.arrayRemove(player)).await()
                loadGlobalTeams()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun updatePlayerInGlobalTeam(teamId: String, oldPlayer: Player, newPlayer: Player) {
        viewModelScope.launch {
            try {
                val teamDoc = firestore.collection("global_teams").document(teamId).get().await()
                val team = teamDoc.toObject(Team::class.java)
                if (team != null) {
                    val updatedPlayers = team.players.map { 
                        if (it.id == oldPlayer.id) newPlayer else it 
                    }
                    firestore.collection("global_teams").document(teamId)
                        .update("players", updatedPlayers).await()
                    loadGlobalTeams()
                }
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun loadTeamsInLeague(leagueId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("leagues").document(leagueId)
                    .collection("teams").get().await()
                _teamsInLeague.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Team::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun addTeamToLeague(leagueId: String, team: Team) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues").document(leagueId)
                    .collection("teams").document(team.id).set(team).await()
                loadTeamsInLeague(leagueId)
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun deleteTeamFromLeague(leagueId: String, teamId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leagues").document(leagueId)
                    .collection("teams").document(teamId).delete().await()
                loadTeamsInLeague(leagueId)
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }
}

data class League(val id: String = "", val name: String = "")
