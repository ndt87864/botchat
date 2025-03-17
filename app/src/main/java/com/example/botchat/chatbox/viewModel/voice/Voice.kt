package com.example.botchat.chatbox.viewModel.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast

class Voice {
    private var speechRecognizer: SpeechRecognizer? = null

    fun initSpeechRecognizer(context: Context, onResult: (String) -> Unit) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Toast.makeText(context, "Không nhận diện được giọng nói!", Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    val resultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!resultList.isNullOrEmpty()) {
                        onResult(resultList[0])
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!partialResultList.isNullOrEmpty()) {
                        onResult(partialResultList[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
    }

    fun release() {
        speechRecognizer?.destroy()
    }
}
