package com.example.ligasport

import android.R
import android.graphics.Paint
import android.widget.Space
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
 * Ekran listy lig.
 */
@Composable
fun LeaguesScreen(
    onLeagueClick: (String) -> Unit,
    viewModel: LeagueViewModel = viewModel()
) {
    // Dane z viewModel
    // collectAsState zmiania StateFlow na State dla Compose
    // Dzięki temu Compose wie kiedy przerysować UI
    val leagues by viewModel.leagues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Stan dialogu tworzenia ligi
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newLeagueName by remember { mutableStateOf("") }

    // Interfejs
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Moje Ligi",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Loading
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Pusta lista
        else if (leagues.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Brak lig",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Kliknij + aby dodać pierwszą ligę",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // Lista lig
        else {
            LazyColumn { // tylko widoczne elementy są renderowane
                items(leagues) { league ->
                    // Karta ligi
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLeagueClick(league.id) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = league.name,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            TextButton(
                                onClick = {
                                    viewModel.deleteLeague(league.id)
                                },
                                modifier = Modifier.defaultMinSize(
                                    minWidth = 40.dp,
                                    minHeight = 40.dp
                                )
                            ) {
                                Text(
                                    text = "X",
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    // Przycisk dodawania ligi
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd // Prawy dolny róg
    ) {
        FloatingActionButton(
            onClick = {
                newLeagueName = ""
                showCreateDialog = true
            }
        ) {
            Text("+", fontSize = 24.sp)
        }
    }

    // Dialog tworzenia ligi
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
                TextButton(
                    onClick = {
                        if (newLeagueName.isNotBlank()) {
                            viewModel.createLeague(newLeagueName)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Utwórz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}