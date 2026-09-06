package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyContact
import com.example.data.model.FamilyGroup
import com.example.data.model.UserProfile
import com.example.ui.components.BentoDialpad
import com.example.ui.components.BentoQuickGrid
import com.example.ui.components.FamilyAvatar
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary

@Composable
fun DialerScreen(
    userProfile: UserProfile,
    contacts: List<FamilyContact>,
    groups: List<FamilyGroup> = emptyList(),
    onStartCall: (name: String, number: String, role: String, avatar: String) -> Unit,
    onSimulateIncomingCall: () -> Unit,
    onNavigateToGroupChat: () -> Unit,
    onNavigateToNeumai: () -> Unit,
    onNavigateToContacts: () -> Unit = {},
    onOpenProfileSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialedNumber by remember { mutableStateOf("") }

    val matchedContact by remember(dialedNumber, contacts) {
        derivedStateOf {
            if (dialedNumber.length < 2) null
            else contacts.firstOrNull {
                it.familyNumber.replace("-", "").endsWith(dialedNumber.replace("-", "")) ||
                        it.familyNumber.contains(dialedNumber)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        // Bento Header: Avatar + Name + Family ID + Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onOpenProfileSettings() }
                    .padding(4.dp)
            ) {
                FamilyAvatar(
                    name = if (userProfile.name.isNotBlank()) userProfile.name else "You",
                    seed = userProfile.avatarSeed,
                    size = 48.dp,
                    borderColor = BentoPrimary
                )
                Column {
                    Text(
                        text = if (userProfile.name.isNotBlank()) userProfile.name else "My Family Phone",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = if (userProfile.familyNumber.isNotBlank()) "${userProfile.familyNumber} (Family ID)" else "Set your family number",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BentoTextSecondary
                    )
                }
            }

            IconButton(
                onClick = onOpenProfileSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BentoSecondaryContainer)
                    .testTag("dialer_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = BentoTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bento Quick Grid
        BentoQuickGrid(
            contacts = contacts,
            groups = groups,
            onContactCall = { contact ->
                onStartCall(contact.name, contact.familyNumber, contact.role, contact.avatarSeed)
            },
            onGroupClick = onNavigateToGroupChat,
            onNeumaiClick = onNavigateToNeumai,
            onAddContactClick = onNavigateToContacts
        )

        // Bento Dialpad Section
        BentoDialpad(
            dialedNumber = dialedNumber,
            matchedContact = matchedContact,
            onNumberChange = { dialedNumber = it },
            onCallClick = {
                if (matchedContact != null) {
                    onStartCall(
                        matchedContact!!.name,
                        matchedContact!!.familyNumber,
                        matchedContact!!.role,
                        matchedContact!!.avatarSeed
                    )
                } else if (dialedNumber.isNotBlank()) {
                    onStartCall("Family Contact", dialedNumber, "Custom", "")
                }
            },
            onSimulateIncoming = onSimulateIncomingCall,
            modifier = Modifier.weight(1f)
        )
    }
}
