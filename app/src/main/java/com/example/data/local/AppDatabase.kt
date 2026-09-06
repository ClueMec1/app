package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CallRecord
import com.example.data.model.ChatMessage
import com.example.data.model.FamilyContact
import com.example.data.model.FamilyGroup
import com.example.data.model.NeumaiMemory
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        FamilyContact::class,
        FamilyGroup::class,
        ChatMessage::class,
        CallRecord::class,
        NeumaiMemory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun contactDao(): ContactDao
    abstract fun familyGroupDao(): FamilyGroupDao
    abstract fun chatDao(): ChatDao
    abstract fun callRecordDao(): CallRecordDao
    abstract fun neumaiMemoryDao(): NeumaiMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "family_phone.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            populateInitialData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val user = UserProfile(
                id = 1,
                name = "Elias Miller",
                familyNumber = "+88-0421",
                avatarSeed = "Elias",
                isInitialized = true
            )
            database.userProfileDao().insertOrUpdate(user)

            val contacts = listOf(
                FamilyContact(
                    familyNumber = "+88-0101",
                    name = "Sarah Miller",
                    role = "Mother",
                    avatarSeed = "Sarah",
                    isOnline = true,
                    statusText = "Online • Home",
                    cardColorType = "blue"
                ),
                FamilyContact(
                    familyNumber = "+88-0102",
                    name = "David Miller",
                    role = "Father",
                    avatarSeed = "David",
                    isOnline = true,
                    statusText = "Wi-Fi Ready",
                    cardColorType = "green"
                ),
                FamilyContact(
                    familyNumber = "+88-0203",
                    name = "Liam Miller",
                    role = "Brother",
                    avatarSeed = "Liam",
                    fatherName = "David Miller",
                    motherName = "Sarah Miller",
                    isOnline = true,
                    statusText = "Active 5m ago",
                    cardColorType = "purple"
                ),
                FamilyContact(
                    familyNumber = "+88-0204",
                    name = "Noah Miller",
                    role = "Brother",
                    avatarSeed = "Noah",
                    fatherName = "David Miller",
                    motherName = "Sarah Miller",
                    isOnline = false,
                    statusText = "Studying • Away",
                    cardColorType = "blue"
                ),
                FamilyContact(
                    familyNumber = "+88-0300",
                    name = "Grandma Evelyn",
                    role = "Grandparent",
                    avatarSeed = "Evelyn",
                    isOnline = true,
                    statusText = "Kitchen Wi-Fi",
                    cardColorType = "pink"
                )
            )
            database.contactDao().insertContacts(contacts)

            val group1 = FamilyGroup(
                id = 1,
                name = "Sunday BBQ",
                memberNumbers = "+88-0101,+88-0102,+88-0203,+88-0204,+88-0421",
                description = "Weekend family dinner & gatherings"
            )
            val group2 = FamilyGroup(
                id = 2,
                name = "Miller Siblings",
                memberNumbers = "+88-0203,+88-0204,+88-0421",
                description = "Brothers hangout group"
            )
            database.familyGroupDao().insertGroup(group1)
            database.familyGroupDao().insertGroup(group2)

            val initialMemories = listOf(
                NeumaiMemory(
                    keySubject = "Sarah",
                    fact = "Sarah's birthday is May 14th, and her favorite flowers are yellow tulips.",
                    category = "BIRTHDAY",
                    recordedBy = "Elias"
                ),
                NeumaiMemory(
                    keySubject = "David",
                    fact = "David prefers dark roast coffee with a splash of oat milk and zero sugar.",
                    category = "PREFERENCE",
                    recordedBy = "Sarah"
                ),
                NeumaiMemory(
                    keySubject = "Wi-Fi",
                    fact = "Home Wi-Fi network is 'Neuman-Family-5G' with password 'FamilyHome2024'.",
                    category = "LOCATION",
                    recordedBy = "David"
                ),
                NeumaiMemory(
                    keySubject = "Noah",
                    fact = "Noah is allergic to shellfish and peanuts.",
                    category = "ALLERGY",
                    recordedBy = "Sarah"
                ),
                NeumaiMemory(
                    keySubject = "Sunday BBQ",
                    fact = "Sunday family BBQ starts at 5:00 PM in the backyard every weekend.",
                    category = "SCHEDULE",
                    recordedBy = "Elias"
                )
            )
            for (mem in initialMemories) {
                database.neumaiMemoryDao().insertMemory(mem)
            }

            val now = System.currentTimeMillis()
            val sampleMessages = listOf(
                ChatMessage(
                    conversationId = "neumai",
                    senderName = "NEUMAI",
                    senderNumber = "AI",
                    text = "Hey family! 👋 I'm NEUMAI — your dedicated family AI assistant (like Meta AI on WhatsApp). Ask me anything, or tell me anything you want me to remember for our family (e.g. 'Sarah's birthday is May 14' or 'Wi-Fi password is...'). I save it all and keep the whole family updated! 🧠✨",
                    timestamp = now - 86400000,
                    isFromMe = false
                ),
                ChatMessage(
                    conversationId = "group_1",
                    senderName = "Sarah Miller",
                    senderNumber = "+88-0101",
                    text = "Don't forget to call Dad before BBQ on Sunday!",
                    timestamp = now - 3600000,
                    isFromMe = false
                ),
                ChatMessage(
                    conversationId = "group_1",
                    senderName = "Liam Miller",
                    senderNumber = "+88-0203",
                    text = "I'll bring the grill skewers!",
                    timestamp = now - 1800000,
                    isFromMe = false
                ),
                ChatMessage(
                    conversationId = "+88-0101",
                    senderName = "Sarah Miller",
                    senderNumber = "+88-0101",
                    text = "Hi Elias, are you connected to home Wi-Fi?",
                    timestamp = now - 7200000,
                    isFromMe = false
                )
            )
            for (msg in sampleMessages) {
                database.chatDao().insertMessage(msg)
            }

            val sampleCalls = listOf(
                CallRecord(
                    callerName = "Sarah Miller",
                    callerNumber = "+88-0101",
                    type = "INCOMING",
                    timestamp = now - 1000 * 60 * 45,
                    durationSeconds = 142
                ),
                CallRecord(
                    callerName = "David Miller",
                    callerNumber = "+88-0102",
                    type = "OUTGOING",
                    timestamp = now - 1000 * 60 * 120,
                    durationSeconds = 75
                ),
                CallRecord(
                    callerName = "Liam Miller",
                    callerNumber = "+88-0203",
                    type = "THREE_WAY",
                    timestamp = now - 1000 * 60 * 300,
                    durationSeconds = 310,
                    secondaryParticipant = "Sarah Miller"
                )
            )
            for (call in sampleCalls) {
                database.callRecordDao().insertCall(call)
            }
        }
    }
}
