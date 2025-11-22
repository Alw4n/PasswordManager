package org.example.project.db

import kotlin.collections.mutableMapOf

object SettingsApi {
    private val cache: MutableMap<String, String> = mutableMapOf()

    /**
     * Загружаем все настройки из БД в память.
     * Вызывать при старте приложения после SettingsRepository.init()
     */
    fun init() {
        cache.clear()
        cache.putAll(SettingsRepository.getAll().orEmpty())
    }

    /**
     * Возвращает строковое значение или default
     */
    fun getString(key: String, default: String? = null): String? =
        cache[key] ?: default

    /**
     * Возвращает Boolean значение или default
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean =
        cache[key]?.toBooleanStrictOrNull() ?: default

    /**
     * Положить строку в кеш и БД
     */
    fun putString(key: String, value: String) {
        cache[key] = value
        SettingsRepository.put(key, value)
    }

    /**
     * Положить boolean в кеш и БД
     */
    fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
    }

    /**
     * Удалить ключ из кеша и БД
     */
    fun remove(key: String) {
        cache.remove(key)
        SettingsRepository.delete(key)
    }

    /**
     * Удобный метод получения всех настроек (копия)
     */
    fun all(): Map<String, String> = HashMap(cache)
}
