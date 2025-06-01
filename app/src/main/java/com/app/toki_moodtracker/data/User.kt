package com.app.toki_moodtracker.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit

data class User(
    val username: String,
    val password: String,
    var userData: MutableMap<String, Any> = mutableMapOf()
) {
    companion object {
        private const val PREFS_NAME = "user_data"
        private const val USERS_KEY = "users"
        private val gson = Gson()

        fun saveUser(context: Context, user: User) {
            val users = getAllUsers(context).toMutableList()
            users.removeIf { it.username == user.username }
            users.add(user)
            
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(USERS_KEY, gson.toJson(users))
                .apply()
        }

        fun getUser(context: Context, username: String): User? {
            return getAllUsers(context).find { it.username == username }
        }

        fun getAllUsers(context: Context): List<User> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(USERS_KEY, "[]")
            val type = object : TypeToken<List<User>>() {}.type
            return try {
                gson.fromJson(json, type) ?: listOf()
            } catch (e: Exception) {
                listOf()
            }
        }

        fun deleteUser(context: Context, username: String) {
            val users = getAllUsers(context).toMutableList()
            users.removeIf { it.username == username }
            
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(USERS_KEY, gson.toJson(users))
                .apply()
        }

        suspend fun findUser(context: Context, username: String, password: String?): User? {
            return withContext(Dispatchers.IO) {
                getAllUsers(context).find { 
                    it.username == username && (password == null || it.password == password)
                }
            }
        }

        suspend fun updateUserData(context: Context, username: String, data: Map<String, Any>) {
            withContext(Dispatchers.IO) {
                val users = getAllUsers(context).toMutableList()
                val userIndex = users.indexOfFirst { it.username == username }
                if (userIndex != -1) {
                    val updatedUser = users[userIndex].copy(
                        userData = users[userIndex].userData.toMutableMap().apply { 
                            putAll(data)
                        }
                    )
                    users[userIndex] = updatedUser
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit {
                            putString(USERS_KEY, gson.toJson(users))
                        }
                }
            }
        }
    }
} 