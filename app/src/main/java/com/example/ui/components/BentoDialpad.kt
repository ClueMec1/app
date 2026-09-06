package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyContact
import com.example.service.TonePlayer
import com.example.ui.theme.BentoCallGreen
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary

data class KeypadKey(val digit: String, val letters: String)

@Composable
fun BentoDialpad(
    dialedNumber: String,
    matchedContact: FamilyContact?,
    onNumberChange: (String) -> Unit,
    onCallClick: () -> Unit,
    onSimulateIncoming: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        KeypadKey("1", " "),
        KeypadKey("2", "ABC"),
        KeypadKey("3", "DEF"),
        KeypadKey("4", "GHI"),
        KeypadKey("5", "JKL"),
        KeypadKey("6", "MNO"),
        KeypadKey("7", "PQRS"),
        KeypadKey("8", "TUV"),
        KeypadKey("9", "WXYZ"),
        KeypadKey("*", ""),
        KeypadKey("0", "+"),
        KeypadKey("#", "")
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = Color.White.copy(alpha = 0.85f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dialed Number Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (dialedNumber.isEmpty()) "" else dialedNumber,
                        fontSize = if (dialedNumber.length > 10) 22.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = BentoTextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    if (dialedNumber.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (dialedNumber.isNotEmpty()) {
                                    onNumberChange(dialedNumber.dropLast(1))
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("dialpad_backspace_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = BentoTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Matched Contact Chip
            AnimatedVisibility(
                visible = matchedContact != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (matchedContact != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoSecondaryContainer)
                            .clickable { onCallClick() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${matchedContact.name} (${matchedContact.role})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3x4 Grid of Dialpad Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                keys.chunked(3).forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowKeys.forEach { key ->
                            DialKeyButton(
                                key = key,
                                onClick = {
                                    TonePlayer.playDialTone(key.digit.first())
                                    onNumberChange(dialedNumber + key.digit)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Incoming Call Simulation Test Button & Big Call Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Incoming Call Test trigger
                IconButton(
                    onClick = onSimulateIncoming,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BentoSecondaryContainer)
                        .testTag("simulate_incoming_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneCallback,
                        contentDescription = "Test Incoming Call Notification",
                        tint = BentoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Call Button
                val callInteractionSource = remember { MutableInteractionSource() }
                val isCallPressed by callInteractionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(if (isCallPressed) 0.92f else 1.0f)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(BentoCallGreen)
                        .clickable(
                            interactionSource = callInteractionSource,
                            indication = ripple(color = Color.White),
                            onClick = onCallClick
                        )
                        .testTag("dialpad_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Clear / Placeholder to balance layout
                IconButton(
                    onClick = { onNumberChange("") },
                    enabled = dialedNumber.isNotEmpty(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (dialedNumber.isNotEmpty()) BentoSecondaryContainer else Color.Transparent)
                ) {
                    if (dialedNumber.isNotEmpty()) {
                        Text(
                            text = "C",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialKeyButton(
    key: KeypadKey,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(54.dp)
            .scale(if (isPressed) 0.93f else 1.0f)
            .clip(CircleShape)
            .background(if (isPressed) BentoSecondaryContainer else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = BentoPrimary.copy(alpha = 0.2f), bounded = true),
                onClick = onClick
            )
            .testTag("dialkey_${key.digit}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = key.digit,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoTextPrimary,
                lineHeight = 22.sp
            )
            if (key.letters.isNotBlank()) {
                Text(
                    text = key.letters,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp,
                    color = BentoTextSecondary.copy(alpha = 0.7f),
                    lineHeight = 9.sp
                )
            }
        }
    }
}
