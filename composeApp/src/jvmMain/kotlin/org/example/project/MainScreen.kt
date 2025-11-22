package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    repository: PasswordRepository,
    language: String,
    onLock: () -> Unit,
    onOpenSettings: () -> Unit,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val header = t("main.title", language)

    var refreshKey by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    var editingEntry by remember { mutableStateOf<PasswordEntry?>(null) }
    var showingDeleteConfirmForId by remember { mutableStateOf<String?>(null) }

    val entriesState = remember(refreshKey, repository) {
        mutableStateOf<List<Any>>(safeFetchEntries(repository))
    }
    val entries by entriesState

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = header, style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onOpenSettings) { Text("⚙") }
                    Spacer(Modifier.width(8.dp))
                    // simple lock icon as emoji to avoid extra dependencies
                    IconButton(onClick = onLock) {
                        Text("🔒")
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = t("main.entries", language), style = MaterialTheme.typography.titleMedium)
                Button(onClick = { showAddDialog = true }) {
                    Text(t("main.add_entry", language))
                }
            }

            Spacer(Modifier.height(12.dp))

            if (entries.isEmpty()) {
                Text(text = "${t("main.entries", language)}: 0", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(entries) { entry ->
                        PasswordEntryRow(
                            entry = entry,
                            language = language,
                            onEdit = { pe -> editingEntry = pe },
                            onDelete = { id -> showingDeleteConfirmForId = id },
                            onView = { pe -> editingEntry = pe } // reuse edit dialog as viewer for now
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // create new
    if (showAddDialog) {
        AddEntryDialog(
            initial = null,
            language = language,
            onDismiss = { showAddDialog = false },
            onSave = { newEntry ->
                repository.upsert(newEntry)
                refreshKey++
                showAddDialog = false
            }
        )
    }

    // edit/view existing
    editingEntry?.let { entry ->
        AddEntryDialog(
            initial = entry,
            language = language,
            onDismiss = { editingEntry = null },
            onSave = { updated ->
                repository.upsert(updated)
                refreshKey++
                editingEntry = null
            }
        )
    }

    // delete confirmation
    showingDeleteConfirmForId?.let { id ->
        AlertDialog(
            onDismissRequest = { showingDeleteConfirmForId = null },
            title = { Text(t("action.delete", language)) },
            text = {
                // localized confirmation: if key missing, fallback implemented in t()
                val tmpl = t("confirm.delete_entry", language)
                Text(if (tmpl.contains("{id}")) tmpl.replace("{id}", id) else tmpl)
            },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        repository.delete(id)
                    } catch (_: Throwable) {}
                    refreshKey++
                    showingDeleteConfirmForId = null
                }) { Text(t("action.delete", language)) }
            },
            dismissButton = {
                Button(onClick = { showingDeleteConfirmForId = null }) { Text(t("action.cancel", language)) }
            }
        )
    }
}

/* ---------------- PasswordEntryRow ---------------- */

@Composable
private fun PasswordEntryRow(
    entry: Any,
    language: String,
    onEdit: (PasswordEntry) -> Unit,
    onDelete: (String) -> Unit,
    onView: (PasswordEntry) -> Unit
) {
    val pe = toPasswordEntry(entry) ?: return
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = pe.title, style = MaterialTheme.typography.bodyLarge)
            if (!pe.login.isNullOrBlank()) {
                Text(text = pe.login, style = MaterialTheme.typography.bodySmall)
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Text("⋯")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text(t("action.copy", language)) }, onClick = {
                    // TODO: implement copy action for secret/login
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(t("action.edit", language)) }, onClick = {
                    onEdit(pe)
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(t("action.delete", language)) }, onClick = {
                    onDelete(pe.id)
                    menuExpanded = false
                })
            }
        }
    }
}

/* ---------------- Helpers ---------------- */

private fun toPasswordEntry(obj: Any): PasswordEntry? {
    if (obj is PasswordEntry) return obj

    val id = extractStringField(obj, listOf("id", "Id", "ID")) ?: return null
    val title = extractStringField(obj, listOf("title", "name", "label")) ?: id
    val login = extractStringField(obj, listOf("login", "username", "user", "account")) ?: ""
    val secret = extractStringField(obj, listOf("secret", "password", "pw", "passwordPlain")) ?: ""
    val notes = extractStringField(obj, listOf("notes", "note")) ?: ""
    val tagsRaw = extractStringField(obj, listOf("tags")) ?: ""
    val createdAtStr = extractStringField(obj, listOf("createdAt", "createdAtEpochMs", "created")) ?: ""
    val updatedAtStr = extractStringField(obj, listOf("updatedAt", "updatedAtEpochMs", "updated")) ?: ""

    val tags = if (tagsRaw.isBlank()) emptyList() else tagsRaw.split(";").map { it.trim() }.filter { it.isNotEmpty() }
    val createdAt = createdAtStr.toLongOrNull() ?: System.currentTimeMillis()
    val updatedAt = updatedAtStr.toLongOrNull() ?: System.currentTimeMillis()

    return PasswordEntry(
        id = id,
        title = title,
        login = login,
        secret = secret,
        notes = notes,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun extractStringField(obj: Any, candidates: List<String>): String? {
    for (name in candidates) {
        try {
            val getterName = "get" + name.replaceFirstChar { it.uppercaseChar() }
            val getter = try { obj.javaClass.getMethod(getterName) } catch (_: NoSuchMethodException) { null }
            if (getter != null) {
                val v = getter.invoke(obj)
                if (v != null) return v.toString()
            }
        } catch (_: Throwable) {}

        try {
            val field = try { obj.javaClass.getDeclaredField(name) } catch (_: NoSuchFieldException) { null }
            if (field != null) {
                field.isAccessible = true
                val v = field.get(obj)
                if (v != null) return v.toString()
            }
        } catch (_: Throwable) {}
    }
    return null
}

private fun safeFetchEntries(repository: PasswordRepository): List<Any> {
    try {
        val cls = repository.javaClass
        val tryNames = arrayOf("getEntries", "listEntries", "getAll", "list", "entries", "allEntries", "findAll")
        for (name in tryNames) {
            try {
                val method = cls.getMethod(name)
                val res = method.invoke(repository)
                val list = toAnyList(res)
                if (list != null) return list
            } catch (_: Throwable) {
            }
        }
    } catch (_: Throwable) {}

    return try {
        val method = repository.javaClass.methods.find { it.name == "list" && it.parameterCount == 0 }
        if (method != null) {
            val res = method.invoke(repository)
            val list = toAnyList(res)
            if (list != null) return list
        }
        when (repository) {
            is DbPasswordRepositoryAdapter -> repository.list().map { it as Any }
            else -> emptyList()
        }
    } catch (_: Throwable) {
        emptyList()
    }
}

@Suppress("UNCHECKED_CAST")
private fun toAnyList(res: Any?): List<Any>? {
    if (res == null) return null
    return when (res) {
        is List<*> -> res.filterNotNull()
        is Array<*> -> res.filterNotNull()
        else -> null
    }
}
