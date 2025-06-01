package com.app.toki_moodtracker.ui

import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.toki_moodtracker.MainActivity
import com.app.toki_moodtracker.R
import com.app.toki_moodtracker.data.MoodRepository
import com.app.toki_moodtracker.data.QuoteRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.content.Context

class HomeFragment : Fragment() {
    private lateinit var greetingText: TextView
    private lateinit var quoteText: TextView
    private lateinit var lastMoodText: TextView
    private lateinit var noteText: TextView
    private lateinit var dateText: TextView
    private lateinit var logMoodButton: MaterialButton
    private lateinit var moodRepository: MoodRepository
    private var username: String = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy; hh:mma", Locale.US)

    companion object {
        private var hasShownInstructionsThisSession = mutableMapOf<String, Boolean>()
        
        fun newInstance() = HomeFragment()
        
        fun resetInstructions(username: String) {
            hasShownInstructionsThisSession[username] = false
        }
        
        fun clearAllInstructions() {
            hasShownInstructionsThisSession.clear()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize views
            greetingText = view.findViewById(R.id.greetingText)
            quoteText = view.findViewById(R.id.quoteText)
            lastMoodText = view.findViewById(R.id.lastMoodText)
            noteText = view.findViewById(R.id.noteText)
            dateText = view.findViewById(R.id.dateText)
            logMoodButton = view.findViewById(R.id.logMoodButton)
            moodRepository = MoodRepository(requireContext())

            // Set username in greeting
            username = arguments?.getString("username") 
                ?: activity?.intent?.getStringExtra("username") 
                ?: "User"
            
            Log.d("HomeFragment", "Username from arguments: ${arguments?.getString("username")}")
            Log.d("HomeFragment", "Username from activity intent: ${activity?.intent?.getStringExtra("username")}")
            Log.d("HomeFragment", "Final username being used: $username")
            
            greetingText.text = getString(R.string.greeting_text, username)

            // Set random quote
            quoteText.text = QuoteRepository.getRandomQuote()

            // Load last mood entry
            loadLastMoodEntry()

            // Set up log mood button
            logMoodButton.setOnClickListener {
                showLogMoodDialog()
            }

            // Show instructions dialog only if not shown this session for this user
            if (hasShownInstructionsThisSession[username] != true) {
                Log.d("HomeFragment", "Showing instructions for user: $username")
                // Post to the main thread to ensure the fragment is fully created
                view.post {
                    val dialog = InstructionsDialog(requireContext())
                    dialog.setOnDismissListener {
                        Log.d("HomeFragment", "Instructions dialog dismissed")
                        hasShownInstructionsThisSession[username] = true
                    }
                    dialog.show()
                }
            }

        } catch (e: Exception) {
            Log.e("HomeFragment", "Error in onViewCreated", e)
            setDefaultValues()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only clear instructions if the activity is finishing (user is logging out or app is closing)
        activity?.let {
            if (it.isFinishing) {
                clearAllInstructions()
            }
        }
    }

    private fun setDefaultValues() {
        try {
            greetingText.text = getString(R.string.greeting_text, username)
            lastMoodText.text = "your last mood:    none"
            noteText.text = "note:    none"
            dateText.text = "date:    none"
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error setting default values", e)
        }
    }

    private fun showLogMoodDialog() {
        try {
            val dialog = LogMoodDialog.newInstance(username)
            dialog.setOnMoodLoggedListener { entry ->
                lifecycleScope.launch {
                    moodRepository.saveMoodEntry(entry)
                    loadLastMoodEntry()
                }
            }
            dialog.show(childFragmentManager, "log_mood")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error showing log mood dialog", e)
        }
    }

    private fun loadLastMoodEntry() {
        lifecycleScope.launch {
            try {
                val lastEntry = moodRepository.getLatestMoodEntry(username)
                if (lastEntry != null) {
                    lastMoodText.text = "your last mood:    ${lastEntry.mood}"
                    noteText.text = "note:    \"${lastEntry.note}\""
                    dateText.text = "date:    ${dateFormat.format(lastEntry.date).lowercase()}"
                } else {
                    setDefaultValues()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading last mood entry", e)
                setDefaultValues()
            }
        }
    }

    private fun showInstructionsIfFirstTime() {
        // This method is no longer needed
    }

    override fun onResume() {
        super.onResume()
        updateThemeColors()
    }

    private fun updateThemeColors() {
        val mainActivity = activity as? MainActivity ?: return
        val isDarkMode = mainActivity.isDarkMode()
        
        // Update text colors
        val textColor = if (isDarkMode) Color.WHITE else Color.parseColor("#191919")
        
        view?.findViewById<TextView>(R.id.greetingText)?.setTextColor(textColor)
        view?.findViewById<TextView>(R.id.howFeelingText)?.setTextColor(textColor)
        view?.findViewById<TextView>(R.id.lastMoodText)?.setTextColor(textColor)
        view?.findViewById<TextView>(R.id.noteText)?.setTextColor(textColor)
        view?.findViewById<TextView>(R.id.dateText)?.setTextColor(textColor)
        
        // Update quote text and bar with 50% opacity
        view?.findViewById<TextView>(R.id.quoteText)?.apply {
            setTextColor(textColor)
            alpha = 0.5f
        }
        
        view?.findViewById<View>(R.id.quoteBar)?.apply {
            setBackgroundColor(textColor)
            alpha = 0.5f
        }
        
        // Update log mood button
        view?.findViewById<MaterialButton>(R.id.logMoodButton)?.apply {
            if (isDarkMode) {
                setTextColor(Color.WHITE)
                iconTint = ColorStateList.valueOf(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E2E2E"))
            } else {
                setTextColor(Color.parseColor("#191919"))
                iconTint = ColorStateList.valueOf(Color.parseColor("#191919"))
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F3F3F3"))
            }
        }
    }
} 
