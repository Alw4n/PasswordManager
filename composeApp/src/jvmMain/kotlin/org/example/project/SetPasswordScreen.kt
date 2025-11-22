package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SetPasswordScreen(
    onSet: (masterPassword: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Set master password", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password2, onValueChange = { password2 = it }, label = { Text("Repeat password") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { if (password.isNotEmpty() && password == password2) onSet(password) }) {
            Text("Set password")
        }
    }
}
