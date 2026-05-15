package dev.moonsetter.shengcat.screens.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.moonsetter.shengcat.R
import dev.moonsetter.shengcat.data.db.entity.PracticeResultEntity
import dev.moonsetter.shengcat.model.PracticeMode
import java.time.format.DateTimeFormatter

@Composable
fun PracticeHistoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PracticeHistoryViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_practice_history)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    viewModel.onClearHistory()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(
                text = stringResource(R.string.practice_history_screen_name),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = { showClearDialog = true },
                enabled = uiState.results.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.clear_practice_history)
                )
            }
        }

        if (uiState.results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_practice_history),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.results) { result ->
                    PracticeHistoryCard(result = result)
                }
            }
        }
    }
}

@Composable
private fun PracticeHistoryCard(
    result: PracticeResultEntity,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        when (result.practiceMode) {
                            PracticeMode.PRONUNCIATION -> R.string.practice_pronunciation_screen_name
                            PracticeMode.RECOGNITION -> R.string.practice_recognition_screen_name
                        }
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = result.date.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.practice_session_score, result.score, result.totalQuestions),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}