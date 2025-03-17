package com.example.botchat.chatbox.viewModel.search

import android.content.Context
import android.util.Log
import com.example.botchat.chatbox.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class YouTubeSearch {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    fun toggleYoutubeMode(
        context: Context,
        currentMode: Boolean,
        onModeChanged: (Boolean) -> Unit
    ) {
        val newMode = !currentMode
        onModeChanged(newMode)

        val message = if (newMode) {
            "Bật chế độ tìm kiếm YouTube"
        } else {
            "Tắt chế độ tìm kiếm YouTube"
        }

        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()

        val userEmail = auth.currentUser?.email ?: return
        saveYouTubeModeState(userEmail, newMode)
    }

    fun saveYouTubeModeState(userEmail: String, isYouTubeMode: Boolean) {
        val userSettings = firestore.collection("chats")
            .document(userEmail)
            .collection("settings")
            .document("user_settings")

        userSettings.set(mapOf("isYouTubeMode" to isYouTubeMode), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("YouTubeModeManager", "YouTube mode state saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("YouTubeModeManager", "Error saving YouTube mode state: ${e.message}")
            }
    }

    suspend fun loadYouTubeModeState(userEmail: String): Boolean {
        return try {
            val userSettings = firestore.collection("chats")
                .document(userEmail)
                .collection("settings")
                .document("user_settings")
                .get()
                .await()

            userSettings.getBoolean("isYouTubeMode") ?: false
        } catch (e: Exception) {
            Log.e("YouTubeModeManager", "Error loading YouTube mode state: ${e.message}")
            false
        }
    }
    suspend fun search(query: String, maxResults: Int): List<String> {
        val client = OkHttpClient()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=$maxResults&q=$encodedQuery&key=${Constants.API_KEY}"

        val request = Request.Builder().url(url).build()
        return try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val jsonObject = JSONObject(jsonResponse)
                val items = jsonObject.getJSONArray("items")

                val videoTitles = mutableListOf<String>()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val videoId = item.getJSONObject("id").optString("videoId", "")
                    if (videoId.isNotEmpty()) {
                        val title = fetchYouTubeVideoTitle(videoId) ?: "Không có tiêu đề"
                        videoTitles.add("$title (https://www.youtube.com/watch?v=$videoId)")
                    }
                }
                videoTitles
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchYouTubeVideoTitle(videoId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = Constants.API_KEY
                val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$videoId&key=$apiKey"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@withContext null
                        val json = JSONObject(body)
                        val items = json.getJSONArray("items")
                        if (items.length() > 0) {
                            val snippet = items.getJSONObject(0).getJSONObject("snippet")
                            return@withContext snippet.getString("title")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("YouTubeSearch", "Error fetching video title: ${e.message}")
            }
            return@withContext null
        }
    }

}
