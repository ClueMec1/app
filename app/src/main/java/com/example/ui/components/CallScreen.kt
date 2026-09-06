package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyContact
import com.example.service.ActiveCallInfo
import com.example.service.CallState
import com.example.ui.theme.BentoCallGreen
import com.example.ui.theme.BentoCallRed
import com.example.ui.theme.BentoCardBlue
import com.example.ui.theme.BentoCardPink
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSecondaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    callInfo: ActiveCallInfo,
    availableContacts: List<FamilyContact>,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onHoldToggle: () -> Unit,
    onAddParticipant: (FamilyContact) -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddParticipantSheet by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primary = callInfo.primaryParticipant
    val secondary = callInfo.secondaryParticipant
    val isConnected = callInfo.state == CallState.CONNECTED
    val formattedTime = String.format(
        "%02d:%02d",
        callInfo.durationSeconds / 60,
        callInfo.durationSeconds % 60
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1D1B20),
                        Color(0xFF2C243B),
                        Color(0xFF141218)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Wi-Fi Badge & 3-Way Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = BentoCallGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (callInfo.isThreeWay) "3-Way Family Conference" else "Family Wi-Fi Call",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        !isConnected -> "Calling via Family Network..."
                        callInfo.isHold -> "Call on Hold"
                        else -> formattedTime
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Middle: Avatars & Names (Supports 1 caller or 2 for 3-way call)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (callInfo.isThreeWay && secondary != null) {
                    // 3-Way Call Display: Two Avatars side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FamilyAvatar(
                                name = primary?.name ?: "Sarah",
                                seed = primary?.avatarSeed ?: "Sarah",
                                size = 80.dp,
                                borderColor = BentoCardBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = primary?.name ?: "",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = primary?.role ?: "",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "&",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FamilyAvatar(
                                name = secondary.name,
                                seed = secondary.avatarSeed,
                                size = 80.dp,
                                borderColor = BentoCardPink
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = secondary.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = secondary.role,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // Single Caller Display with gentle pulsing ring
                    Box(contentAlignment = Alignment.Center) {
                        if (isConnected && !callInfo.isHold) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(BentoPrimary.copy(alpha = 0.25f))
                            )
                        }
                        FamilyAvatar(
                            name = primary?.name ?: "Sarah",
                            seed = primary?.avatarSeed ?: "Sarah",
                            size = 110.dp,
                            borderColor = BentoPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = primary?.name ?: "Unknown",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${primary?.role ?: "Family"} • ${primary?.number ?: ""}",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 15.sp
                    )
                }
            }

            // Bottom: In-Call Control Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Controls Row: Mute, Speaker, 3-Way Add, Hold
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CallControlButton(
                        icon = if (callInfo.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (callInfo.isMuted) "Unmute" else "Mute",
                        isActive = callInfo.isMuted,
                        onClick = onMuteToggle
                    )

                    CallControlButton(
                        icon = if (callInfo.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        label = "Speaker",
                        isActive = callInfo.isSpeakerOn,
                        onClick = onSpeakerToggle
                    )

                    CallControlButton(
                        icon = Icons.Default.GroupAdd,
                        label = if (callInfo.isThreeWay) "3-Way Active" else "Add Call",
                        isActive = callInfo.isThreeWay,
                        onClick = {
                            if (!callInfo.isThreeWay) {
                                showAddParticipantSheet = true
                            }
                        }
                    )

                    CallControlButton(
                        icon = if (callInfo.isHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                        label = if (callInfo.isHold) "Resume" else "Hold",
                        isActive = callInfo.isHold,
                        onClick = onHoldToggle
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Big Red End Call Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BentoCallRed)
                        .clickable { onEndCall() }
                        .testTag("end_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet to add 3-Way Participant
    if (showAddParticipantSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddParticipantSheet = false },
            containerColor = Color(0xFF2C243B)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add Family Member (3-Way Call)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Merge another family member into this live conversation.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val available = availableContacts.filter {
                        it.familyNumber != primary?.number
                    }
                    items(available) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable {
                                    onAddParticipant(contact)
                                    showAddParticipantSheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FamilyAvatar(
                                name = contact.name,
                                seed = contact.avatarSeed,
                                size = 44.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${contact.role} • ${contact.familyNumber}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoCallGreen)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Merge",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.15f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF1D1B20) else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
