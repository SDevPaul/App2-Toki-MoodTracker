package com.app.toki_moodtracker.ui

import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.toki_moodtracker.MainActivity
import com.app.toki_moodtracker.R
import com.app.toki_moodtracker.data.MoodEntry
import com.app.toki_moodtracker.data.MoodRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class JournalFragment : Fragment() {
    private lateinit var moodRepository: MoodRepository
    private lateinit var moodEntryAdapter: MoodEntryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var logMoodButton: MaterialButton
    private var username: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        username = activity?.intent?.getStringExtra("username") ?: "User"
        moodRepository = MoodRepository(requireContext())

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.moodEntriesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        moodEntryAdapter = MoodEntryAdapter { entry, isLatest ->
            if (isLatest) {
                showEditReflectionDialog(entry)
            } else {
                showViewReflectionDialog(entry)
            }
        }
        recyclerView.adapter = moodEntryAdapter

        // Set up log mood button
        logMoodButton = view.findViewById(R.id.logMoodButton)
        logMoodButton.setOnClickListener {
            showLogMoodDialog()
        }

        // Load mood entries
        loadMoodEntries()
    }

    private fun loadMoodEntries() {
        lifecycleScope.launch {
            val entries = moodRepository.getAllMoodEntries()
                .filter { it.userId == username }
            val mainActivity = activity as? MainActivity
            moodEntryAdapter.submitList(entries, mainActivity?.isDarkMode() ?: false)
        }
    }

    override fun onResume() {
        super.onResume()
        updateThemeColors()
    }

    private fun updateThemeColors() {
        val mainActivity = activity as? MainActivity ?: return
        val isDarkMode = mainActivity.isDarkMode()

        // Update text colors
        view?.findViewById<TextView>(R.id.titleText)?.setTextColor(
            if (isDarkMode) Color.WHITE else Color.parseColor("#191919")
        )
        view?.findViewById<TextView>(R.id.subtitleText)?.setTextColor(
            if (isDarkMode) Color.WHITE else Color.parseColor("#191919")
        )

        // Update log mood button colors
        view?.findViewById<MaterialButton>(R.id.logMoodButton)?.apply {
            if (isDarkMode) {
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#2E2E2E"))
                iconTint = ColorStateList.valueOf(Color.WHITE)
            } else {
                setTextColor(Color.parseColor("#191919"))
                setBackgroundColor(Color.parseColor("#F3F3F3"))
                iconTint = ColorStateList.valueOf(Color.parseColor("#191919"))
            }
        }

        // Update separator color
        view?.findViewById<View>(R.id.separator)?.setBackgroundColor(
            if (isDarkMode) Color.parseColor("#404040") else Color.parseColor("#E0E0E0")
        )
    }

    private fun showLogMoodDialog() {
        val dialog = LogMoodDialog.newInstance(username)
        dialog.setOnMoodLoggedListener { entry ->
            lifecycleScope.launch {
                moodRepository.saveMoodEntry(entry)
                loadMoodEntries()
            }
        }
        dialog.show(childFragmentManager, "log_mood")
    }

    private fun showEditReflectionDialog(entry: MoodEntry) {
        val dialog = EditReflectionDialog.newInstance(entry)
        dialog.setOnReflectionUpdatedListener { updatedEntry ->
            lifecycleScope.launch {
                moodRepository.updateMoodEntry(updatedEntry)
                loadMoodEntries()
            }
        }
        dialog.show(childFragmentManager, "edit_reflection")
    }

    private fun showViewReflectionDialog(entry: MoodEntry) {
        val dialog = ViewReflectionDialog.newInstance(entry)
        dialog.show(childFragmentManager, "view_reflection")
    }

    companion object {
        fun newInstance() = JournalFragment()
    }
} 