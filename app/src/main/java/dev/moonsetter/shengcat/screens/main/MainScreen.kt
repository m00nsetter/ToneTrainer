package dev.moonsetter.shengcat.screens.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.moonsetter.shengcat.R

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // заголовок
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.app_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.main_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // блок: как пользоваться
        InfoCard(title = stringResource(R.string.main_how_to_use_title)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HowToUseItem(
                    number = "1",
                    text = stringResource(R.string.main_how_to_use_step1)
                )
                HowToUseItem(
                    number = "2",
                    text = stringResource(R.string.main_how_to_use_step2)
                )
                HowToUseItem(
                    number = "3",
                    text = stringResource(R.string.main_how_to_use_step3)
                )
                HowToUseItem(
                    number = "4",
                    text = stringResource(R.string.main_how_to_use_step4)
                )
            }
        }

        // блок: о китайских тонах
        InfoCard(title = stringResource(R.string.main_tones_title)) {
            Text(
                text = stringResource(R.string.main_tones_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            // визуализация четырёх тонов
            ToneShapesRow()
        }

        // блок: о приложении
        InfoCard(title = stringResource(R.string.main_about_title)) {
            Text(
                text = stringResource(R.string.main_about_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
private fun HowToUseItem(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ToneShapesRow() {
    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // четыре карточки тонов
        listOf(
            Triple("ā", stringResource(R.string.main_tone1_name), 1),
            Triple("á", stringResource(R.string.main_tone2_name), 2),
            Triple("ǎ", stringResource(R.string.main_tone3_name), 3),
            Triple("à", stringResource(R.string.main_tone4_name), 4)
        ).forEach { (mark, name, toneNumber) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = mark,
                        style = MaterialTheme.typography.titleLarge,
                        color = primary
                    )
                    // контур тона
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val pad = 4.dp.toPx()
                        val path = Path()

                        when (toneNumber) {
                            // тон 1: ровный высокий
                            1 -> {
                                path.moveTo(pad, pad)
                                path.lineTo(w - pad, pad)
                            }
                            // тон 2: восходящий
                            2 -> {
                                path.moveTo(pad, h - pad)
                                path.lineTo(w - pad, pad)
                            }
                            // тон 3: нисходяще-восходящий
                            3 -> {
                                path.moveTo(pad, h * 0.3f)
                                path.cubicTo(
                                    w * 0.3f, h - pad,
                                    w * 0.6f, h - pad,
                                    w - pad, h * 0.4f
                                )
                            }
                            // тон 4: резко нисходящий
                            4 -> {
                                path.moveTo(pad, pad)
                                path.lineTo(w - pad, h - pad)
                            }
                        }

                        drawPath(
                            path = path,
                            color = primary,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                }
            }
        }
    }
}