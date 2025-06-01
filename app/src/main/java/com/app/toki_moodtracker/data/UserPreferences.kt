package com.app.toki_moodtracker.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(username: String, password: String) {
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }

    fun getUser(): Pair<String?, String?> {
        val username = prefs.getString("username", null)
        val password = prefs.getString("password", null)
        return Pair(username, password)
    }

    fun isLoggedIn(): Boolean {
        return prefs.contains("username") && prefs.contains("password")
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
} 