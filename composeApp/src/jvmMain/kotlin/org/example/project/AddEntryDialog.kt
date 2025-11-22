package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun AddEntryDialog(
    initial: PasswordEntry? = null,
    language: String,
    onDismiss: () -> Unit,
    onSave: (PasswordEntry) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var login by remember { mutableStateOf(initial?.login ?: "") }
    var secret by remember { mutableStateOf(initial?.secret ?: "") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var tagsRaw by remember { mutableStateOf(initial?.tags?.joinToString(";") ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }

    val scroll = rememberScrollState()

    fun validate(): Boolean {
        return if (title.isBlank()) {
            titleError = t("validation.title_required", language)
            false
        } else {
            titleError = null
            true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) t("entry.add_title", language) else t("entry.edit_title", language))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(vertical = 4.dp)
                    .heightIn(min = 160.dp, max = 560.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (!it.isBlank()) titleError = null
                    },
                    label = { Text(t("field.title_optional", language)) },
                    isError = !titleError.isNullOrBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!titleError.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(text = t("validation.title_required", language), color = Color(0xFFB00020))
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text(t("field.login_optional", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text(t("field.password_optional", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(t("field.email_optional", language)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(t("field.phone_optional", language)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(t("field.website_optional", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = tagsRaw,
                    onValueChange = { tagsRaw = it },
                    label = { Text(t("field.tags_optional", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(t("field.notes_optional", language)) },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!validate()) return@TextButton

                val structured = buildString {
                    if (email.isNotBlank()) append("${t("label.email", language)}:$email\n")
                    if (phone.isNotBlank()) append("${t("label.phone", language)}:$phone\n")
                    if (url.isNotBlank()) append("${t("label.website", language)}:$url\n")
                    if (isNotBlank()) append("\n")
                    append(notes)
                }.trim()

                val tags = if (tagsRaw.isBlank()) emptyList() else tagsRaw.split(";").map { it.trim() }.filter { it.isNotEmpty() }

                val entry = PasswordEntry(
                    id = initial?.id ?: "",
                    title = title,
                    login = login,
                    secret = secret,
                    notes = structured,
                    tags = tags,
                    createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                onSave(entry)
            }) {
                Text(t("action.save", language))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(t("action.cancel", language)) }
        }
    )
}
