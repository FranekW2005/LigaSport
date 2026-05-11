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
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Ikony dla bottom bar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.List


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
    onNavigateToLeagues: () -> Unit,
    onLeagueClick: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: LeagueViewModel = viewModel()
) {
    // Która zakładka jest aktywna (domyślnie HOME)
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    // Pobierz dane użytkownika
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: ""
    var userName by remember { mutableStateOf("Użytkowniku") }
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
            userName = doc.getString("userName") ?: "Użytkowniku"
        }
    }

    // Scaffold z bottom barem
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                // Zakładka HOME
                NavigationBarItem(
                    selected = selectedTab == HomeTab.HOME,
                    onClick = { selectedTab = HomeTab.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Główna") },
                    label = { Text("Główna") }
                )

                // Zakładka LIGII
                NavigationBarItem(
                    selected = selectedTab == HomeTab.LEAGUES,
                    onClick = { selectedTab = HomeTab.LEAGUES },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Ligii") },
                    label = { Text("Ligii") }
                )

                // Zakładka DRUŻYNA
                NavigationBarItem(
                    selected = selectedTab == HomeTab.TEAM,
                    onClick = { selectedTab = HomeTab.TEAM },
                    icon = { Icon(Icons.Filled.Groups, contentDescription = "Drużyna") },
                    label = { Text("Drużyna") }
                )

                // Zakładka KALENDARZ
                NavigationBarItem(
                    selected = selectedTab == HomeTab.CALENDAR,
                    onClick = { selectedTab = HomeTab.CALENDAR },
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Kalendarz") },
                    label = { Text("Kalendarz") }
                )

                // Zakładka PROFIL
                NavigationBarItem(
                    selected = selectedTab == HomeTab.PROFILE,
                    onClick = { selectedTab = HomeTab.PROFILE },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
                    label = { Text("Profil") }
                )
            }
        }
    ) { innerPadding ->
        // === ZAWARTOŚĆ AKTYWNEJ ZAKŁADKI ===
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                HomeTab.HOME -> HomeTabContent(
                    userName = userName
                )
                HomeTab.LEAGUES -> LeaguesScreen(
                    onLeagueClick = onLeagueClick,
                    onBack = { selectedTab = HomeTab.HOME },
                    viewModel = viewModel
                )
                HomeTab.TEAM -> TeamTabContent(viewModel = viewModel)
                HomeTab.CALENDAR -> CalendarTabContent()
                HomeTab.PROFILE -> ProfileTabContent(
                    userEmail = userEmail,
                    userName = userName,
                    onUserNameChanged = { newName -> userName = newName },
                    onLogout = onLogout
                )
            }
        }
    }
}

// === ZAWARTOŚĆ ZAKŁADKI GŁÓWNA ===
@Composable
fun HomeTabContent(
    userName: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Powitanie
        Text(
            text = "Witaj, $userName!",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Karta "Twoja Liga"
        Text(
            text = "Twoja Liga",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Liga Podwórkowa 2024", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "4 drużyny • 6 meczów",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sekcja "Najbliższe mecze"
        Text(
            text = "Najbliższe Mecze",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Przykładowy mecz
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FC Orły", fontSize = 16.sp)
                Text("vs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("KS Sport", fontSize = 16.sp)
            }
        }
    }
}

// === ZAWARTOŚĆ ZAKŁADKI DRUŻYNA ===
@Composable
fun TeamTabContent(viewModel: LeagueViewModel) {
    val globalTeams by viewModel.globalTeams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var newTeamName by remember { mutableStateOf("") }
    
    // Stan dla widoku zawodników wybranej drużyny
    var selectedTeamForPlayers by remember { mutableStateOf<Team?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadGlobalTeams()
    }

    if (selectedTeamForPlayers != null) {
        // Widok zawodników w wybranej drużynie
        val team = selectedTeamForPlayers!!
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Drużyna: ${team.name}", fontSize = 24.sp)
                Button(onClick = { selectedTeamForPlayers = null }) {
                    Text("Powrót")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var newPlayerName by remember { mutableStateOf("") }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Nowy zawodnik") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (newPlayerName.isNotBlank()) {
                        viewModel.addPlayerToGlobalTeam(team.id, newPlayerName)
                        newPlayerName = ""
                    }
                }) {
                    Text("Dodaj")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Pobieramy aktualną wersję drużyny z listy globalTeams, aby widzieć zawodników
            val currentTeam = globalTeams.find { it.id == team.id } ?: team
            
            LazyColumn {
                items(currentTeam.players) { player ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(player.name, modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.deletePlayerFromGlobalTeam(currentTeam.id, player) }) {
                                Text("Usuń", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Główny widok zakładki Drużyna - lista drużyn
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Moje Drużyny", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(globalTeams) { team ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedTeamForPlayers = team }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(team.name, fontSize = 18.sp)
                                    Text("Zawodników: ${team.players.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { viewModel.deleteGlobalTeam(team.id) }) {
                                    Text("Usuń", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { showCreateTeamDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Utwórz nową drużynę")
                }
            }
        }
    }

    if (showCreateTeamDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTeamDialog = false },
            title = { Text("Nowa Drużyna") },
            text = {
                OutlinedTextField(
                    value = newTeamName,
                    onValueChange = { newTeamName = it },
                    label = { Text("Nazwa drużyny") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTeamName.isNotBlank()) {
                        viewModel.createGlobalTeam(newTeamName)
                        newTeamName = ""
                        showCreateTeamDialog = false
                    }
                }) {
                    Text("Utwórz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTeamDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
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

// === ZAWARTOŚĆ ZAKŁADKI PROFIL ===
// === ZAWARTOŚĆ ZAKŁADKI PROFIL ===
@Composable
fun ProfileTabContent(
    userEmail: String,
    userName: String,
    onUserNameChanged: (String) -> Unit,
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mój Profil",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Awatar(pierwsza litera imienia)
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
                        text = userName.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 36.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nazwa użytkownika
        if (isEditing) {
            // Tryb edycji
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
                // Przycisk zapisz
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            isSaving = true
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // Zapisz w Firestore
                                firestore.collection("users")
                                    .document(userId)
                                    .update("userName", editedName)
                                    .addOnSuccessListener {
                                        onUserNameChanged(editedName)
                                        isEditing = false
                                        isSaving = false
                                    }
                                    .addOnFailureListener {
                                        isSaving = false
                                    }
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Zapisz")
                    }
                }

                // Przycisk anuluj
                OutlinedButton(
                    onClick = {
                        isEditing = false
                    }
                ) {
                    Text("Anuluj")
                }
            }
        } else {
            // Tryb wyświetlania
            Text(
                text = userName,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    editedName = userName
                    isEditing = true
                }
            ) {
                Text("Zmień nazwę")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Email
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Email:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(userEmail, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Wylogowanie
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wyloguj się")
        }
    }
}
