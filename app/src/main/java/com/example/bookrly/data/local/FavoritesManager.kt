package com.example.bookrly.data.local

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bookrly_prefs", Context.MODE_PRIVATE)

    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
    }

    fun addFavorite(id: String) {
        val current = getFavorites().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("favorite_ids", current).apply()
    }

    fun removeFavorite(id: String) {
        val current = getFavorites().toMutableSet()
        current.remove(id)
        prefs.edit().putStringSet("favorite_ids", current).apply()
    }

    fun isFavorite(id: String): Boolean {
        return getFavorites().contains(id)
    }
}
