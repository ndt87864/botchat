package com.example.botchat.chatbox.viewModel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botchat.chatbox.constants.Constants
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.viewModel.text.TextProcessor
import com.example.botchat.chatbox.viewModel.call.CallHandler
import com.example.botchat.chatbox.model.UiEvent
import com.example.botchat.chatbox.viewModel.data.ChatRoomManager
import com.example.botchat.chatbox.viewModel.data.FirestoreHandler
import com.example.botchat.chatbox.viewModel.data.MessageHandler
import com.example.botchat.chatbox.viewModel.scanner.GoogleDriveManager
import com.example.botchat.chatbox.viewModel.scanner.ImageProcessor
import com.example.botchat.chatbox.viewModel.search.GoogleSearch
import com.example.botchat.chatbox.viewModel.search.YouTubeSearch
import com.example.botchat.chatbox.viewModel.sms.SMSHandler
import com.example.botchat.chatbox.viewModel.thinking.SelfReasoningHandler
import com.example.botchat.chatbox.viewModel.voice.Voice
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {

    // 1. Trạng thái và dữ liệu cơ bản
    // 1.1. Biến trạng thái giao diện và tin nhắn
    var message by mutableStateOf("") // Tin nhắn hiện tại
    val messageList = mutableStateListOf<MessageModel>() // Danh sách các tin nhắn
    var roomId by mutableStateOf(0) // ID phòng chat, giá trị mặc định, được cập nhật trong init
    val isChatbotResponding: Boolean
        get() = messageHandler.isChatbotResponding
    private var shouldStopTyping by mutableStateOf(false) // Trạng thái dừng gõ
    private var hasContentInCurrentRoom: Boolean = false // Phòng chat có nội dung không

    // 1.2. Xác thực và quản lý dữ liệu
    val auth = FirebaseAuth.getInstance() // Xác thực Firebase
    val firestoreHandler = FirestoreHandler() // Trình xử lý Firestore
    val chatRoomManager = ChatRoomManager(firestoreHandler, auth, messageList) { newRoomId ->
        roomId = newRoomId
    }
    private val _event = Channel<UiEvent>() // Kênh sự kiện giao diện
    val event = _event.receiveAsFlow() // Luồng sự kiện giao diện

    // 2. Cấu hình mô hình AI
    // 2.1. Biến cấu hình mô hình
    var generativeModel: GenerativeModel = GenerativeModel(modelName = Constants.MODEL_NAME, apiKey = Constants.API_KEY)
    var currentModelName by mutableStateOf(Constants.MODEL_NAME) // Tên mô hình hiện tại
        private set
    val modelNameMapping = mapOf(
        Constants.MODEL_NAME to "Genmini 2",
        Constants.MODEL_NAME_1 to "Genmini 2 Thinking",
        Constants.MODEL_NAME_2 to "Genmini 2 Pro",
        Constants.MODEL_NAME_3 to "Genmini 1.5 ",
        Constants.MODEL_NAME_4 to "Genmini 1.5 Pro",
        Constants.MODEL_NAME_5 to "Genmini 1.5 Pro Experimental",
        Constants.MODEL_NAME_6 to "Genmini 1 Pro"
    )
    val availableModels = modelNameMapping.values.toList() // Danh sách các mô hình có sẵn

    // 2.2. Hàm thay đổi mô hình
    fun changeModel(customModelName: String) {
        val actualModelName = modelNameMapping.entries.find { it.value == customModelName }?.key
        if (actualModelName != null) {
            currentModelName = customModelName
            generativeModel = GenerativeModel(modelName = actualModelName, apiKey = Constants.API_KEY)
        }
    }

    // 3. Cài đặt người dùng và tùy chỉnh
    // 3.1. Biến cài đặt người dùng
    var applyCustomization by mutableStateOf(false) // Áp dụng tùy chỉnh
    var userName by mutableStateOf("Bạn") // Tên người dùng
    var occupation by mutableStateOf("") // Nghề nghiệp
    var botTraits by mutableStateOf("") // Đặc điểm của bot
    var additionalInfo by mutableStateOf("") // Thông tin bổ sung
    var isAnonymous by mutableStateOf(false) // Trạng thái ẩn danh

    // 3.2. Hàm quản lý cài đặt người dùng
    fun toggleAnonymous() {
        isAnonymous = !isAnonymous
    }

    private fun loadUserSettings() {
        val userEmail = auth.currentUser?.email ?: return
        firestoreHandler.loadUserSettings(userEmail) { applyCustomization, userName, occupation, botTraits, additionalInfo ->
            this.applyCustomization = applyCustomization
            this.userName = userName
            this.occupation = occupation
            this.botTraits = botTraits
            this.additionalInfo = additionalInfo
        }
    }

    fun updateSettings(
        applyCustomization: Boolean,
        userName: String,
        occupation: String,
        botTraits: String,
        additionalInfo: String,
        context: Context,
        onSaveSuccess: () -> Unit
    ) {
        val userEmail = auth.currentUser?.email ?: return
        viewModelScope.launch {
            val result = firestoreHandler.updateSettings(
                userEmail,
                applyCustomization,
                userName,
                occupation,
                botTraits,
                additionalInfo
            )
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    this@ChatViewModel.applyCustomization = applyCustomization
                    this@ChatViewModel.userName = userName
                    this@ChatViewModel.occupation = occupation
                    this@ChatViewModel.botTraits = botTraits
                    this@ChatViewModel.additionalInfo = additionalInfo
                    Toast.makeText(context, "Lưu thành công", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                } else {
                    Toast.makeText(context, "Lưu thất bại: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 4. Tư duy sâu và lý luận
    // 4.1. Biến tư duy sâu
    var isDeepThinkingEnabled by mutableStateOf(false) // Trạng thái bật/tắt tư duy sâu
        private set
    private val selfReasoningHandler = SelfReasoningHandler(
        messageList = messageList,
        generativeModel = generativeModel,
        googleSearch = GoogleSearch(),
        youtubeSearch = YouTubeSearch()
    )
    val formattedCurrentDate = selfReasoningHandler.formattedCurrentDate // Ngày hiện tại được định dạng

    // 4.2. Hàm quản lý tư duy sâu
    fun toggleDeepThinking(context: Context) {
        isDeepThinkingEnabled = !isDeepThinkingEnabled
        if (isDeepThinkingEnabled) {
            Toast.makeText(context, "Bật chế độ suy nghĩ", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Tắt chế độ suy nghĩ", Toast.LENGTH_SHORT).show()
        }
    }

    // 5. Quản lý phòng chat
    // 5.1. Khởi tạo phòng chat
    init {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch
            roomId = chatRoomManager.initialize(userEmail)
            checkAndCreateNewRoomIfNeeded(userEmail)
            isYoutubeMode = youtubeSearch.loadYouTubeModeState(userEmail)
            loadUserSettings()
        }
    }

    private suspend fun checkAndCreateNewRoomIfNeeded(userEmail: String) {
        val currentMessages = firestoreHandler.loadMessages(roomId, userEmail)
        if (currentMessages.isNotEmpty()) {
            hasContentInCurrentRoom = true
            chatRoomManager.createNewChatRoom(userEmail)
            chatRoomManager.loadMessages(userEmail) // Tải tin nhắn cho phòng mới
        }
    }

    // 5.2. Hàm quản lý phòng chat
    fun loadMessagesForRoom(roomId: Int) {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch
            chatRoomManager.loadMessagesForRoom(roomId, userEmail)
        }
    }

    fun createNewChatRoom() {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch
            chatRoomManager.createNewChatRoom(userEmail)
        }
    }

    fun getOldestMessagesPerRoom(onResult: (List<Pair<Int, MessageModel>>) -> Unit) {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch
            val oldestMessages = chatRoomManager.getOldestMessagesPerRoom(userEmail)
            onResult(oldestMessages)
        }
    }

    fun deleteMessage(context: Context, roomId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch
            chatRoomManager.deleteRoom(context, roomId, userEmail, onSuccess)
        }
    }

    fun deleteAllChatRooms(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch
            val hasRooms = firestoreHandler.hasRooms(userEmail)
            if (hasRooms) {
                try {
                    val rooms = chatRoomManager.getOldestMessagesPerRoom(userEmail)
                    rooms.forEach { (roomId, _) ->
                        chatRoomManager.deleteRoom(context, roomId, userEmail, {})
                    }
                    messageList.clear()
                    onSuccess()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Lỗi khi xóa lịch sử: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Đã xóa toàn bộ lịch sử trò chuyện",
                        Toast.LENGTH_SHORT
                    ).show()
                    createNewChatRoom()
                }
            }
        }
    }

    fun resetMessages() {
        messageList.clear()
    }

    // 6. Gửi và xử lý tin nhắn
    // 6.1. Khởi tạo handler tin nhắn
    private val callHandler = CallHandler(_event, messageList, saveMessage = { messageModel ->
        val userEmail = auth.currentUser?.email ?: return@CallHandler
        viewModelScope.launch { chatRoomManager.saveMessage(messageModel, "model", userEmail) }
    })
    private val smsHandler = SMSHandler(_event, messageList, saveMessage = { messageModel ->
        val userEmail = auth.currentUser?.email ?: return@SMSHandler
        viewModelScope.launch { chatRoomManager.saveMessage(messageModel, "model", userEmail) }
    })
    private val messageHandler = MessageHandler(
        chatRoomManager = chatRoomManager,
        generativeModel = generativeModel,
        selfReasoningHandler = selfReasoningHandler,
        callHandler = callHandler,
        smsHandler = smsHandler,
        _event = _event,
        messageList = messageList,
        auth = auth,
        formattedCurrentDate = formattedCurrentDate
    )

    // 6.2. Gọi hàm từ messageHandler
    fun sendMessage(question: String) {
        messageHandler.sendMessage(
            question = question,
            applyCustomization = applyCustomization,
            userName = userName,
            occupation = occupation,
            botTraits = botTraits,
            additionalInfo = additionalInfo,
            isAnonymous = isAnonymous,
            isSearchMode = isSearchMode,
            isYoutubeMode = isYoutubeMode,
            isDeepThinkingEnabled = isDeepThinkingEnabled,
            currentModelName = currentModelName,
            context = null // Có thể truyền context nếu cần hiển thị Toast
        )
    }


    fun editMessage(
        originalMessage: MessageModel,
        newMessageText: String,
        context: Context,
        onComplete: () -> Unit
    ) {
        messageHandler.editMessage(originalMessage, newMessageText, context, onComplete)
    }

    fun stopTyping() {
        messageHandler.stopTyping()
    }

    // 7. Tính năng gọi điện và SMS
    // 7.1. Biến quản lý gọi điện và SMS
    val isCallMode get() = callHandler.isCallMode.value // Trạng thái chế độ gọi điện
    val isMessageMode get() = smsHandler.isMessageMode.value // Trạng thái chế độ tin nhắn SMS

    // 7.2. Hàm quản lý gọi điện và SMS
    fun toggleCallMode(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            val REQUEST_CALL_PHONE = 1002
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE
            )
            return
        }
        callHandler.toggleCallMode(context)
    }

    fun toggleMessageMode(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            val REQUEST_SEND_SMS = 1001
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.SEND_SMS),
                REQUEST_SEND_SMS
            )
            return
        }
        smsHandler.toggleMessageMode(context)
    }

    // 8. Chức năng tìm kiếm
    // 8.1. Biến tìm kiếm
    var isSearchMode by mutableStateOf(false) // Trạng thái chế độ tìm kiếm
    var isYoutubeMode by mutableStateOf(true) // Trạng thái chế độ tìm kiếm YouTube
    private val googleSearch = GoogleSearch() // Trình tìm kiếm Google
    private val youtubeSearch = YouTubeSearch() // Trình tìm kiếm YouTube

    // 8.2. Hàm tìm kiếm
    fun toggleSearchMode(context: Context) {
        isSearchMode = !isSearchMode
        if (isSearchMode) {
            Toast.makeText(context, "Bật chức năng tìm kiếm!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Tắt chức năng tìm kiếm!", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleYoutubeMode(context: Context) {
        youtubeSearch.toggleYoutubeMode(context, isYoutubeMode) { newMode ->
            isYoutubeMode = newMode
        }
    }

    fun extractYouTubeVideoIds(message: String): List<String> {
        val youtubeRegex = "https?://(?:www\\.)?(?:youtube\\.com/(?:watch\\?v=|embed/|v/)|youtu\\.be/)([\\w-]+)".toRegex()
        return youtubeRegex.findAll(message)
            .mapNotNull { it.groupValues.lastOrNull() }
            .toList()
    }

    // 9. Nhận diện giọng nói
    // 9.1. Biến nhận diện giọng nói
    var isRecording by mutableStateOf(false) // Trạng thái đang ghi âm
    private val voiceRecognitionManager = Voice() // Trình quản lý nhận diện giọng nói
    private var onMessageSendCallback: ((String) -> Unit)? = null // Callback khi gửi tin nhắn bằng giọng nói

    // 9.2. Hàm nhận diện giọng nói
    fun initVoiceRecognition(context: Context, onMessageSend: (String) -> Unit) {
        onMessageSendCallback = onMessageSend
        voiceRecognitionManager.initSpeechRecognizer(context) { recognizedText ->
            message = recognizedText
            sendMessage(message)
            message = ""
        }
    }

    fun startVoiceRecognition() {
        voiceRecognitionManager.startVoiceRecognition()
    }

    fun stopVoiceRecognition() {
        voiceRecognitionManager.stopVoiceRecognition()
        isRecording = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: return@launch
            if (hasContentInCurrentRoom) {
                chatRoomManager.createNewChatRoom(userEmail)
                roomId = chatRoomManager.roomId
                chatRoomManager.loadMessages(userEmail)
            }
        }
        voiceRecognitionManager.release() // Giải phóng tài nguyên giọng nói
    }

    // 10. Xử lý hình ảnh
    // 10.1. Biến xử lý hình ảnh
    var isORC by mutableStateOf(false) // Trạng thái nhận diện ký tự quang học (OCR)
    var shouldAnalyzeImage by mutableStateOf(false) // Trạng thái phân tích hình ảnh
    var selectedImageUri by mutableStateOf<String?>(null) // URI của hình ảnh được chọn
    private lateinit var driveManager: GoogleDriveManager // Trình quản lý Google Drive
    private val imageProcessor = ImageProcessor(this) // Trình xử lý hình ảnh
    var uploadProgress by mutableStateOf(0f) // Tiến trình tải lên (0f đến 1f)
    var isUploading by mutableStateOf(false) // Trạng thái đang tải lên

    // 10.2. Hàm xử lý hình ảnh
    fun initGoogleSignIn(context: Context) {
        driveManager = GoogleDriveManager(context)
        driveManager.initGoogleSignIn()
    }

    fun startGoogleSignIn(activity: Activity): Intent {
        return driveManager.startGoogleSignIn()
    }

    fun handleSignInResult(data: Intent?, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            driveManager.handleSignInResult(data, onSuccess)
        }
    }

    fun uploadImageToDrive(uri: Uri, context: Context, activity: Activity? = null, onSuccess: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            isUploading = true
            uploadProgress = 0f
            val fileLink = driveManager.uploadImageToDrive(uri, activity) { progress ->
                viewModelScope.launch(Dispatchers.Main) {
                    uploadProgress = progress // Cập nhật tiến trình trên giao diện chính
                }
            }
            fileLink?.let { link ->
                withContext(Dispatchers.Main) {
                    uploadProgress = 1f // Hoàn thành 100%
                    Toast.makeText(context, "Tải lên thành công", Toast.LENGTH_SHORT).show()
                    onSuccess(link) // Gọi callback với driveLink
                    isUploading = false
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    uploadProgress = 0f
                    isUploading = false
                    Toast.makeText(context, "Tải lên thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun fetchImageFromDrive(fileLink: String, context: Context): ByteArray? {
        return driveManager.fetchImageFromDrive(fileLink)
    }

    fun processImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            imageProcessor.processImage(uri, context)
        }
    }

    fun processImageAnalysis(uri: Uri, context: Context) {
        viewModelScope.launch {
            imageProcessor.processImageAnalysis(uri, context)
        }
    }

    // 11. Tiện ích xử lý văn bản
    fun extractUrlsFromText(text: String): List<String> {
        return TextProcessor.extractUrlsFromText(text)
    }

    fun splitTextByCode(input: String): List<String> {
        return TextProcessor.splitTextByCode(input)
    }

    fun splitCodeHeaderAndBody(code: String): Pair<String, String> {
        return TextProcessor.splitCodeHeaderAndBody(code)
    }

    fun buildAnnotatedText(
        text: String,
        linkColor: Color,
        boldColor: Color,
        codeInlineColor: Color
    ): AnnotatedString {
        return TextProcessor.buildAnnotatedText(text, linkColor, boldColor, codeInlineColor)
    }

    fun filterSpecialCharacters(input: String): String {
        return TextProcessor.filterSpecialCharacters(input)
    }
}