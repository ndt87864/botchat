package com.example.botchat.converstation.viewModel.conversation

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.botchat.R
import com.example.botchat.chatbox.viewModel.search.GoogleSearch
import com.example.botchat.chatbox.viewModel.search.YouTubeSearch
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConversationHandler(
    private val generativeModel: GenerativeModel,
    private val firestore: FirebaseFirestore,
    private val googleSearch: GoogleSearch,
    private val youtubeSearch: YouTubeSearch,
    private val conversation: MutableList<Pair<String, String>>,
    private val userName: MutableState<String>,
    private val botName: MutableState<String>,
    private val botCharacteristics: MutableState<String>,
    private val otherRequirements: MutableState<String>,
    private val speakBotResponse: (String, Context) -> Unit,
    private val viewModelScope: CoroutineScope
) {

    // =========================
    // (3) HÀM XỬ LÝ HỘI THOẠI & AI
    // =========================
    fun refreshConversation(context: Context, shouldSpeak: Boolean = true) {
        conversation.clear()
        if (conversation.isEmpty() && shouldSpeak) {
            greetUser(context)
        }
    }

    private suspend fun createDefaultSetting(userEmail: String) {
        val defaultData = mapOf(
            "userName" to "Bạn",
            "botName" to "Bot",
            "speechRate" to 1.0,
            "pitch" to 1.0,
            "settingName" to "Default Setting",
            "botCharacteristics" to "",
            "otherRequirements" to "",
            "timestamp" to System.currentTimeMillis(),
            "isDefault" to true
        )
        firestore.collection("void_settings")
            .document(userEmail)
            .collection("settings")
            .document("default")
            .set(defaultData)
            .await()
    }

    fun greetUser(context: Context) {
        if (userName.value.isEmpty() || botName.value.isEmpty()) {
            viewModelScope.launch {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return@launch
                val settingsCollection = firestore.collection("void_settings")
                    .document(userEmail)
                    .collection("settings")
                    .get()
                    .await()

                if (settingsCollection.isEmpty) {
                    createDefaultSetting(userEmail)
                }
                // Note: reloadUsedDoc is not here, assumed to be called externally
                val greeting = "Xin chào ${userName.value}, ${botName.value} có thể giúp gì cho ${userName.value}?"
                conversation.add(Pair("", greeting))
                speakBotResponse(greeting, context)
            }
        } else {
            val greeting = "Xin chào ${userName.value}, ${botName.value} có thể giúp gì cho ${userName.value}?"
            conversation.add(Pair("", greeting))
            speakBotResponse(greeting, context)
        }
    }

    fun processUserMessage(message: String, context: Context) {
        viewModelScope.launch {
            conversation.add(Pair(message, "Đang suy nghĩ sâu..."))
            val mp = MediaPlayer.create(context, R.raw.cho_rep)
            try {
                mp.isLooping = true
                mp.setOnCompletionListener { mp.release() }
                mp.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val botResponse = generateSelfReasoningResponse(message)
            mp.stop()
            conversation[conversation.size - 1] = Pair(message, botResponse)
            speakBotResponse(botResponse, context)
        }
    }

    private suspend fun generateSelfReasoningResponse(question: String): String {
        val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val chatHistory = conversation.joinToString(separator = "\n") {
            "${userName.value}: ${it.first}\n${botName.value}: ${it.second}"
        }
        val googleSearchResults = googleSearch.search(question)
        val googleJsonResults = processSearchResults(googleSearchResults)
        val youtubeResults = youtubeSearch.search(question, maxResults = 3)
        val youtubeJsonResults = processYouTubeResults(youtubeResults)
        val prompt = """
            Câu hỏi từ ${userName.value}: $question
            Ngày hiện tại: $currentDate
            Lịch sử trò chuyện: $chatHistory
            Tài liệu tham khảo từ YouTube (JSON): $youtubeJsonResults
            Đặc điểm của tôi: ${botCharacteristics.value}
            Yêu cầu khác: ${otherRequirements.value}
            Dựa trên các dữ liệu trên và kiến thức của bạn, hãy đưa ra câu trả lời ngắn gọn, chính xác nhất.
            Chú ý : 
            1. Bạn là ${botName.value} (tên tôi đặt cho bạn), tôi là ${userName.value}, hãy xưng hô phù hợp.
            2. Không nêu lại nguồn tham khảo.
            3. Nếu câu trả lời dài, hãy trả lời theo đợt và hỏi thêm người dùng.
        """.trimIndent()
        val chat = generativeModel.startChat(emptyList())
        val response = chat.sendMessage(prompt)
        return response.text ?: "Không thể trả lời dựa trên dữ liệu hiện có."
    }

    private suspend fun processSearchResults(searchResults: String): String = withContext(Dispatchers.IO) {
        val lines = searchResults.split("\n").filter { it.isNotBlank() }.take(5)
        val jsonArray = mutableListOf<JSONObject>()
        lines.forEach { line ->
            val url = extractUrl(line)
            if (url != null) {
                try {
                    val document = Jsoup.connect(url).get()
                    val bodyText = document.body().text()
                    val jsonObject = JSONObject().apply {
                        put("url", url)
                        put("content", bodyText)
                    }
                    jsonArray.add(jsonObject)
                } catch (e: Exception) {
                    Log.e("VoidChatViewModel", "Lỗi truy cập $url: ${e.message}")
                }
            }
        }
        jsonArray.toString()
    }

    private suspend fun processYouTubeResults(youtubeResults: List<String>): String = withContext(Dispatchers.IO) {
        val jsonArray = mutableListOf<JSONObject>()
        youtubeResults.forEach { result ->
            val url = extractUrl(result)
            if (url != null) {
                val title = result.substringBefore("-").trim()
                val jsonObject = JSONObject().apply {
                    put("title", title)
                    put("url", url)
                }
                jsonArray.add(jsonObject)
            }
        }
        jsonArray.toString()
    }

    private fun extractUrl(text: String): String? {
        val urlRegex = Regex("(https?://\\S+)")
        return urlRegex.find(text)?.value?.trim()?.removeSuffix("\"")
    }
}