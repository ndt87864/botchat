package com.example.botchat.account.viewModel.edit

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.auth.EmailAuthProvider

class EditAccountHandler(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun updateUserInfo(
        username: String,
        email: String,
        currentPassword: String? = null,
        password: String,
        dateOfBirth: String
    ): Result<String> {
        val user = auth.currentUser
        return try {
            if (user == null) {
                return Result.failure(Exception("Bạn cần đăng nhập để cập nhật thông tin"))
            }

            // Parse ngày sinh từ định dạng dd/mm/yyyy
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = dateFormat.parse(dateOfBirth) ?: throw Exception("Ngày sinh không hợp lệ")
            val currentDate = Calendar.getInstance().time

            // Tính khoảng cách năm
            val calendarBirth = Calendar.getInstance().apply { time = birthDate }
            val calendarCurrent = Calendar.getInstance().apply { time = currentDate }
            var age = calendarCurrent.get(Calendar.YEAR) - calendarBirth.get(Calendar.YEAR)

            if (calendarCurrent.get(Calendar.DAY_OF_YEAR) < calendarBirth.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            if (age < 15) {
                return Result.failure(Exception("Bạn phải ít nhất 15 tuổi để cập nhật thông tin"))
            }

            // Xác thực lại nếu cần thay đổi email hoặc mật khẩu
            val oldEmail = user.email!!
            if ((email != oldEmail || password.isNotEmpty()) && currentPassword != null) {
                val credential = EmailAuthProvider.getCredential(oldEmail, currentPassword)
                user.reauthenticate(credential).await()
            }

            // Cập nhật thông tin trong collection "users"
            val userData = mapOf(
                "username" to username,
                "email" to email,
                "dateOfBirth" to dateOfBirth
            )
            db.collection("users").document(user.uid).update(userData).await()

            // Nếu email thay đổi, cập nhật các collection khác
            if (email != oldEmail) {
                // Cập nhật email trong FirebaseAuth
                user.updateEmail(email).await()

                // Danh sách các collection cần cập nhật
                val collectionsToUpdate = listOf("chats", "user_settings", "void_settings")

                for (collection in collectionsToUpdate) {
                    val oldDocRef = db.collection(collection).document(oldEmail)
                    val newDocRef = db.collection(collection).document(email)

                    // Lấy dữ liệu từ tài liệu cũ
                    val oldDocSnapshot = oldDocRef.get().await()
                    if (oldDocSnapshot.exists()) {
                        val data = oldDocSnapshot.data
                        if (data != null) {
                            // Sao chép dữ liệu sang tài liệu mới
                            newDocRef.set(data).await()
                            // Xóa tài liệu cũ (tùy chọn)
                            oldDocRef.delete().await()
                        }
                    }
                }
            }

            // Nếu mật khẩu không rỗng, cập nhật mật khẩu
            if (password.isNotEmpty()) {
                user.updatePassword(password).await()
            }

            Result.success("Cập nhật thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}