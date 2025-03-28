package com.example.botchat.chatbox.viewModel.data

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botchat.chatbox.constants.Constants
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.viewModel.call.CallHandler
import com.example.botchat.chatbox.model.UiEvent
import com.example.botchat.chatbox.viewModel.search.GoogleSearch
import com.example.botchat.chatbox.viewModel.search.YouTubeSearch
import com.example.botchat.chatbox.viewModel.sms.SMSHandler
import com.example.botchat.chatbox.viewModel.thinking.SelfReasoningHandler
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessageHandler(
    private val chatRoomManager: ChatRoomManager,
    private val generativeModel: GenerativeModel,
    private val selfReasoningHandler: SelfReasoningHandler,
    private val callHandler: CallHandler,
    private val smsHandler: SMSHandler,
    private val _event: Channel<UiEvent>,
    private val messageList: MutableList<MessageModel>,
    private val auth: FirebaseAuth,
    private val formattedCurrentDate: String
) : ViewModel() {

    var shouldStopTyping by mutableStateOf(false)
    var isChatbotResponding by mutableStateOf(false)
        private set
    //Hàm gửi và xử lý tin nhắn
    fun sendMessage(question: String, applyCustomization: Boolean, userName: String, occupation: String,
                    botTraits: String, additionalInfo: String, isAnonymous: Boolean, isSearchMode: Boolean,
                    isYoutubeMode: Boolean, isDeepThinkingEnabled: Boolean, currentModelName: String, context: Context? = null) {
        viewModelScope.launch {
            shouldStopTyping = false
            val userEmail = auth.currentUser?.email
            try {
                if (messageList.none { it.message == question && it.role == "user" }) {
                    val userMessage = MessageModel(question, "user", System.currentTimeMillis())
                    messageList.add(userMessage)

                    if (userEmail != null && !isAnonymous) {
                        chatRoomManager.saveMessage(userMessage, "user", userEmail)
                    }
                }

                // Tạo prompt tùy chỉnh nếu applyCustomization bật
                val promptBase = if (applyCustomization) {
                    """
                    Ngày hiện tại: $formattedCurrentDate
                    Câu hỏi: $question
                    Hãy đưa ra câu trả lời chính xác nhất.
                    Lưu ý:
                    1.Gọi tôi là: $userName!
                    2.Tôi là: $occupation.(nếu $occupation != "" thì mới sử dụng dữ kiện này )
                    3.Thông tin khác về tôi: $additionalInfo
                    4. Phản hồi theo phong cách: $botTraits
                    5. Hãy chỉ sử dụng thông tin $occupation/$additionalInfo để tham khảo chứ không nhắc đến ở phần trả lời.
                """.trimIndent()
                } else {
                    ""
                }

                var searchReference =
                    """
                    Hãy đưa ra câu trả lời chính xác nhất.
                    Nếu câu hỏi liên quan đến thời gian hãy sử dụng dữ kiện thời gian $formattedCurrentDate, ngược lại 
                    thì không đưa ra dữ kiện này.
                """.trimIndent()

                // Nếu chế độ tìm kiếm bật, lấy kết quả tìm kiếm từ Google và YouTube làm tài liệu tham khảo
                if (isSearchMode) {
                    val googleResults = GoogleSearch().search(question)
                    val youtubeResults = if (isYoutubeMode && question.contains("video", ignoreCase = true)) {
                        val searchQuery = question.replace("video", "", ignoreCase = true).replace(Regex("[:,.?]"), "").trim()
                        val maxResults = extractResultCount(question)
                        YouTubeSearch().search(searchQuery, maxResults).joinToString("\n")
                    } else ""

                    searchReference = """
                    Dữ liệu tham khảo từ Google:
                    $googleResults
                    
                    Dữ liệu tham khảo từ YouTube:
                    $youtubeResults
                    Dựa trên các dữ liệu trên và kiến thức của bạn, hãy đưa ra câu trả lời.
                    Lưu ý: Hiển thị lại các đường link từ tài liệu tham khảo nếu phù hợp.
                """.trimIndent()
                }

                // Kết hợp promptBase với dữ liệu tham khảo (nếu có)
                val finalPrompt = if (applyCustomization) {
                    "$promptBase\n\n$searchReference"
                } else {
                    "$question\n\n$searchReference"
                }

                if (isDeepThinkingEnabled || currentModelName == "Genmini 2 Thinking") {
                    val botAnswer = selfReasoningHandler.selfReasoningResponse(finalPrompt)
                    simulateTyping(botAnswer, userEmail, isAnonymous)
                } else {
                    val typingMessage = MessageModel("Đang gõ....", "model", System.currentTimeMillis())
                    messageList.add(typingMessage)

                    if (question == "[Hình ảnh]") return@launch
                    if (question.startsWith("#send|")) {
                        val parts = question.split("|")
                        if (parts.size == 3) {
                            val number = parts[1]
                            val content = parts[2]
                            messageList.removeLast()
                            smsHandler.sendMessage(number, content)
                            return@launch
                        }
                    }

                    if (smsHandler.isMessageCommand(question)) {
                        delay(1000)
                        smsHandler.handleMessageCommand(question)
                    } else if (callHandler.isCallCommand(question)) {
                        delay(1000)
                        messageList.removeLast()
                        delay(1000)
                        callHandler.handleCallCommand(question)
                    } else if (callHandler.isDialCommand(question)) {
                        delay(1000)
                        messageList.removeLast()
                        delay(1000)
                        callHandler.handleDialCommand(question)
                    } else if (isQuestionAboutBot(question)) {
                        delay(2000)
                        val botAnswer = "Mình là ${Constants.BOT_NAME}. ${Constants.PROFILE}"
                        messageList.removeLast()
                        simulateTyping(botAnswer, userEmail, isAnonymous)
                    } else {
                        val history = messageList.map { content(it.role) { text(it.message) } }.toList()
                        val chat = generativeModel.startChat(history)
                        val response = chat.sendMessage(finalPrompt)

                        messageList.removeLast()
                        val botAnswer = response.text ?: "Không tìm thấy câu trả lời!"
                        simulateTyping(botAnswer, userEmail, isAnonymous)
                    }
                }
            } catch (e: Exception) {
                if (messageList.isNotEmpty() && (messageList.last().message == "Đang suy nghĩ sâu..." || messageList.last().message == "Đang gõ....")) {
                    messageList.removeLast()
                }
                messageList.add(MessageModel("Lỗi: ${e.message}", "model", System.currentTimeMillis()))
                context?.let { Toast.makeText(it, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    fun editMessage(
        originalMessage: MessageModel,
        newMessageText: String,
        context: Context,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch

            // Tìm chỉ số của tin nhắn cần chỉnh sửa trong danh sách
            val messageIndex = messageList.indexOf(originalMessage)
            if (messageIndex == -1) {
                Toast.makeText(context, "Không tìm thấy tin nhắn để chỉnh sửa", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Xóa tất cả tin nhắn từ vị trí của tin nhắn được chỉnh sửa trở đi
            if (messageIndex < messageList.size) {
                messageList.subList(messageIndex, messageList.size).clear()
            }

            // Thêm tin nhắn mới vào danh sách (thay thế tin nhắn cũ)
            val updatedMessage = MessageModel(
                message = newMessageText,
                role = "user",
                timestamp = System.currentTimeMillis()
            )
            messageList.add(messageIndex, updatedMessage)

            // Đồng bộ với Firestore
            if (userEmail != null) {
                // Xóa các tin nhắn phía sau trong Firestore
                FirestoreHandler().deleteMessagesAfterTimestamp(userEmail, 0, originalMessage.timestamp)
                // Lưu tin nhắn mới
                chatRoomManager.saveMessage(updatedMessage, "user", userEmail)
            }

            // Gọi sendMessage để bot trả lời dựa trên tin nhắn mới
            sendMessage(newMessageText, applyCustomization = false, userName = "Bạn", occupation = "",
                botTraits = "", additionalInfo = "", isAnonymous = false, isSearchMode = false,
                isYoutubeMode = false, isDeepThinkingEnabled = false, currentModelName = "")

            // Gọi callback khi hoàn tất
            onComplete()
        }
    }

    private suspend fun simulateTyping(botAnswer: String, userEmail: String?, isAnonymous: Boolean) {
        val botMessage = MessageModel("", "model", System.currentTimeMillis())
        messageList.add(botMessage)
        var currentText = ""
        shouldStopTyping = false // Reset trạng thái dừng
        isChatbotResponding = true // Đặt trạng thái chatbot đang trả lời

        botAnswer.forEach { char ->
            if (shouldStopTyping) {
                // Không xóa tin nhắn, giữ nguyên nội dung đã gõ
                if (userEmail != null && !isAnonymous) {
                    chatRoomManager.saveMessage(botMessage.copy(message = currentText), "model", userEmail)
                }
                isChatbotResponding = false
                return // Thoát vòng lặp, không thêm ký tự mới
            }
            currentText += char
            messageList[messageList.size - 1] = botMessage.copy(message = currentText)
            delay(8) // Delay điều chỉnh tốc độ giữa mỗi ký tự
        }

        // Nếu không bị dừng giữa chừng, lưu toàn bộ tin nhắn
        if (!shouldStopTyping && userEmail != null && !isAnonymous) {
            chatRoomManager.saveMessage(botMessage.copy(message = botAnswer), "model", userEmail)
        }
        isChatbotResponding = false // Kết thúc trạng thái trả lời
    }

    fun stopTyping() {
        shouldStopTyping = true
        isChatbotResponding = false
    }

    private suspend fun isQuestionAboutBot(question: String): Boolean {
        val classificationPrompt = """
        Xác định xem câu hỏi sau có phải hỏi/xác nhận về tên /profile của chatbot hoặc chatbot là gì hay không.
        Nếu có, trả lời "YES". Nếu không, trả lời "NO".
        Câu hỏi: "$question"
    """.trimIndent()

        val chat = generativeModel.startChat(emptyList()) // Không cần lịch sử hội thoại ở đây
        val classificationResponse = chat.sendMessage(classificationPrompt).text ?: "NO"

        return classificationResponse.contains("YES", ignoreCase = true)
    }

    private fun extractResultCount(question: String): Int {
        val regex = Regex("hiển thị (\\d+) kết quả")
        val matchResult = regex.find(question)
        return matchResult?.groups?.get(1)?.value?.toIntOrNull() ?: 5
    }
}