package com.example.ligasport.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Ikony
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.List

// Modele
import com.example.ligasport.data.models.Team
import com.example.ligasport.data.models.Player

// ViewModele
import com.example.ligasport.ui.teams.TeamViewModel
import com.example.ligasport.ui.profile.ProfileViewModel
import com.example.ligasport.ui.leagues.LeaguesViewModel
import com.example.ligasport.ui.leagueDetail.LeagueDetailViewModel

// Ekrany z innych pakietów
import com.example.ligasport.ui.leagues.LeaguesScreen
import com.example.ligasport.ui.leagueDetail.LeagueDetailScreen

// === ZAKŁADKI BOTTOM BARA ===
enum class HomeTab(val title: String) {
    HOME("Główna"),
    LEAGUES("Ligii"),
    TEAM("Drużyna"),
    CALENDAR("Kalendarz"),
    PROFILE("Profil")
}

@Composable
fun HomeScreen(
    onLeagueClick: (String) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    leaguesViewModel: LeaguesViewModel = viewModel(),
    leagueDetailViewModel: LeagueDetailViewModel = viewModel()
) {
    val selectedTabName by homeViewModel.selectedTab.collectAsState()
    val selectedTab = HomeTab.valueOf(selectedTabName)

    val userName by homeViewModel.userName.collectAsState()

    val selectedLeagueId by homeViewModel.selectedLeagueId.collectAsState()
    val selectedLeagueName by homeViewModel.selectedLeagueName.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { homeViewModel.setSelectedTab(tab.name) },
                        icon = {
                            val icon = when (tab) {
                                HomeTab.HOME -> Icons.Filled.Home
                                HomeTab.LEAGUES -> Icons.AutoMirrored.Filled.List
                                HomeTab.TEAM -> Icons.Filled.Groups
                                HomeTab.CALENDAR -> Icons.Filled.CalendarMonth
                                HomeTab.PROFILE -> Icons.Filled.Person
                            }
                            Icon(icon, contentDescription = tab.title)
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                HomeTab.HOME -> HomeTabContent(
                    userName = userName,
                    viewModel = homeViewModel,
                    selectedLeagueId = selectedLeagueId,
                    selectedLeagueName = selectedLeagueName,
                    onLeagueSelected = { id, name ->
                        homeViewModel.setSelectedLeague(id, name)
                    }
                )

                HomeTab.LEAGUES -> LeaguesScreen(
                    onLeagueClick = onLeagueClick,
                    onBack = { homeViewModel.setSelectedTab(HomeTab.HOME.name) },
                    viewModel = leaguesViewModel
                )

                HomeTab.TEAM -> TeamTabContent(viewModel = teamViewModel)
                HomeTab.CALENDAR -> CalendarTabContent()
                HomeTab.PROFILE -> ProfileTabContent(
                    userEmail = profileViewModel.userEmail,
                    userName = userName,
                    onUserNameChanged = { newName ->
                        profileViewModel.updateUserName(newName) {}
                    },
                    onLogout = onLogout,
                    viewModel = profileViewModel
                )
            }
        }
    }
}

