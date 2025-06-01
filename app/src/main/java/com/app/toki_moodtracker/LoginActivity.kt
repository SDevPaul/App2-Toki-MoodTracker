package com.app.toki_moodtracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.toki_moodtracker.data.User
import com.app.toki_moodtracker.data.UserSettings
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var progressBar: ProgressBar
    private var currentSettings = UserSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        usernameLayout = findViewById(R.id.usernameLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        progressBar = findViewById(R.id.progressBar)

        // Load user settings
        lifecycleScope.launch {
            try {
                val lastUser = User.getAllUsers(this@LoginActivity).lastOrNull()
                if (lastUser != null) {
                    currentSettings = lastUser.userData["settings"] as? UserSettings ?: UserSettings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isBlank()) {
                usernameLayout.error = "Username cannot be empty"
                return@setOnClickListener
            } else {
                usernameLayout.error = null
            }

            if (password.isBlank()) {
                passwordLayout.error = "Password cannot be empty"
                return@setOnClickListener
            } else {
                passwordLayout.error = null
            }

            // Show loading state
            setLoadingState(true)

            lifecycleScope.launch {
                try {
                    val user = User.findUser(this@LoginActivity, username, password)
                    if (user != null) {
                        Toast.makeText(this@LoginActivity, "Welcome to your safe place", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("username", username)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        if (currentSettings.isAnimationEnabled) {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show()
                        setLoadingState(false)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                    setLoadingState(false)
                }
            }
        }

        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            if (currentSettings.isAnimationEnabled) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }
    }

    private fun setLoadingState(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !loading
        signupButton.isEnabled = !loading
        usernameInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
    }
} 