package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyContact
import com.example.data.model.FamilyGroup
import com.example.ui.theme.BentoCardBlue
import com.example.ui.theme.BentoCardBlueText
import com.example.ui.theme.BentoCardGreen
import com.example.ui.theme.BentoCardGreenText
import com.example.ui.theme.BentoCardPink
import com.example.ui.theme.BentoCardPinkText
import com.example.ui.theme.BentoPrimary

@Composable
fun BentoQuickGrid(
    contacts: List<FamilyContact>,
    groups: List<FamilyGroup> = emptyList(),
    onContactCall: (FamilyContact) -> Unit,
    onGroupClick: () -> Unit,
    onNeumaiClick: () -> Unit,
    onAddContactClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val topContact = contacts.firstOrNull { it.role.equals("Mother", ignoreCase = true) }
        ?: contacts.firstOrNull()
    val secondContact = contacts.firstOrNull { it != topContact }
    val groupName = groups.firstOrNull()?.name ?: "Family Chat"

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top 2-row Bento section: Left is Tall Card (Mother/Contact), Right is 2 stacked cards (Group & Contact 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Primary Contact or Add Contact
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BentoCardBlue)
                    .clickable {
                        if (topContact != null) {
                            onContactCall(topContact)
                        } else {
                            onAddContactClick()
                        }
                    }
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (topContact != null) topContact.role.uppercase() else "DIRECTORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = BentoCardBlueText
                        )
                        if (topContact != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34A853))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = BentoCardBlueText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (topContact != null) topContact.name else "Add Family",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoCardBlueText
                        )
                        Text(
                            text = if (topContact != null) topContact.statusText else "Tap to add contact",
                            fontSize = 11.sp,
                            color = BentoCardBlueText.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Right column: Group + Contact 2
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 2: Family Group
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BentoCardPink)
                        .clickable { onGroupClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = BentoCardPinkText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = groupName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoCardPinkText,
                        maxLines = 1
                    )
                }

                // Card 3: Second Contact or Fast Dial
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BentoCardGreen)
                        .clickable {
                            if (secondContact != null) {
                                onContactCall(secondContact)
                            } else if (topContact != null) {
                                onContactCall(topContact)
                            } else {
                                onAddContactClick()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = secondContact?.name?.split(" ")?.firstOrNull() ?: if (contacts.isNotEmpty()) "Fast Dial" else "Wi-Fi Call",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoCardGreenText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.55f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (secondContact?.isOnline == true) "Online" else "Ready",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoCardGreenText.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Card 4: NEUMAI Family AI (col-span-2 row-span-1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF312E81), // Deep Indigo
                            Color(0xFF4338CA), // Royal Indigo
                            Color(0xFF6D28D9)  // Deep Purple
                        )
                    )
                )
                .clickable { onNeumaiClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF38BDF8), // Cyan
                                    Color(0xFF818CF8), // Indigo
                                    Color(0xFFF472B6), // Pink
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
                            contentDescription = "NEUMAI",
                            tint = Color(0xFFE0E7FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NEUMAI",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEC4899).copy(alpha = 0.3f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "FAMILY AI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBCFE8)
                            )
                        }
                    }
                    Text(
                        text = "Ask anything • Remembers our family info",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Chat →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