// --- HomeTabContent (używa HomeViewModel) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabContent(
    userName: String,
    viewModel: HomeViewModel,
    selectedLeagueId: String,
    selectedLeagueName: String,
    onLeagueSelected: (String, String) -> Unit
) {
    val leagues by viewModel.leagues.collectAsState()
    val matches by viewModel.matches.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAddMatchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadLeagues() }
    LaunchedEffect(selectedLeagueId) {
        if (selectedLeagueId.isNotEmpty()) viewModel.loadMatches(selectedLeagueId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Witaj, $userName!", fontSize = 28.sp, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Twoja Liga", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }) {
            OutlinedTextField(
                value = selectedLeagueName, onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }) {
                if (leagues.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Brak lig") },
                        onClick = { dropdownExpanded = false })
                } else {
                    leagues.forEach { league ->
                        DropdownMenuItem(text = { Text(league.name) }, onClick = {
                            onLeagueSelected(league.id, league.name)
                            dropdownExpanded = false
                        })
                    }
                }
            }
        }

        if (selectedLeagueId.isNotEmpty()) {
            val isAdmin = viewModel.isUserAdmin(selectedLeagueId)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedLeagueName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (isAdmin) Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "Admin",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showAddMatchDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Dodaj mecz") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Najbliższe Mecze", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        when {
            selectedLeagueId.isEmpty() -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Wybierz ligę, aby zobaczyć najbliższe mecze",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            matches.isEmpty() -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Brak zaplanowanych meczów",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                matches.forEach { match ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(match.homeTeam, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("vs", color = MaterialTheme.colorScheme.primary)
                                Text(match.awayTeam, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${match.date} • ${match.time}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            if (match.homeScore != null && match.awayScore != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${match.homeScore} : ${match.awayScore}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddMatchDialog) {
            var selectedHomeTeam by remember { mutableStateOf("") }
            var selectedAwayTeam by remember { mutableStateOf("") }
            var matchDate by remember { mutableStateOf("") }
            var matchTime by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddMatchDialog = false },
                title = { Text("Nowy Mecz") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = selectedHomeTeam,
                            onValueChange = { selectedHomeTeam = it },
                            label = { Text("Gospodarze") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = selectedAwayTeam,
                            onValueChange = { selectedAwayTeam = it },
                            label = { Text("Goście") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = matchDate,
                            onValueChange = { matchDate = it },
                            label = { Text("Data (DD.MM.RRRR)") },
                            placeholder = { Text("np. 15.05.2026") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = matchTime,
                            onValueChange = { matchTime = it },
                            label = { Text("Godzina (GG:MM)") },
                            placeholder = { Text("np. 18:00") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (selectedHomeTeam.isNotBlank() && selectedAwayTeam.isNotBlank()
                                && selectedHomeTeam != selectedAwayTeam
                            ) {
                                viewModel.addMatch(
                                    selectedLeagueId,
                                    selectedHomeTeam,
                                    selectedAwayTeam,
                                    matchDate,
                                    matchTime
                                )
                                showAddMatchDialog = false
                            }
                        }
                    ) {
                        Text("Dodaj")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddMatchDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }  // koniec Column
}  // koniec HomeTabContent

// --- TeamTabContent (używa TeamViewModel) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamTabContent(viewModel: TeamViewModel) {
    val globalTeams by viewModel.globalTeams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var selectedTeamForPlayers by remember { mutableStateOf<Team?>(null) }
    var selectedPlayerForDetails by remember { mutableStateOf<Player?>(null) }

    LaunchedEffect(Unit) { viewModel.loadGlobalTeams() }

    if (selectedPlayerForDetails != null && selectedTeamForPlayers != null) {
        PlayerDetailScreen(
            player = selectedPlayerForDetails!!,
            onBack = { selectedPlayerForDetails = null },
            onSave = { updatedPlayer ->
                viewModel.updatePlayerInGlobalTeam(
                    selectedTeamForPlayers!!.id,
                    selectedPlayerForDetails!!,
                    updatedPlayer
                )
                selectedPlayerForDetails = null
            }
        )
    } else if (selectedTeamForPlayers != null) {
        val team = selectedTeamForPlayers!!
        var showAddPlayerDialog by remember { mutableStateOf(false) }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Drużyna: ${team.name}", fontSize = 24.sp, modifier = Modifier.weight(1f))
                Button(onClick = { selectedTeamForPlayers = null }) { Text("Powrót") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showAddPlayerDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Dodaj zawodnika") }
            Spacer(modifier = Modifier.height(16.dp))
            val currentTeam = globalTeams.find { it.id == team.id } ?: team
            LazyColumn {
                items(currentTeam.players) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedPlayerForDetails = player }) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(player.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${player.position} • ${player.age} lat",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = {
                                viewModel.deletePlayerFromGlobalTeam(
                                    currentTeam.id,
                                    player
                                )
                            }) {
                                Text("Usuń", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        if (showAddPlayerDialog) {
            AddPlayerDialog(onDismiss = { showAddPlayerDialog = false }, onConfirm = { newPlayer ->
                viewModel.addPlayerToGlobalTeam(team.id, newPlayer)
                showAddPlayerDialog = false
            })
        }
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text("Moje Drużyny", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(globalTeams) { team ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedTeamForPlayers = team }) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(team.name, fontSize = 18.sp)
                                    Text(
                                        "Zawodników: ${team.players.size}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { viewModel.deleteGlobalTeam(team.id) }) {
                                    Text(
                                        "Usuń",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = { showCreateTeamDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) { Text("Utwórz nową drużynę") }
            }
        }
    }

    if (showCreateTeamDialog) {
        var newTeamName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateTeamDialog = false },
            title = { Text("Nowa Drużyna") },
            text = {
                OutlinedTextField(
                    value = newTeamName,
                    onValueChange = { newTeamName = it },
                    label = { Text("Nazwa drużyny") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTeamName.isNotBlank()) {
                        viewModel.createGlobalTeam(newTeamName); showCreateTeamDialog = false
                    }
                }) { Text("Utwórz") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateTeamDialog = false
                }) { Text("Anuluj") }
            }
        )
    }
}

// --- ProfileTabContent (używa ProfileViewModel) ---
@Composable
fun ProfileTabContent(
    userEmail: String,
    userName: String,
    onUserNameChanged: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel
) {
    val isSaving by viewModel.isSaving.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mój Profil", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        userName.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 36.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isEditing) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Nowa nazwa") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            viewModel.updateUserName(editedName) {
                                onUserNameChanged(editedName)
                                isEditing = false
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text(
                        "Zapisz"
                    )
                }
                OutlinedButton(onClick = { isEditing = false }) { Text("Anuluj") }
            }
        } else {
            Text(userName, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                editedName = userName; isEditing = true
            }) { Text("Zmień nazwę") }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Email:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(userEmail, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Wyloguj się") }
    }
}

