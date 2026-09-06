package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.NeumaiEngine
import com.example.data.local.AppDatabase
import com.example.data.model.CallRecord
import com.example.data.model.ChatMessage
import com.example.data.model.FamilyContact
import com.example.data.model.FamilyGroup
import com.example.data.model.NeumaiMemory
import com.example.data.model.UserProfile
import com.example.service.ActiveCallInfo
import com.example.service.CallManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyPhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val userProfile: StateFlow<UserProfile> = db.userProfileDao().getUserProfileFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UserProfile()
        ) as StateFlow<UserProfile>

    val contacts: StateFlow<List<FamilyContact>> = db.contactDao().getAllContactsFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val groups: StateFlow<List<FamilyGroup>> = db.familyGroupDao().getAllGroupsFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val messages: StateFlow<List<ChatMessage>> = db.chatDao().getAllRecentMessagesFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val neumaiMemories: StateFlow<List<NeumaiMemory>> = db.neumaiMemoryDao().getAllMemoriesFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val callRecords: StateFlow<List<CallRecord>> = db.callRecordDao().getAllCallsFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val callState: StateFlow<ActiveCallInfo> = CallManager.callState

    private val _isNeumaiTyping = MutableStateFlow(false)
    val isNeumaiTyping: StateFlow<Boolean> = _isNeumaiTyping.asStateFlow()

    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            db.userProfileDao().insertOrUpdate(profile)
        }
    }

    fun addContact(contact: FamilyContact) {
        viewModelScope.launch(Dispatchers.IO) {
            db.contactDao().insertContact(contact)
        }
    }

    fun createGroup(name: String, members: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.familyGroupDao().insertGroup(
                FamilyGroup(
                    name = name,
                    memberNumbers = members,
                    description = "Family Group"
                )
            )
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        val currentProfile = userProfile.value
        val now = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            db.chatDao().insertMessage(
                ChatMessage(
                    conversationId = conversationId,
                    senderName = currentProfile.name,
                    senderNumber = currentProfile.familyNumber,
                    text = text,
                    timestamp = now,
                    isFromMe = true
                )
            )

            // If messaging NEUMAI, generate intelligent AI response with memory awareness
            if (conversationId == "neumai") {
                _isNeumaiTyping.value = true
                delay(600) // Brief natural pause
                val response = NeumaiEngine.processUserMessage(
                    userMessage = text,
                    senderName = currentProfile.name,
                    memoryDao = db.neumaiMemoryDao()
                )
                _isNeumaiTyping.value = false

                db.chatDao().insertMessage(
                    ChatMessage(
                        conversationId = "neumai",
                        senderName = "NEUMAI",
                        senderNumber = "AI",
                        text = response.replyText,
                        timestamp = System.currentTimeMillis(),
                        isFromMe = false
                    )
                )
            }
        }
    }

    fun saveFamilyMemory(subject: String, fact: String, category: String) {
        val currentProfile = userProfile.value
        viewModelScope.launch(Dispatchers.IO) {
            db.neumaiMemoryDao().insertMemory(
                NeumaiMemory(
                    keySubject = subject,
                    fact = fact,
                    category = category,
                    recordedBy = currentProfile.name
                )
            )
        }
    }

    fun deleteFamilyMemory(memoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.neumaiMemoryDao().deleteMemoryById(memoryId)
        }
    }

    fun clearFamilyMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            db.neumaiMemoryDao().clearAllMemories()
        }
    }

    fun startCall(context: Context, name: String, number: String, role: String, avatar: String) {
        CallManager.startOutgoingCall(context, name, number, role, avatar)
    }

    fun simulateIncomingCall(context: Context) {
        val mother = contacts.value.firstOrNull { it.role.equals("Mother", ignoreCase = true) }
            ?: contacts.value.firstOrNull()
        val name = mother?.name ?: "Sarah Miller"
        val number = mother?.familyNumber ?: "+88-0101"
        val role = mother?.role ?: "Mother"
        val avatar = mother?.avatarSeed ?: "Sarah"
        CallManager.triggerIncomingCall(context, name, number, role, avatar)
    }

    fun answerCall(context: Context) {
        CallManager.answerCall(context)
    }

    fun endCall(context: Context) {
        CallManager.endCall(context)
    }

    fun toggleMute() {
        CallManager.toggleMute()
    }

    fun toggleSpeaker() {
        CallManager.toggleSpeaker()
    }

    fun toggleHold() {
        CallManager.toggleHold()
    }

    fun addThreeWayParticipant(contact: FamilyContact) {
        CallManager.addThreeWayParticipant(
            name = contact.name,
            number = contact.familyNumber,
            role = contact.role,
            avatar = contact.avatarSeed
        )
    }

    fun clearCallHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            db.callRecordDao().clearHistory()
        }
    }
}
