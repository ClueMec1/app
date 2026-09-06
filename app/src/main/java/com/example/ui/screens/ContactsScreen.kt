package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyContact
import com.example.ui.components.FamilyAvatar
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoCallGreen
import com.example.ui.theme.BentoCardBlue
import com.example.ui.theme.BentoCardGreen
import com.example.ui.theme.BentoCardPink
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary

@Composable
fun ContactsScreen(
    contacts: List<FamilyContact>,
    onAddContact: (FamilyContact) -> Unit,
    onStartCall: (name: String, number: String, role: String, avatar: String) -> Unit,
    onOpenChat: (familyNumber: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true) ||
                it.familyNumber.contains(searchQuery)
    }

    Scaffold(
        modifier = modifier.background(BentoBackground),
        containerColor = BentoBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BentoPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_contact_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Text(
                text = "Family Directory",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search family members...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BentoTextSecondary
                    )
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Contacts List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts) { contact ->
                    ContactCardItem(
                        contact = contact,
                        onCall = {
                            onStartCall(contact.name, contact.familyNumber, contact.role, contact.avatarSeed)
                        },
                        onChat = {
                            onOpenChat(contact.familyNumber)
                        }
                    )
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newNumber by remember { mutableStateOf("+88-") }
        var newRole by remember { mutableStateOf("Family") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Family Contact", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Grandma Evelyn") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNumber,
                        onValueChange = { newNumber = it },
                        label = { Text("Family Wi-Fi Number") },
                        placeholder = { Text("+88-0205") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRole,
                        onValueChange = { newRole = it },
                        label = { Text("Role / Relationship") },
                        placeholder = { Text("Mom, Dad, Cousin, Uncle, Friend...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newNumber.isNotBlank()) {
                            val seed = newName.split(" ").firstOrNull() ?: "Member"
                            val cardColor = when (newRole.lowercase()) {
                                "mother", "mom" -> "blue"
                                "father", "dad" -> "green"
                                "sister", "grandma" -> "pink"
                                else -> "purple"
                            }
                            onAddContact(
                                FamilyContact(
                                    familyNumber = newNumber.trim(),
                                    name = newName.trim(),
                                    role = newRole.trim(),
                                    avatarSeed = seed,
                                    cardColorType = cardColor
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContactCardItem(
    contact: FamilyContact,
    onCall: () -> Unit,
    onChat: () -> Unit
) {
    val bgTint = when (contact.cardColorType) {
        "blue" -> BentoCardBlue.copy(alpha = 0.4f)
        "pink" -> BentoCardPink.copy(alpha = 0.4f)
        "green" -> BentoCardGreen.copy(alpha = 0.4f)
        else -> BentoSecondaryContainer.copy(alpha = 0.6f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            FamilyAvatar(
                name = contact.name,
                seed = contact.avatarSeed,
                size = 46.dp
            )

            Column {
                Text(
                    text = contact.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgTint)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = contact.role,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoPrimary
                        )
                    }
                    Text(
                        text = contact.familyNumber,
                        fontSize = 12.sp,
                        color = BentoTextSecondary
                    )
                }
            }
        }

        // Action Buttons: Call & Chat
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onChat,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BentoSecondaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Chat",
                    tint = BentoPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onCall,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BentoCallGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
