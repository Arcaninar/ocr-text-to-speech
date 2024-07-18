package com.ocrtts.base

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class OfflineTextSynthesis(private val context: Context) : TextToSpeech.OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 默认设置为英语
            setLanguage(Locale.US)
        } else {
            Log.e("OfflineTTSManager", "Initialization failed")
        }
    }

    fun setLanguage(locale: Locale) {
        val result = textToSpeech?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("OfflineTTSManager", "Language is not supported")
        } else {
            isInitialized = true
        }
    }

    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }

    fun speak(text: String) {
        if (isInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("OfflineTTSManager", "TTS not initialized")
        }
    }

    //Split the sentence into smaller pieces to reduce generation time
    fun segmentedPlay(text: String) {
        if (isInitialized) {
            val segments = text.split(".") //maybe need to set a variable for changes
            for (segment in segments) {
                textToSpeech?.speak(segment.trim(), TextToSpeech.QUEUE_ADD, null, null)
            }
        } else {
            Log.e("OfflineTTSManager", "TTS  not initialized")
        }
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}


//import android.content.Context
//import android.speech.tts.TextToSpeech
//import android.util.Log
//import java.util.Locale
//import java.util.concurrent.Executors
//import java.util.concurrent.atomic.AtomicBoolean
//import java.util.concurrent.locks.Condition
//import java.util.concurrent.locks.ReentrantLock
//import kotlin.concurrent.withLock
//
//class OfflineTextSynthesis(private val context: Context) {
//
//    private var textToSpeech: TextToSpeech? = null
//    private val lock = ReentrantLock()
//    private val condition: Condition = lock.newCondition()
//    private var stopState = false
//    val isPaused = AtomicBoolean(false)
//    private var ttsThread = Executors.newSingleThreadExecutor()
//
//    init {
//        textToSpeech = TextToSpeech(context) { status ->
//            if (status == TextToSpeech.SUCCESS) {
//                textToSpeech?.setLanguage(Locale.US)
//            } else {
//                Log.e("OfflineTextSynthesis", "Initialization failed")
//            }
//        }
//    }
//
//    fun setLanguage(locale: Locale) {
//        val result = textToSpeech?.setLanguage(locale)
//        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//            Log.e("OfflineTextSynthesis", "Language is not supported")
//        }
//    }
//
//    fun setSpeechRate(rate: Float) {
//        textToSpeech?.setSpeechRate(rate)
//    }
//
//    fun startSpeaking(text: String?) {
//        val inputText = text ?: "Empty!"
//        ttsThread.submit(SynthesisRunnable(textToSpeech!!, lock, condition, stopState, isPaused, inputText))
//    }
//
//    fun pauseSynthesis() {
//        lock.withLock {
//            isPaused.set(true)
//            textToSpeech?.stop()
//        }
//    }
//
//    fun resumeSynthesis() {
//        lock.withLock {
//            isPaused.set(false)
//            condition.signalAll()
//        }
//    }
//
//    fun stopSynthesis() {
//        lock.withLock {
//            stopState = true
//        }
//        textToSpeech?.stop()
//    }
//
//    fun replay(text: String?) {
//        stopSynthesis()
//        startSpeaking(text)
//    }
//
//    fun destroy() {
//        textToSpeech?.shutdown()
//        ttsThread.shutdownNow()
//    }
//}