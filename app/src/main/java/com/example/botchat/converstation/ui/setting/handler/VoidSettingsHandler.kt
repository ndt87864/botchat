package com.example.botchat.converstation.ui.setting.handler

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.botchat.converstation.viewModel.VoidChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object VoidSettingsHandler {

    @Composable
    fun LoadSettings(voidChatViewModel: VoidChatViewModel) {
        LaunchedEffect(voidChatViewModel.currentEditingSettingId.value) {
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return@LaunchedEffect
            if (voidChatViewModel.currentEditingSettingId.value != null) {
                voidChatViewModel.firestore.collection("void_settings")
                    .document(userEmail)
                    .collection("settings")
                    .document(voidChatViewModel.currentEditingSettingId.value!!)
                    .get()
                    .addOnSuccessListener { document ->
                        document.data?.let { data ->
                            voidChatViewModel.userName.value = data["userName"] as? String ?: ""
                            voidChatViewModel.botName.value = data["botName"] as? String ?: ""
                            voidChatViewModel.speechRate.value = (data["speechRate"] as? Double ?: 1.0).toFloat()
                            voidChatViewModel.pitch.value = (data["pitch"] as? Double ?: 1.0).toFloat()
                            voidChatViewModel.settingName.value = data["settingName"] as? String ?: ""
                            voidChatViewModel.botCharacteristics.value = data["botCharacteristics"] as? String ?: ""
                            voidChatViewModel.otherRequirements.value = data["otherRequirements"] as? String ?: ""
                        }
                    }
            } else {
                voidChatViewModel.userName.value = ""
                voidChatViewModel.botName.value = ""
                voidChatViewModel.speechRate.value = 1.0f
                voidChatViewModel.pitch.value = 1.0f
                voidChatViewModel.settingName.value = ""
                voidChatViewModel.botCharacteristics.value = ""
                voidChatViewModel.otherRequirements.value = ""
            }
        }
    }

    @Composable
    fun HandleBack(
        voidChatViewModel: VoidChatViewModel,
        coroutineScope: CoroutineScope,
        onNavigateToVoiceChat: () -> Unit
    ) {
        BackHandler {
            if (voidChatViewModel.currentEditingSettingId.value == null) {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email
                if (userEmail != null) {
                    coroutineScope.launch {
                        try {
                            val snapshot = voidChatViewModel.firestore.collection("void_settings")
                                .document(userEmail)
                                .collection("settings")
                                .whereEqualTo("isDefault", true)
                                .get()
                                .await()
                            if (!snapshot.isEmpty) {
                                val doc = snapshot.documents.first()
                                voidChatViewModel.userName.value = doc.getString("userName") ?: ""
                                voidChatViewModel.botName.value = doc.getString("botName") ?: ""
                                voidChatViewModel.speechRate.value = doc.getDouble("speechRate")?.toFloat() ?: 1.0f
                                voidChatViewModel.pitch.value = doc.getDouble("pitch")?.toFloat() ?: 1.0f
                                voidChatViewModel.settingName.value = doc.getString("settingName") ?: ""
                                voidChatViewModel.botCharacteristics.value = doc.getString("botCharacteristics") ?: ""
                                voidChatViewModel.otherRequirements.value = doc.getString("otherRequirements") ?: ""
                            }
                        } catch (e: Exception) {
                        } finally {
                            onNavigateToVoiceChat()
                        }
                    }
                } else {
                    onNavigateToVoiceChat()
                }
            } else {
                onNavigateToVoiceChat()
            }
        }
    }

    @Composable
    fun HandleSave(
        voidChatViewModel: VoidChatViewModel,
        coroutineScope: CoroutineScope,
        context: Context,
        onNavigateToVoiceChat: () -> Unit
    ): () -> Unit {
        var showRefreshDialog by remember { mutableStateOf(false) }

        val saveAction: () -> Unit = {
            val userEmail = FirebaseAuth.getInstance().currentUser?.email
            if (userEmail != null) {
                voidChatViewModel.applySpeechSettings()
                if (voidChatViewModel.currentEditingSettingId.value != null) {
                    coroutineScope.launch {
                        val document = voidChatViewModel.firestore.collection("void_settings")
                            .document(userEmail)
                            .collection("settings")
                            .document(voidChatViewModel.currentEditingSettingId.value!!)
                            .get()
                            .await()
                        val isDefault = document.getBoolean("isDefault") == true
                        if (isDefault) {
                            showRefreshDialog = true
                        } else {
                            voidChatViewModel.saveSettingsToFirestore(userEmail, context)
                            Toast.makeText(context, "Đã cập nhật thiết lập", Toast.LENGTH_SHORT).show()
                            onNavigateToVoiceChat()
                        }
                    }
                } else {
                    voidChatViewModel.saveSettingsToFirestore(userEmail, context)
                    Toast.makeText(context, "Đã lưu thiết lập mới", Toast.LENGTH_SHORT).show()
                    onNavigateToVoiceChat()
                }
            } else {
                Toast.makeText(context, "Vui lòng đăng nhập để lưu thiết lập", Toast.LENGTH_SHORT).show()
            }
        }

        if (showRefreshDialog) {
            AlertDialog(
                onDismissRequest = { showRefreshDialog = false },
                title = { Text("Bạn có muốn làm mới cuộc trò chuyện ngay bây giờ?") },
                text = { Text("Tính năng này sẽ xóa sạch dữ liệu trò chuyện gần nhất") },
                confirmButton = {
                    TextButton(onClick = {
                        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return@TextButton
                        voidChatViewModel.saveSettingsToFirestore(userEmail, context)
                        voidChatViewModel.conversation.clear()
                        Toast.makeText(context, "Đã cập nhật thiết lập và làm mới cuộc trò chuyện", Toast.LENGTH_SHORT).show()
                        showRefreshDialog = false
                        onNavigateToVoiceChat()
                    }) {
                        Text("Có")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return@TextButton
                        voidChatViewModel.saveSettingsToFirestore(userEmail, context)
                        Toast.makeText(context, "Đã cập nhật thiết lập", Toast.LENGTH_SHORT).show()
                        showRefreshDialog = false
                        onNavigateToVoiceChat()
                    }) {
                        Text("Không")
                    }
                }
            )
        }

        return saveAction
    }
}