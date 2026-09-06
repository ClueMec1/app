package com.example.data.engine

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.NeumaiMemoryDao
import com.example.data.model.NeumaiMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object NeumaiEngine {

    private const val TAG = "NeumaiEngine"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    data class EngineResponse(
        val replyText: String,
        val newlySavedMemory: NeumaiMemory? = null
    )

    suspend fun processUserMessage(
        userMessage: String,
        senderName: String,
        memoryDao: NeumaiMemoryDao
    ): EngineResponse = withContext(Dispatchers.IO) {
        val trimmed = userMessage.trim()
        val allMemories = memoryDao.getAllMemories()

        // 1. Check if the message is teaching / saving a family memory
        val detectedMemory = detectAndExtractMemory(trimmed, senderName)
        if (detectedMemory != null) {
            memoryDao.insertMemory(detectedMemory)
            val ackText = buildSavedAckText(detectedMemory)
            return@withContext EngineResponse(
                replyText = ackText,
                newlySavedMemory = detectedMemory
            )
        }

        // 2. Check if user is asking to list all memories
        if (isListMemoriesQuery(trimmed)) {
            val listReply = buildAllMemoriesSummary(allMemories)
            return@withContext EngineResponse(replyText = listReply)
        }

        // 3. Try Gemini REST API if valid key is available
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "default_key") {
            try {
                val geminiReply = callGeminiApi(trimmed, senderName, allMemories, apiKey)
                if (geminiReply.isNotBlank()) {
                    return@withContext EngineResponse(replyText = geminiReply)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to local memory engine", e)
            }
        }

        // 4. Intelligent Local Family Memory & Q&A Engine
        val localReply = answerWithLocalMemory(trimmed, senderName, allMemories)
        EngineResponse(replyText = localReply)
    }

    private fun detectAndExtractMemory(input: String, senderName: String): NeumaiMemory? {
        val lower = input.lowercase()

        // Explicit "remember that..." or "save this:..." or "don't forget that..."
        val rememberPrefixes = listOf(
            "remember that ", "remember ", "save this: ", "save this ",
            "note that ", "note down: ", "note: ", "don't forget that ",
            "don't forget ", "make sure to remember that ", "teach you that ",
            "just so you know, ", "just so you know "
        )
        for (prefix in rememberPrefixes) {
            if (lower.startsWith(prefix)) {
                val factText = input.substring(prefix.length).trim().removeSuffix(".")
                val subject = extractSubject(factText)
                val category = categorizeFact(factText)
                return NeumaiMemory(
                    keySubject = subject,
                    fact = factText,
                    category = category,
                    recordedBy = senderName
                )
            }
        }

        // Birthday patterns: "Sarah's birthday is May 14th" or "My birthday is..."
        val birthdayRegex = Regex("(?i)(.*?)'?s?\\s+birthday\\s+is\\s+(.*)")
        val birthdayMatch = birthdayRegex.find(input)
        if (birthdayMatch != null) {
            val subj = birthdayMatch.groupValues[1].trim()
            val cleanSubj = if (subj.equals("my", ignoreCase = true)) senderName else subj
            return NeumaiMemory(
                keySubject = cleanSubj,
                fact = "$cleanSubj's birthday is ${birthdayMatch.groupValues[2].trim().removeSuffix(".")}.",
                category = "BIRTHDAY",
                recordedBy = senderName
            )
        }

        // Allergy patterns: "Noah is allergic to shellfish"
        val allergyRegex = Regex("(?i)(.*?)\\s+is\\s+allergic\\s+to\\s+(.*)")
        val allergyMatch = allergyRegex.find(input)
        if (allergyMatch != null) {
            val subj = allergyMatch.groupValues[1].trim()
            val cleanSubj = if (subj.equals("i", ignoreCase = true)) senderName else subj
            val allergen = allergyMatch.groupValues[2].trim().removeSuffix(".")
            return NeumaiMemory(
                keySubject = cleanSubj,
                fact = "$cleanSubj is allergic to $allergen.",
                category = "ALLERGY",
                recordedBy = senderName
            )
        }

        // Preference patterns: "David likes / loves / prefers dark roast coffee"
        val prefRegex = Regex("(?i)(.*?)\\s+(likes|loves|prefers|favorite\\s+\\w+\\s+is)\\s+(.*)")
        val prefMatch = prefRegex.find(input)
        if (prefMatch != null && !lower.startsWith("what") && !lower.startsWith("who") && !lower.startsWith("does")) {
            val subj = prefMatch.groupValues[1].trim()
            val cleanSubj = if (subj.equals("i", ignoreCase = true)) senderName else subj
            val verb = prefMatch.groupValues[2].trim()
            val detail = prefMatch.groupValues[3].trim().removeSuffix(".")
            return NeumaiMemory(
                keySubject = cleanSubj,
                fact = "$cleanSubj $verb $detail.",
                category = "PREFERENCE",
                recordedBy = senderName
            )
        }

        // Wi-Fi & Password patterns: "The Wi-Fi password is..."
        if (lower.contains("wi-fi") || lower.contains("wifi") || lower.contains("password") || lower.contains("gate code") || lower.contains("passcode")) {
            if (lower.contains("is ") && !lower.startsWith("what") && !lower.startsWith("how")) {
                return NeumaiMemory(
                    keySubject = "Wi-Fi & Codes",
                    fact = input.trim().removeSuffix("."),
                    category = "LOCATION",
                    recordedBy = senderName
                )
            }
        }

        // Schedule / Event patterns: "Sunday dinner is at 6 PM"
        if ((lower.contains("dinner is") || lower.contains("bbq is") || lower.contains("practice is") || lower.contains("meeting is"))
            && !lower.startsWith("when") && !lower.startsWith("what")
        ) {
            return NeumaiMemory(
                keySubject = extractSubject(input),
                fact = input.trim().removeSuffix("."),
                category = "SCHEDULE",
                recordedBy = senderName
            )
        }

        return null
    }

    private fun extractSubject(fact: String): String {
        val words = fact.split(" ")
        val firstTwo = words.take(2).joinToString(" ").removeSuffix("'s")
        return when {
            fact.contains("wi-fi", ignoreCase = true) || fact.contains("wifi", ignoreCase = true) -> "Wi-Fi"
            fact.contains("bbq", ignoreCase = true) -> "Sunday BBQ"
            fact.contains("grandma", ignoreCase = true) -> "Grandma Evelyn"
            fact.contains("mom", ignoreCase = true) || fact.contains("sarah", ignoreCase = true) -> "Sarah"
            fact.contains("dad", ignoreCase = true) || fact.contains("david", ignoreCase = true) -> "David"
            fact.contains("liam", ignoreCase = true) -> "Liam"
            fact.contains("noah", ignoreCase = true) -> "Noah"
            else -> firstTwo.replaceFirstChar { it.uppercase() }
        }
    }

    private fun categorizeFact(fact: String): String {
        val lower = fact.lowercase()
        return when {
            lower.contains("birthday") || lower.contains("born") || lower.contains("age") -> "BIRTHDAY"
            lower.contains("allergic") || lower.contains("allergy") || lower.contains("medical") -> "ALLERGY"
            lower.contains("like") || lower.contains("love") || lower.contains("prefer") || lower.contains("favorite") -> "PREFERENCE"
            lower.contains("wi-fi") || lower.contains("wifi") || lower.contains("password") || lower.contains("address") || lower.contains("code") -> "LOCATION"
            lower.contains("pm") || lower.contains("am") || lower.contains("sunday") || lower.contains("dinner") || lower.contains("schedule") -> "SCHEDULE"
            else -> "NOTE"
        }
    }

    private fun buildSavedAckText(memory: NeumaiMemory): String {
        val catEmoji = when (memory.category) {
            "BIRTHDAY" -> "🎂"
            "ALLERGY" -> "⚠️"
            "PREFERENCE" -> "❤️"
            "LOCATION" -> "🔑"
            "SCHEDULE" -> "📅"
            else -> "📝"
        }
        return "🧠 **Saved to NEUMAI Family Memory!** $catEmoji\n\n" +
                "I've committed this fact to our shared family knowledge base:\n" +
                "> \"${memory.fact}\"\n\n" +
                "• **Subject:** ${memory.keySubject}\n" +
                "• **Category:** ${memory.category}\n" +
                "• **Saved by:** ${memory.recordedBy}\n\n" +
                "Any family member can now ask me about this anytime!"
    }

    private fun isListMemoriesQuery(query: String): Boolean {
        val lower = query.lowercase().trim()
        return lower.contains("what do you know") ||
                lower.contains("list memories") ||
                lower.contains("all memories") ||
                lower.contains("what have you saved") ||
                lower.contains("show memories") ||
                lower.contains("family facts") ||
                lower == "memories"
    }

    private fun buildAllMemoriesSummary(memories: List<NeumaiMemory>): String {
        if (memories.isEmpty()) {
            return "🧠 **NEUMAI Family Memory Vault**\n\nI haven't saved any family memories yet! Tell me things to remember like birthdays, allergies, favorite dishes, or Wi-Fi passwords and I'll keep them safe for everyone."
        }

        val sb = StringBuilder()
        sb.append("🧠 **NEUMAI Family Memory Vault (${memories.size} facts)**\n\n")
        val grouped = memories.groupBy { it.category }

        grouped.forEach { (category, list) ->
            val icon = when (category) {
                "BIRTHDAY" -> "🎂 Birthdays"
                "ALLERGY" -> "⚠️ Allergies & Health"
                "PREFERENCE" -> "❤️ Favorites & Preferences"
                "LOCATION" -> "🔑 Wi-Fi & Home Codes"
                "SCHEDULE" -> "📅 Family Schedules"
                else -> "📝 Family Notes"
            }
            sb.append("**$icon**\n")
            list.forEach { mem ->
                sb.append("• ${mem.fact} *(by ${mem.recordedBy})*\n")
            }
            sb.append("\n")
        }
        sb.append("Ask me anything about these anytime!")
        return sb.toString().trim()
    }

    private fun answerWithLocalMemory(
        query: String,
        senderName: String,
        memories: List<NeumaiMemory>
    ): String {
        val lower = query.lowercase()

        // Greetings
        if (lower in listOf("hi", "hello", "hey", "hola", "sup", "yo", "good morning", "good evening")) {
            return "Hey $senderName! 👋 I'm **NEUMAI**, your family AI assistant. I have ${memories.size} family memories saved in my vault. What would you like to know, or what should I remember for the family today?"
        }

        // Search for relevant memories
        val matchedMemories = memories.filter { memory ->
            val subMatch = memory.keySubject.isNotBlank() && lower.contains(memory.keySubject.lowercase())
            val factWords = memory.fact.lowercase().split(" ", ",", ".", "'").filter { it.length > 3 }
            val factMatch = factWords.any { word -> lower.contains(word) }
            val catMatch = lower.contains(memory.category.lowercase())
            subMatch || factMatch || catMatch
        }

        if (matchedMemories.isNotEmpty()) {
            val sb = StringBuilder()
            val primary = matchedMemories.first()
            sb.append("🧠 According to our family memory:\n\n")
            matchedMemories.take(3).forEach { mem ->
                sb.append("• **${mem.fact}** *(Saved by ${mem.recordedBy})*\n")
            }
            sb.append("\nIs there anything else you'd like me to save or check for the family?")
            return sb.toString()
        }

        // Default friendly family response
        return "I heard you, $senderName! 😊 As **NEUMAI**, I keep track of all our family's details. I don't have a specific note about that in the memory vault yet. \n\nIf you want me to remember it, just tell me: *\"Remember that [info]\"* or *\"Sarah's birthday is [date]\"* and I will save it for everyone!"
    }

    private suspend fun callGeminiApi(
        userMessage: String,
        senderName: String,
        memories: List<NeumaiMemory>,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val memoriesContext = if (memories.isEmpty()) {
            "No family memories saved yet."
        } else {
            memories.joinToString("\n") { "- [${it.category}] (${it.keySubject}): ${it.fact} (Saved by ${it.recordedBy})" }
        }

        val systemPrompt = """
            You are NEUMAI (pronounced "New-M-A-I"), a clever, affectionate, and witty family AI assistant built specifically for the Neuman family (a humorous blend of the family name and AI, just like Meta AI on WhatsApp).
            
            You have a persistent Family Memory Vault containing facts saved by family members:
            $memoriesContext
            
            Guidelines:
            1. Warm, witty, and deeply family-oriented tone.
            2. When the user asks a question about family members, schedules, Wi-Fi, allergies, or favorites, consult the Family Memory Vault above and answer directly with accuracy.
            3. If the user is teaching you something new, thank them playfully and confirm it's now locked into NEUMAI family memory.
            4. Keep responses concise and conversational (ideal for a mobile chat interface).
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "From family member $senderName: $userMessage"))
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext ""

        if (!response.isSuccessful) {
            Log.w(TAG, "Gemini error: code=${response.code} body=$responseBody")
            return@withContext ""
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates") ?: return@withContext ""
        if (candidates.length() == 0) return@withContext ""

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return@withContext ""
        val parts = content.optJSONArray("parts") ?: return@withContext ""
        if (parts.length() == 0) return@withContext ""

        parts.getJSONObject(0).optString("text", "")
    }
}
