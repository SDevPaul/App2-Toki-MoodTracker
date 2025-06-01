package com.app.toki_moodtracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.toki_moodtracker.ui.HomeFragment
import com.app.toki_moodtracker.ui.JournalFragment
import com.app.toki_moodtracker.ui.SettingsFragment
import com.app.toki_moodtracker.data.User
import com.app.toki_moodtracker.data.UserSettings
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.Color
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.runBlocking
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var username: String
    private var currentSettings = UserSettings()

    // Getter for animation state
    fun isAnimationEnabled(): Boolean = currentSettings.isAnimationEnabled

    // Getter for theme state
    fun isDarkMode(): Boolean = currentSettings.isDarkMode
    
    // Getter for reminder interval
    fun getReminderInterval(): Int = currentSettings.reminderInterval
    
    // Update reminder settings
    fun updateReminderSetting(hours: Int) {
        currentSettings = currentSettings.copy(reminderInterval = hours)
        // Save settings
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = User.findUser(this@MainActivity, username, password = null)
                if (user != null) {
                    user.userData["settings"] = currentSettings
                    User.saveUser(this@MainActivity, user)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        username = intent.getStringExtra("username") ?: run {
            // If no username is provided, return to login screen
            Log.d("MainActivity", "No username provided in intent, returning to login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        Log.d("MainActivity", "Username received in MainActivity: $username")
        loadUserSettings()

        bottomNavigation = findViewById(R.id.bottomNavigation)
        setupNavigation()
        updateNavigationTheme(currentSettings.isDarkMode)

        // Load the default fragment without animation initially
        if (savedInstanceState == null) {
            val homeFragment = HomeFragment.newInstance()
            homeFragment.arguments = Bundle().apply {
                putString("username", username)
                Log.d("MainActivity", "Setting username in HomeFragment arguments: $username")
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, homeFragment)
                .commit()
            bottomNavigation.selectedItemId = R.id.navigation_home
        }
    }

    fun loadUserSettings() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = User.findUser(this@MainActivity, username, password = null)
                currentSettings = user?.userData?.get("settings") as? UserSettings ?: UserSettings()
                
                // Apply saved animation settings
                if (!currentSettings.isAnimationEnabled) {
                    window.setWindowAnimations(0)
                }
                
                // Apply theme
                updateThemeSetting(currentSettings.isDarkMode)
                
            } catch (e: Exception) {
                e.printStackTrace()
                currentSettings = UserSettings()
            }
        }
    }

    private fun setupNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val fragment = HomeFragment.newInstance()
                    fragment.arguments = Bundle().apply {
                        putString("username", username)
                    }
                    loadFragment(fragment)
                    true
                }
                R.id.navigation_journal -> {
                    val fragment = JournalFragment.newInstance()
                    fragment.arguments = Bundle().apply {
                        putString("username", username)
                    }
                    loadFragment(fragment)
                    true
                }
                R.id.navigation_settings -> {
                    val fragment = SettingsFragment.newInstance(username)
                    loadFragment(fragment)
                    true
                }
                else -> false
            }
        }

        // Prevent reloading the same fragment
        bottomNavigation.setOnItemReselectedListener { }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        
        // Only apply animations if explicitly enabled
        if (currentSettings.isAnimationEnabled) {
            transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            // Explicitly set no animations
            transaction.setCustomAnimations(0, 0, 0, 0)
        }
        
        transaction.replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    fun updateAnimationSetting(enabled: Boolean) {
        currentSettings = currentSettings.copy(isAnimationEnabled = enabled)
        if (!enabled) {
            // Immediately disable all animations
            window.setWindowAnimations(0)
        } else {
            // Re-enable window animations
            window.setWindowAnimations(android.R.style.Animation)
        }
    }

    fun logout() {
        val intent = Intent(this, GreetActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        if (currentSettings.isAnimationEnabled) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

    @SuppressLint("DetachAndAttachSameFragment")
    fun updateThemeSetting(isDark: Boolean) {
        currentSettings = currentSettings.copy(isDarkMode = isDark)
        
        // Set background color based on theme
        findViewById<View>(R.id.mainContainer).setBackgroundColor(
            if (isDark) Color.parseColor("#171717") else Color.WHITE
        )
        
        // Update navigation theme
        updateNavigationTheme(isDark)
        
        // Save settings
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = User.findUser(this@MainActivity, username, password = null)
                if (user != null) {
                    user.userData["settings"] = currentSettings
                    User.saveUser(this@MainActivity, user)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Reload current fragment to update its colors
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (currentFragment != null) {
            supportFragmentManager.beginTransaction()
                .detach(currentFragment)
                .attach(currentFragment)
                .commit()
        }
    }

    private fun updateNavigationTheme(isDark: Boolean) {
        if (isDark) {
            bottomNavigation.setBackgroundColor(getColor(R.color.white))  // White background in dark mode
            
            // Create color state list for dark mode - all icons white
            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            )
            val colors = intArrayOf(
                getColor(R.color.white),  // Selected: white icon
                getColor(R.color.white)   // Unselected: also white icon
            )
            val colorStateList = ColorStateList(states, colors)
            
            bottomNavigation.itemIconTintList = colorStateList
            bottomNavigation.itemTextColor = colorStateList
            bottomNavigation.setItemBackgroundResource(R.drawable.bottom_nav_item_background_selector_dark)
        } else {
            bottomNavigation.setBackgroundColor(getColor(R.color.white))
            
            // Create color state list for light mode
            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            )
            val colors = intArrayOf(
                getColor(R.color.white),  // Selected item: white icon
                getColor(R.color.black)   // Unselected item: black icon
            )
            val colorStateList = ColorStateList(states, colors)
            
            bottomNavigation.itemIconTintList = colorStateList
            bottomNavigation.itemTextColor = colorStateList
            bottomNavigation.setItemBackgroundResource(R.drawable.bottom_nav_item_background_selector_light)
        }
    }
}