// === ZAWARTOŚĆ ZAKŁADKI KALENDARZ ===
@Composable
fun CalendarTabContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kalendarz Meczów", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Tu będzie kalendarz z nadchodzącymi meczami", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
    }
}

// === DIALOG DODAWANIA ZAWODNIKA ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (Player) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedPosition by remember { mutableStateOf("Napastnik") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val positions = listOf("Napastnik", "Pomocnik", "Obrońca", "Bramkarz")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj Zawodnika") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Imię i Nazwisko") })
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedPosition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pozycja") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        positions.forEach { pos ->
                            DropdownMenuItem(
                                text = { Text(pos) },
                                onClick = { selectedPosition = pos; expanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Wiek") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Wzrost (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Waga (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(Player(name = name, position = selectedPosition, age = age, height = height, weight = weight))
                }
            }) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// === SZCZEGÓŁY ZAWODNIKA ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(player: Player, onBack: () -> Unit, onSave: (Player) -> Unit) {
    var name by remember { mutableStateOf(player.name) }
    var selectedPosition by remember { mutableStateOf(player.position) }
    var age by remember { mutableStateOf(player.age) }
    var height by remember { mutableStateOf(player.height) }
    var weight by remember { mutableStateOf(player.weight) }

    val positions = listOf("Napastnik", "Pomocnik", "Obrońca", "Bramkarz")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Szczegóły Zawodnika", fontSize = 24.sp)
            Button(onClick = onBack) { Text("Powrót") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Imię i Nazwisko") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedPosition,
                onValueChange = {},
                readOnly = true,
                label = { Text("Pozycja") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                positions.forEach { pos ->
                    DropdownMenuItem(
                        text = { Text(pos) },
                        onClick = { selectedPosition = pos; expanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Wiek") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Wzrost (cm)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Waga (kg)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onSave(player.copy(name = name, position = selectedPosition, age = age, height = height, weight = weight))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zapisz zmiany")
        }
    }
}