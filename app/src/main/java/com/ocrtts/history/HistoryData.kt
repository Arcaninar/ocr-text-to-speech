package com.ocrtts.history

import android.content.Context
import android.content.SharedPreferences
import java.io.File

const val MAX_HISTORY = 10

fun addToHistory(context: Context, fileName: String) {
    val sharedPreferences = getSharedPreferences(context)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putString(fileName, fileName)
    editor.apply()
}

fun getHistory(context: Context): List<MutableMap.MutableEntry<String, out Any?>> {
    val sharedPreferences = getSharedPreferences(context)
    val allEntries = sharedPreferences.all.entries.sortedByDescending { it.key }
    if (allEntries.size > MAX_HISTORY) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        for (i in (allEntries.size - 1) downTo MAX_HISTORY) {
            val filePath = allEntries[i].value as String
            val file = File(filePath)
            file.delete()
            editor.remove(allEntries[i].key)
        }
        editor.apply()
    }
    return allEntries.take(MAX_HISTORY)
}

private fun getSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("photo_history", Context.MODE_PRIVATE)
}