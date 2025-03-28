package com.example.botchat.converstation.viewModel

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botchat.chatbox.constants.Constants
import com.example.botchat.chatbox.viewModel.search.GoogleSearch
import com.example.botchat.chatbox.viewModel.search.YouTubeSearch
import com.example.botchat.converstation.viewModel.conversation.ConversationHandler
import com.example.botchat.converstation.viewModel.setting.SettingsHandler
import com.example.botchat.converstation.viewModel.speech.SpeechHandler
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

class VoidChatViewModel : ViewModel() {

    private val generativeModel = GenerativeModel(
        modelName = Constants.MODEL_NAME,
        apiKey = Constants.API_KEY
    )
    val firestore = FirebaseFirestore.getInstance()
    private val googleSearch = GoogleSearch()
    private val youtubeSearch = YouTubeSearch()
    private var speechRecognizer: SpeechRecognizer? = null
    var textToSpeech: TextToSpeech? = null
    var isRecording = mutableStateOf(false)
    var isBotSpeaking = mutableStateOf(false)
    var isSpeakerOn = mutableStateOf(true)
    private lateinit var sharedPreferences: SharedPreferences
    val conversation = mutableStateListOf<Pair<String, String>>()
    val currentEditingSettingId = mutableStateOf<String?>(null)
    var userName = mutableStateOf("Bạn")
    var botName = mutableStateOf("Bot")
    var speechRate = mutableStateOf(1.0f)
    var pitch = mutableStateOf(1.0f)
    var settingName = mutableStateOf("")
    var botCharacteristics = mutableStateOf("")
    var otherRequirements = mutableStateOf("")

    private lateinit var settingsHandler: SettingsHandler
    private lateinit var conversationHandler: ConversationHandler
    private lateinit var speechHandler: SpeechHandler

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("BotSettings", Context.MODE_PRIVATE)
        settingsHandler = SettingsHandler(
            firestore,
            sharedPreferences,
            userName,
            botName,
            speechRate,
            pitch,
            settingName,
            botCharacteristics,
            otherRequirements,
            ::applySpeechSettings,
            currentEditingSettingId
        )
        conversationHandler = ConversationHandler(
            generativeModel,
            firestore,
            googleSearch,
            youtubeSearch,
            conversation,
            userName,
            botName,
            botCharacteristics,
            otherRequirements,
            ::speakBotResponse,
            viewModelScope
        )

        if (speechRecognizer == null && SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d("Speech", "Sẵn sàng ghi âm")
                    }
                    override fun onBeginningOfSpeech() {
                        Log.d("Speech", "Bắt đầu nói")
                        speechHandler.resetSilenceTimer()
                    }
                    override fun onRmsChanged(rmsdB: Float) {
                        // Giảm ngưỡng để nhạy hơn với âm thanh nhỏ
                        if (rmsdB > 0.5f) { // Thử nghiệm với ngưỡng thấp hơn
                            speechHandler.resetSilenceTimer()
                            Log.d("Speech", "Âm thanh được phát hiện: $rmsdB")
                        }
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        Log.d("Speech", "Kết thúc nói")
                        // Không dừng ngay, để timer xử lý
                    }
                    override fun onError(error: Int) {
                        isRecording.value = false
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Lỗi âm thanh"
                            SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Đang bận"
                            else -> "Lỗi không xác định: $error"
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        Log.e("Speech", errorMsg)
                    }
                    override fun onResults(results: Bundle?) {
                        val resultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!resultList.isNullOrEmpty()) {
                            val userMessage = resultList[0]
                            processUserMessage(userMessage, context)
                        } else {
                            Toast.makeText(context, "Không nhận được kết quả", Toast.LENGTH_SHORT).show()
                        }
                        isRecording.value = false
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        // Theo dõi kết quả từng phần để reset timer
                        val partialList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!partialList.isNullOrEmpty()) {
                            speechHandler.resetSilenceTimer()
                            Log.d("Speech", "Kết quả từng phần: ${partialList[0]}")
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } else {
            Toast.makeText(context, "Thiết bị không hỗ trợ ghi âm", Toast.LENGTH_LONG).show()
        }

        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale("vi_VN")
                    applySpeechSettings()
                    reloadSettings(context)
                } else {
                    Toast.makeText(context, "Không khởi tạo được TextToSpeech", Toast.LENGTH_LONG).show()
                    Log.e("TextToSpeech", "Khởi tạo thất bại: $status")
                }
            }
        } else {
            reloadSettings(context)
        }

        speechHandler = SpeechHandler(
            speechRecognizer,
            textToSpeech,
            isRecording,
            isBotSpeaking,
            isSpeakerOn
        )

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Vui lòng cấp quyền ghi âm trong cài đặt", Toast.LENGTH_LONG).show()
        }

        viewModelScope.launch {
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "default@example.com"
            saveUserToFirestore(userEmail)
            saveSettings()
        }
    }

    private fun reloadSettings(context: Context) {
        viewModelScope.launch {
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "default@example.com"
            val loadedFromFirestore = loadDefaultOrLatestSettings(userEmail)
            if (!loadedFromFirestore) {
                settingsHandler.loadSettings()
                Log.d("VoidChatViewModel", "Tải cài đặt từ SharedPreferences")
            }
            settingsHandler.reloadUsedDoc(context, userEmail)
            if (conversation.isEmpty()) {
                conversationHandler.greetUser(context)
            }
            Log.d("VoidChatViewModel", "Đã tải lại cài đặt: userName=${userName.value}, botName=${botName.value}")
        }
    }

    fun onResume(context: Context) {
        reloadSettings(context)
        saveSettings()
    }

    fun refreshConversation(context: Context, shouldSpeak: Boolean = true) = conversationHandler.refreshConversation(context, shouldSpeak)
    fun greetUser(context: Context) = conversationHandler.greetUser(context)
    fun processUserMessage(message: String, context: Context) = conversationHandler.processUserMessage(message, context)
    fun startRecording(context: Context) = speechHandler.startRecording(context)
    fun stopRecording() = speechHandler.stopRecording()
    fun speakBotResponse(response: String, context: Context) = speechHandler.speakBotResponse(response, context)
    fun stopBotSpeaking() = speechHandler.stopBotSpeaking()
    fun toggleSpeaker() = speechHandler.toggleSpeaker()
    fun applySpeechSettings() = speechHandler.applySpeechSettings(speechRate.value, pitch.value)
    suspend fun saveUserToFirestore(userEmail: String) = settingsHandler.saveUserToFirestore(userEmail)
    suspend fun loadDefaultOrLatestSettings(userEmail: String) = settingsHandler.loadDefaultOrLatestSettings(userEmail)
    suspend fun reloadUsedDoc(context: Context, userEmail: String) = settingsHandler.reloadUsedDoc(context, userEmail)
    fun saveSettings() = settingsHandler.saveSettings()
    fun saveSettingsToFirestore(userEmail: String, context: Context) = settingsHandler.saveSettingsToFirestore(userEmail, context)
    fun reset() = settingsHandler.reset()

    override fun onCleared() {
        settingsHandler.onCleared(speechRecognizer, textToSpeech)
        super.onCleared()
    }
}