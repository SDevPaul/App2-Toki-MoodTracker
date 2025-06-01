package com.app.toki_moodtracker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class MoodEntry(
    val id: Long = System.currentTimeMillis(),
    val mood: String,
    val note: String,
    val reflection: String = "",
    private val _date: Date = Date(),
    val userId: String
) : Parcelable {
    val date: Date
        get() = try {
            Date(_date.time)
        } catch (e: Exception) {
            Date() // Return current date as fallback
        }

    companion object {
        fun createWithCurrentDate(
            mood: String,
            note: String = "",
            reflection: String = "",
            userId: String
        ): MoodEntry {
            return MoodEntry(
                mood = mood,
                note = note,
                reflection = reflection,
                _date = Date(),
                userId = userId
            )
        }
    }
} 