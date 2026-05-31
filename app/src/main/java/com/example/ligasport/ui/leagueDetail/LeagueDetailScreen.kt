package com.example.ligasport.ui.leagueDetail

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

@Composable
fun LeagueDetailScreen(
    leagueId: String,
    onBack: () -> Unit,
    viewModel: LeagueDetailViewModel = viewModel()
) {
    val teamsInLeague by viewModel.teamsInLeague.collectAsState()
    val globalTeams by viewModel.globalTeams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddTeamDialog by remember { mutableStateOf(false) }

    LaunchedEffect(leagueId) {
        viewModel.loadTeamsInLeague(leagueId)
        viewModel.loadGlobalTeams()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Szczegóły Ligi", fontSize = 24.sp)
            Button(onClick = onBack) { Text("Powrót") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text("Drużyny w tej lidze:", fontSize = 20.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(teamsInLeague) { team ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(team.name, fontSize = 18.sp)
                                Text("Zawodników: ${team.players.size}", fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deleteTeamFromLeague(leagueId, team.id) }) {
                                Text("Usuń", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showAddTeamDialog = true }, modifier = Modifier.fillMaxWidth().padding(bottom = 25.dp)) {
                Text("Dodaj drużynę do ligi")
            }
        }
    }

    if (showAddTeamDialog) {
        val availableTeams = globalTeams.filter { gt -> teamsInLeague.none { it.name == gt.name } }
        AlertDialog(
            onDismissRequest = { showAddTeamDialog = false },
            title = { Text("Wybierz drużynę") },
            text = {
                if (availableTeams.isEmpty()) {
                    Text("Brak dostępnych drużyn. Stwórz je w zakładce 'Drużyna'.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(availableTeams) { team ->
                            ListItem(
                                headlineContent = { Text(team.name) },
                                modifier = Modifier.clickable {
                                    viewModel.addTeamToLeague(leagueId, team)
                                    showAddTeamDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddTeamDialog = false }) { Text("Zamknij") } }
        )
    }
}