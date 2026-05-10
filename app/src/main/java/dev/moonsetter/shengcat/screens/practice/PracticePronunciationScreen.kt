package dev.moonsetter.shengcat.screens.practice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.moonsetter.shengcat.R
import dev.moonsetter.shengcat.data.audio.ToneReference
import dev.moonsetter.shengcat.model.SyllableItem

@Composable
fun PracticePronunciationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PronunciationViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showExitDialog by remember { mutableStateOf(false) }
    var showRepeatDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterVertically as Alignment
        ) {
            CircularProgressIndicator()
        }
        return
    }

    BackHandler(enabled = !uiState.isFinished) {
        if (uiState.isRecording) viewModel.stopRecording()
        showExitDialog = true
    }

    // диалог: выйти из практики
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

    // диалог: повторить сессию
    if (showRepeatDialog) {
        AlertDialog(
            onDismissRequest = { showRepeatDialog = false },
            title = { Text(stringResource(R.string.practice_repeat_title)) },
            confirmButton = {
                TextButton(onClick = {
                    showRepeatDialog = false
                    viewModel.onRepeatSession()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRepeatDialog = false
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (uiState.isFinished) {
        PronunciationResultScreen(
            uiState = uiState,
            onRepeat = { showRepeatDialog = true },
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
        // шапка: назад + прогресс
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (uiState.isRecording) viewModel.stopRecording()
                showExitDialog = true
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(
                text = "${uiState.currentIndex + 1} / ${uiState.totalQuestions}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // блок: слог
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.currentSyllable?.let { syllable ->
                    Text(
                        text = syllable.character,
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = syllable.pinyin,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.tone_number, syllable.toneNumber),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // блок: график контура
        uiState.currentSyllable?.let { syllable ->
            PitchContourCard(
                reference = ToneReference.getContour(syllable.toneNumber),
                recorded = uiState.currentPitchPoints,
                similarity = uiState.lastResult?.similarity
            )
        }

        // блок: результат
        uiState.lastResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.similarity >= 0.6f)
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
                        text = if (result.similarity >= 0.6f)
                            stringResource(R.string.practice_result_correct)
                        else
                            stringResource(R.string.practice_result_incorrect),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (result.similarity >= 0.6f)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.practice_similarity, (result.similarity * 100).toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (result.similarity >= 0.6f)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // ряд кнопок: озвучить + запись
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // кнопка: прослушать эталон
            OutlinedButton(
                onClick = { viewModel.onPlayReferenceClick() },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isRecording
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                    contentDescription = stringResource(R.string.tts)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.practice_listen))
            }

            // кнопка: запись
            RecordButton(
                isRecording = uiState.isRecording,
                onClick = { viewModel.onRecordClick() },
                modifier = Modifier.weight(1f)
            )
        }

        // кнопка: далее (только после результата)
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
private fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "запись")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "мигание"
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Outlined.MicOff else Icons.Outlined.Mic,
            contentDescription = stringResource(R.string.practice_record),
            modifier = if (isRecording) Modifier.graphicsLayer { this.alpha = alpha } else Modifier
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = if (isRecording)
                stringResource(R.string.practice_stop)
            else
                stringResource(R.string.practice_record)
        )
    }
}

@Composable
private fun PitchContourCard(
    reference: FloatArray,
    recorded: List<Float>,
    similarity: Float?,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val error = MaterialTheme.colorScheme.error

    // цвет записанного контура зависит от схожести
    val recordedColor = when {
        similarity == null -> primary
        similarity >= 0.6f -> Color(0xFF4CAF50)
        else -> error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.practice_pitch_chart),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height
                val padding = 12.dp.toPx()

                // эталонный контур (пунктир, серый)
                if (reference.isNotEmpty()) {
                    val refPath = Path()
                    reference.forEachIndexed { i, value ->
                        val x = padding + i.toFloat() / (reference.size - 1) * (w - 2 * padding)
                        val y = h - padding - value * (h - 2 * padding)
                        if (i == 0) refPath.moveTo(x, y) else refPath.lineTo(x, y)
                    }
                    drawPath(
                        path = refPath,
                        color = onSurfaceVariant.copy(alpha = 0.4f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // записанный контур пользователя
                if (recorded.size >= 2) {
                    val min = recorded.min()
                    val max = recorded.max()
                    val range = (max - min).takeIf { it > 0f } ?: 1f

                    val userPath = Path()
                    recorded.forEachIndexed { i, value ->
                        val normalized = (value - min) / range
                        val x = padding + i.toFloat() / (recorded.size - 1) * (w - 2 * padding)
                        val y = h - padding - normalized * (h - 2 * padding)
                        if (i == 0) userPath.moveTo(x, y) else userPath.lineTo(x, y)
                    }
                    drawPath(
                        path = userPath,
                        color = recordedColor,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                // оси
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.2f),
                    start = Offset(padding, padding),
                    end = Offset(padding, h - padding),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.2f),
                    start = Offset(padding, h - padding),
                    end = Offset(w - padding, h - padding),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // легенда
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(
                    color = onSurfaceVariant.copy(alpha = 0.4f),
                    label = stringResource(R.string.practice_reference_contour)
                )
                LegendItem(
                    color = recordedColor,
                    label = stringResource(R.string.practice_your_contour)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(16.dp, 2.dp)) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2.dp.toPx()
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PronunciationResultScreen(
    uiState: PronunciationUiState,
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

        // разбивка по тонам
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.practice_tone_breakdown),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                (1..4).forEach { tone ->
                    val toneResults = uiState.sessionResults.filter { it.syllable.toneNumber == tone }
                    if (toneResults.isNotEmpty()) {
                        val correct = toneResults.count { it.similarity >= 0.6f }
                        Text(
                            text = stringResource(R.string.practice_tone_score, tone, correct, toneResults.size),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

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