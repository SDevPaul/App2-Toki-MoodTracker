package com.app.toki_moodtracker.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.app.toki_moodtracker.MainActivity
import com.app.toki_moodtracker.R
import com.app.toki_moodtracker.data.MoodEntry
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale

class EditReflectionDialog : DialogFragment() {
    private var onReflectionUpdated: ((MoodEntry) -> Unit)? = null
    private lateinit var dateText: TextView
    private lateinit var moodText: TextView
    private lateinit var reflectionInput: EditText
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private var entry: MoodEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        entry = arguments?.getParcelable(ARG_ENTRY, MoodEntry::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_reflection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateText = view.findViewById(R.id.dateText)
        moodText = view.findViewById(R.id.moodText)
        reflectionInput = view.findViewById(R.id.reflectionInput)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        entry?.let { currentEntry ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy; hh:mma", Locale.US)
            dateText.text = "log date: ${dateFormat.format(currentEntry.date).lowercase()}"
            moodText.text = "mood: ${currentEntry.mood}"
            reflectionInput.setText(currentEntry.reflection)

            saveButton.setOnClickListener {
                val updatedEntry = currentEntry.copy(
                    reflection = reflectionInput.text.toString()
                )
                onReflectionUpdated?.invoke(updatedEntry)
                dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        // Update theme colors
        updateThemeColors(view)
    }

    private fun updateThemeColors(view: View) {
        val mainActivity = activity as? MainActivity ?: return
        val isDarkMode = mainActivity.isDarkMode()
        
        if (isDarkMode) {
            // Set background color
            view.findViewById<View>(R.id.dialogContainer).setBackgroundColor(Color.parseColor("#171717"))
            
            // Set text colors to white
            dateText.setTextColor(Color.parseColor("#CCCCCC"))
            moodText.setTextColor(Color.WHITE)

            // Set input background to dark
            val darkInputBackground = Color.parseColor("#2E2E2E")
            view.findViewById<TextInputLayout>(R.id.reflectionInputLayout).apply {
                setBoxBackgroundColor(darkInputBackground)
                defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
            }

            // Set input text colors
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

    fun setOnReflectionUpdatedListener(listener: (MoodEntry) -> Unit) {
        onReflectionUpdated = listener
    }

    companion object {
        private const val ARG_ENTRY = "entry"

        fun newInstance(entry: MoodEntry): EditReflectionDialog {
            return EditReflectionDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ENTRY, entry)
                }
            }
        }
    }
} 