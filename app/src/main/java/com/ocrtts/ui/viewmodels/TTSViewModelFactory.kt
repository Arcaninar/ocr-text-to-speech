package com.ocrtts.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TTSViewModelFactory(
    private val context: Context,
    private val initialLanguage: String,
    private val initialSpeed: Float
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TTSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TTSViewModel(context.applicationContext as Application, initialLanguage, initialSpeed) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
