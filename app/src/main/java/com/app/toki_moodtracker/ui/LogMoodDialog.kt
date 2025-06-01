package com.app.toki_moodtracker.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.app.toki_moodtracker.MainActivity
import com.app.toki_moodtracker.R
import com.app.toki_moodtracker.data.MoodEntry
import com.app.toki_moodtracker.data.QuoteRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogMoodDialog : DialogFragment() {
    private var onMoodLogged: ((MoodEntry) -> Unit)? = null
    private lateinit var quoteText: TextView
    private lateinit var dateValueText: TextView
    private lateinit var moodInput: EditText
    private lateinit var noteInput: EditText
    private lateinit var reflectionInput: EditText
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private var userId: String = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy; hh:mma", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
            userId = arguments?.getString(ARG_USER_ID) ?: ""
        } catch (e: Exception) {
            Log.e("LogMoodDialog", "Error in onCreate", e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_log_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize views
            quoteText = view.findViewById(R.id.quoteText)
            dateValueText = view.findViewById(R.id.dateValueText)
            moodInput = view.findViewById(R.id.moodInput)
            noteInput = view.findViewById(R.id.noteInput)
            reflectionInput = view.findViewById(R.id.reflectionInput)
            saveButton = view.findViewById(R.id.saveButton)
            cancelButton = view.findViewById(R.id.cancelButton)

            // Set random quote
            quoteText.text = QuoteRepository.getRandomQuote()

            // Set current date
            dateValueText.text = dateFormat.format(Date()).lowercase()

            // Update theme colors
            updateThemeColors(view)

            saveButton.setOnClickListener {
                val mood = moodInput.text.toString().trim()
                val note = noteInput.text.toString().trim()
                val reflection = reflectionInput.text.toString().trim()
                
                if (mood.isNotBlank()) {
                    val entry = MoodEntry.createWithCurrentDate(
                        mood = mood,
                        note = note,
                        reflection = reflection,
                        userId = userId
                    )
                    onMoodLogged?.invoke(entry)
                    dismiss()
                }
            }

            cancelButton.setOnClickListener {
                dismiss()
            }
        } catch (e: Exception) {
            Log.e("LogMoodDialog", "Error in onViewCreated", e)
            dismiss()
        }
    }

    private fun updateThemeColors(view: View) {
        val mainActivity = activity as? MainActivity ?: return
        val isDarkMode = mainActivity.isDarkMode()
        
        if (isDarkMode) {
            // Set background color
            view.findViewById<View>(R.id.dialogContainer).setBackgroundColor(Color.parseColor("#171717"))
            
            // Set text colors to white
            view.findViewById<TextView>(R.id.quoteText).setTextColor(Color.WHITE)
            view.findViewById<TextView>(R.id.dateText).setTextColor(Color.WHITE)
            view.findViewById<TextView>(R.id.dateValueText).setTextColor(Color.parseColor("#CCCCCC"))
            view.findViewById<TextView>(R.id.moodLabel).setTextColor(Color.WHITE)
            view.findViewById<TextView>(R.id.noteLabel).setTextColor(Color.WHITE)
            view.findViewById<TextView>(R.id.reflectionLabel).setTextColor(Color.WHITE)

            // Set input backgrounds to dark
            val darkInputBackground = Color.parseColor("#2E2E2E")
            view.findViewById<TextInputLayout>(R.id.moodInputLayout).apply {
                setBoxBackgroundColor(darkInputBackground)
                defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
                setCounterTextColor(ColorStateList.valueOf(Color.WHITE))
            }
            view.findViewById<TextInputLayout>(R.id.noteInputLayout).apply {
                setBoxBackgroundColor(darkInputBackground)
                defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
                setCounterTextColor(ColorStateList.valueOf(Color.WHITE))
            }
            view.findViewById<TextInputLayout>(R.id.reflectionInputLayout).apply {
                setBoxBackgroundColor(darkInputBackground)
                defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
            }

            // Set input text colors
            moodInput.setTextColor(Color.WHITE)
            noteInput.setTextColor(Color.WHITE)
            reflectionInput.apply {
                setTextColor(Color.WHITE)
                setHintTextColor(Color.parseColor("#CCCCCC"))
            }

            // Set button colors
            cancelButton.apply {
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#171717"))
            }
            saveButton.apply {
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E2E2E"))
            }
        }
    }

    fun setOnMoodLoggedListener(listener: (MoodEntry) -> Unit) {
        onMoodLogged = listener
    }

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): LogMoodDialog {
            return LogMoodDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
        }
    }
} 