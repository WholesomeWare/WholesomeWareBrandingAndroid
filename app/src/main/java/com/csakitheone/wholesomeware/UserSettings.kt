package com.csakitheone.wholesomeware

import android.content.Context
import android.content.SharedPreferences

class UserSettings {
    companion object {
        private const val PREFS_NAME = "wholesomeware_user_settings"
        private const val KEY_UNLOCKED_ARTWORKS = "unlocked_artworks"

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        /**
         * Get the set of unlocked artwork unlock data strings
         */
        fun getUnlockedArtworks(context: Context): Set<String> {
            return getPrefs(context).getStringSet(KEY_UNLOCKED_ARTWORKS, emptySet()) ?: emptySet()
        }

        /**
         * Check if a specific artwork is unlocked
         * @param unlockData The unlockData field from the Artwork
         * @return true if unlocked, false otherwise. Empty unlockData is always considered unlocked.
         */
        fun isArtworkUnlocked(context: Context, unlockData: String): Boolean {
            if (unlockData.isEmpty()) return true
            return getUnlockedArtworks(context).contains(unlockData)
        }

        /**
         * Unlock an artwork by adding its unlockData to the set
         * @param unlockData The unlockData field from the Artwork
         */
        fun unlockArtwork(context: Context, unlockData: String) {
            if (unlockData.isEmpty()) return

            val unlocked = getUnlockedArtworks(context).toMutableSet()
            unlocked.add(unlockData)

            getPrefs(context).edit()
                .putStringSet(KEY_UNLOCKED_ARTWORKS, unlocked)
                .apply()
        }

        /**
         * Lock an artwork by removing its unlockData from the set (useful for testing/debugging)
         * @param unlockData The unlockData field from the Artwork
         */
        fun lockArtwork(context: Context, unlockData: String) {
            if (unlockData.isEmpty()) return

            val unlocked = getUnlockedArtworks(context).toMutableSet()
            unlocked.remove(unlockData)

            getPrefs(context).edit()
                .putStringSet(KEY_UNLOCKED_ARTWORKS, unlocked)
                .apply()
        }

        /**
         * Clear all unlocked artworks (useful for testing/debugging)
         */
        fun clearUnlockedArtworks(context: Context) {
            getPrefs(context).edit()
                .remove(KEY_UNLOCKED_ARTWORKS)
                .apply()
        }
    }
}