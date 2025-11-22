package org.example.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import org.example.project.db.Db
import org.example.project.db.SettingsRepository
import org.example.project.db.SettingsApi
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    // начальные значения — русский по умолчанию
    var darkTheme by remember { mutableStateOf(false) }
    var currentLang by remember { mutableStateOf(L.RU) }

    var showSettings by remember { mutableStateOf(false) }
    var loggedIn by remember { mutableStateOf(false) }
    var showingSetPassword by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Инициализация DB и SettingsApi; затем читаем реальные значения в состояния
    LaunchedEffect(Unit) {
        Db.init()
        SettingsRepository.init()
        SettingsApi.init()

        // Если в БД есть значение языка — используем его, иначе остаёмся на RU
        currentLang = SettingsApi.getString("language", L.RU) ?: L.RU
        darkTheme = SettingsApi.getBoolean("darkTheme", false)

        // проверяем наличие мастер-пароля
        val repo = DbPasswordRepositoryAdapter()
        showingSetPassword = !repo.hasMasterPassword()
    }

    // repository используем адаптер старого интерфейса
    val repository = remember { DbPasswordRepositoryAdapter() }

    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
        if (!loggedIn) {
            if (showingSetPassword) {
                // <- SetPasswordScreen не принимает parameter 'language' в твоей реализации,
                // оставляем вызов без передачи language
                SetPasswordScreen(onSet = { pw ->
                    val ok = repository.setMasterPassword(pw)
                    if (ok) showingSetPassword = false
                })
            } else {
                AuthScreen(
                    onLogin = { pw ->
                        val ok = repository.open(pw)
                        if (ok) loggedIn = true
                    },
                    language = currentLang,
                    modifier = Modifier.fillMaxSize().safeContentPadding()
                )
            }
        } else {
            MainScreen(
                repository = repository,
                language = currentLang,
                onLock = {
                    repository.close()
                    loggedIn = false
                },
                onOpenSettings = { showSettings = true },
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxSize().safeContentPadding()
            )

            if (showSettings) {
                ModalOverlay(
                    onDismiss = { showSettings = false },
                    clickOutsideDismiss = true,
                    blurRadiusDp = 6.dp
                ) {
                    SettingsScreen(
                        settings = null,
                        repository = repository,
                        darkTheme = darkTheme,
                        currentLang = currentLang,
                        onToggleTheme = {
                            darkTheme = !darkTheme
                            SettingsApi.putBoolean("darkTheme", darkTheme)
                        },
                        onSetLanguage = { lang ->
                            currentLang = lang
                            SettingsApi.putString("language", lang)
                        },
                        onLoadMoreLanguages = {},
                        onBack = { showSettings = false }
                    )
                }
            }
        }
    }
}
