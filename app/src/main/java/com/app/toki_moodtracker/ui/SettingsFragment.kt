package com.app.toki_moodtracker.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.toki_moodtracker.GreetActivity
import com.app.toki_moodtracker.MainActivity
import com.app.toki_moodtracker.R
import com.app.toki_moodtracker.data.User
import com.app.toki_moodtracker.data.UserSettings
import com.app.toki_moodtracker.ReminderReceiver
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsFragment : Fragment() {
    private lateinit var themeButton: MaterialCardView
    private lateinit var themeStatus: TextView
    private lateinit var animationButton: MaterialCardView
    private lateinit var animationStatus: TextView
    private lateinit var soundSlider: Slider
    private lateinit var reminderButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private var username: String = ""
    private var currentSettings = UserSettings()
    private var isInitialLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get username from arguments
        username = arguments?.getString("username") ?: "User"

        // Initialize views
        themeButton = view.findViewById(R.id.themeButton)
        themeStatus = view.findViewById(R.id.themeStatus)
        animationButton = view.findViewById(R.id.animationButton)
        animationStatus = view.findViewById(R.id.animationStatus)
        soundSlider = view.findViewById(R.id.soundSlider)
        reminderButton = view.findViewById(R.id.reminderButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        // Set up click listeners
        themeButton.setOnClickListener {
            toggleTheme()
        }

        animationButton.setOnClickListener {
            toggleAnimation()
        }

        soundSlider.addOnChangeListener { _, value, _ ->
            updateSoundVolume(value)
        }

        reminderButton.setOnClickListener {
            showReminderDialog()
        }

        logoutButton.setOnClickListener {
            logout()
        }

        // Load settings only on initial creation
        if (isInitialLoad) {
            loadUserSettings()
            isInitialLoad = false
        } else {
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        // Just update UI without reloading settings
        updateUI()
        updateThemeColors()
    }

    private fun loadUserSettings() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = User.findUser(requireContext(), username, password = null)
                currentSettings = user?.userData?.get("settings") as? UserSettings ?: UserSettings()
                updateUI()
            } catch (e: Exception) {
                e.printStackTrace()
                currentSettings = UserSettings()
                updateUI()
            }
        }
    }

    private fun updateUI() {
        // Get MainActivity reference
        val mainActivity = activity as? MainActivity ?: return
        val isDarkMode = mainActivity.isDarkMode()
        
        // Update theme status text using MainActivity's state
        themeStatus.text = if (isDarkMode) 
            getString(R.string.theme_status_dark) 
        else 
            getString(R.string.theme_status_light)
            
        // Update animation status using MainActivity's state
        animationStatus.text = if (mainActivity.isAnimationEnabled()) 
            getString(R.string.animation_status_on) 
        else 
            getString(R.string.animation_status_off)
            
        soundSlider.value = currentSettings.soundVolume
        
        // Use MainActivity's reminder interval
        reminderButton.text = when (mainActivity.getReminderInterval()) {
            0 -> getString(R.string.reminder_none)
            1 -> getString(R.string.reminder_1h)
            2 -> getString(R.string.reminder_2h)
            3 -> getString(R.string.reminder_3h)
            6 -> getString(R.string.reminder_6h)
            12 -> getString(R.string.reminder_12h)
            24 -> getString(R.string.reminder_24h)
            else -> getString(R.string.reminder_none)
        }

        // Set logout button style based on theme
        if (isDarkMode) {
            logoutButton.setTextColor(Color.WHITE)
            logoutButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E2E2E"))
        } else {
            logoutButton.setTextColor(Color.WHITE)
            logoutButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.button_primary, null))
        }
    }

    private fun toggleTheme() {
        // Get MainActivity reference
        val mainActivity = activity as? MainActivity ?: return

        // Toggle theme state
        val newThemeState = !mainActivity.isDarkMode()
        
        // Update MainActivity's theme setting immediately
        mainActivity.updateThemeSetting(newThemeState)
        
        // Update UI immediately to reflect theme changes
        updateUI()
        updateThemeColors()
    }

    private fun toggleAnimation() {
        // Get MainActivity reference
        val mainActivity = activity as? MainActivity ?: return

        // Toggle animation state
        val newAnimationState = !mainActivity.isAnimationEnabled()
        currentSettings = currentSettings.copy(isAnimationEnabled = newAnimationState)
        
        // Update MainActivity's animation setting immediately
        mainActivity.updateAnimationSetting(newAnimationState)
        
        // Save the setting
        saveSettings()
        
        // Update UI
        updateUI()
    }

    private fun updateSoundVolume(volume: Float) {
        currentSettings = currentSettings.copy(soundVolume = volume)
        saveSettings()
    }

    private fun showReminderDialog() {
        val intervals = arrayOf(
            "No reminder" to 0,
            "Every hour" to 1,
            "Every 2 hours" to 2,
            "Every 3 hours" to 3,
            "Every 6 hours" to 6,
            "Every 12 hours" to 12,
            "Every 24 hours" to 24
        )

        val mainActivity = activity as? MainActivity ?: return
        val isDarkMode = mainActivity.isDarkMode()
        val dialogTheme = if (isDarkMode) R.style.DarkModeReminderDialog else 0

        MaterialAlertDialogBuilder(requireContext(), dialogTheme)
            .setTitle("Choose Reminder Interval")
            .setItems(intervals.map { it.first }.toTypedArray()) { _, which ->
                val (_, hours) = intervals[which]
                // Update both local settings and MainActivity's settings
                currentSettings = currentSettings.copy(reminderInterval = hours)
                mainActivity.updateReminderSetting(hours)
                saveSettings()
                updateUI()
                scheduleReminder(hours)
            }
            .show()
    }

    private fun scheduleReminder(hours: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (hours > 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, hours)

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                hours * 60 * 60 * 1000L,
                pendingIntent
            )
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun saveSettings() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = User.findUser(requireContext(), username, password = null)
                if (user != null) {
                    user.userData["settings"] = currentSettings
                    User.saveUser(requireContext(), user)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun logout() {
        val intent = Intent(requireContext(), GreetActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun updateThemeColors() {
        val mainActivity = activity as? MainActivity ?: return
        val isDarkMode = mainActivity.isDarkMode()
        val whiteColor = Color.WHITE
        val darkTextColor = Color.parseColor("#191919")
        val darkBackgroundColor = Color.parseColor("#2E2E2E")

        // Update title text color
        view?.findViewById<TextView>(R.id.titleText)?.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )

        // Update theme section colors
        view?.findViewById<TextView>(R.id.themeLabel)?.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )
        themeButton.findViewById<ImageView>(R.id.themeIcon)?.setImageTintList(
            ColorStateList.valueOf(if (isDarkMode) whiteColor else darkTextColor)
        )
        themeButton.findViewById<TextView>(android.R.id.text1)?.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )
        themeStatus.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )

        // Update animation section colors
        view?.findViewById<TextView>(R.id.animationLabel)?.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )
        animationButton.findViewById<ImageView>(R.id.animationIcon)?.setImageTintList(
            ColorStateList.valueOf(if (isDarkMode) whiteColor else darkTextColor)
        )
        animationButton.findViewById<TextView>(android.R.id.text1)?.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )
        animationStatus.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )

        // Update sound section colors
        view?.findViewById<TextView>(R.id.soundLabel)?.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )

        // Update reminder section colors
        view?.findViewById<TextView>(R.id.reminderLabel)?.setTextColor(
            if (isDarkMode) whiteColor else darkTextColor
        )
        reminderButton.apply {
            if (isDarkMode) {
                setTextColor(whiteColor)
                backgroundTintList = ColorStateList.valueOf(darkBackgroundColor)
            } else {
                setTextColor(darkTextColor)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F3F3F3"))
            }
        }

        // Update slider colors
        soundSlider.apply {
            if (isDarkMode) {
                thumbTintList = ColorStateList.valueOf(whiteColor)
                trackActiveTintList = ColorStateList.valueOf(whiteColor)
                trackInactiveTintList = ColorStateList.valueOf(Color.parseColor("#404040"))
            } else {
                thumbTintList = ColorStateList.valueOf(darkTextColor)
                trackActiveTintList = ColorStateList.valueOf(darkTextColor)
                trackInactiveTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            }
        }

        // Update card backgrounds
        themeButton.setCardBackgroundColor(if (isDarkMode) darkBackgroundColor else Color.parseColor("#F3F3F3"))
        animationButton.setCardBackgroundColor(if (isDarkMode) darkBackgroundColor else Color.parseColor("#F3F3F3"))

        // Update logout button colors
        logoutButton.apply {
            if (isDarkMode) {
                setTextColor(whiteColor)
                backgroundTintList = ColorStateList.valueOf(darkBackgroundColor)
            } else {
                setTextColor(whiteColor)
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.button_primary, null))
            }
        }
    }

    companion object {
        fun newInstance(username: String) = SettingsFragment().apply {
            arguments = Bundle().apply {
                putString("username", username)
            }
        }
    }
} 