package com.alish.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alish.securevault.model.PasswordEntry

@Composable
fun PasswordsScreen(
    modifier: Modifier = Modifier,
    entries: List<PasswordEntry>,
    onAddPassword: (String, String, String) -> Unit,
    onRevealToggle: (String) -> Unit,
    revealedSecret: (PasswordEntry) -> String,
    onDeletePassword: (String) -> Unit
) {
    var label by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var secret by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Password vault",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        item {
            Card(shape = RoundedCornerShape(26.dp)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add a new secret",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Account name") }
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Username or email") }
                    )
                    OutlinedTextField(
                        value = secret,
                        onValueChange = { secret = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") }
                    )
                    Button(
                        onClick = {
                            onAddPassword(label, username, secret)
                            label = ""
                            username = ""
                            secret = ""
                        }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(10.dp))
                        Text("Save encrypted entry")
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            item {
                EmptyCard(
                    title = "No saved passwords yet",
                    body = "Add your first encrypted credential and it will be stored in local private app storage."
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                PasswordEntryCard(
                    entry = entry,
                    secret = revealedSecret(entry),
                    onRevealToggle = { onRevealToggle(entry.id) },
                    onDelete = { onDeletePassword(entry.id) }
                )
            }
        }
    }
}

@Composable
private fun PasswordEntryCard(
    entry: PasswordEntry,
    secret: String,
    onRevealToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRevealToggle) {
                        Icon(
                            imageVector = if (secret == "Tap reveal to decrypt") {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            HorizontalDivider()
            Text(
                text = secret,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = entry.updatedAt,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
