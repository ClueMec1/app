package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer

@Composable
fun FamilyAvatar(
    name: String,
    seed: String = "",
    size: Dp = 48.dp,
    borderColor: Color = BentoPrimary,
    modifier: Modifier = Modifier
) {
    val initial = if (name.isNotBlank()) name.first().uppercase() else "F"
    
    // Aesthetic gradient background based on name hash
    val gradients = listOf(
        listOf(Color(0xFFEADDFF), Color(0xFFD0BCFF)),
        listOf(Color(0xFFD3E3FD), Color(0xFFA8C7FA)),
        listOf(Color(0xFFFAD8FD), Color(0xFFF3B0F9)),
        listOf(Color(0xFFE7F0E0), Color(0xFFC4EDD0)),
        listOf(Color(0xFFFFD8E4), Color(0xFFF2B8B5))
    )
    val colorIndex = Math.abs((name + seed).hashCode()) % gradients.size
    val gradientColors = gradients[colorIndex]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors))
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = (size.value * 0.44f).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )
    }
}
