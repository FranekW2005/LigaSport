package com.example.ligasport.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Ikony
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos

// Modele
import com.example.ligasport.data.models.Team
import com.example.ligasport.data.models.Player
import com.example.ligasport.data.models.Match

// ViewModele
import com.example.ligasport.ui.teams.TeamViewModel
import com.example.ligasport.ui.profile.ProfileViewModel
import com.example.ligasport.ui.leagues.LeaguesViewModel
import com.example.ligasport.ui.leagueDetail.LeagueDetailViewModel
import com.example.ligasport.ui.calendar.CalendarViewModel

// Ekrany z innych pakietów
import com.example.ligasport.ui.leagues.LeaguesScreen
import com.example.ligasport.ui.leagueDetail.LeagueDetailScreen

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Definicja zakładek, które widzimy na dole ekranu.
 */
enum class HomeTab(val title: String) {
    HOME("Główna"),
    LEAGUES("Ligi"),
    TEAM("Drużyna"),
    CALENDAR("Kalendarz"),
    PROFILE("Profil")
}

/**
 * Główny kontener aplikacji po zalogowaniu. 
 * Obsługuje Scaffold z dolnym paskiem nawigacji i przełącza widoki w Boxie.
 */
@Composable
fun HomeScreen(
    onLeagueClick: (String) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    leaguesViewModel: LeaguesViewModel = viewModel(),
    calendarViewModel: CalendarViewModel = viewModel()
) {
    // Obserwujemy, która zakładka jest obecnie kliknięta
    val selectedTabName by homeViewModel.selectedTab.collectAsState()
    val selectedTab = HomeTab.valueOf(selectedTabName)

    val userName by homeViewModel.userName.collectAsState()
    val selectedLeagueId by homeViewModel.selectedLeagueId.collectAsState()
    val selectedLeagueName by homeViewModel.selectedLeagueName.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Dolny pasek nawigacyjny - klasyka Material 3
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
        // Tutaj wyświetlamy treść wybranej zakładki
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
                HomeTab.CALENDAR -> CalendarTabContent(viewModel = calendarViewModel, homeViewModel = homeViewModel)
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

/**
 * Treść pierwszej zakładki - "Główna".
 * Wybór ligi, info o adminie i lista meczów.
 */
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
    val teamsInLeague by viewModel.teamsInLeague.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAddMatchDialog by remember { mutableStateOf(false) }

    /** Czy pokazać dialog wprowadzania wyniku */
    var showResultDialog by remember { mutableStateOf(false) }
    /** Który mecz jest aktualnie edytowany (do wyniku) */
    var selectedMatchForResult by remember { mutableStateOf<com.example.ligasport.data.models.Match?>(null) }
    /** Zmienne dla pól wyniku */
    var homeScoreInput by remember { mutableStateOf("") }
    var awayScoreInput by remember { mutableStateOf("") }

    // Załaduj ligi na starcie
    LaunchedEffect(Unit) { viewModel.loadLeagues() }
    
    // Jeśli zmienimy ligę w dropdownie, pobierz nowe mecze i drużyny
    LaunchedEffect(selectedLeagueId) {
        if (selectedLeagueId.isNotEmpty()) {
            viewModel.loadMatches(selectedLeagueId)
            viewModel.loadTeamsInLeague(selectedLeagueId)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Witaj, $userName!", fontSize = 28.sp, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Twoja Liga", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown do wyboru aktywnej ligi
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

        // Jeśli wybraliśmy ligę, sprawdźmy czy jesteśmy jej adminem
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
            // Tylko admin widzi przycisk dodawania meczu
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

        // Wyświetlanie listy meczów
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
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(matches) { match ->
                        MatchCard(
                            match = match,
                            isAdmin = if (selectedLeagueId.isNotEmpty()) viewModel.isUserAdmin(match.leagueId) else false,
                            onDelete = { viewModel.deleteMatch(match.id, match.leagueId) },
                            onClick = {
                                selectedMatchForResult = match
                                homeScoreInput = match.homeScore?.toString() ?: ""
                                awayScoreInput = match.awayScore?.toString() ?: ""
                                showResultDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Dialog do dodawania nowego spotkania
        if (showAddMatchDialog) {
            AddMatchDialog(
                teams = teamsInLeague,
                onDismiss = { showAddMatchDialog = false },
                onConfirm = { home, away, date, time ->
                    viewModel.addMatch(selectedLeagueId, home, away, date, time)
                    showAddMatchDialog = false
                }
            )
        }

        // Dialog do wpisywania wyniku meczu
        if (showResultDialog && selectedMatchForResult != null) {
            val match = selectedMatchForResult!!

            AlertDialog(
                onDismissRequest = {
                    showResultDialog = false
                    selectedMatchForResult = null
                },
                title = { Text("Wynik meczu", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Nazwy drużyn
                        Text(
                            text = "${match.homeTeam}  vs  ${match.awayTeam}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pola na wynik - obok siebie
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Bramki gospodarzy
                            OutlinedTextField(
                                value = homeScoreInput,
                                onValueChange = { newValue ->
                                    // Pozwól tylko na cyfry
                                    if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                        homeScoreInput = newValue
                                    }
                                },
                                label = { Text(match.homeTeam) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(120.dp)
                            )

                            Text(
                                text = " : ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Bramki gości
                            OutlinedTextField(
                                value = awayScoreInput,
                                onValueChange = { newValue ->
                                    // Pozwól tylko na cyfry
                                    if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                        awayScoreInput = newValue
                                    }
                                },
                                label = { Text(match.awayTeam) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Zapisz wynik do Firestore
                            val homeScore = homeScoreInput.toIntOrNull() ?: 0
                            val awayScore = awayScoreInput.toIntOrNull() ?: 0
                            viewModel.updateMatchResult(
                                matchId = match.id,
                                homeScore = homeScore,
                                awayScore = awayScore,
                                leagueId = selectedLeagueId
                            )
                            showResultDialog = false
                            selectedMatchForResult = null
                        }
                    ) {
                        Text("Zapisz")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showResultDialog = false
                            selectedMatchForResult = null
                        }
                    ) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}

/**
 * Komponent karty pojedynczego meczu.
 */
@Composable
fun MatchCard(match: Match, isAdmin: Boolean, onDelete: () -> Unit, onClick: () -> Unit = {}) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
        .clickable(onClick = onClick) ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(match.homeTeam, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("vs", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp))
                    Text(match.awayTeam, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${match.date} • ${match.time}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                // Wyświetlamy wynik tylko jeśli został wpisany
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
            // Admin ma dodatkowy przycisk usuwania meczu
            if (isAdmin) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Usuń mecz",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog dodawania nowego meczu z wyborem drużyn z listy i natywnymi pickerami daty/godziny.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMatchDialog(
    teams: List<Team>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var selectedHomeTeam by remember { mutableStateOf("") }
    var selectedAwayTeam by remember { mutableStateOf("") }
    var matchDate by remember { mutableStateOf(LocalDate.now()) }
    var matchTime by remember { mutableStateOf(LocalTime.of(18, 0)) }

    var homeExpanded by remember { mutableStateOf(false) }
    var awayExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy Mecz") },
        text = {
            Column {
                Text("Gospodarze:", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(expanded = homeExpanded, onExpandedChange = { homeExpanded = !homeExpanded }) {
                    OutlinedTextField(
                        value = selectedHomeTeam,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = homeExpanded, onDismissRequest = { homeExpanded = false }) {
                        if (teams.isEmpty()) {
                            DropdownMenuItem(text = { Text("Brak drużyn w lidze") }, onClick = {})
                        } else {
                            teams.forEach { team ->
                                DropdownMenuItem(text = { Text(team.name) }, onClick = {
                                    selectedHomeTeam = team.name
                                    homeExpanded = false
                                })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                Text("Goście:", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(expanded = awayExpanded, onExpandedChange = { awayExpanded = !awayExpanded }) {
                    OutlinedTextField(
                        value = selectedAwayTeam,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = awayExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = awayExpanded, onDismissRequest = { awayExpanded = false }) {
                        if (teams.isEmpty()) {
                            DropdownMenuItem(text = { Text("Brak drużyn w lidze") }, onClick = {})
                        } else {
                            teams.forEach { team ->
                                DropdownMenuItem(text = { Text(team.name) }, onClick = {
                                    selectedAwayTeam = team.name
                                    awayExpanded = false
                                })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Data:", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(
                            onClick = {
                                DatePickerDialog(context, { _, y, m, d ->
                                    matchDate = LocalDate.of(y, m + 1, d)
                                }, matchDate.year, matchDate.monthValue - 1, matchDate.dayOfMonth).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(matchDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Godzina:", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(context, { _, h, min ->
                                    matchTime = LocalTime.of(h, min)
                                }, matchTime.hour, matchTime.minute, true).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(matchTime.format(timeFormatter)) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedHomeTeam.isNotBlank() && selectedAwayTeam.isNotBlank() && selectedHomeTeam != selectedAwayTeam) {
                        onConfirm(selectedHomeTeam, selectedAwayTeam, matchDate.format(dateFormatter), matchTime.format(timeFormatter))
                    }
                }
            ) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

/**
 * Zawartość zakładki kalendarza. 
 * Pozwala przeglądać mecze dzień po dniu w widoku miesiąca.
 */
@Composable
fun CalendarTabContent(viewModel: CalendarViewModel, homeViewModel: HomeViewModel) {
    val matches by viewModel.allMatches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) { viewModel.loadAllMatches() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kalendarz Rozgrywek", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Nagłówek kalendarza z przełączaniem miesięcy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Poprzedni miesiąc")
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pl"))} ${currentMonth.year}".replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Następny miesiąc")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rysowanie siatki dni
            MonthView(
                month = currentMonth,
                selectedDate = selectedDate,
                matches = matches,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista meczów pod kalendarzem dla wybranego dnia
            val matchesInSelectedDay = matches.filter { it.date == selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
            
            Text(
                text = "Mecze w dniu ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (matchesInSelectedDay.isEmpty()) {
                Text("Brak meczów tego dnia.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(matchesInSelectedDay) { match ->
                        MatchCard(
                            match = match,
                            isAdmin = homeViewModel.isUserAdmin(match.leagueId),
                            onDelete = { viewModel.deleteMatch(match.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Komponent budujący siatkę dni dla konkretnego miesiąca.
 */
@Composable
fun MonthView(
    month: YearMonth,
    selectedDate: LocalDate,
    matches: List<Match>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1).dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val daysBefore = firstDayOfMonth - 1
    
    val days = (1..daysInMonth).toList()
    val weekdays = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    Column {
        // Nazwy dni tygodnia
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp) 
        ) {
            // Wypełnienie pustych pól przed 1. dniem miesiąca
            items(daysBefore) { Spacer(modifier = Modifier.padding(4.dp)) }

            items(days) { day ->
                val date = month.atDay(day)
                val isSelected = date == selectedDate
                val hasMatch = matches.any { it.date == date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
                val isToday = date == LocalDate.now()

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = if (isToday) 1.dp else 0.dp,
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        // Kropka oznaczająca, że tego dnia są mecze
                        if (hasMatch) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Treść zakładki "Drużyna". 
 * Zarządzanie własnymi drużynami i ich zawodnikami.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamTabContent(viewModel: TeamViewModel) {
    val globalTeams by viewModel.globalTeams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var selectedTeamForPlayers by remember { mutableStateOf<Team?>(null) }
    var selectedPlayerForDetails by remember { mutableStateOf<Player?>(null) }

    LaunchedEffect(Unit) { viewModel.loadGlobalTeams() }

    // Nawigacja wewnątrz zakładki: Szczegóły Gracza -> Lista Graczy -> Lista Drużyn
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

/**
 * Treść zakładki "Profil".
 * Wyświetla dane użytkownika, pozwala zmienić nick i się wylogować.
 */
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
        
        // Avatar z pierwszą literą imienia
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
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Zapisz")
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

/**
 * Dialog do wprowadzania danych nowego zawodnika.
 */
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

/**
 * Ekran szczegółów zawodnika (edycja danych).
 */
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