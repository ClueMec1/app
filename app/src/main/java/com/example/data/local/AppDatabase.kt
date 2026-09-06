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
    version = 3,
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

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            ensureInitialData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun ensureInitialData(database: AppDatabase) {
            val existingProfile = database.userProfileDao().getUserProfile()
            val existingContacts = database.contactDao().getAllContacts()
            val hasMockData = (existingProfile != null && (existingProfile.name == "Elias Miller" || existingProfile.familyNumber == "+88-0421" || existingProfile.name == "Elias")) ||
                    existingContacts.any { it.name in listOf("Sarah Miller", "David Miller", "Liam Miller", "Noah Miller", "Grandma Evelyn") }

            if (hasMockData) {
                database.clearAllTables()
            }

            if (database.userProfileDao().getUserProfile() == null) {
                populateInitialData(database)
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val user = UserProfile(
                id = 1,
                name = "",
                familyNumber = "",
                avatarSeed = "",
                isInitialized = false
            )
            database.userProfileDao().insertOrUpdate(user)
        }
    }
}
