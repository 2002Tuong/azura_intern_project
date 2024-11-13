package com.example.claptofindphone.presenter.common.audio

import android.content.Context
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.core.BaseOptions
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AudioClassificationHelper(
    val context: Context,
    val classificationListener: AudioClassificationListener,
    var currentModelName: String = YAMNET_MODEL_NAME,
    var threshold: Float = THRESHOLD,
    var overlapValue: Float = DEFAULT_OVERLAP,
    var sizeOfResults: Int = DEFAULT_SIZE_OF_RESULTS,
    var currDelegate: Int = 0,
    var numOfThreads: Int = 2
) {
    private lateinit var audioClassifier: AudioClassifier
    private lateinit var tensorAudioInstance: TensorAudio
    private var audioRecorder: AudioRecord? = null
    private lateinit var executorInstance: ScheduledThreadPoolExecutor

    private val classifyRunnableObject = Runnable {
        classifyAudio()
    }

    init {
        initClassifier()
    }

    fun initClassifier() {
        // Set general detection options, e.g. number of used threads
        val audioBaseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(numOfThreads)

        // Use the specified hardware for running the model. Default to CPU.
        // Possible to also use a GPU delegate, but this requires that the classifier be created
        // on the same thread that is using the classifier, which is outside of the scope of this
        // sample's design.
        when (currDelegate) {
            CPU_DELEGATE -> {
                // Default
            }

            NNAPI_DELEGATE -> {
                audioBaseOptionsBuilder.useNnapi()
            }
        }

        // Configures a set of parameters for the classifier and what results will be returned.
        val audioClassifierOptions = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(sizeOfResults)
            .setBaseOptions(audioBaseOptionsBuilder.build())
            .build()

        try {
            // Create the classifier and required supporting objects
            audioClassifier = AudioClassifier.createFromFileAndOptions(context, currentModelName, audioClassifierOptions)
            tensorAudioInstance = audioClassifier.createInputTensorAudio()
            audioRecorder = audioClassifier.createAudioRecord()
            startAudioClassification()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            classificationListener.onError(
                "Audio Classifier failed to initialize. See error logs for details"
            )

            Log.e("AudioClassification", "TFLite failed to load with error: " + e.message)
        }
    }

    fun startAudioClassification() {
        if (audioRecorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return
        }

        audioRecorder?.startRecording()
        executorInstance = ScheduledThreadPoolExecutor(1)

        // Each model will expect a specific audio recording length. This formula calculates that
        // length using the input buffer size and tensor format sample rate.
        // For example, YAMNET expects 0.975 second length recordings.
        // This needs to be in milliseconds to avoid the required Long value dropping decimals.
        val durationInMilliSeconds = ((audioClassifier.requiredInputBufferSize * 1.0f) /
                audioClassifier.requiredTensorAudioFormat.sampleRate) * 1000

        val period = (durationInMilliSeconds * (1 - overlapValue)).toLong()

        executorInstance.scheduleAtFixedRate(
            classifyRunnableObject,
            0,
            period,
            TimeUnit.MILLISECONDS
        )
    }

    private fun classifyAudio() {
        try {
            tensorAudioInstance.load(audioRecorder)
            var classifyTime = SystemClock.uptimeMillis()
            val output = audioClassifier.classify(tensorAudioInstance)
            classifyTime = SystemClock.uptimeMillis() - classifyTime
            classificationListener.onResult(output[0].categories, classifyTime)
        } catch (error: Exception) {
            Log.d("classifyAudio", "UnknownError")
            FirebaseCrashlytics.getInstance().recordException(error)
        }
    }

    fun stopAudioClassification() {
        audioRecorder?.stop()
        executorInstance.shutdownNow()
    }

    companion object {
        const val CPU_DELEGATE = 0
        const val NNAPI_DELEGATE = 1
        const val THRESHOLD = 0.3f
        const val DEFAULT_SIZE_OF_RESULTS = 2
        const val DEFAULT_OVERLAP = 0.5f
        const val YAMNET_MODEL_NAME = "yamnet.tflite"
    }
}
