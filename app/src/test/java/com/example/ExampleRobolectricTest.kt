package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.NeumaiEngine
import com.example.data.local.AppDatabase
import com.example.data.model.NeumaiMemory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FamilyPhone", appName)
    }

    @Test
    fun `neumai engine automatically extracts and saves family facts`() = runBlocking {
        val userMessage = "Sarah's birthday is May 14th"
        val response = NeumaiEngine.processUserMessage(
            userMessage = userMessage,
            senderName = "Elias",
            memoryDao = db.neumaiMemoryDao()
        )

        assertNotNull(response.newlySavedMemory)
        assertEquals("Sarah", response.newlySavedMemory?.keySubject)
        assertEquals("BIRTHDAY", response.newlySavedMemory?.category)
        assertTrue(response.replyText.contains("Saved to NEUMAI Family Memory"))

        // Verify stored in DAO
        val memories = db.neumaiMemoryDao().getAllMemories()
        assertEquals(1, memories.size)
        assertEquals("Sarah", memories.first().keySubject)
    }

    @Test
    fun `neumai answers family queries based on saved memory`() = runBlocking {
        // Seed a memory
        db.neumaiMemoryDao().insertMemory(
            NeumaiMemory(
                keySubject = "Wi-Fi",
                fact = "Home Wi-Fi password is FamilyHome2024",
                category = "LOCATION",
                recordedBy = "David"
            )
        )

        val query = "What is the Wi-Fi password?"
        val response = NeumaiEngine.processUserMessage(
            userMessage = query,
            senderName = "Elias",
            memoryDao = db.neumaiMemoryDao()
        )

        assertTrue(response.replyText.contains("FamilyHome2024"))
    }
}
