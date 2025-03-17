package com.example.botchat.chatbox.viewModel.scanner

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.MessageModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ImageProcessor(
    private val viewModel: ChatViewModel
) {

    // Hàm xử lý OCR
    suspend fun processImage(uri: Uri, context: Context) {
        try {
            // Tải ảnh lên Google Drive trước và nhận driveLink qua callback
            viewModel.uploadImageToDrive(uri, context) { driveLink ->
                viewModel.viewModelScope.launch {
                    val inputImage = InputImage.fromFilePath(context, uri)
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    val visionText = recognizer.process(inputImage).await()
                    val recognizedText = visionText.text
                    if (recognizedText.isNotBlank()) {
                        val typingMessage = MessageModel("Đang gõ....", "model", System.currentTimeMillis())
                        viewModel.messageList.add(typingMessage)
                        delay(1000) // Hiển thị "Đang gõ...." trong 1 giây
                        viewModel.messageList.removeLast() // Xóa "Đang gõ...." sau khi hết delay

                        // Thêm tin nhắn kết quả OCR với driveLink
                        val botResponse = buildString {
                            append("Hình ảnh trên có nội dung:\n")
                            append("```text\n")
                            append(recognizedText)
                            append("\n```")
                        }
                        val botMessage = MessageModel(
                            message = botResponse,
                            role = "model",
                            timestamp = System.currentTimeMillis(),
                            driveLink = driveLink // Lưu driveLink
                        )
                        viewModel.messageList.add(botMessage)
                        viewModel.auth.currentUser?.email?.let { email ->
                            viewModel.firestoreHandler.saveMessageToFirestore(botMessage, "model", email, viewModel.roomId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi xử lý ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            if (viewModel.messageList.lastOrNull()?.message == "Đang gõ....") {
                viewModel.messageList.removeLast()
            }
        }
    }

    // Hàm phân tích ảnh
    suspend fun processImageAnalysis(uri: Uri, context: Context) {
        try {
            // Tải ảnh lên Google Drive trước và nhận driveLink qua callback
            viewModel.uploadImageToDrive(uri, context) { driveLink ->
                viewModel. viewModelScope.launch {
                    val inputImage = InputImage.fromFilePath(context, uri)
                    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                    val labels = labeler.process(inputImage).await()
                    val labelResults = labels.joinToString(separator = ", ") {
                        "${it.text} (${String.format("%.2f", it.confidence)})"
                    }
                    val typingMessage = MessageModel("Đang gõ....", "model", System.currentTimeMillis())
                    viewModel.messageList.add(typingMessage)
                    delay(1000) // Hiển thị "Đang gõ...." trong 1 giây
                    viewModel.messageList.removeLast() // Xóa "Đang gõ...." sau khi hết delay

                    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    val textResult = textRecognizer.process(inputImage).await()
                    val recognizedText = textResult.text.takeIf { it.isNotBlank() } ?: "Không phát hiện văn bản"
                    val aggregatedData = buildString {
                        append("Dữ liệu nhận diện: ")
                        append(if (labelResults.isNotEmpty()) labelResults else "Không phát hiện nhãn")
                        append(". ")
                        append("Văn bản trong ảnh: ")
                        append(recognizedText)
                    }
                    val prompt = "Dựa trên dữ liệu sau: \"$aggregatedData\", hãy phân tích và mô tả chi tiết nội dung hình ảnh một cách tự nhiên và cụ thể."
                    val chat = viewModel.generativeModel.startChat(emptyList())
                    val aiResponse = chat.sendMessage(prompt).text ?: "Không thể phân tích chi tiết."
                    val analysisResult = buildString {
                        append("Diễn giải tổng thể:\n\n")
                        append(aiResponse)
                    }
                    val botMessage = MessageModel(
                        message = analysisResult,
                        role = "model",
                        timestamp = System.currentTimeMillis(),
                        driveLink = driveLink // Lưu driveLink
                    )
                    viewModel.messageList.add(botMessage)
                    viewModel.auth.currentUser?.email?.let { email ->
                        viewModel.firestoreHandler.saveMessageToFirestore(botMessage, "model", email, viewModel.roomId)
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi xử lý ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            if (viewModel.messageList.lastOrNull()?.message == "Đang gõ....") {
                viewModel.messageList.removeLast()
            }
        }
    }
}