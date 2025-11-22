package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChangePasswordDialog(
    repository: PasswordRepository,
    language: String,
    onClose: () -> Unit,
    onResult: (success: Boolean, message: String) -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var newPwd2 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(t("changepwd.title", language)) },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text(t("changepwd.current", language)) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text(t("changepwd.new", language)) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPwd2,
                    onValueChange = { newPwd2 = it },
                    label = { Text(t("changepwd.repeat", language)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPwd.isBlank()) {
                    onResult(false, t("changepwd.empty_error", language))
                    return@TextButton
                }
                if (newPwd != newPwd2) {
                    onResult(false, t("changepwd.nomatch_error", language))
                    return@TextButton
                }
                val ok = repository.changeMasterPassword(oldPwd, newPwd)
                if (ok) onResult(true, t("changepwd.change", language))
                else onResult(false, t("changepwd.incorrect_error", language))
            }) {
                Text(t("changepwd.change", language))
            }
        },
        dismissButton = {
            Button(onClick = onClose) { Text(t("changepwd.cancel", language)) }
        }
    )
}
