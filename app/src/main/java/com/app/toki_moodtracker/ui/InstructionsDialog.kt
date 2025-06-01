package com.app.toki_moodtracker.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.view.Window
import android.widget.TextView
import com.app.toki_moodtracker.R
import com.google.android.material.button.MaterialButton

class InstructionsDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_instructions)

        // Find views
        val instructionsText = findViewById<TextView>(R.id.instructionsText)
        val closeButton = findViewById<MaterialButton>(R.id.closeButton)

        // Create instructions text with bullet points
        val instructions = SpannableStringBuilder()
        
        val bulletPoints = listOf(
            "Track Your Mood: Use the 'Log Mood' button on the home screen to record how you're feeling at any time.",
            "Journal Entries: Visit the Journal tab to view your mood history and reflect on your emotional journey.",
            "Set Reminders: Go to Settings to set up regular mood tracking reminders that work best for you.",
            "Dark Mode: Toggle between light and dark themes in Settings for comfortable viewing.",
            "Sound Settings: Adjust notification sounds in Settings according to your preference.",
            "View Progress: Check your mood patterns and history in the Journal section to understand your emotional trends.",
            "Daily Quotes: Find inspiration from daily quotes on the home screen.",
            "Quick Navigation: Use the bottom navigation bar to switch between Home, Journal, and Settings.",
            "Personalization: Customize your experience through the Settings menu to make the app work best for you."
        )

        bulletPoints.forEachIndexed { index, text ->
            val bulletPoint = SpannableString(text)
            bulletPoint.setSpan(
                BulletSpan(20, context.getColor(R.color.button_primary)),
                0,
                text.length,
                0
            )
            instructions.append(bulletPoint)
            if (index < bulletPoints.size - 1) {
                instructions.append("\n\n")
            }
        }

        instructionsText.text = instructions

        // Set up close button
        closeButton.setOnClickListener {
            dismiss()
        }
    }
} 