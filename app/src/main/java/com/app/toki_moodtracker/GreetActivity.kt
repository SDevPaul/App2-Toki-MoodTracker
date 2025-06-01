package com.app.toki_moodtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.toki_moodtracker.data.User
import com.app.toki_moodtracker.data.UserSettings
import com.app.toki_moodtracker.databinding.ActivityGreetBinding
import kotlinx.coroutines.launch

class GreetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGreetBinding
    private var currentSettings = UserSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGreetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load user settings
        lifecycleScope.launch {
            try {
                val lastUser = User.getAllUsers(this@GreetActivity).lastOrNull()
                if (lastUser != null) {
                    currentSettings = lastUser.userData["settings"] as? UserSettings ?: UserSettings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            if (currentSettings.isAnimationEnabled) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }

        binding.signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            if (currentSettings.isAnimationEnabled) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }
    }
} 