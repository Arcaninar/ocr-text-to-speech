package com.ocrtts.base

//
//import android.media.AudioTrack
//import android.os.Build
//import android.util.Log
//import com.microsoft.cognitiveservices.speech.AudioDataStream
//import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult
//import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
//import java.util.concurrent.atomic.AtomicBoolean
//
//class SynthesisRunnable(
//    private val synthesizer: SpeechSynthesizer,
//    private val audioTrack: AudioTrack,
////    private val synchronizedLock: Any,
//    private val synchronizedLock: Object = Object(),
//    private var stopState: Boolean,
//    private val isPaused: AtomicBoolean,
//    private val content: String,
//    private val speed: Float
//) : Runnable {
//
//    override fun run() {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                audioTrack.playbackParams = audioTrack.playbackParams.setSpeed(this.speed)
//            }
//            audioTrack.play()
//
//            synchronized(synchronizedLock) {
//                stopState = false
//            }
//
//            val result: SpeechSynthesisResult = synthesizer.StartSpeakingTextAsync(content).get()
//            val audioDataStream = AudioDataStream.fromResult(result)
//
//            // 设置 chunk 大小为 50 ms: 24000 * 16 * 0.05 / 8 = 2400
//            val buffer = ByteArray(2400)
//
//            while (!stopState) {
//                Log.i("check", "checking isPaused ${isPaused.get()}")
//                synchronized(synchronizedLock) {
//                    while (isPaused.get()) {
//                        Log.i("check", "synchronizedLock waiting")
//                        synchronizedLock.wait()
//                    }
//                    val len = audioDataStream.readData(buffer).toInt()
//                    if (len == 0) {
//                        break
//                    }
//                    Log.i("check", "checking stopState $stopState")
//                    audioTrack.write(buffer, 0, len)
//                }
//            }
//            audioDataStream.close()
//        } catch (ex: Exception) {
//            Log.e("Speech Synthesis Demo", "unexpected ${ex.message}")
//            ex.printStackTrace()
//            assert(false)
//        }
//    }
//}


//version2

import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.microsoft.cognitiveservices.speech.AudioDataStream
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Condition
import kotlin.concurrent.withLock

class SynthesisRunnable(
    private val synthesizer: SpeechSynthesizer,
    private val audioTrack: AudioTrack,
    private val lock: ReentrantLock,
    private val condition: Condition,
    private var stopState: Boolean,
    private val isPaused: AtomicBoolean,
    private val content: String,
    private val speed: Float
) : Runnable {

    override fun run() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioTrack.playbackParams = audioTrack.playbackParams.setSpeed(this.speed)
            }
            audioTrack.play()
            Log.i("check", "audio start playing")

            lock.withLock {
                stopState = false
            }

            val result: SpeechSynthesisResult = synthesizer.StartSpeakingTextAsync(content).get()
            val audioDataStream = AudioDataStream.fromResult(result)

            // 设置 chunk 大小为 50 ms: 24000 * 16 * 0.05 / 8 = 2400
            val buffer = ByteArray(2400)

            while (!stopState) {
                Log.i("check", "checking isPaused ${isPaused.get()}")

                lock.withLock {
                    while (isPaused.get()) {
                        Log.i("check", "lock waiting")
                        condition.await()
                    }
                }

                val len = audioDataStream.readData(buffer).toInt()
                if (len == 0) {
                    break
                }
                Log.i("check", "checking stopState $stopState")
                audioTrack.write(buffer, 0, len)
            }
            audioDataStream.close()
        } catch (ex: Exception) {
            Log.e("Speech Synthesis Demo", "unexpected ${ex.message}")
            ex.printStackTrace()
            assert(false)
        }
    }
}