package dev.moonsetter.shengcat.screens.translator

import android.content.ClipData
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.moonsetter.shengcat.R
import dev.moonsetter.shengcat.model.Language
import dev.moonsetter.shengcat.model.displayName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TranslatorViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current
    val focusManager = LocalFocusManager.current

    Column (
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .padding(16.dp, 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // блок: ввод
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // выбор языка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // список sourceLang
                    LanguageDropdown(
                        selectedLanguage = uiState.sourceLang,
                        onLanguageSelected = { viewModel.onSourceLanguageChange(it) },
                        options = viewModel.getSourceOptions(),
                        modifier = Modifier.weight(1f)
                    )
                    // кнопка смены местами
                    IconButton(
                        onClick = { viewModel.onLanguageSwapClick() },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz ,
                            contentDescription = stringResource(R.string.swap_languages)
                        )
                    }
                    // список targetLang
                    LanguageDropdown(
                        selectedLanguage = uiState.targetLang,
                        onLanguageSelected = { viewModel.onTargetLanguageChange(it) },
                        options = viewModel.getTargetOptions(),
                        modifier = Modifier.weight(1f)
                    )
                }
                // поле ввода
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.onInputTextChange(it) },
                    placeholder = { Text(stringResource(R.string.enter_text)) },
                    minLines = 6,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    // кнопка очистить
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.onClearClick() },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }

                    }
                )
                // ряд кнопок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // кнопка на экран истории
                    IconButton(onClick = { onNavigateToHistory() }) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = stringResource(R.string.translator_history_screen_name),
                        )
                    }
                    // кнопка перевести
                    Button(
                        onClick = { viewModel.onTranslateClick() },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.inputText.isNotBlank() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Translate,
                                contentDescription = stringResource(R.string.translate)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.translate))
                        }
                    }
//                    // кнопка озвучить
//                    IconButton(
//                        onClick = { viewModel.onTtsClick(uiState.inputText, uiState.sourceLang) },
//                    ) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
//                            contentDescription = stringResource(R.string.tts)
//                        )
//                    }
                    // кнопка вставить
                    IconButton(
                        onClick = {
                            scope.launch {
                                val text = clipboardManager.getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()
                                if (!text.isNullOrBlank()) {
                                    viewModel.onInputTextChange(text)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = stringResource(R.string.copy)
                        )
                    }
                }
            }
        }

        // блок: транскрипция
        if (!uiState.pinyin.isNullOrEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // заголовок
                    Text (
                        text = stringResource(R.string.pinyin),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    // транскрипция
                    Text (
                        text = uiState.pinyin,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    // кнопка копировать
                    IconButton(
                        onClick = {
                            scope.launch {
                                val clipEntry = ClipEntry(
                                    ClipData.newPlainText("pinyin", uiState.pinyin)
                                )
                                clipboardManager.setClipEntry(clipEntry)
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(R.string.copy),
                        )
                    }
                }
            }
        }

        // блок: перевод
        if (uiState.translatedText.isNotEmpty() || uiState.translationError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.translationError != null)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // заголовок
                    Text (
                        text = if (uiState.translationError != null) stringResource(R.string.error) else stringResource(R.string.translation),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.translationError != null)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    // перевод
                    Text (
                        text = uiState.translationError ?: uiState.translatedText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.translationError != null)
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    // если ошибки нет, добавляется ряд кнопок
                    if (uiState.translationError == null) {
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // кнопка озвучить
                            IconButton(
                                onClick = { viewModel.onTtsClick(uiState.translatedText, uiState.targetLang) }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = stringResource(R.string.tts)
                                )
                            }
                            // кнопка копировать
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val clipEntry = ClipEntry(
                                            ClipData.newPlainText("translation", uiState.translatedText)
                                        )
                                        clipboardManager.setClipEntry(clipEntry)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(R.string.copy)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    options: List<Language>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // сначала выбранное значение потом остальные
    val orderedOptions = listOf(selectedLanguage) + options.filter { it != selectedLanguage }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = stringResource(selectedLanguage.displayName()),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            orderedOptions.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(language.displayName()))
                    },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}