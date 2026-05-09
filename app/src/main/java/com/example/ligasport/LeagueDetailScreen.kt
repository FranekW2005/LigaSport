package com.example.ligasport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Ekran szczegółów ligi, umożliwiający zarządzanie drużynami i ich zawodnikami (Dane z Firebase).
 */
@Composable
fun LeagueDetailScreen(
    leagueId: String,
    onBack: () -> Unit,
    viewModel: LeagueViewModel = viewModel()
) {
    // Obserwowanie drużyn z ViewModel
    val teams by viewModel.teams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Załaduj drużyny po wejściu na ekran
    LaunchedEffect(leagueId) {
        viewModel.loadTeams(leagueId)
    }
    
    // Stan przechowujący aktualnie wybraną drużynę (do widoku zawodników)
    // Szukamy w aktualnej liście teams, aby mieć świeże dane po aktualizacjach
    var selectedTeamId by remember { mutableStateOf<String?>(null) }
    val selectedTeam = teams.find { it.id == selectedTeamId }

    if (selectedTeam == null) {
        // --- WIDOK 1: LISTA DRUŻYN W LIDZE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Szczegóły Ligi", fontSize = 24.sp)
                Button(onClick = onBack) {
                    Text("Powrót")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Formularz dodawania nowej drużyny
                var newTeamName by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTeamName,
                        onValueChange = { newTeamName = it },
                        label = { Text("Nazwa nowej drużyny") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTeamName.isNotBlank()) {
                                viewModel.addTeam(leagueId, newTeamName)
                                newTeamName = ""
                            }
                        }
                    ) {
                        Text("Dodaj")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Drużyny:",
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Lista drużyn
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(teams) { team ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTeamId = team.id }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = team.name, fontSize = 18.sp)
                                    Text(
                                        text = "Zawodników: ${team.players.size}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Przycisk usuwania drużyny
                                TextButton(onClick = { viewModel.deleteTeam(leagueId, team.id) }) {
                                    Text("Usuń", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- WIDOK 2: LISTA ZAWODNIKÓW W WYBRANEJ DRUŻYNIE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Drużyna: ${selectedTeam.name}", fontSize = 24.sp)
                Button(onClick = { selectedTeamId = null }) {
                    Text("Zamknij")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Formularz dodawania zawodnika
            var newPlayerName by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Imię i nazwisko") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newPlayerName.isNotBlank()) {
                            viewModel.addPlayer(leagueId, selectedTeam.id, newPlayerName)
                            newPlayerName = ""
                        }
                    }
                ) {
                    Text("Dodaj")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Zawodnicy:",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Lista zawodników
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(selectedTeam.players) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = player.name,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Przycisk usuwania zawodnika
                            TextButton(
                                onClick = { 
                                    viewModel.deletePlayer(leagueId, selectedTeam.id, player)
                                }
                            ) {
                                Text("Usuń", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
