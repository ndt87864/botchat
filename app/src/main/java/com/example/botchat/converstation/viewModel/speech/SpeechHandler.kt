package com.example.botchat.converstation.viewModel.speech

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.MutableState

class SpeechHandler(
    private val speechRecognizer: SpeechRecognizer?,
    private val textToSpeech: TextToSpeech?,
    private val isRecording: MutableState<Boolean>,
    private val isBotSpeaking: MutableState<Boolean>,
    private val isSpeakerOn: MutableState<Boolean>
) {

    fun startRecording(context: Context) {
        if (!isRecording.value && !isBotSpeaking.value) {
            if (speechRecognizer == null) {
                Log.e("SpeechHandler", "SpeechRecognizer chưa được khởi tạo")
                return
            }
            isRecording.value = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN") // Ngôn ngữ tiếng Việt
            }
            speechRecognizer.startListening(intent)
            Log.d("SpeechHandler", "Bắt đầu ghi âm")
        } else {
            Log.d("SpeechHandler", "Không thể ghi âm: Đang ghi hoặc bot đang nói")
        }
    }

    fun stopRecording() {
        if (isRecording.value) {
            speechRecognizer?.stopListening()
            isRecording.value = false
            Log.d("SpeechHandler", "Dừng ghi âm")
        }
    }

    fun speakBotResponse(response: String, context: Context) {
        if (!isSpeakerOn.value) {
            Log.d("SpeechHandler", "Loa bị tắt, không phát âm thanh")
            return
        }
        if (textToSpeech == null) {
            Log.e("SpeechHandler", "TextToSpeech chưa được khởi tạo")
            return
        }
        val sanitizedResponse = response.replace(Regex("[~!^*?|\\[\\]\\\\{};`-]"), "")
        isBotSpeaking.value = true
        textToSpeech.speak(sanitizedResponse, TextToSpeech.QUEUE_FLUSH, null, "BotResponse")
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("SpeechHandler", "Bắt đầu phát âm thanh")
            }
            override fun onDone(utteranceId: String?) {
                isBotSpeaking.value = false
                Log.d("SpeechHandler", "Kết thúc phát âm thanh")
            }
            override fun onError(utteranceId: String?) {
                isBotSpeaking.value = false
                Log.e("SpeechHandler", "Lỗi phát âm thanh")
            }
        })
    }

    fun stopBotSpeaking() {
        textToSpeech?.stop()
        isBotSpeaking.value = false
        Log.d("SpeechHandler", "Dừng phát âm thanh")
    }

    fun toggleSpeaker() {
        isSpeakerOn.value = !isSpeakerOn.value
        if (!isSpeakerOn.value && isBotSpeaking.value) {
            stopBotSpeaking()
        }
        Log.d("SpeechHandler", "Loa: ${if (isSpeakerOn.value) "Bật" else "Tắt"}")
    }

    fun applySpeechSettings(speechRate: Float, pitch: Float) {
        textToSpeech?.let {
            it.setSpeechRate(speechRate)
            it.setPitch(pitch)
            Log.d("SpeechHandler", "Áp dụng cài đặt giọng nói: speed=$speechRate, pitch=$pitch")
        } ?: Log.e("SpeechHandler", "TextToSpeech chưa sẵn sàng")
    }
}