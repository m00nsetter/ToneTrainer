package dev.moonsetter.shengcat.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.moonsetter.shengcat.R
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.LANG_EN
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.LANG_RU
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_DARK
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_LIGHT
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_SYSTEM

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    // диалог: сброс данных
    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetDismiss() },
            title = { Text(stringResource(R.string.settings_reset_title)) },
            text = { Text(stringResource(R.string.settings_reset_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onResetConfirm() }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onResetDismiss() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // заголовок
        Text(
            text = stringResource(R.string.settings_screen_name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        // блок: язык интерфейса
        SettingsCard(title = stringResource(R.string.settings_language)) {
            SettingsSegmentedButtons(
                options = listOf(
                    LANG_EN to stringResource(R.string.eng_language),
                    LANG_RU to stringResource(R.string.ru_language)
                ),
                selected = uiState.language,
                onSelect = { viewModel.onLanguageSelected(it) }
            )
        }

        // блок: тема оформления
        SettingsCard(title = stringResource(R.string.settings_theme)) {
            SettingsSegmentedButtons(
                options = listOf(
                    THEME_SYSTEM to stringResource(R.string.settings_theme_system),
                    THEME_LIGHT to stringResource(R.string.settings_theme_light),
                    THEME_DARK to stringResource(R.string.settings_theme_dark)
                ),
                selected = uiState.theme,
                onSelect = { viewModel.onThemeSelected(it) }
            )
        }

        // блок: сброс данных
        SettingsCard(title = stringResource(R.string.settings_data)) {
            OutlinedButton(
                onClick = { viewModel.onResetClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_reset_button))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsCard(
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun SettingsSegmentedButtons(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MultiChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                checked = selected == value,
                onCheckedChange = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}