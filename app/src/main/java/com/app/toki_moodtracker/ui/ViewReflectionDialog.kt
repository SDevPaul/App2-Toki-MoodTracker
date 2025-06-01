package com.app.toki_moodtracker.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.app.toki_moodtracker.MainActivity
import com.app.toki_moodtracker.R
import com.app.toki_moodtracker.data.MoodEntry
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class ViewReflectionDialog : DialogFragment() {
    private lateinit var dateText: TextView
    private lateinit var moodText: TextView
    private lateinit var reflectionText: TextView
    private lateinit var closeButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_view_reflection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateText = view.findViewById(R.id.dateText)
        moodText = view.findViewById(R.id.moodText)
        reflectionText = view.findViewById(R.id.reflectionText)
        closeButton = view.findViewById(R.id.closeButton)

        val entry = arguments?.getParcelable(ARG_ENTRY, MoodEntry::class.java)
        if (entry != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy; hh:mma", Locale.US)
            dateText.text = "log date: ${dateFormat.format(entry.date).lowercase()}"
            moodText.text = "mood: ${entry.mood}"
            reflectionText.text = entry.reflection
        }

        closeButton.setOnClickListener {
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
            reflectionText.setTextColor(Color.WHITE)

            // Set button colors
            closeButton.apply {
                setTextColor(Color.WHITE)
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2E2E2E"))
            }
        }
    }

    companion object {
        private const val ARG_ENTRY = "entry"

        fun newInstance(entry: MoodEntry): ViewReflectionDialog {
            return ViewReflectionDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ENTRY, entry)
                }
            }
        }
    }
} 