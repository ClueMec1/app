package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Elias Miller",
    val familyNumber: String = "+88-0421",
    val avatarSeed: String = "Elias",
    val fatherName: String = "",
    val motherName: String = "",
    val siblings: String = "",
    val isInitialized: Boolean = true
)

@Entity(tableName = "neumai_memories")
data class NeumaiMemory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keySubject: String,    // Subject e.g. "Sarah", "David", "Wi-Fi", "Noah", "Sunday BBQ"
    val fact: String,          // The piece of information e.g. "Sarah's birthday is May 14th"
    val category: String,      // "BIRTHDAY", "PREFERENCE", "ALLERGY", "SCHEDULE", "LOCATION", "NOTE"
    val recordedBy: String,    // Who told NEUMAI (e.g. "Elias")
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_contacts")
data class FamilyContact(
    @PrimaryKey val familyNumber: String,
    val name: String,
    val role: String, // "Mother", "Father", "Brother", "Sister", "Son", "Daughter", etc.
    val avatarSeed: String,
    val fatherName: String = "",
    val motherName: String = "",
    val isOnline: Boolean = true,
    val statusText: String = "Online • Home",
    val cardColorType: String = "blue" // "blue", "pink", "green", "purple"
)

@Entity(tableName = "family_groups")
data class FamilyGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val memberNumbers: String, // comma-separated family numbers
    val description: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String, // target familyNumber or "group_{id}"
    val senderName: String,
    val senderNumber: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true
)

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerName: String,
    val callerNumber: String,
    val type: String, // "INCOMING", "OUTGOING", "MISSED", "THREE_WAY"
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val secondaryParticipant: String? = null // For 3-way calls
)
