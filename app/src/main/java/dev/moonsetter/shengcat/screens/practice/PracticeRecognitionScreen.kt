package dev.moonsetter.shengcat.screens.practice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.moonsetter.shengcat.R
import dev.moonsetter.shengcat.model.pinyinWithTone

@Composable
fun PracticeRecognitionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecognitionViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !uiState.isFinished) {
        showExitDialog = true
    }

    // выход
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.practice_exit_title)) },
            text = { Text(stringResource(R.string.practice_exit_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.isFinished) {
        RecognitionResultScreen(
            uiState = uiState,
            onRepeat = { viewModel.onRepeatSession() },
            onFinish = onNavigateBack
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // шапка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showExitDialog = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(
                text = "${uiState.currentIndex + 1} / ${uiState.totalQuestions}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1).toFloat() / uiState.totalQuestions },
            modifier = Modifier.fillMaxWidth(),
        )

        // кнопки
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = uiState.currentSyllable?.character ?: "",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )
                // TTS
                OutlinedButton(onClick = { viewModel.onPlayClick() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = stringResource(R.string.tts)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.practice_listen))
                }
            }
        }

        // результаты
        uiState.lastResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.isCorrect)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (result.isCorrect)
                            stringResource(R.string.practice_result_correct)
                        else
                            stringResource(R.string.practice_result_incorrect),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (result.isCorrect)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    if (!result.isCorrect) {
                        Text(
                            text = stringResource(R.string.practice_correct_tone, result.syllable.pinyin),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // кнопки выбора тона
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (1..4).forEach { tone ->
                val isSelected = uiState.selectedTone == tone
                val isCorrectTone = uiState.currentSyllable?.toneNumber == tone
                val hasAnswered = uiState.lastResult != null

                // цвет кнопки после ответа
                val containerColor = when {
                    hasAnswered && isCorrectTone -> MaterialTheme.colorScheme.primaryContainer
                    hasAnswered && isSelected && !isCorrectTone -> MaterialTheme.colorScheme.errorContainer
                    hasAnswered -> MaterialTheme.colorScheme.surfaceVariant // остальные кнопки после ответа
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val contentColor = when {
                    hasAnswered && isCorrectTone -> MaterialTheme.colorScheme.onPrimaryContainer
                    hasAnswered && isSelected && !isCorrectTone -> MaterialTheme.colorScheme.onErrorContainer
                    hasAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Button(
                    onClick = { viewModel.onToneSelected(tone) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !hasAnswered,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                        disabledContainerColor = containerColor,
                        disabledContentColor = contentColor
                    )
                ) {
                    Text(uiState.currentSyllable?.pinyinWithTone(tone) ?: stringResource(R.string.tone_number, tone))
                }
            }
        }

        if (uiState.lastResult != null) {
            Button(
                onClick = { viewModel.onNextClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.isLastQuestion)
                        stringResource(R.string.practice_finish)
                    else
                        stringResource(R.string.practice_next)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RecognitionResultScreen(
    uiState: RecognitionUiState,
    onRepeat: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.practice_session_done),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.practice_session_score, uiState.score, uiState.totalQuestions),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onRepeat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.practice_repeat))
        }
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.practice_finish))
        }
    }
}