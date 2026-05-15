package dev.moonsetter.shengcat.screens.practice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.moonsetter.shengcat.R
import dev.moonsetter.shengcat.data.audio.ToneReference

@Composable
fun PracticePronunciationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PronunciationViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showExitDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
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

    BackHandler(enabled = !uiState.isFinished) {
        if (uiState.isRecording) viewModel.stopRecording()
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

    if (uiState.isFinished) {
        PronunciationResultScreen(
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1).toFloat() / uiState.totalQuestions },
            modifier = Modifier.fillMaxWidth(),
        )

        // слог
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

        // график контура
        uiState.currentSyllable?.let { syllable ->
            PitchContourCard(
                currentTone = syllable.toneNumber,
                recorded = uiState.currentPitchPoints,
                similarity = uiState.lastResult?.similarity
            )
        }

        // результат
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
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TTS
            OutlinedButton(
                onClick = { viewModel.onPlayReferenceClick() },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isRecording && uiState.lastResult == null
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                    contentDescription = null
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.practice_listen))
            }

            // запись
            RecordButton(
                isRecording = uiState.isRecording,
                onClick = {
                    if (uiState.lastResult == null) {
                        if (hasAudioPermission) {
                            viewModel.onRecordClick()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer(alpha = if (uiState.lastResult == null) 1f else 0.5f)
            )
        }

        // кнопка далее
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
    val infiniteTransition = rememberInfiniteTransition(label = "Record")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Record"
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
    currentTone: Int,
    recorded: List<Float>,
    similarity: Float?,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val error = MaterialTheme.colorScheme.error

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
                    .aspectRatio(1.5f)
            ) {
                val w = size.width
                val h = size.height
                val padLeft = 16.dp.toPx()
                val padRight = 36.dp.toPx()
                val padTop = 12.dp.toPx()
                val padBottom = 12.dp.toPx()
                val drawW = w - padLeft - padRight
                val drawH = h - padTop - padBottom

                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = 11.dp.toPx()
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                // график тонов
                (1..4).forEach { tone ->
                    val contour = ToneReference.getContour(tone)
                    val isCurrentTone = tone == currentTone
                    val contourAlpha = if (isCurrentTone) 0.85f else 0.22f
                    val strokeWidth = if (isCurrentTone) 2.5.dp.toPx() else 1.2.dp.toPx()
                    val contourColor = onSurfaceVariant.copy(alpha = contourAlpha)

                    val points = contour.mapIndexed { i, value ->
                        Offset(
                            x = padLeft + i.toFloat() / (contour.size - 1) * drawW,
                            y = padTop + (1f - value) * drawH
                        )
                    }
                    val path = Path()
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val mx = (p0.x + p1.x) / 2f
                        val my = (p0.y + p1.y) / 2f
                        path.quadraticTo(p0.x, p0.y, mx, my)
                    }
                    path.lineTo(points.last().x, points.last().y)

                    drawPath(path = path, color = contourColor, style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ))

                    // подпись в конце линии справа
                    val lastValue = contour.last()
                    val labelX = padLeft + drawW + 4.dp.toPx()
                    val labelY = padTop + (1f - lastValue) * drawH + paint.textSize / 3f

                    paint.color = android.graphics.Color.argb(
                        (contourAlpha * 255).toInt(),
                        (onSurfaceVariant.red * 255).toInt(),
                        (onSurfaceVariant.green * 255).toInt(),
                        (onSurfaceVariant.blue * 255).toInt()
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        tone.toString(),
                        labelX,
                        labelY,
                        paint
                    )
                }

                // записанный контур пользователя
                if (recorded.size >= 2) {
                    val min = recorded.min()
                    val max = recorded.max()
                    val range = (max - min).takeIf { it > 0f } ?: 1f

                    // сглаживание точек
                    val windowSize = 5
                    val smoothed = List(recorded.size) { i ->
                        val from = maxOf(0, i - windowSize / 2)
                        val to = minOf(recorded.size - 1, i + windowSize / 2)
                        recorded.subList(from, to + 1).average().toFloat()
                    }

                    val points = smoothed.mapIndexed { i, value ->
                        val normalized = (value - min) / range
                        Offset(
                            x = padLeft + i.toFloat() / (smoothed.size - 1) * drawW,
                            y = padTop + (1f - normalized) * drawH
                        )
                    }

                    val userPath = Path()
                    userPath.moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val mx = (p0.x + p1.x) / 2f
                        val my = (p0.y + p1.y) / 2f
                        userPath.quadraticTo(p0.x, p0.y, mx, my)
                    }
                    userPath.lineTo(points.last().x, points.last().y)

                    drawPath(
                        path = userPath,
                        color = recordedColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
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