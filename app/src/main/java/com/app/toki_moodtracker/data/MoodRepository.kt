package com.app.toki_moodtracker.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

class MoodRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mood_data", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

    suspend fun saveMoodEntry(mood: MoodEntry) {
        val json = gson.toJson(mood)
        prefs.edit().putString(mood.id.toString(), json).apply()
    }

    suspend fun getMoodEntry(id: Long): MoodEntry? {
        val json = prefs.getString(id.toString(), null)
        return if (json != null) {
            gson.fromJson(json, MoodEntry::class.java)
        } else null
    }

    suspend fun getAllMoodEntries(): List<MoodEntry> {
        return prefs.all.mapNotNull { (_, value) ->
            try {
                gson.fromJson(value as String, MoodEntry::class.java)
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.date }
    }

    suspend fun getLatestMoodEntry(userId: String): MoodEntry? {
        return getAllMoodEntries()
            .filter { it.userId == userId }
            .maxByOrNull { it.date }
    }

    suspend fun updateMoodEntry(entry: MoodEntry) {
        saveMoodEntry(entry)
    }

    suspend fun deleteMoodEntry(id: Long) {
        prefs.edit().remove(id.toString()).apply()
    }

    suspend fun clearAllMoodEntries() {
        prefs.edit().clear().apply()
    }
} 