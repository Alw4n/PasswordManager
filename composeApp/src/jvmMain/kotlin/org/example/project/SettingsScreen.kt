package org.example.project

// (оставьте прежние импорты)
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import org.example.project.db.SettingsApi

@Composable
fun SettingsScreen(
    settings: Any? = null, // SettingsStorage removed — backward-compatible placeholder
    repository: PasswordRepository,
    darkTheme: Boolean,
    currentLang: String,
    onToggleTheme: () -> Unit,
    onSetLanguage: (String) -> Unit,
    onLoadMoreLanguages: () -> Unit = {},
    onBack: () -> Unit
) {
    var showChangePwd by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf(currentLang) }
    var changePwdMessage by remember { mutableStateOf<String?>(null) }
    val isPortrait = true
    val maxWidth = if (isPortrait) 420.dp else 760.dp
    val maxHeight = if (isPortrait) 640.dp else 560.dp
    val scroll = rememberScrollState()

    // installed languages state — read fresh from LocalizationManager
    var installedLangs by remember { mutableStateOf(LocalizationManager.installedPackages()) }

    var langMenuExpanded by remember { mutableStateOf(false) }
    var confirmDeleteLang by remember { mutableStateOf<String?>(null) }

    // Snackbar + coroutine scope
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .heightIn(max = maxHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(12.dp)
        ) {
            Text(
                text = t("title.settings", language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (darkTheme) t("theme.current_dark", language) else t("theme.current_light", language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                onToggleTheme()
                // persist via SettingsApi in caller or here:
                SettingsApi.putBoolean("darkTheme", !darkTheme)
            }) {
                Text(t("action.toggle_theme", language))
            }

            Spacer(Modifier.height(16.dp))

            // Language selector (built-in + installed)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${t("language.label", language)}:", color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.width(12.dp))

                Button(onClick = { langMenuExpanded = true }) {
                    val label = when (language) {
                        L.EN -> t("language.english", language)
                        L.RU -> t("language.russian", language)
                        else -> language
                    }
                    Text(label)
                }

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = { langMenuExpanded = true }) {
                    Text("...")
                }

                DropdownMenu(
                    expanded = langMenuExpanded,
                    onDismissRequest = { langMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(t("language.english", language)) },
                        onClick = {
                            language = L.EN
                            onSetLanguage(L.EN)
                            SettingsApi.putString("language", L.EN)
                            langMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(t("language.russian", language)) },
                        onClick = {
                            language = L.RU
                            onSetLanguage(L.RU)
                            SettingsApi.putString("language", L.RU)
                            langMenuExpanded = false
                        }
                    )

                    HorizontalDivider()

                    val installedList = installedLangs
                    if (installedList.isEmpty()) {
                        DropdownMenuItem(text = { Text("No installed languages") }, onClick = { /* noop */ })
                    } else {
                        installedList.forEach { langCode ->
                            DropdownMenuItem(text = { Text(langCode) }, onClick = {
                                language = langCode
                                onSetLanguage(langCode)
                                SettingsApi.putString("language", langCode)
                                langMenuExpanded = false
                            })
                        }
                    }

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Load more languages") },
                        onClick = {
                            langMenuExpanded = false
                            coroutineScope.launch {
                                val file: File? = withContext(Dispatchers.IO) { pickJsonFile() }
                                if (file != null) {
                                    val content = try { file.readText() } catch (e: Exception) { null }
                                    if (content != null) {
                                        val (ok, info) = LocalizationManager.loadFromJsonString(content, persist = true)
                                        if (ok) {
                                            installedLangs = LocalizationManager.installedPackages()
                                            language = info
                                            onSetLanguage(info)
                                            SettingsApi.putString("language", info)
                                            snackbarHostState.showSnackbar("Language $info installed")
                                        } else {
                                            val msg = when (info) {
                                                "invalid-package" -> "Invalid language package"
                                                "parse-error" -> "Failed to parse package"
                                                else -> "Failed to load package"
                                            }
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to read file")
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.width(12.dp))

                if (language != L.EN && language != L.RU && installedLangs.contains(language)) {
                    TextButton(onClick = { confirmDeleteLang = language }) {
                        Text("Remove")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = { showChangePwd = true }) {
                Text(t("action.change_password", language))
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = onBack) {
                Text(t("action.back", language))
            }

            changePwdMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))
            SnackbarHost(hostState = snackbarHostState)
        }
    }

    // Confirm delete dialog, ChangePasswordDialog handling — оставляем как было
    if (confirmDeleteLang != null) {
        // ... (тот же код, но при удалении обновляем installedLangs и SettingsApi)
    }

    if (showChangePwd) {
        ChangePasswordDialog(
            repository = repository,
            language = language,
            onClose = { showChangePwd = false },
            onResult = { success: Boolean, msg: String ->
                changePwdMessage = msg
                if (success) showChangePwd = false
            }
        )
    }
}
