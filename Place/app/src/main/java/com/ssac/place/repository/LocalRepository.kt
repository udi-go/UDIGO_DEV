package com.ssac.place.repository

import android.content.Context

class LocalRepository {
    fun setMyToken(context: Context, token: String) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(MY_TOKEN, token).apply()
    }

    fun getMyToken(context: Context): String? {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getString(MY_TOKEN,  null)
    }

    companion object {
        private const val PREFERENCE_NAME = "place_preference"
        private const val MY_TOKEN = "my_token"

        val instance = LocalRepository()
    }
}