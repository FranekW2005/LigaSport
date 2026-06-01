package com.example.ligasport.ui.leagues

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

@Composable
fun LeaguesScreen(
    onLeagueClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LeaguesViewModel = viewModel()
) {
    val leagues by viewModel.leagues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUserId = viewModel.currentUserId

    var showCreateDialog by remember { mutableStateOf(false) }
    var newLeagueName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Moje Ligi", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Powrót") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (leagues.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Brak lig", fontSize = 18.sp)
                    Text("Kliknij + aby dodać", fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(leagues) { league ->
                    val isAdmin = league.adminId == currentUserId
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLeagueClick(league.id) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(league.name, fontSize = 18.sp)
                                if (isAdmin) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            "Admin",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                            if (isAdmin) {
                                IconButton(onClick = { viewModel.deleteLeague(league.id) }) {
                                    Text("X", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = { showCreateDialog = true }) {
            Text("+", fontSize = 24.sp)
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nowa Liga") },
            text = {
                OutlinedTextField(
                    value = newLeagueName,
                    onValueChange = { newLeagueName = it },
                    label = { Text("Nazwa ligi") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newLeagueName.isNotBlank()) {
                        viewModel.createLeague(newLeagueName)
                        showCreateDialog = false
                    }
                }) { Text("Utwórz") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Anuluj") }
            }
        )
    }
}