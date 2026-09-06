package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.FamilyContact
import com.example.data.model.FamilyGroup
import com.example.data.model.NeumaiMemory
import com.example.data.model.UserProfile
import com.example.ui.components.FamilyAvatar
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoCardPink
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    userProfile: UserProfile,
    contacts: List<FamilyContact>,
    groups: List<FamilyGroup>,
    messages: List<ChatMessage>,
    neumaiMemories: List<NeumaiMemory>,
    isNeumaiTyping: Boolean,
    initialConversationId: String = "neumai",
    onSendMessage: (conversationId: String, text: String) -> Unit,
    onCreateGroup: (name: String, members: String) -> Unit,
    onSaveMemory: (subject: String, fact: String, category: String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onStartCall: (name: String, number: String, role: String, avatar: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedConversationId by remember { mutableStateOf(initialConversationId) }
    var messageInput by remember { mutableStateOf("") }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showMemoryVaultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialConversationId) {
        selectedConversationId = initialConversationId
    }

    val isNeumai = selectedConversationId == "neumai"
    val activeGroup = groups.firstOrNull { "group_${it.id}" == selectedConversationId }
    val activeContact = contacts.firstOrNull { it.familyNumber == selectedConversationId }

    val conversationName = when {
        isNeumai -> "NEUMAI"
        activeGroup != null -> activeGroup.name
        activeContact != null -> activeContact.name
        else -> "Family Chat"
    }

    val conversationSubtitle = when {
        isNeumai -> "Family AI Assistant • ${neumaiMemories.size} memories saved"
        activeGroup != null -> "${groups.firstOrNull()?.memberNumbers?.split(",")?.size ?: 4} members"
        activeContact != null -> "${activeContact.role} • ${activeContact.familyNumber}"
        else -> "Family"
    }

    val activeMessages = messages.filter { it.conversationId == selectedConversationId }

    val neumaiQuickPrompts = listOf(
        "🧠 What do you know?",
        "🎂 When is Sarah's birthday?",
        "🔑 What's the Wi-Fi password?",
        "⚠️ What is Noah allergic to?",
        "📅 When is Sunday BBQ?",
        "☕ What does Dad like to drink?",
        "Remember that Grandma Evelyn loves lavender tea"
    )

    val standardQuickPhrases = listOf(
        "I'm on Wi-Fi!",
        "Starting family dinner",
        "Can we do a 3-way call?",
        "Love you all ❤️",
        "Call me when free"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        // Top Conversation Selector Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Family Conversations",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoSecondaryContainer)
                        .clickable { showCreateGroupDialog = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Group",
                        tint = BentoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New Group",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoPrimary
                    )
                }
            }

            // Conversations List Chip Row (NEUMAI pinned first!)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // 1. NEUMAI Pinned AI Chip (Meta AI style)
                item {
                    val isNeumaiSelected = selectedConversationId == "neumai"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isNeumaiSelected) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF312E81), Color(0xFF6D28D9))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFEEF2FF), Color(0xFFFAF5FF))
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isNeumaiSelected) Color(0xFF818CF8) else Color(0xFFC7D2FE),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedConversationId = "neumai" }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "NEUMAI",
                                tint = if (isNeumaiSelected) Color(0xFFFDE047) else Color(0xFF6366F1),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "NEUMAI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNeumaiSelected) Color.White else Color(0xFF312E81)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isNeumaiSelected) Color(0xFFEC4899) else Color(0xFFE0E7FF)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "AI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isNeumaiSelected) Color.White else Color(0xFF4338CA)
                                )
                            }
                        }
                    }
                }

                // 2. Groups
                items(groups) { group ->
                    val isSelected = selectedConversationId == "group_${group.id}"
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedConversationId = "group_${group.id}" },
                        label = { Text(group.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BentoPrimaryContainer,
                            selectedLabelColor = BentoPrimary
                        )
                    )
                }

                // 3. Contacts
                items(contacts) { contact ->
                    val isSelected = selectedConversationId == contact.familyNumber
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedConversationId = contact.familyNumber },
                        label = { Text(contact.name.split(" ").first()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BentoSecondaryContainer,
                            selectedLabelColor = BentoPrimary
                        )
                    )
                }
            }
        }

        // Active Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isNeumai) Color(0xFFF5F3FF) else BentoSecondaryContainer.copy(alpha = 0.5f),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isNeumai) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF38BDF8),
                                            Color(0xFF818CF8),
                                            Color(0xFFF472B6),
                                            Color(0xFF38BDF8)
                                        )
                                    )
                                )
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1B4B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "NEUMAI AI",
                                    tint = Color(0xFFE0E7FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    } else if (activeGroup != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BentoCardPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF2B1630),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        FamilyAvatar(
                            name = conversationName,
                            seed = activeContact?.avatarSeed ?: "",
                            size = 40.dp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = conversationName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                            if (isNeumai) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEDE9FE))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Family AI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9)
                                    )
                                }
                            }
                        }
                        Text(
                            text = conversationSubtitle,
                            fontSize = 11.sp,
                            color = BentoTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Action Button: Vault for NEUMAI, Call for Contacts/Groups
                if (isNeumai) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEDE9FE))
                            .clickable { showMemoryVaultDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Memory Vault",
                                tint = Color(0xFF5B21B6),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Vault (${neumaiMemories.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5B21B6)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (activeContact != null) {
                                onStartCall(
                                    activeContact.name,
                                    activeContact.familyNumber,
                                    activeContact.role,
                                    activeContact.avatarSeed
                                )
                            } else {
                                val firstMember = contacts.firstOrNull()
                                if (firstMember != null) {
                                    onStartCall(
                                        conversationName,
                                        firstMember.familyNumber,
                                        "Group Call",
                                        ""
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .testTag("chat_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = BentoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activeMessages) { msg ->
                if (msg.senderName == "NEUMAI" && !msg.isFromMe) {
                    NeumaiBubble(message = msg)
                } else {
                    ChatBubble(message = msg, isFromMe = msg.isFromMe)
                }
            }

            if (isNeumai && isNeumaiTyping) {
                item {
                    NeumaiTypingIndicator()
                }
            }
        }

        // Quick Suggestions / Prompts
        val activePrompts = if (isNeumai) neumaiQuickPrompts else standardQuickPhrases
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(activePrompts) { phrase ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isNeumai) Color(0xFFF3E8FF) else Color.White)
                        .border(
                            width = 0.5.dp,
                            color = if (isNeumai) Color(0xFFDDD6FE) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            onSendMessage(selectedConversationId, phrase)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = phrase,
                        fontSize = 11.sp,
                        color = if (isNeumai) Color(0xFF6B21A8) else BentoPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                placeholder = {
                    Text(
                        if (isNeumai) "Ask NEUMAI or tell it family info..." else "Family message...",
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("message_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        onSendMessage(selectedConversationId, messageInput.trim())
                        messageInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isNeumai) Color(0xFF4F46E5) else BentoPrimary)
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Dialog to create a new family group
    if (showCreateGroupDialog) {
        var newGroupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Create Family Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a group name (e.g., 'Family Vacation', 'Weekend BBQ'):", fontSize = 13.sp)
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            val members = contacts.map { it.familyNumber }.joinToString(",")
                            onCreateGroup(newGroupName.trim(), members)
                            showCreateGroupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: NEUMAI Family Memory Vault
    if (showMemoryVaultDialog) {
        MemoryVaultDialog(
            memories = neumaiMemories,
            onDismiss = { showMemoryVaultDialog = false },
            onAddMemory = { subj, fact, cat ->
                onSaveMemory(subj, fact, cat)
            },
            onDeleteMemory = onDeleteMemory
        )
    }
}

@Composable
private fun NeumaiBubble(message: ChatMessage) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 8.dp, bottom = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "NEUMAI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4338CA)
            )
            Text(
                text = "• Family AI",
                fontSize = 10.sp,
                color = BentoTextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 18.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFBFBFE), Color(0xFFF5F3FF))
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFDDD6FE),
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color(0xFF1E1B4B),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    fontSize = 9.sp,
                    color = BentoTextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun NeumaiTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEDE9FE))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFF6D28D9),
            modifier = Modifier
                .size(16.dp)
                .scale(pulse)
        )
        Text(
            text = "NEUMAI is thinking and accessing family memory...",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5B21B6)
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, isFromMe: Boolean) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        if (!isFromMe) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoTextSecondary,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isFromMe) 18.dp else 4.dp,
                        bottomEnd = if (isFromMe) 4.dp else 18.dp
                    )
                )
                .background(if (isFromMe) BentoPrimaryContainer else Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (isFromMe) Color(0xFF21005D) else BentoTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedTime,
                    fontSize = 9.sp,
                    color = BentoTextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun MemoryVaultDialog(
    memories: List<NeumaiMemory>,
    onDismiss: () -> Unit,
    onAddMemory: (subject: String, fact: String, category: String) -> Unit,
    onDeleteMemory: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isAddingNew by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }
    var newFact by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("PREFERENCE") }

    val filteredMemories = remember(searchQuery, memories) {
        if (searchQuery.isBlank()) memories
        else memories.filter {
            it.keySubject.contains(searchQuery, ignoreCase = true) ||
                    it.fact.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color(0xFF6D28D9),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "NEUMAI Family Memory Vault",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Everything NEUMAI remembers for our family",
                        fontSize = 11.sp,
                        color = BentoTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search memories...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Add Fact Button toggle
                AnimatedVisibility(visible = !isAddingNew) {
                    Button(
                        onClick = { isAddingNew = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Family Fact Manually", fontSize = 12.sp)
                    }
                }

                // Add Memory Input Form
                AnimatedVisibility(visible = isAddingNew) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F3FF))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Save Information to NEUMAI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA)
                        )
                        OutlinedTextField(
                            value = newSubject,
                            onValueChange = { newSubject = it },
                            placeholder = { Text("Subject (e.g. Sarah, Wi-Fi)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newFact,
                            onValueChange = { newFact = it },
                            placeholder = { Text("Fact (e.g. Birthday is May 14)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    if (newSubject.isNotBlank() && newFact.isNotBlank()) {
                                        onAddMemory(newSubject.trim(), newFact.trim(), newCategory)
                                        newSubject = ""
                                        newFact = ""
                                        isAddingNew = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Fact", fontSize = 12.sp)
                            }
                            TextButton(onClick = { isAddingNew = false }) {
                                Text("Cancel", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Memories List
                if (filteredMemories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No memories saved yet.\nTalk to NEUMAI to teach it facts!" else "No matching family memories found.",
                            fontSize = 12.sp,
                            color = BentoTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredMemories, key = { it.id }) { memory ->
                            val catEmoji = when (memory.category) {
                                "BIRTHDAY" -> "🎂"
                                "ALLERGY" -> "⚠️"
                                "PREFERENCE" -> "❤️"
                                "LOCATION" -> "🔑"
                                "SCHEDULE" -> "📅"
                                else -> "📝"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF9FAFB))
                                    .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = catEmoji, fontSize = 12.sp)
                                        Text(
                                            text = memory.keySubject,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
                                        )
                                        Text(
                                            text = "• ${memory.category}",
                                            fontSize = 9.sp,
                                            color = BentoTextSecondary
                                        )
                                    }
                                    Text(
                                        text = memory.fact,
                                        fontSize = 11.sp,
                                        color = Color(0xFF374151)
                                    )
                                    Text(
                                        text = "Saved by ${memory.recordedBy}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteMemory(memory.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Memory",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("Close")
            }
        }
    )
}
