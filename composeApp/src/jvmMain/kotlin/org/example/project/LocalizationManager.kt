package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Serializable
data class LangPackage(val lang: String, val strings: Map<String, String>)

object LocalizationManager {
    private val json = Json { ignoreUnknownKeys = true }
    private val appDir: Path = Path.of(System.getProperty("user.home"), ".kotlinproject", "lang")
    private val runtime = mutableMapOf<String, MutableMap<String, String>>()

    init {
        try {
            if (!appDir.exists()) appDir.createDirectories()
            Files.list(appDir).use { stream ->
                stream.forEach { path ->
                    try { loadFromFile(path, register = true) } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {}
    }

    fun availableLanguages(): List<String> = runtime.keys.sorted()

    fun installedPackages(): List<String> {
        return try {
            // return list of installed package lang codes (files registered in runtime)
            runtime.keys.toList().sorted()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun translate(key: String, lang: String): String? {
        return runtime[lang]?.get(key)
    }

    /**
     * Load JSON content and optionally persist to disk.
     * Returns Pair(success, info). info is lang on success or error code on failure.
     */
    fun loadFromJsonString(content: String, persist: Boolean = true): Pair<Boolean, String> {
        return try {
            val pkg = json.decodeFromString<LangPackage>(content)
            if (pkg.lang.isBlank() || pkg.strings.isEmpty()) {
                false to "invalid-package"
            } else {
                runtime[pkg.lang] = pkg.strings.toMutableMap()
                if (persist) {
                    try {
                        val file = appDir.resolve("${pkg.lang}.json")
                        Files.writeString(file, content)
                    } catch (_: Throwable) { /* ignore file write errors */ }
                }
                true to pkg.lang
            }
        } catch (e: Exception) {
            false to "parse-error"
        }
    }

    fun loadFromFile(path: Path, register: Boolean = true): Boolean {
        return try {
            val content = Files.readString(path)
            val pkg = json.decodeFromString<LangPackage>(content)
            if (pkg.lang.isBlank() || pkg.strings.isEmpty()) return false
            if (register) runtime[pkg.lang] = pkg.strings.toMutableMap()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removePackage(lang: String): Boolean {
        return try {
            runtime.remove(lang)
            val file = appDir.resolve("$lang.json")
            if (Files.exists(file)) Files.delete(file)
            true
        } catch (_: Throwable) {
            false
        }
    }
}
