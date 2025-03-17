package com.example.botchat.chatbox.viewModel.thinking

import android.util.Log
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.viewModel.search.GoogleSearch
import com.example.botchat.chatbox.viewModel.search.YouTubeSearch
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SelfReasoningHandler(
    private val messageList: MutableList<MessageModel>,
    private val generativeModel: GenerativeModel,
    private val googleSearch: GoogleSearch,
    private val youtubeSearch: YouTubeSearch    // Thêm YouTubeSearch vào constructor
) {
    val currentDate = LocalDateTime.now()
    val dateFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
    val formattedCurrentDate = currentDate.format(dateFormatter)
    suspend fun selfReasoningResponse(question: String): String {
        var elapsedSeconds = 0
        val thinkingMessage = MessageModel("Đang suy nghĩ sâu (0s)...", "model", System.currentTimeMillis())
        messageList.add(thinkingMessage)

        // Chạy coroutine để đếm giây
        val scope = CoroutineScope(Dispatchers.Main)
        val countingJob = scope.launch {
            while (true) {
                delay(1000) // Cập nhật mỗi giây
                elapsedSeconds++
                messageList[messageList.size - 1] = thinkingMessage.copy(message = "Đang suy nghĩ sâu (${elapsedSeconds}s)...")
            }
        }
        try {
            // Lấy lịch sử trò chuyện
            val chatHistory = messageList.joinToString(separator = "\n") { "${it.role}: ${it.message}" }

            //Tìm kiếm trên Google để lấy danh sách các đường link
            val googleSearchResults = googleSearch.search(question)
            // Xử lý kết quả Google thành JSON
            val googleJsonResults = processSearchResults(googleSearchResults)

            // Lấy dữ liệu từ YouTube API dựa trên câu hỏi
            val youtubeResults = youtubeSearch.search(question, maxResults = 3)
            val youtubeJsonResults = processYouTubeResults(youtubeResults)

            // Hiển thị dữ liệu JSON trên log
            Log.d("SelfReasoningHandler", "Google JSON data: $googleJsonResults")
            Log.d("SelfReasoningHandler", "YouTube JSON data: $youtubeJsonResults")

            // Tạo prompt cho generative model với dữ liệu lịch sử, Google và YouTube
            val prompt = """
            Câu hỏi: $question
            Ngày hiện tại: $formattedCurrentDate

            Lịch sử trò chuyện:
            $chatHistory

            Tài liệu tham khảo từ Google (JSON):
            $googleJsonResults

            Tài liệu tham khảo từ YouTube (JSON):
            $youtubeJsonResults

            Dựa trên các dữ liệu trên và kiến thức của bạn, hãy đưa ra câu trả lời chi tiết và chính xác nhất.
        """.trimIndent()

            // Gọi generative model để nhận phản hồi
            val chat = generativeModel.startChat(emptyList())
            val response = chat.sendMessage(prompt)

            // Xóa thông báo "Đang suy nghĩ sâu..."
            countingJob.cancel()
            messageList.removeLast()
            return response.text ?: "Không thể đưa ra câu trả lời dựa trên dữ liệu hiện có."

        }catch (e: Exception) {
            // Xử lý lỗi: dừng đếm và xóa thông báo
            countingJob.cancel()
            messageList.removeLast()
            throw e
        }
    }

    // Hàm xử lý kết quả tìm kiếm (Google hoặc YouTube) thành JSON
    private suspend fun processSearchResults(searchResults: String): String = withContext(Dispatchers.IO) {
        // Giả định kết quả tìm kiếm là các dòng chứa URL hoặc mô tả kèm URL
        val lines = searchResults.split("\n").filter { it.isNotBlank() }.take(5)
        val jsonArray = mutableListOf<JSONObject>()

        lines.forEach { line ->
            // Trích xuất URL hợp lệ từ dòng kết quả
            val url = extractUrl(line)
            if (url != null) {
                try {
                    // Sử dụng Jsoup để lấy nội dung trang web từ URL
                    val document = Jsoup.connect(url).get()
                    val bodyText = document.body().text()

                    // Chuyển đổi nội dung trang web thành JSON
                    val jsonObject = JSONObject().apply {
                        put("url", url)
                        put("content", bodyText)
                    }
                    jsonArray.add(jsonObject)
                } catch (e: Exception) {
                    // Log lỗi nếu không thể truy cập trang web
                    Log.e("SelfReasoningHandler", "Lỗi truy cập $url: ${e.message}")
                }
            } else {
                Log.e("SelfReasoningHandler", "Không tìm thấy URL hợp lệ trong: $line")
            }
        }
        // Trả về chuỗi JSON dạng mảng chứa các đối tượng JSON
        return@withContext jsonArray.toString()
    }

    // Xử lý riêng kết quả trả về từ YouTube API (giả sử kết quả là chuỗi các dòng)
    private suspend fun processYouTubeResults(youtubeResults: List<String>): String = withContext(Dispatchers.IO) {
        val jsonArray = mutableListOf<JSONObject>()
        youtubeResults.forEach { result ->
            // Giả sử mỗi kết quả từ YouTube là một chuỗi có chứa tiêu đề và URL, ví dụ: "Tiêu đề - https://youtu.be/abc123"
            val url = extractUrl(result)
            if (url != null) {
                try {
                    // Với YouTube, ta có thể chỉ lấy thông tin tiêu đề và URL
                    val title = result.substringBefore("-").trim()
                    val jsonObject = JSONObject().apply {
                        put("title", title)
                        put("url", url)
                    }
                    jsonArray.add(jsonObject)
                } catch (e: Exception) {
                    Log.e("SelfReasoningHandler", "Lỗi xử lý kết quả YouTube: ${e.message}")
                }
            } else {
                Log.e("SelfReasoningHandler", "Không tìm thấy URL hợp lệ trong kết quả YouTube: $result")
            }
        }
        return@withContext jsonArray.toString()
    }

    // Hàm trích xuất URL hợp lệ từ một dòng text sử dụng regex
    private fun extractUrl(text: String): String? {
        val urlRegex = Regex("(https?://\\S+)")
        return urlRegex.find(text)?.value?.trim()?.removeSuffix("\"")
    }
}
