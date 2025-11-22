package org.example.project

object L {
    const val EN = "en"
    const val RU = "ru"
}

private val strings = mapOf(
    // settings, theme, language, changepwd, auth (существующие)
    "title.settings" to mapOf(L.EN to "Settings", L.RU to "Настройки"),
    "theme.current_light" to mapOf(L.EN to "Current theme: Light", L.RU to "Текущая тема: Светлая"),
    "theme.current_dark" to mapOf(L.EN to "Current theme: Dark", L.RU to "Текущая тема: Тёмная"),
    "action.toggle_theme" to mapOf(L.EN to "Toggle theme", L.RU to "Переключить тему"),
    "language.label" to mapOf(L.EN to "Language", L.RU to "Язык"),
    "language.english" to mapOf(L.EN to "English", L.RU to "Английский"),
    "language.russian" to mapOf(L.EN to "Russian", L.RU to "Русский"),
    "action.change_password" to mapOf(L.EN to "Change master password", L.RU to "Сменить мастер-пароль"),
    "action.back" to mapOf(L.EN to "Back", L.RU to "Назад"),
    "changepwd.title" to mapOf(L.EN to "Change master password", L.RU to "Сменить мастер-пароль"),
    "changepwd.current" to mapOf(L.EN to "Current password", L.RU to "Текущий пароль"),
    "changepwd.new" to mapOf(L.EN to "New password", L.RU to "Новый пароль"),
    "changepwd.repeat" to mapOf(L.EN to "Repeat new password", L.RU to "Повторите новый пароль"),
    "changepwd.change" to mapOf(L.EN to "Change", L.RU to "Изменить"),
    "changepwd.cancel" to mapOf(L.EN to "Cancel", L.RU to "Отмена"),
    "changepwd.empty_error" to mapOf(L.EN to "New password cannot be empty", L.RU to "Новый пароль не может быть пустым"),
    "changepwd.nomatch_error" to mapOf(L.EN to "New passwords do not match", L.RU to "Пароли не совпадают"),
    "changepwd.incorrect_error" to mapOf(L.EN to "Current password is incorrect", L.RU to "Текущий пароль неверен"),
    "auth.title" to mapOf(L.EN to "Master password", L.RU to "Мастер-пароль"),
    "auth.password_label" to mapOf(L.EN to "Password", L.RU to "Пароль"),
    "auth.enter" to mapOf(L.EN to "Enter", L.RU to "Войти"),
    "auth.show" to mapOf(L.EN to "Show", L.RU to "Показать"),
    "auth.hide" to mapOf(L.EN to "Hide", L.RU to "Скрыть"),

    // main
    "main.title" to mapOf(L.EN to "Password Manager", L.RU to "Менеджер паролей"),
    "main.entries" to mapOf(L.EN to "Entries", L.RU to "Записи"),
    "main.add_entry" to mapOf(L.EN to "Add entry", L.RU to "Добавить запись"),
    "main.lock" to mapOf(L.EN to "Lock", L.RU to "Блокировка"),

    // AddEntryDialog / form fields (добавлены)
    "entry.add_title" to mapOf(L.EN to "Add entry", L.RU to "Добавить запись"),
    "entry.edit_title" to mapOf(L.EN to "Edit entry", L.RU to "Редактировать запись"),
    "entry.untitled" to mapOf(L.EN to "Untitled", L.RU to "Без названия"),

    "field.title" to mapOf(L.EN to "Title", L.RU to "Название"),
    "validation.title_required" to mapOf(L.EN to "Title is required", L.RU to "Название обязательно"),
    "field.title_optional" to mapOf(L.EN to "Title", L.RU to "Название"),
    "field.login_optional" to mapOf(L.EN to "Login / Username (optional)", L.RU to "Логин / Имя пользователя (необязательно)"),
    "field.password_optional" to mapOf(L.EN to "Password (optional)", L.RU to "Пароль (необязательно)"),
    "field.email_optional" to mapOf(L.EN to "Email (optional)", L.RU to "Email (необязательно)"),
    "field.phone_optional" to mapOf(L.EN to "Phone (optional)", L.RU to "Телефон (необязательно)"),
    "field.website_optional" to mapOf(L.EN to "Website (optional)", L.RU to "Сайт (необязательно)"),
    "field.tags_optional" to mapOf(L.EN to "Tags (semicolon separated, optional)", L.RU to "Теги (через ; , необязательно)"),
    "field.notes_optional" to mapOf(L.EN to "Notes (optional)", L.RU to "Заметки (необязательно)"),

    "label.email" to mapOf(L.EN to "Email", L.RU to "Email"),
    "label.phone" to mapOf(L.EN to "Phone", L.RU to "Телефон"),
    "label.website" to mapOf(L.EN to "Website", L.RU to "Сайт"),

    "action.save" to mapOf(L.EN to "Save", L.RU to "Сохранить"),
    "action.cancel" to mapOf(L.EN to "Cancel", L.RU to "Отмена"),

    // small UI bits
    "action.copy" to mapOf(L.EN to "Copy", L.RU to "Копировать"),
    "action.edit" to mapOf(L.EN to "Edit", L.RU to "Редактировать"),
    "action.delete" to mapOf(L.EN to "Delete", L.RU to "Удалить")
)

fun t(key: String, lang: String): String {
    LocalizationManager.translate(key, lang)?.let { return it }
    return strings[key]?.get(lang) ?: strings[key]?.get(L.EN) ?: key
}
