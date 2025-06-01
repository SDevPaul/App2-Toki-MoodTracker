package com.app.toki_moodtracker.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.toki_moodtracker.MainActivity
import com.app.toki_moodtracker.R
import com.app.toki_moodtracker.data.MoodEntry
import java.text.SimpleDateFormat
import java.util.Locale

class MoodEntryAdapter(
    private val onEntryClicked: (MoodEntry, Boolean) -> Unit
) : RecyclerView.Adapter<MoodEntryAdapter.MoodEntryViewHolder>() {

    private var entries: List<MoodEntry> = emptyList()
    private var isDarkMode: Boolean = false

    fun submitList(newEntries: List<MoodEntry>?, isDarkMode: Boolean = false) {
        entries = newEntries?.sortedByDescending { it.date } ?: emptyList()
        this.isDarkMode = isDarkMode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        if (position < 0 || position >= entries.size) return
        
        val entry = entries[position]
        val isLatest = position == 0
        holder.bind(entry, isLatest, isDarkMode)
        
        holder.itemView.setOnClickListener {
            onEntryClicked(entry, isLatest)
        }
    }

    override fun getItemCount() = entries.size

    class MoodEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logDateText: TextView = itemView.findViewById(R.id.logDateText)
        private val moodText: TextView = itemView.findViewById(R.id.moodText)
        private val reflectionText: TextView = itemView.findViewById(R.id.reflectionText)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy; hh:mma", Locale.US)

        fun bind(entry: MoodEntry, isLatest: Boolean, isDarkMode: Boolean) {
            try {
                val alpha = if (isLatest) 1f else 0.25f
                val textColor = if (isDarkMode) Color.WHITE else Color.parseColor("#191919")
                
                // Set background color and corner radius
                if (isDarkMode) {
                    itemView.setBackgroundResource(R.drawable.bg_mood_entry_dark)
                } else {
                    itemView.setBackgroundResource(R.drawable.bg_mood_entry)
                }
                
                logDateText.apply {
                    text = "log date: ${dateFormat.format(entry.date).lowercase()}"
                    this.alpha = alpha
                    setTextColor(textColor)
                }
                
                moodText.apply {
                    text = "mood: ${entry.mood}"
                    this.alpha = alpha
                    setTextColor(textColor)
                }
                
                reflectionText.apply {
                    if (isLatest) {
                        visibility = View.VISIBLE
                        text = entry.reflection
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        this.alpha = 1f
                        setTextColor(textColor)
                    } else {
                        visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Fallback in case of date parsing issues
                logDateText.text = "Invalid date"
                moodText.text = "mood: ${entry.mood}"
                reflectionText.visibility = View.GONE
            }
        }
    }
} 