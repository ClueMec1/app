package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallRecord
import com.example.data.model.ChatMessage
import com.example.data.model.FamilyContact
import com.example.data.model.FamilyGroup
import com.example.data.model.NeumaiMemory
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM family_contacts ORDER BY name ASC")
    fun getAllContactsFlow(): Flow<List<FamilyContact>>

    @Query("SELECT * FROM family_contacts ORDER BY name ASC")
    suspend fun getAllContacts(): List<FamilyContact>

    @Query("SELECT * FROM family_contacts WHERE familyNumber = :number LIMIT 1")
    suspend fun getContactByNumber(number: String): FamilyContact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: FamilyContact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<FamilyContact>)

    @Query("DELETE FROM family_contacts WHERE familyNumber = :number")
    suspend fun deleteContact(number: String)
}

@Dao
interface FamilyGroupDao {
    @Query("SELECT * FROM family_groups ORDER BY id DESC")
    fun getAllGroupsFlow(): Flow<List<FamilyGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: FamilyGroup): Long

    @Query("DELETE FROM family_groups WHERE id = :id")
    suspend fun deleteGroup(id: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesFlow(conversationId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllRecentMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long
}

@Dao
interface CallRecordDao {
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllCallsFlow(): Flow<List<CallRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(record: CallRecord): Long

    @Query("DELETE FROM call_records")
    suspend fun clearHistory()
}

@Dao
interface NeumaiMemoryDao {
    @Query("SELECT * FROM neumai_memories ORDER BY timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<NeumaiMemory>>

    @Query("SELECT * FROM neumai_memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<NeumaiMemory>

    @Query("SELECT * FROM neumai_memories WHERE keySubject LIKE '%' || :query || '%' OR fact LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<NeumaiMemory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: NeumaiMemory): Long

    @Query("DELETE FROM neumai_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM neumai_memories")
    suspend fun clearAllMemories()
}
