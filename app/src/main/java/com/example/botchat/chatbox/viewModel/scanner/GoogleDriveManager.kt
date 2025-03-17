package com.example.botchat.chatbox.viewModel.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.Drive.Files.Create
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveManager(private val context: Context) {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var driveService: Drive? = null
    private var consentIntent: Intent? = null
    val firestore = FirebaseFirestore.getInstance()
    companion object {
        private const val REQUEST_CODE_CONSENT = 1001
    }

    // Khởi tạo Google Sign-In và Drive Service
    fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        setupDriveService()
    }

    // Kiểm tra và thiết lập Drive Service dựa trên tài khoản Google đã đăng nhập
    private fun setupDriveService() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("BotChat").build()
        }
    }

    // Kích hoạt đăng nhập Google
    fun startGoogleSignIn(): Intent {
        return googleSignInClient.signInIntent
    }

    // Xử lý kết quả đăng nhập Google
    suspend fun handleSignInResult(data: Intent?, onSuccess: () -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data ?: return)
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("BotChat").build()
            onSuccess()
        } catch (e: Exception) {
            Log.e("GoogleDriveManager", "Google Sign-In failed: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Đăng nhập thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveScannerModeState(userEmail: String, isScannerMode: Boolean) {
        val userSettings = firestore.collection("chats")
            .document(userEmail)
            .collection("settings")
            .document("user_settings")

        userSettings.set(mapOf("isScannerMode" to isScannerMode), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("CallHandler", "Call mode state saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CallHandler", "Error saving Call mode state: ${e.message}")
            }
    }
    // Tải ảnh lên Google Drive với tiến trình
    suspend fun uploadImageToDrive(uri: Uri, activity: Activity? = null, onProgress: (Float) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            driveService?.let { drive ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        Log.e("GoogleDriveManager", "Failed to open InputStream from URI: $uri")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Không thể đọc tệp ảnh", Toast.LENGTH_SHORT).show()
                        }
                        return@withContext null
                    }

                    // Đọc dữ liệu ảnh và tính kích thước
                    val imageBytes = inputStream.readBytes()
                    inputStream.close()
                    val fileSize = imageBytes.size.toLong() // Kích thước tệp (bytes)

                    val folderId = getOrCreateFolder(drive, "botchat")
                    val fileMetadata = DriveFile().apply {
                        name = "Image_${System.currentTimeMillis()}.jpg"
                        parents = listOf(folderId)
                    }

                    val mediaContent = ByteArrayContent("image/jpeg", imageBytes)
                    val createRequest: Create = drive.files().create(fileMetadata, mediaContent)
                        .setFields("id, webViewLink")

                    // Theo dõi tiến trình tải lên
                    val uploader: MediaHttpUploader = createRequest.mediaHttpUploader
                    uploader.setDirectUploadEnabled(false) // Sử dụng resumable upload để theo dõi tiến trình
                    uploader.chunkSize = MediaHttpUploader.MINIMUM_CHUNK_SIZE
                    uploader.setProgressListener(MediaHttpUploaderProgressListener { uploader ->
                        when (uploader.uploadState) {
                            MediaHttpUploader.UploadState.NOT_STARTED -> Log.d("GoogleDriveManager", "Upload not started")
                            MediaHttpUploader.UploadState.INITIATION_STARTED -> Log.d("GoogleDriveManager", "Initiation started")
                            MediaHttpUploader.UploadState.INITIATION_COMPLETE -> Log.d("GoogleDriveManager", "Initiation complete")
                            MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> {
                                val progress = uploader.numBytesUploaded.toFloat() / fileSize.toFloat()
                                onProgress.invoke(progress.coerceIn(0f, 1f)) // Cập nhật tiến trình
                            }
                            MediaHttpUploader.UploadState.MEDIA_COMPLETE -> {
                                Log.d("GoogleDriveManager", "Upload complete")
                                onProgress.invoke(1f)
                            }
                        }
                    })

                    // Thực hiện tải lên
                    val uploadedFile = createRequest.execute()
                    uploadedFile.webViewLink
                } catch (e: UserRecoverableAuthIOException) {
                    Log.w("GoogleDriveManager", "User consent required for Drive access", e)
                    consentIntent = e.intent
                    withContext(Dispatchers.Main) {
                        activity?.startActivityForResult(consentIntent, REQUEST_CODE_CONSENT)
                            ?: Toast.makeText(context, "Không thể yêu cầu đồng ý, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                    }
                    null
                } catch (e: Exception) {
                    Log.e("GoogleDriveManager", "Failed to upload image: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi tải lên Google Drive: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
            } ?: run {
                Log.e("GoogleDriveManager", "Drive service not initialized")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Không thể truy cập Google Drive. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    // Tải ảnh từ Google Drive
    suspend fun fetchImageFromDrive(fileLink: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            driveService?.let { drive ->
                try {
                    val fileId = extractFileIdFromLink(fileLink)
                    if (fileId == null) {
                        Log.e("GoogleDriveManager", "Invalid Google Drive link: $fileLink")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Liên kết Google Drive không hợp lệ", Toast.LENGTH_SHORT).show()
                        }
                        return@withContext null
                    }

                    val inputStream = drive.files().get(fileId).executeMediaAsInputStream()
                    val imageBytes = inputStream.readBytes()
                    inputStream.close()
                    imageBytes
                } catch (e: UserRecoverableAuthIOException) {
                    Log.w("GoogleDriveManager", "User consent required for Drive access", e)
                    consentIntent = e.intent
                    withContext(Dispatchers.Main) {
                        (context as? Activity)?.startActivityForResult(consentIntent, REQUEST_CODE_CONSENT)
                    }
                    null
                } catch (e: Exception) {
                    Log.e("GoogleDriveManager", "Failed to fetch image: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi tải ảnh từ Google Drive: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
            } ?: run {
                Log.e("GoogleDriveManager", "Drive service not initialized")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Không thể truy cập Google Drive. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    // Trích xuất fileId từ link
    private fun extractFileIdFromLink(fileLink: String): String? {
        val regex = "[-\\w]{25,}".toRegex()
        return regex.find(fileLink)?.value
    }

    // Tìm hoặc tạo thư mục
    private suspend fun getOrCreateFolder(drive: Drive, folderName: String): String {
        return withContext(Dispatchers.IO) {
            val query = "mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false"
            val folderList = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id)")
                .execute()

            if (folderList.files.isNotEmpty()) {
                folderList.files[0].id
            } else {
                val folderMetadata = DriveFile().apply {
                    name = folderName
                    mimeType = "application/vnd.google-apps.folder"
                }
                val folder = drive.files().create(folderMetadata)
                    .setFields("id")
                    .execute()
                folder.id
            }
        }
    }
}