package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AuthScreen(
    onLogin: (masterPassword: String) -> Unit,
    language: String,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(t("auth.title", language), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(t("auth.password_label", language)) },
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()
            )
            Spacer(Modifier.height(8.dp))
            Row {
                TextButton(onClick = { visible = !visible }) {
                    Text(if (visible) t("auth.hide", language) else t("auth.show", language))
                }
                Spacer(Modifier.width(12.dp))
                Button(onClick = { onLogin(password) }) {
                    Text(t("auth.enter", language))
                }
            }
        }
    }
}
