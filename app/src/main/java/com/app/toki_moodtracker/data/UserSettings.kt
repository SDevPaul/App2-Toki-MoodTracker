package com.app.toki_moodtracker.data

data class UserSettings(
    val isDarkMode: Boolean = false,
    val isAnimationEnabled: Boolean = true,
    val soundVolume: Float = 80f,
    val reminderInterval: Int = 0 // in hours, 0 means no reminder
) 