package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserProfile
import com.example.service.CallState
import com.example.ui.components.CallScreen
import com.example.ui.components.IncomingCallOverlay
import com.example.ui.screens.CallLogScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DialerScreen
import com.example.ui.screens.OnboardingDialog
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoNavSurface
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextPrimary

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    PHONE("Phone", Icons.Default.Dialpad, "tab_phone"),
    RECENTS("Recents", Icons.Default.History, "tab_recents"),
    CHAT("Family & AI", Icons.Default.ChatBubble, "tab_chat"),
    CONTACTS("Contacts", Icons.Default.ContactPage, "tab_contacts")
}

@Composable
fun FamilyPhoneApp(
    viewModel: FamilyPhoneViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.PHONE) }
    var targetChatId by remember { mutableStateOf("neumai") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val rawUserProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val userProfile = rawUserProfile ?: UserProfile()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val neumaiMemories by viewModel.neumaiMemories.collectAsStateWithLifecycle()
    val isNeumaiTyping by viewModel.isNeumaiTyping.collectAsStateWithLifecycle()
    val callRecords by viewModel.callRecords.collectAsStateWithLifecycle()
    val callInfo by viewModel.callState.collectAsStateWithLifecycle()

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* granted or denied */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // First time onboarding dialog if not initialized
    if (!userProfile.isInitialized) {
        OnboardingDialog(
            initialProfile = userProfile,
            onComplete = { updated ->
                viewModel.updateUserProfile(updated)
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        bottomBar = {
            // Bento Bottom Navigation Bar matching design HTML
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                color = BentoNavSurface,
                tonalElevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top border line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BentoBorder)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            BentoNavItem(
                                tab = tab,
                                isSelected = isSelected,
                                onClick = { currentTab = tab }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    AppTab.PHONE -> DialerScreen(
                        userProfile = userProfile,
                        contacts = contacts,
                        groups = groups,
                        onStartCall = { name, number, role, avatar ->
                            viewModel.startCall(context, name, number, role, avatar)
                        },
                        onSimulateIncomingCall = {
                            viewModel.simulateIncomingCall(context)
                        },
                        onNavigateToGroupChat = {
                            targetChatId = if (groups.isNotEmpty()) "group_${groups.first().id}" else "neumai"
                            currentTab = AppTab.CHAT
                        },
                        onNavigateToNeumai = {
                            targetChatId = "neumai"
                            currentTab = AppTab.CHAT
                        },
                        onNavigateToContacts = {
                            currentTab = AppTab.CONTACTS
                        },
                        onOpenProfileSettings = {
                            showEditProfileDialog = true
                        }
                    )

                    AppTab.RECENTS -> CallLogScreen(
                        callRecords = callRecords,
                        onClearHistory = { viewModel.clearCallHistory() },
                        onRedial = { name, number ->
                            val c = contacts.firstOrNull { it.familyNumber == number }
                            viewModel.startCall(
                                context,
                                name,
                                number,
                                c?.role ?: "Family",
                                c?.avatarSeed ?: ""
                            )
                        }
                    )

                    AppTab.CHAT -> ChatScreen(
                        userProfile = userProfile,
                        contacts = contacts,
                        groups = groups,
                        messages = messages,
                        neumaiMemories = neumaiMemories,
                        isNeumaiTyping = isNeumaiTyping,
                        initialConversationId = targetChatId,
                        onSendMessage = { conversationId, text ->
                            viewModel.sendMessage(conversationId, text)
                        },
                        onCreateGroup = { name, members ->
                            viewModel.createGroup(name, members)
                        },
                        onSaveMemory = { subj, fact, cat ->
                            viewModel.saveFamilyMemory(subj, fact, cat)
                        },
                        onDeleteMemory = { id ->
                            viewModel.deleteFamilyMemory(id)
                        },
                        onStartCall = { name, number, role, avatar ->
                            viewModel.startCall(context, name, number, role, avatar)
                        }
                    )

                    AppTab.CONTACTS -> ContactsScreen(
                        contacts = contacts,
                        onAddContact = { contact ->
                            viewModel.addContact(contact)
                        },
                        onStartCall = { name, number, role, avatar ->
                            viewModel.startCall(context, name, number, role, avatar)
                        },
                        onOpenChat = {
                            targetChatId = it
                            currentTab = AppTab.CHAT
                        }
                    )
                }
            }

            // Edit Profile (Your Name & Number) Dialog
            if (showEditProfileDialog) {
                var editName by remember(userProfile.name) { mutableStateOf(userProfile.name) }
                var editNumber by remember(userProfile.familyNumber) { mutableStateOf(userProfile.familyNumber) }

                AlertDialog(
                    onDismissRequest = { showEditProfileDialog = false },
                    title = { Text("My Family Phone Profile", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Configure your identity on the private family Wi-Fi network:",
                                fontSize = 12.sp,
                                color = BentoTextPrimary
                            )
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Your Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editNumber,
                                onValueChange = { editNumber = it },
                                label = { Text("Your Family Wi-Fi Number") },
                                placeholder = { Text("e.g. +88-0101") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearAllData()
                                    showEditProfileDialog = false
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDC2626)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset All App Data", fontSize = 13.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editName.isNotBlank() && editNumber.isNotBlank()) {
                                    viewModel.updateUserProfile(
                                        userProfile.copy(
                                            name = editName.trim(),
                                            familyNumber = editNumber.trim()
                                        )
                                    )
                                    showEditProfileDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Incoming Call Overlay Dialog
            if (callInfo.state == CallState.INCOMING) {
                IncomingCallOverlay(
                    callInfo = callInfo,
                    onAnswer = { viewModel.answerCall(context) },
                    onDecline = { viewModel.endCall(context) }
                )
            }

            // Active Call Full-Screen (When Dialing or Connected)
            if (callInfo.state == CallState.DIALING || callInfo.state == CallState.CONNECTED) {
                CallScreen(
                    callInfo = callInfo,
                    availableContacts = contacts,
                    onMuteToggle = { viewModel.toggleMute() },
                    onSpeakerToggle = { viewModel.toggleSpeaker() },
                    onHoldToggle = { viewModel.toggleHold() },
                    onAddParticipant = { contact ->
                        viewModel.addThreeWayParticipant(contact)
                    },
                    onEndCall = { viewModel.endCall(context) }
                )
            }
        }
    }
}

@Composable
private fun BentoNavItem(
    tab: AppTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(tab.tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(BentoSecondaryContainer)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = BentoTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = BentoTextPrimary.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(20.dp)
            )
        }

        Text(
            text = tab.title,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BentoTextPrimary else BentoTextPrimary.copy(alpha = 0.65f)
        )
    }
}
