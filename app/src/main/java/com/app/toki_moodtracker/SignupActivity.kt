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

class SignupActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var signupButton: Button
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private var currentSettings = UserSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        usernameLayout = findViewById(R.id.usernameLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        signupButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)

        // Load user settings
        lifecycleScope.launch {
            try {
                val lastUser = User.getAllUsers(this@SignupActivity).lastOrNull()
                if (lastUser != null) {
                    currentSettings = lastUser.userData["settings"] as? UserSettings ?: UserSettings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        signupButton.setOnClickListener {
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

            setLoadingState(true)

            lifecycleScope.launch {
                try {
                    // Check if user already exists
                    if (User.getAllUsers(this@SignupActivity).any { it.username == username }) {
                        Toast.makeText(this@SignupActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                        setLoadingState(false)
                        return@launch
                    }

                    // Create and save new user
                    val newUser = User(username, password)
                    User.saveUser(this@SignupActivity, newUser)

                    Toast.makeText(this@SignupActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate back to login
                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("username", username)
                    startActivity(intent)
                    if (currentSettings.isAnimationEnabled) {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@SignupActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                    setLoadingState(false)
                    e.printStackTrace()
                }
            }
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            if (currentSettings.isAnimationEnabled) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }
    }

    private fun setLoadingState(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        signupButton.isEnabled = !loading
        loginButton.isEnabled = !loading
        usernameInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
    }
} 