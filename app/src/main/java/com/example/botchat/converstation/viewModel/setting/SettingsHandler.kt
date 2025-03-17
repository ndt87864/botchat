package com.example.botchat.converstation.viewModel.setting

import android.content.Context
import android.content.SharedPreferences
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsHandler(
    private val firestore: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences,
    val userName: MutableState<String>,
    val botName: MutableState<String>,
    val speechRate: MutableState<Float>,
    val pitch: MutableState<Float>,
    val settingName: MutableState<String>,
    val botCharacteristics: MutableState<String>,
    val otherRequirements: MutableState<String>,
    private val applySpeechSettings: () -> Unit,
    private val currentEditingSettingId: MutableState<String?>
) {

    suspend fun saveUserToFirestore(userEmail: String) {
        val userData = mapOf(
            "email" to userEmail,
            "userName" to userName.value,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestore.collection("users")
                .document(userEmail)
                .set(userData)
                .await()
            Log.d("SettingsHandler", "Đã lưu user: $userEmail")
        } catch (e: Exception) {
            Log.e("SettingsHandler", "Lỗi lưu user: ${e.message}")
        }
    }

    fun loadSettings() {
        userName.value = sharedPreferences.getString("userName", "Bạn") ?: "Bạn"
        botName.value = sharedPreferences.getString("botName", "Bot") ?: "Bot"
        speechRate.value = sharedPreferences.getFloat("speechRate", 1.0f)
        pitch.value = sharedPreferences.getFloat("pitch", 1.0f)
        botCharacteristics.value = sharedPreferences.getString("botCharacteristics", "") ?: ""
        otherRequirements.value = sharedPreferences.getString("otherRequirements", "") ?: ""
        Log.d("SettingsHandler", "Tải từ SharedPreferences: userName=${userName.value}, botName=${botName.value}")
    }

    suspend fun loadDefaultOrLatestSettings(userEmail: String): Boolean {
        return try {
            val settingsCollection = firestore.collection("void_settings")
                .document(userEmail)
                .collection("settings")
                .get()
                .await()

            if (settingsCollection.isEmpty) {
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

                userName.value = defaultData["userName"] as String
                botName.value = defaultData["botName"] as String
                speechRate.value = (defaultData["speechRate"] as Double).toFloat()
                pitch.value = (defaultData["pitch"] as Double).toFloat()
                settingName.value = defaultData["settingName"] as String
                botCharacteristics.value = defaultData["botCharacteristics"] as String
                otherRequirements.value = defaultData["otherRequirements"] as String
                applySpeechSettings()
                Log.d("SettingsHandler", "Tạo cài đặt mặc định: userName=${userName.value}")
                return true
            }

            val defaultSetting = settingsCollection.documents.find { it.getBoolean("isDefault") == true }
            val settingToLoad = defaultSetting ?: settingsCollection.documents.maxByOrNull { it.getLong("timestamp") ?: 0L }
            settingToLoad?.let { snapshot ->
                userName.value = snapshot.getString("userName") ?: "Bạn"
                botName.value = snapshot.getString("botName") ?: "Bot"
                speechRate.value = snapshot.getDouble("speechRate")?.toFloat() ?: 1.0f
                pitch.value = snapshot.getDouble("pitch")?.toFloat() ?: 1.0f
                settingName.value = snapshot.getString("settingName") ?: ""
                botCharacteristics.value = snapshot.getString("botCharacteristics") ?: ""
                otherRequirements.value = snapshot.getString("otherRequirements") ?: ""
                applySpeechSettings()
                Log.d("SettingsHandler", "Tải cài đặt từ Firestore: userName=${userName.value}, settingName=${settingName.value}")
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("SettingsHandler", "Lỗi tải cài đặt: ${e.message}")
            false
        }
    }

    suspend fun reloadUsedDoc(context: Context, userEmail: String) {
        val settingsCollection = firestore.collection("void_settings")
            .document(userEmail)
            .collection("settings")
            .get()
            .await()
        if (settingsCollection.isEmpty) {
            Log.d("SettingsHandler", "Không có cài đặt nào trong Firestore")
            return
        }
        val defaultSetting = settingsCollection.documents.find { it.getBoolean("isDefault") == true }
        val docToLoad = defaultSetting ?: settingsCollection.documents.maxByOrNull { it.getLong("timestamp") ?: 0L }
        docToLoad?.let { snapshot ->
            snapshot.data?.let { data ->
                userName.value = data["userName"] as? String ?: "Bạn"
                botName.value = data["botName"] as? String ?: "Bot"
                speechRate.value = (data["speechRate"] as? Double ?: 1.0).toFloat()
                pitch.value = (data["pitch"] as? Double ?: 1.0).toFloat()
                settingName.value = data["settingName"] as? String ?: ""
                botCharacteristics.value = data["botCharacteristics"] as? String ?: ""
                otherRequirements.value = data["otherRequirements"] as? String ?: ""
                applySpeechSettings()
                Log.d("SettingsHandler", "Tải lại cài đặt: userName=${userName.value}, settingName=${settingName.value}")
            }
        }
    }

    fun saveSettings() {
        with(sharedPreferences.edit()) {
            putString("userName", userName.value)
            putString("botName", botName.value)
            putFloat("speechRate", speechRate.value)
            putFloat("pitch", pitch.value)
            putString("settingName", settingName.value)
            putString("botCharacteristics", botCharacteristics.value)
            putString("otherRequirements", otherRequirements.value)
            apply()
        }
        applySpeechSettings()
        Log.d("SettingsHandler", "Lưu cài đặt vào SharedPreferences")
    }

    fun saveSettingsToFirestore(userEmail: String, context: Context) {
        // Giữ nguyên logic, chỉ thêm log
        CoroutineScope(Dispatchers.Main).launch {
            val settingsCollection = firestore.collection("void_settings")
                .document(userEmail)
                .collection("settings")
            try {
                val defaultSettingQuery = settingsCollection.whereEqualTo("isDefault", true).get().await()
                val hasDefault = !defaultSettingQuery.isEmpty
                val editingId = currentEditingSettingId.value
                if (editingId != null) {
                    val newData = mapOf(
                        "userName" to userName.value,
                        "botName" to botName.value,
                        "speechRate" to speechRate.value,
                        "pitch" to pitch.value,
                        "settingName" to settingName.value,
                        "botCharacteristics" to botCharacteristics.value,
                        "otherRequirements" to otherRequirements.value,
                        "timestamp" to System.currentTimeMillis()
                    )
                    settingsCollection.document(editingId)
                        .update(newData)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                val snapshot = settingsCollection.document(editingId).get().await()
                                val isDefaultNow = snapshot.getBoolean("isDefault") ?: false
                                if (isDefaultNow) {
                                    reloadUsedDoc(context, userEmail)
                                    saveSettings()
                                }
                                currentEditingSettingId.value = null
                                Log.d("SettingsHandler", "Cập nhật cài đặt: $editingId")
                            }
                        }
                        .addOnFailureListener { Log.e("SettingsHandler", "Lỗi cập nhật: ${it.message}") }
                } else {
                    val isDefaultForNew = !hasDefault
                    val existingSettings = settingsCollection.get().await()
                    var maxSettingNumber = 0
                    existingSettings.documents.forEach { document ->
                        val docId = document.id
                        if (docId.startsWith("setting")) {
                            val numberPart = docId.substringAfter("setting")
                            if (numberPart.all { it.isDigit() }) {
                                val number = numberPart.toInt()
                                if (number > maxSettingNumber) maxSettingNumber = number
                            }
                        }
                    }
                    val nextSettingNumber = maxSettingNumber + 1
                    val settingDoc = "setting$nextSettingNumber"
                    val newSettingData = mapOf(
                        "userName" to userName.value,
                        "botName" to botName.value,
                        "speechRate" to speechRate.value,
                        "pitch" to pitch.value,
                        "settingName" to settingName.value,
                        "botCharacteristics" to botCharacteristics.value,
                        "otherRequirements" to otherRequirements.value,
                        "timestamp" to System.currentTimeMillis(),
                        "isDefault" to isDefaultForNew
                    )
                    settingsCollection.document(settingDoc)
                        .set(newSettingData)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                reloadUsedDoc(context, userEmail)
                                Log.d("SettingsHandler", "Tạo mới cài đặt: $settingDoc")
                            }
                        }
                        .addOnFailureListener { Log.e("SettingsHandler", "Lỗi tạo mới: ${it.message}") }
                    saveSettings()
                }
            } catch (e: Exception) {
                Log.e("SettingsHandler", "Lỗi lưu cài đặt: ${e.message}")
            }
        }
    }

    fun onCleared(speechRecognizer: SpeechRecognizer?, textToSpeech: TextToSpeech?) {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
        Log.d("SettingsHandler", "Giải phóng tài nguyên")
    }

    fun reset() {
        userName.value = ""
        botName.value = ""
        speechRate.value = 1.0f
        pitch.value = 1.0f
        settingName.value = ""
        botCharacteristics.value = ""
        otherRequirements.value = ""
        Log.d("SettingsHandler", "Đặt lại cài đặt")
    }
}