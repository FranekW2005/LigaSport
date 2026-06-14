package com.example.ligasport.ui.leagueDetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Ekran szczegółów wybranej ligi. 
 * Pokazuje listę drużyn, które w niej grają i pozwala adminowi na zarządzanie składem ligi.
 */
@Composable
fun LeagueDetailScreen(
    leagueId: String,
    onBack: () -> Unit,
    viewModel: LeagueDetailViewModel = viewModel()
) {
    val teamsInLeague by viewModel.teamsInLeague.collectAsState()
    val globalTeams by viewModel.globalTeams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    var showAddTeamDialog by remember { mutableStateOf(false) }

    // Gdy wchodzimy na ten ekran, odpalamy pobieranie wszystkich potrzebnych danych
    LaunchedEffect(leagueId) {
        viewModel.loadTeamsInLeague(leagueId)
        viewModel.loadGlobalTeams()
        viewModel.checkIfAdmin(leagueId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Górny pasek z tytułem i przyciskiem powrotu
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Szczegóły Ligi", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (isAdmin) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            "Panel Administratora",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Button(onClick = onBack) { Text("Powrót") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Text("Drużyny w tej lidze:", fontSize = 20.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            if (teamsInLeague.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Brak drużyn w lidze", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Lista drużyn w lidze
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(teamsInLeague) { team ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(team.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("Zawodników: ${team.players.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                // Tylko admin może wyrzucić drużynę z ligi
                                if (isAdmin) {
                                    TextButton(onClick = { viewModel.deleteTeamFromLeague(leagueId, team.id) }) {
                                        Text("Usuń", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Przycisk dodawania nowej drużyny (widoczny tylko dla admina)
            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showAddTeamDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 25.dp)
                ) {
                    Text("Dodaj drużynę do ligi")
                }
            }
        }
    }

    // Dialog wyboru drużyny spośród Twoich globalnych składów
    if (showAddTeamDialog) {
        // Pokazujemy tylko te drużyny, których jeszcze nie ma w tej lidze
        val availableTeams = globalTeams.filter { gt -> teamsInLeague.none { it.id == gt.id } }
        AlertDialog(
            onDismissRequest = { showAddTeamDialog = false },
            title = { Text("Dodaj drużynę do ligi") },
            text = {
                if (availableTeams.isEmpty()) {
                    Text("Wszystkie dostępne drużyny są już w tej lidze lub musisz je najpierw stworzyć w zakładce 'Drużyna'.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(availableTeams) { team ->
                            ListItem(
                                headlineContent = { Text(team.name) },
                                supportingContent = { Text("Zawodników: ${team.players.size}") },
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