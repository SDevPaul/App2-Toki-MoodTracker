package com.app.toki_moodtracker

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.toki_moodtracker.data.User
import com.app.toki_moodtracker.data.UserSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    private var currentSettings = UserSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        // Load user settings
        lifecycleScope.launch {
            try {
                val lastUser = User.getAllUsers(this@SplashScreenActivity).lastOrNull()
                if (lastUser != null) {
                    currentSettings = lastUser.userData["settings"] as? UserSettings ?: UserSettings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Animate the logo if animations are enabled
            val splashLogo = findViewById<ImageView>(R.id.splashLogo)
            if (currentSettings.isAnimationEnabled) {
                val fadeIn = AnimationUtils.loadAnimation(this@SplashScreenActivity, android.R.anim.fade_in)
                splashLogo.startAnimation(fadeIn)
            }

            delay(1500) // Reduced to 1.5 seconds
            startActivity(Intent(this@SplashScreenActivity, GreetActivity::class.java))
            if (currentSettings.isAnimationEnabled) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }
    }
}