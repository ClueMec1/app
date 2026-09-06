package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var stateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        listenToCallState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CALL, ACTION_UPDATE_CALL -> {
                val notification = buildCallNotification(CallManager.callState.value)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        startForeground(
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                        )
                    } catch (e: Exception) {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
            ACTION_ANSWER_CALL -> {
                CallManager.answerCall(this)
            }
            ACTION_TOGGLE_MUTE -> {
                CallManager.toggleMute()
            }
            ACTION_TOGGLE_SPEAKER -> {
                CallManager.toggleSpeaker()
            }
            ACTION_END_CALL -> {
                CallManager.endCall(this)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun listenToCallState() {
        stateJob?.cancel()
        stateJob = serviceScope.launch {
            CallManager.callState.collectLatest { info ->
                if (info.state == CallState.IDLE || info.state == CallState.ENDED) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, buildCallNotification(info))
                }
            }
        }
    }

    private fun buildCallNotification(info: ActiveCallInfo): Notification {
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endCallIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_END_CALL
        }
        val endPendingIntent = PendingIntent.getService(
            this,
            1,
            endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val primary = info.primaryParticipant
        val callerTitle = when {
            info.isThreeWay && info.secondaryParticipant != null ->
                "3-Way Family Call: ${primary?.name} & ${info.secondaryParticipant.name}"
            primary != null ->
                "${primary.name} (${primary.role})"
            else -> "Family Wi-Fi Phone"
        }

        val formattedTime = String.format(
            "%02d:%02d",
            info.durationSeconds / 60,
            info.durationSeconds % 60
        )

        val statusSubtitle = when (info.state) {
            CallState.DIALING -> "Dialing via Family Wi-Fi..."
            CallState.INCOMING -> "Incoming Family Call"
            CallState.CONNECTED -> "Ongoing Call • $formattedTime (Wi-Fi Audio Active)"
            else -> "Family Phone"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle(callerTitle)
            .setContentText(statusSubtitle)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (info.state == CallState.INCOMING) {
            val answerIntent = Intent(this, CallService::class.java).apply {
                action = ACTION_ANSWER_CALL
            }
            val answerPendingIntent = PendingIntent.getService(
                this,
                2,
                answerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.sym_action_call, "Answer", answerPendingIntent)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", endPendingIntent)
        } else {
            val muteIntent = Intent(this, CallService::class.java).apply {
                action = ACTION_TOGGLE_MUTE
            }
            val mutePendingIntent = PendingIntent.getService(
                this,
                3,
                muteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val muteLabel = if (info.isMuted) "Unmute" else "Mute"
            builder.addAction(android.R.drawable.stat_notify_chat, muteLabel, mutePendingIntent)
            builder.addAction(android.R.drawable.ic_menu_call, "End Call", endPendingIntent)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Family Phone Ongoing Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows live call status and controls for Family Wi-Fi calls"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stateJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "family_phone_calls"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_CALL = "com.example.action.START_CALL"
        const val ACTION_UPDATE_CALL = "com.example.action.UPDATE_CALL"
        const val ACTION_ANSWER_CALL = "com.example.action.ANSWER_CALL"
        const val ACTION_END_CALL = "com.example.action.END_CALL"
        const val ACTION_TOGGLE_MUTE = "com.example.action.TOGGLE_MUTE"
        const val ACTION_TOGGLE_SPEAKER = "com.example.action.TOGGLE_SPEAKER"
    }
}
