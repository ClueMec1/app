package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.data.model.CallRecord
import com.example.ui.components.FamilyAvatar
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoCallGreen
import com.example.ui.theme.BentoCallRed
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallLogScreen(
    callRecords: List<CallRecord>,
    onClearHistory: () -> Unit,
    onRedial: (name: String, number: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredRecords = when (selectedFilter) {
        "MISSED" -> callRecords.filter { it.type == "MISSED" }
        "THREE_WAY" -> callRecords.filter { it.type == "THREE_WAY" }
        else -> callRecords
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Calls",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )

            if (callRecords.isNotEmpty()) {
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BentoSecondaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = BentoTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Filter chips: All, Missed, 3-Way
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL" to "All Calls", "MISSED" to "Missed", "THREE_WAY" to "3-Way Conference")
            items(filters) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BentoPrimaryContainer,
                        selectedLabelColor = BentoPrimary
                    )
                )
            }
        }

        // Call list
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No call records yet",
                    color = BentoTextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecords) { record ->
                    CallRecordItem(
                        record = record,
                        onCall = { onRedial(record.callerName, record.callerNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallRecordItem(
    record: CallRecord,
    onCall: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.timestamp))

    val (icon, iconColor, typeLabel) = when (record.type) {
        "MISSED" -> Triple(Icons.AutoMirrored.Filled.CallMissed, BentoCallRed, "Missed Call")
        "INCOMING" -> Triple(Icons.AutoMirrored.Filled.CallReceived, BentoCallGreen, "Incoming")
        "THREE_WAY" -> Triple(Icons.Default.Group, BentoPrimary, "3-Way Conference")
        else -> Triple(Icons.AutoMirrored.Filled.CallMade, BentoPrimary, "Outgoing")
    }

    val durationText = if (record.durationSeconds > 0) {
        String.format("%02d:%02d", record.durationSeconds / 60, record.durationSeconds % 60)
    } else {
        if (record.type == "MISSED") "Missed" else "Canceled"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
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
            FamilyAvatar(name = record.callerName, size = 42.dp)

            Column {
                Text(
                    text = if (record.secondaryParticipant != null)
                        "${record.callerName} & ${record.secondaryParticipant}"
                    else record.callerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (record.type == "MISSED") BentoCallRed else BentoTextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$typeLabel • $durationText",
                        fontSize = 11.sp,
                        color = BentoTextSecondary
                    )
                }

                Text(
                    text = "$formattedDate • Wi-Fi",
                    fontSize = 10.sp,
                    color = BentoTextSecondary.copy(alpha = 0.7f)
                )
            }
        }

        IconButton(
            onClick = onCall,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BentoCallGreen)
                .testTag("redial_button")
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Redial",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
