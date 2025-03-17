package com.example.botchat.chatbox.ui.chat.components

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.botchat.account.viewModel.AccountViewModel
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.viewModel.search.YouTubeSearch
import com.example.botchat.chatbox.ui.chat.media.YouTubePlayer
import com.example.botchat.chatbox.viewModel.text.TextProcessor
import com.example.botchat.ui.theme.Yellow900

@Composable
fun MessageRow(messageModel: MessageModel, viewModel: ChatViewModel) {
    val isModel = messageModel.role == "model"
    val isModelVoice = messageModel.role == "modelVoice"
    val isUserVoice = messageModel.role == "userVoice"
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val textToSpeech = remember { TextToSpeech(context) {} }
    var isReading by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editedMessage by remember { mutableStateOf(messageModel.message) }

    // Theme-related colors
    val isDarkTheme = isSystemInDarkTheme()
    val defaultTextColor = if (isDarkTheme) Color.White else Color.Black
    val codeBlockBackground = if (isDarkTheme) Color(0xFF282C34) else Color(0xFFF5F5F5)
    val codeBlockTextColor = if (isDarkTheme) Color.White else Color.Black
    val bubbleBackground = if (isDarkTheme) Color.DarkGray else Color.LightGray
    val linkColor = if (isDarkTheme) Color.Cyan else Color.Blue
    val boldColor = if (isDarkTheme) Color.Green else Color(0xFF006400)
    val codeInlineColor = if (isDarkTheme) Color.Yellow else Color(0xFFB8860B)
    val isMessageBox = messageModel.message.contains("**Yêu cầu gửi tin nhắn**")

    val textParts = if (isModel || isModelVoice) viewModel.splitTextByCode(messageModel.message) else listOf(messageModel.message)
    val youtubeVideoIds = if (isModel || isModelVoice) viewModel.extractYouTubeVideoIds(messageModel.message) else emptyList()
    val urls = if (!isModel && !isModelVoice) viewModel.extractUrlsFromText(messageModel.message) else emptyList()
    val driveLinks = urls.filter { it.contains("drive.google.com") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageError by remember { mutableStateOf(false) }
    var imageLoaded by remember { mutableStateOf(false) }

    // Kiểm tra và tải ảnh khi có driveLink
    LaunchedEffect(messageModel.driveLink) {
        if (messageModel.driveLink != null && !imageLoaded) {
            imageBytes = viewModel.fetchImageFromDrive(messageModel.driveLink, context)
            imageLoaded = true
            if (imageBytes == null) imageError = true
        }
    }

    val loginViewModel: AccountViewModel = viewModel()
    val userName by remember { derivedStateOf { loginViewModel.userName ?: "Khách" } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isModel || isModelVoice) Arrangement.Start else Arrangement.End
        ) {
            Text(
                text = if (isModel || isModelVoice) "Bot" else userName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = if (isModel || isModelVoice) 12.dp else 0.dp, end = if (!isModel && !isModelVoice) 12.dp else 0.dp)
            )
            // Chỉ hiển thị loa cho role "model", không hiển thị cho "modelVoice"
            if (isModel && messageModel.message != "Đang gõ....") {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        if (isReading) {
                            textToSpeech.stop()
                            Toast.makeText(context, "Tắt đọc", Toast.LENGTH_SHORT).show()
                        } else {
                            val filteredMessage = viewModel.filterSpecialCharacters(messageModel.message)
                            Toast.makeText(context, "Đọc", Toast.LENGTH_SHORT).show()
                            textToSpeech.speak(
                                filteredMessage,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                null
                            )
                        }
                        isReading = !isReading
                    }
                ) {
                    Icon(
                        imageVector = if (isReading) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Read"
                    )
                }
            }
        }

        // Hiển thị thanh tiến trình nếu đang tải ảnh
        if (!isModel && !isModelVoice && viewModel.isUploading && messageModel.driveLink == null) {
            LinearProgressIndicator(
                progress = viewModel.uploadProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .heightIn(min = 4.dp, max = 8.dp),
                color = Yellow900
            )
            Text(
                text = "Đang tải lên: ${String.format("%.0f", viewModel.uploadProgress * 100)}%",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                color = Color.Gray
            )
        }

        // Hiển thị hình ảnh hoặc lỗi
        if (!isModel && !isModelVoice) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (imageLoaded && imageBytes != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageBytes),
                        contentDescription = "Hình ảnh",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(end = 12.dp)
                    )
                } else if (imageError && messageModel.driveLink != null) {
                    Text(
                        text = "Lỗi hiển thị ảnh từ Google Drive",
                        color = Color.Red,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(bubbleBackground, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
        }

        // Hiển thị nội dung tin nhắn
        if (messageModel.message.isNotEmpty()) {
            if (isModel || isModelVoice) {
                if (isMessageBox) {
                    val regexPhone = Regex("Số điện thoại\\*\\*: (.+?)\\s")
                    val regexContent = Regex("Nội dung\\*\\*: (.+?)\\s\\[Sửa đổi")
                    val phoneMatch = regexPhone.find(messageModel.message)
                    val contentMatch = regexContent.find(messageModel.message)
                    val phone = phoneMatch?.groupValues?.get(1) ?: ""
                    val content = contentMatch?.groupValues?.get(1) ?: ""

                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, top = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bubbleBackground)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Yêu cầu gửi tin nhắn",
                                fontWeight = FontWeight.Bold,
                                color = defaultTextColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Số điện thoại: $phone", color = defaultTextColor)
                            Text(text = "Nội dung: $content", color = defaultTextColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:$phone")
                                        putExtra("sms_body", content)
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Text("Sửa đổi")
                                }
                                Button(onClick = { viewModel.sendMessage("#send|$phone|$content") }) {
                                    Text("Gửi")
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                        textParts.forEach { part ->
                            if (part.startsWith("```") || part.endsWith("```")) {
                                val (header, body) = viewModel.splitCodeHeaderAndBody(part)
                                val displayBody = if (body.isEmpty()) header else body
                                val highlightedCode = TextProcessor.highlightCode(header, displayBody)

                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(codeBlockBackground)
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column {
                                        if (header.isNotEmpty() && body.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = header,
                                                    fontWeight = FontWeight.W500,
                                                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                                                    fontSize = 14.sp
                                                )
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(displayBody))
                                                        Toast.makeText(context, "Sao chép thành công", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy Code",
                                                        tint = codeBlockTextColor
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 700.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            SelectionContainer {
                                                Text(
                                                    text = highlightedCode,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            if (body.isEmpty() && header.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(displayBody))
                                                        Toast.makeText(context, "Sao chép thành công", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy Code",
                                                        tint = codeBlockTextColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                val tableOrTextBlocks = TextProcessor.splitTextByTable(part)
                                tableOrTextBlocks.forEach { block ->
                                    when (block) {
                                        is TextProcessor.TableOrTextBlock.TableBlock -> {
                                            // Gọi composable hiển thị bảng
                                            MarkdownTableView(
                                                tableData = block.table,
                                                defaultTextColor = defaultTextColor,
                                                bubbleBackground = bubbleBackground
                                            )
                                        }

                                        is TextProcessor.TableOrTextBlock.TextBlock -> {
                                            // Hiển thị text bình thường
                                            val annotatedText = viewModel.buildAnnotatedText(
                                                block.text, linkColor, boldColor, codeInlineColor
                                            )
                                            SelectionContainer {
                                                ClickableText(
                                                    text = annotatedText,
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        color = defaultTextColor
                                                    ),
                                                    onClick = { offset ->
                                                        annotatedText.getStringAnnotations(
                                                            "URL",
                                                            offset,
                                                            offset
                                                        )
                                                            .firstOrNull()?.let { annotation ->
                                                                val intent = Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    Uri.parse(annotation.item)
                                                                )
                                                                context.startActivity(intent)
                                                            }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val displayText = driveLinks.fold(messageModel.message) { acc, link -> acc.replace(link, "").trim() }
                if (displayText.isNotEmpty() || imageLoaded || imageError) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            if (isEditing && !isUserVoice) { // Chỉ hiển thị chỉnh sửa nếu không phải userVoice
                                TextField(
                                    value = editedMessage,
                                    onValueChange = { editedMessage = it },
                                    modifier = Modifier
                                        .width(300.dp)
                                        .padding(vertical = 8.dp),
                                    textStyle = TextStyle(color = defaultTextColor, fontSize = 18.sp),
                                    trailingIcon = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(onClick = {
                                                viewModel.editMessage(messageModel, editedMessage, context) {
                                                    isEditing = false
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = "Send Edited Message",
                                                    tint = defaultTextColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(onClick = { isEditing = false }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Cancel Edit",
                                                    tint = defaultTextColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(48f))
                                        .background(bubbleBackground)
                                        .padding(20.dp)
                                ) {
                                    SelectionContainer {
                                        val annotatedText = viewModel.buildAnnotatedText(displayText, linkColor, boldColor, codeInlineColor)
                                        ClickableText(
                                            text = annotatedText,
                                            style = TextStyle(fontSize = 18.sp, color = defaultTextColor),
                                            onClick = { offset ->
                                                annotatedText.getStringAnnotations("URL", offset, offset)
                                                    .firstOrNull()?.let { annotation ->
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                                        context.startActivity(intent)
                                                    }
                                            }
                                        )
                                    }
                                }
                            }
                            // Hành động dưới tin nhắn
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!isUserVoice) { // Không hiển thị Copy và Edit cho userVoice
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(displayText))
                                        Toast.makeText(context, "Đã sao chép", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Message",
                                            tint = defaultTextColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        isEditing = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Message",
                                            tint = defaultTextColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    // Hiển thị icon Mic cho userVoice
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Message",
                                        tint = defaultTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if ((isModel || isModelVoice) && youtubeVideoIds.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                youtubeVideoIds.forEach { videoId ->
                    YouTubePlayer(videoId, YouTubeSearch())
                }
            }
        }
    }
}

@Composable
fun MarkdownTableView(
    tableData: List<List<String>>,
    defaultTextColor: Color,
    bubbleBackground: Color
) {
    if (tableData.isEmpty()) return

    val columnCount = tableData.maxOfOrNull { it.size } ?: 0

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bubbleBackground)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .border(2.dp, Color.Black)
        ) {
            tableData.forEachIndexed { rowIndex, rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Black),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (columnIndex in 0 until columnCount) {
                        // Lấy text trong ô, nếu null thì để ""
                        val cellText = rowItems.getOrNull(columnIndex) ?: ""

                        val singleStarRegex = "(?<!\\*)\\*(?!\\*)".toRegex()
                        val replacedText = singleStarRegex.replace(cellText, "\n•")

                        // 2) Áp dụng regex bold cho **...**
                        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
                        var lastIndex = 0
                        val formattedText = buildAnnotatedString {
                            boldRegex.findAll(replacedText).forEach { match ->
                                // Text trước đoạn **...**
                                append(replacedText.substring(lastIndex, match.range.first))
                                // Nội dung bên trong **...**
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)) {
                                    append(match.groupValues[1])
                                }
                                lastIndex = match.range.last + 1
                            }
                            // Phần còn lại sau cùng
                            if (lastIndex < replacedText.length) {
                                append(replacedText.substring(lastIndex))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(120.dp) // Đặt chiều rộng tối thiểu
                                .padding(8.dp),
                            contentAlignment = Alignment.Center // Căn giữa nội dung
                        ) {
                            Text(
                                text = formattedText,
                                color = defaultTextColor,
                                fontSize = if (rowIndex == 0) 20.sp else 15.sp,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}



