package com.code4galaxy.compose_cards.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.TextFieldValue
import org.json.JSONArray
import org.json.JSONObject

object CardRepository {
    private const val PREFS_NAME = "cards_repo_prefs"
    private const val KEY_CARDS = "cards_json"

    private lateinit var sharedPrefs: SharedPreferences

    val savedCards = mutableStateListOf<List<TextFieldValue>>()

    fun init(context: Context) {
        if (!::sharedPrefs.isInitialized) {
            sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun load() {
        val json = sharedPrefs.getString(KEY_CARDS, null) ?: return
        runCatching {
            val array = JSONArray(json)
            val loaded = mutableListOf<List<TextFieldValue>>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val number = TextFieldValue(obj.optString("number"))
                val name = TextFieldValue(obj.optString("name"))
                val expiry = TextFieldValue(obj.optString("expiry"))
                val cvv = TextFieldValue(obj.optString("cvv"))
                loaded.add(listOf(number, name, expiry, cvv))
            }
            savedCards.clear()
            savedCards.addAll(loaded)
        }
    }

    private fun persist() {
        val array = JSONArray()
        savedCards.forEach { card ->
            val obj = JSONObject()
            obj.put("number", card.getOrNull(0)?.text ?: "")
            obj.put("name", card.getOrNull(1)?.text ?: "")
            obj.put("expiry", card.getOrNull(2)?.text ?: "")
            obj.put("cvv", card.getOrNull(3)?.text ?: "")
            array.put(obj)
        }
        sharedPrefs.edit().putString(KEY_CARDS, array.toString()).apply()
    }

    fun addCard(card: List<TextFieldValue>) {
        savedCards.add(card)
        persist()
    }

    fun removeAt(index: Int) {
        if (index in 0 until savedCards.size) {
            savedCards.removeAt(index)
            persist()
        }
    }
}


