package com.ocrtts.base

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
//Azure SDK
import com.microsoft.cognitiveservices.speech.Connection
import com.microsoft.cognitiveservices.speech.PropertyId
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
//import okio.AsyncTimeout.Companion.condition
//import okio.AsyncTimeout.Companion.lock
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.withLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Condition

class AzureTextSynthesis(voice: String) {

    private var speechConfig: SpeechConfig? = null
    private var synthesizer: SpeechSynthesizer? = null //‘？’可能为null的对象
    private var connection: Connection? = null
    private var audioTrack: AudioTrack

    //private val synchronizedLock = Any()
    // any类 没有notifyAll()方法 只有java.lang.Object类有
//    private val synchronizedLock = Object()

    //reentrantLock和condition方法
    private val lock = ReentrantLock()
    private val condition: Condition = lock.newCondition()

    private var stopState = false
    val isPaused = AtomicBoolean(false)
    val voiceSet = arrayOf("zh-HK-HiuMaanNeural", "zh-TW-HsiaoChenNeural", "en-GB-SoniaNeural")
    private var ttsThread = Executors.newSingleThreadExecutor()

    init {
        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(24000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        Log.i("check", "create audioTrack")
        //调用
        createSynthesizer(voice)
    }

    fun updateVoice(voice: String){
        speechConfig?.speechSynthesisVoiceName = voice
        Log.i("generate", "Dynamic updates!!")
    }

    private fun createSynthesizer(voice: String) {
        try {
            ttsThread = Executors.newSingleThreadExecutor()

            synthesizer?.close()
            speechConfig?.close()
            connection?.close()

            speechConfig = SpeechConfig.fromSubscription(SUBSCRIPTION_KEY, SERVICE_REGION).apply {
                setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Raw24Khz16BitMonoPcm)
                speechSynthesisVoiceName = voice
            }
            synthesizer = SpeechSynthesizer(speechConfig, null)
            connection = Connection.fromSpeechSynthesizer(synthesizer)

            connection?.connected?.addEventListener { _, _ ->
                Log.i("check", "Connection established.")
            }

            connection?.disconnected?.addEventListener { _, _ ->
                Log.i("check", "Disconnected.")
            }

            synthesizer?.SynthesisStarted?.addEventListener { _, e ->
                Log.i("check", "Synthesis started. Result Id: ${e.result.resultId}")
                e.close()
            }

            synthesizer?.Synthesizing?.addEventListener { _, e ->
                Log.i("check", "Synthesizing. received ${e.result.audioLength} bytes")
                e.close()
            }

            synthesizer?.SynthesisCompleted?.addEventListener { _, e ->
                Log.i("check", "Synthesis finished.")
                Log.i("check", "\tFirst byte latency: ${e.result.properties.getProperty(PropertyId.SpeechServiceResponse_SynthesisFirstByteLatencyMs)} ms.")
                Log.i("check", "\tFinish latency: ${e.result.properties.getProperty(PropertyId.SpeechServiceResponse_SynthesisFinishLatencyMs)} ms.")
                e.close()
            }

            synthesizer?.SynthesisCanceled?.addEventListener { _, e ->
                val cancellationDetails = SpeechSynthesisCancellationDetails.fromResult(e.result).toString()
                Log.i("check", "Error synthesizing. Result ID: ${e.result.resultId}. Error detail: \n$cancellationDetails")
                e.close()
            }

            connection?.openConnection(true)

        } catch (ex: Exception) {
            Log.e("check", "unexpected in onCreate() ${ex.message}")
            assert(false)
        }
    }

    fun startPlaying(inputText: String?, speed: Float) {
        val text = inputText ?: "Empty!"
        ttsThread.submit(SynthesisRunnable(synthesizer!!, audioTrack, lock, condition, stopState, isPaused, text, speed))
    }

    fun pauseSynthesis() {
        lock.withLock {
            isPaused.set(true)
            audioTrack.pause()
        }
    }

    fun resumeSynthesis() {
        lock.withLock {
            isPaused.set(false)
            condition.signalAll() // Wake up all waiting threads
            audioTrack.play()
        }
    }

    fun stopSynthesis() {
        synthesizer?.StopSpeakingAsync()
        lock.withLock {
            stopState = true
        }
//        audioTrack.pause()
        audioTrack.stop()
        audioTrack.flush()
    }

    fun replay(inputText: String?, speed: Float) {
        stopSynthesis()
        startPlaying(inputText, speed)
    }

    fun destroy() {
        synthesizer?.close()
        speechConfig?.close()
        ttsThread.shutdownNow()
    }

    companion object {
        private const val SUBSCRIPTION_KEY = "767ddc9821bc4c868eab093c2c8b080c"
        //private static String speechSubscriptionKey = "*************************";
        private const val SERVICE_REGION = "southeastasia"
    }
}