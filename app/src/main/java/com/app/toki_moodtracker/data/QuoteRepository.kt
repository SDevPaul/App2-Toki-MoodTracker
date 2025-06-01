package com.app.toki_moodtracker.data

object QuoteRepository {
    private val quotes = mutableListOf(
        "not every day has to be productive. some days are for feeling.",
        "your emotions are valid, embrace them.",
        "take it one breath at a time.",
        "feeling is healing.",
        "it's okay to not be okay.",
        "your journey is uniquely yours.",
        "emotions are like waves, let them flow.",
        "be gentle with yourself today.",
        "every feeling is a teacher.",
        "pause, breathe, feel.",
        "your feelings matter, take time to understand them.",
        "self-reflection is an act of self-love.",
        "today is a new beginning.",
        "listen to your heart, it knows the way.",
        "embrace the present moment."
    )

    fun getRandomQuote(): String {
        return quotes.random()
    }

    fun addQuote(quote: String) {
        quotes.add(quote)
    }
} 