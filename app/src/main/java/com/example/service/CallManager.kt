package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.model.CallRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class CallState {
    IDLE,
    DIALING,
    INCOMING,
    CONNECTED,
    ENDED
}

data class CallParticipant(
    val name: String,
    val number: String,
    val role: String = "Family",
    val avatarSeed: String = "User"
)

data class ActiveCallInfo(
    val state: CallState = CallState.IDLE,
    val primaryParticipant: CallParticipant? = null,
    val secondaryParticipant: CallParticipant? = null,
    val isThreeWay: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isHold: Boolean = false,
    val durationSeconds: Int = 0
)

object CallManager {
    private val _callState = MutableStateFlow(ActiveCallInfo())
    val callState = _callState.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun startOutgoingCall(context: Context, name: String, number: String, role: String, avatar: String = "") {
        val seed = if (avatar.isNotBlank()) avatar else name.split(" ").firstOrNull() ?: "Elias"
        _callState.value = ActiveCallInfo(
            state = CallState.DIALING,
            primaryParticipant = CallParticipant(name, number, role, seed),
            isThreeWay = false,
            durationSeconds = 0
        )
        startService(context)

        // Automatically connect after a realistic 2.5 second ring
        scope.launch {
            delay(2500)
            if (_callState.value.state == CallState.DIALING) {
                _callState.value = _callState.value.copy(state = CallState.CONNECTED)
                startTimer()
                updateNotification(context)
            }
        }
    }

    fun triggerIncomingCall(context: Context, name: String, number: String, role: String, avatar: String = "") {
        val seed = if (avatar.isNotBlank()) avatar else name.split(" ").firstOrNull() ?: "Sarah"
        _callState.value = ActiveCallInfo(
            state = CallState.INCOMING,
            primaryParticipant = CallParticipant(name, number, role, seed),
            isThreeWay = false,
            durationSeconds = 0
        )
        startService(context)
    }

    fun answerCall(context: Context) {
        if (_callState.value.state == CallState.INCOMING) {
            _callState.value = _callState.value.copy(state = CallState.CONNECTED)
            startTimer()
            updateNotification(context)
        }
    }

    fun addThreeWayParticipant(name: String, number: String, role: String, avatar: String = "") {
        val current = _callState.value
        if (current.state == CallState.CONNECTED) {
            val seed = if (avatar.isNotBlank()) avatar else name.split(" ").firstOrNull() ?: "Liam"
            _callState.value = current.copy(
                secondaryParticipant = CallParticipant(name, number, role, seed),
                isThreeWay = true
            )
        }
    }

    fun toggleMute() {
        _callState.value = _callState.value.copy(isMuted = !_callState.value.isMuted)
    }

    fun toggleSpeaker() {
        _callState.value = _callState.value.copy(isSpeakerOn = !_callState.value.isSpeakerOn)
    }

    fun toggleHold() {
        _callState.value = _callState.value.copy(isHold = !_callState.value.isHold)
    }

    fun endCall(context: Context) {
        val current = _callState.value
        TonePlayer.playCallEndTone()

        // Save call log in Room
        val primary = current.primaryParticipant
        if (primary != null) {
            val callType = when {
                current.isThreeWay -> "THREE_WAY"
                current.state == CallState.INCOMING && current.durationSeconds == 0 -> "MISSED"
                current.state == CallState.INCOMING -> "INCOMING"
                else -> "OUTGOING"
            }

            scope.launch(Dispatchers.IO) {
                AppDatabase.getInstance(context).callRecordDao().insertCall(
                    CallRecord(
                        callerName = primary.name,
                        callerNumber = primary.number,
                        type = callType,
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = current.durationSeconds,
                        secondaryParticipant = current.secondaryParticipant?.name
                    )
                )
            }
        }

        timerJob?.cancel()
        timerJob = null
        _callState.value = ActiveCallInfo(state = CallState.ENDED)

        scope.launch {
            delay(500)
            _callState.value = ActiveCallInfo(state = CallState.IDLE)
            stopService(context)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                _callState.value = _callState.value.copy(
                    durationSeconds = _callState.value.durationSeconds + 1
                )
            }
        }
    }

    private fun startService(context: Context) {
        val intent = Intent(context, CallService::class.java).apply {
            action = CallService.ACTION_START_CALL
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopService(context: Context) {
        val intent = Intent(context, CallService::class.java).apply {
            action = CallService.ACTION_END_CALL
        }
        context.startService(intent)
    }

    private fun updateNotification(context: Context) {
        val intent = Intent(context, CallService::class.java).apply {
            action = CallService.ACTION_UPDATE_CALL
        }
        context.startService(intent)
    }
}
