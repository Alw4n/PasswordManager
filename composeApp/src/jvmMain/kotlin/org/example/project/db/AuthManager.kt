package org.example.project.db

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

/**
 * Высокоуровневый менеджер авторизации/masterKey.
 *
 * Поддерживаем простой рабочий сценарий:
 * - WrapAuth хранит/читает зашитрованный masterKey в Settings (ключ "auth.wrapped_master")
 * - AuthManager держит расшифрованный masterKey в памяти (SecretKey) пока сессия открыта
 *
 * Flows:
 * - setRememberedMaster(password): сгенерирует новый masterKey и сохранит его в Settings через WrapAuth
 * - tryUnlock(password): попробует по введённому паролю распаковать wrapped master и если успешно —
 *   сохранит masterKey в памяти (и вернёт true)
 * - isUnlocked/lock/clearRemembered/hasRemembered
 *
 * Важно: AuthManager не хранит пароли в БД и не хранит мастер-ключ на диске в открытом виде.
 */
object AuthManager {
    private const val SETTINGS_KEY_WRAPPED = "auth.wrapped_master"

    // Храним masterKey в памяти как SecretKey; сбрасываем при lock()
    @Volatile
    private var masterKey: SecretKey? = null

    /**
     * Проверяет, есть ли "запомненный" wrapped masterKey в Settings
     */
    fun hasRemembered(): Boolean {
        return SettingsRepository.get(SETTINGS_KEY_WRAPPED) != null
    }

    /**
     * Создаёт новый masterKey и сохраняет зашифрованный/обёрнутый вариант в Settings.
     * Вызывать когда пользователь выбирает "Запомнить на этом устройстве".
     *
     * После вызова Wrapping менеджер не раскрывает masterKey в память — только сохраняет wrapped blob.
     * Если нужно сразу открыть сессию, вызови tryUnlock(password) после setRememberedMaster.
     */
    fun setRememberedMaster(password: CharArray) {
        WrapAuth.setRememberedMaster(password)
    }

    /**
     * Попытка разблокировать приложение с помощью введённого пароля.
     * Если успешна — masterKey сохраняется в памяти и возвращается true.
     */
    fun tryUnlock(password: CharArray): Boolean {
        val bytes = WrapAuth.tryUnlock(password) ?: return false
        // Конвертируем в SecretKey и держим в памяти
        masterKey = SecretKeySpec(bytes, "AES")
        // очищаем временный массив bytes по возможности
        secureWipe(bytes)
        return true
    }

    /**
     * Возвращает SecretKey если сессия открыта, иначе null.
     * Не копирует ключ — возвращает ссылку; вызывающий код не должен модифицировать ключ.
     */
    fun getMasterKey(): SecretKey? = masterKey

    /**
     * Заблокировать приложение: стереть masterKey из памяти.
     */
    fun lock() {
        val mk = masterKey ?: return
        secureWipe(mk.encoded)
        masterKey = null
    }

    /**
     * Удалить запомненный wrapped masterKey из Settings (фактически "забыть устройство")
     */
    fun clearRemembered() {
        WrapAuth.clearRemembered()
    }

    /**
     * Смена пароля, который использовался для оборачивания wrapped masterKey.
     *
     * Алгоритм:
     * - распаковать текущий wrapped master ключ с помощью oldPassword (через WrapAuth.unwrapMaster)
     * - заново обернуть его новым паролем и сохранить
     *
     * Возвращает true при успехе, false при неверном старом пароле или отсутствии wrapped ключа.
     *
     * Если у тебя хранится masterKey только в памяти (пользователь ранее вызвал tryUnlock),
     * можно также вызвать changeWrapWithInMemoryKey(newPassword) чтобы не требовать ввод старого пароля.
     */
    fun changePassword(oldPassword: CharArray, newPassword: CharArray): Boolean {
        val wrapped = SettingsRepository.get(SETTINGS_KEY_WRAPPED) ?: return false
        val oldMaster = WrapAuth.unwrapMaster(wrapped, oldPassword) ?: return false
        val newWrapped = WrapAuth.wrapMaster(oldMaster, newPassword)
        // перезаписываем в Settings
        SettingsRepository.put(SETTINGS_KEY_WRAPPED, newWrapped)
        secureWipe(oldMaster)
        return true
    }

    /**
     * Если masterKey уже расшифрован и держится в памяти, можно обернуть его новым паролем
     * без знания старого пароля и сохранить в Settings.
     */
    fun changeWrapWithInMemoryKey(newPassword: CharArray): Boolean {
        val mk = masterKey ?: return false
        val out = WrapAuth.wrapMaster(mk.encoded, newPassword)
        SettingsRepository.put(SETTINGS_KEY_WRAPPED, out)
        return true
    }

    /**
     * Утилита для безопасного затирания массивов байт.
     */
    private fun secureWipe(bytes: ByteArray?) {
        bytes?.fill(0)
    }
}
