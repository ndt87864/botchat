package com.example.botchat.chatbox.viewModel.search

import com.example.botchat.chatbox.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class GoogleSearch {
    suspend fun search(query: String): String {
        return withContext(Dispatchers.IO) {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://www.googleapis.com/customsearch/v1?key=${Constants.GOOGLE_API_KEY}&cx=${Constants.GOOGLE_CX}&q=$encodedQuery"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse)
                    val items = jsonObject.optJSONArray("items")

                    if (items != null && items.length() > 0) {
                        val resultCount = items.length()
                        val requestedCount = extractResultCount(query)
                        val limitCount = if (requestedCount in 1..resultCount) requestedCount else resultCount

                        val resultText = StringBuilder()
                        for (i in 0 until limitCount) {
                            val item = items.getJSONObject(i)
                            val title = item.getString("title")
                            val link = item.getString("link")
                            resultText.append("Kết quả ${i + 1}: $title\nLink: $link\n\n")
                        }
                        return@withContext resultText.toString()
                    } else {
                        return@withContext "Không tìm thấy kết quả nào trên Google."
                    }
                } else {
                    return@withContext "Lỗi khi tìm kiếm trên Google: ${response.message}"
                }
            } catch (e: Exception) {
                return@withContext "Lỗi trong quá trình tìm kiếm: ${e.message}"
            }
        }
    }

    private fun extractResultCount(query: String): Int {
        val regex = Regex("hiển thị (\\d+) kết quả")
        val matchResult = regex.find(query)
        return matchResult?.groups?.get(1)?.value?.toIntOrNull() ?: 5
    }
}
