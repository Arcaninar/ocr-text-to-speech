package com.ocrtts.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

object TimingUtility {
    private const val TAG = "TimingUtility"

    // For regular (non-suspending) functions
    fun measureExecutionTime(description: String = "", function: () -> Unit) {
        val duration = measureTimeMillis {
            function()
        }

        if (description.isNotEmpty()) {
            Log.d(TAG, "$description Execution time: $duration ms")
        } else {
            Log.d(TAG, "Execution time: $duration ms")
        }
    }

    @SuppressLint("ComposeNamingUppercase")
    @Composable
    fun measureComposableExecutionTime(description: String = "", function: @Composable () -> Unit) {
        val duration = measureTimeMillis {
            function()
        }

        if (description.isNotEmpty()) {
            Log.d(TAG, "$description Execution time: $duration ms")
        } else {
            Log.d(TAG, "Execution time: $duration ms")
        }
    }

    // For suspending functions
    suspend fun measureSuspendingExecutionTime(description: String = "", function: suspend () -> Unit) {
        val duration = measureTimeMillis {
            withContext(Dispatchers.Default) {
                function()
            }
        }

        if (description.isNotEmpty()) {
            Log.d(TAG, "$description Execution time: $duration ms")
        } else {
            Log.d(TAG, "Execution time: $duration ms")
        }
    }
}
