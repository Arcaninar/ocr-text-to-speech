package com.ocrtts.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime

object TimingUtility {
    private const val TAG = "TimingUtility"

    // For regular (non-suspending) functions
    fun measureExecutionTime(description: String = "", function: () -> Unit) {
        val duration = measureNanoTime {
            function()
        }

        if (description.isNotEmpty()) {
            Log.d(TAG, "$description Execution time: $duration ns")
        } else {
            Log.d(TAG, "Execution time: $duration ns")
        }
    }

    // For suspending functions
    suspend fun measureSuspendingExecutionTime(description: String = "", function: suspend () -> Unit) {
        val duration = measureNanoTime {
            withContext(Dispatchers.Default) {
                function()
            }
        }

        if (description.isNotEmpty()) {
            Log.d(TAG, "$description Execution time: $duration ns")
        } else {
            Log.d(TAG, "Execution time: $duration ns")
        }
    }
}
