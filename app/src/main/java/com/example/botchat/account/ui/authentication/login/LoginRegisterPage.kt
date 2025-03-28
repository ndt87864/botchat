package com.example.botchat.account.ui.authentication.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.botchat.R
import com.example.botchat.account.viewModel.AccountViewModel
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import java.util.Calendar
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.botchat.BuildConfig
import com.example.botchat.account.ui.authentication.login.components.ConfirmPasswordField
import com.example.botchat.account.ui.authentication.login.components.EmailField
import com.example.botchat.account.ui.authentication.login.components.ErrorMessage
import com.example.botchat.account.ui.authentication.login.components.GoogleSignInButton
import com.example.botchat.account.ui.authentication.login.components.Header
import com.example.botchat.account.ui.authentication.login.components.PasswordField
import com.example.botchat.account.ui.authentication.login.components.RegistrationFields
import com.example.botchat.account.ui.authentication.login.components.ActionButton
import com.example.botchat.account.ui.authentication.login.components.ForgotPasswordButton
import com.example.botchat.account.ui.authentication.login.components.ToggleRegisterLoginButton

@Composable
fun LoginRegisterPage(
    viewModel: AccountViewModel,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToChat: () -> Unit
) {
    val chatViewModel: ChatViewModel = viewModel() // Lấy instance của ChatViewModel

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var dateOfBirth by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Cấu hình DatePickerDialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                dateOfBirth = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    // Xử lý đăng nhập Google
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        println("Google Sign-In Result: ${result.resultCode}")
        println("Result Data: ${result.data?.extras}")
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    println("Google Account: ${account.email}, Token: ${account.idToken}")
                    coroutineScope.launch {
                        val success = viewModel.signInWithGoogle(
                            googleIdToken = account.idToken ?: "",
                            displayName = account.displayName,
                            email = account.email
                        )
                        println("Sign-in with Google result: $success")
                        if (success) {
                            println("Showing success Toast and navigating to chat")
                            Toast.makeText(context, "Đăng nhập thành công với Google!", Toast.LENGTH_SHORT).show()
                            chatViewModel.createNewChatRoom()
                            onNavigateToChat()
                        } else {
                            errorMessage = "Đăng nhập với Google thất bại dù Google báo thành công"
                            println("Sign-in failed despite Google success")
                        }
                    }
                } catch (e: ApiException) {
                    errorMessage = "Lỗi Google: ${e.statusCode} - ${e.message}"
                    println("Google Sign-In Exception: ${e.statusCode}: ${e.message}")
                }
            }
            Activity.RESULT_CANCELED -> {
                // Xử lý như trước
            }
            else -> {
                errorMessage = "Đăng nhập Google thất bại với mã lỗi: ${result.resultCode}"
            }
        }
    }
    // Giao diện chính
    Column(modifier = modifier.padding(16.dp)) {
        Spacer(modifier = if (isRegistering) Modifier.height(32.dp) else Modifier.height(50.dp))
        Header(isRegistering = isRegistering)
        Spacer(modifier = Modifier.height(32.dp))
        if (isRegistering) {
            RegistrationFields(
                username = username,
                onUsernameChange = { username = it },
                dateOfBirth = dateOfBirth,
                onDateOfBirthChange = { dateOfBirth = it },
                onDatePickerClick = { datePickerDialog.show() }
            )
        }
        EmailField(email = email, onEmailChange = { email = it })
        PasswordField(
            password = password,
            onPasswordChange = { password = it },
            isPasswordVisible = isPasswordVisible,
            onPasswordVisibilityChange = { isPasswordVisible = it }
        )
        if (isRegistering) {
            ConfirmPasswordField(
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                isConfirmPasswordVisible = isConfirmPasswordVisible,
                onConfirmPasswordVisibilityChange = { isConfirmPasswordVisible = it }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ErrorMessage(errorMessage = errorMessage)
        ActionButton(
            isRegistering = isRegistering,
            isProcessing = isProcessing,
            onClick = {
                if (!isProcessing) {
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            if (isRegistering) {
                                if (password != confirmPassword) {
                                    errorMessage = "Mật khẩu không khớp"
                                    isProcessing = false
                                    return@launch
                                }
                                if (!dateOfBirth.matches("\\d{2}/\\d{2}/\\d{4}".toRegex())) {
                                    errorMessage = "Ngày sinh không đúng định dạng (dd/mm/yyyy)"
                                    isProcessing = false
                                    return@launch
                                }
                                viewModel.registerUser(email, password, username, dateOfBirth)
                                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                isRegistering = false
                            } else {
                                val success = viewModel.loginUser(email, password)
                                if (success) {
                                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                    chatViewModel.createNewChatRoom()
                                    onNavigateToChat()
                                } else {
                                    errorMessage = "Đăng nhập thất bại. Kiểm tra lại thông tin."
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Có lỗi xảy ra: ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        GoogleSignInButton(
            isProcessing = isProcessing,
            onClick = {
                val activity = context as ComponentActivity
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.google_client_id)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(activity, gso)
                launcher.launch(googleSignInClient.signInIntent)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!isRegistering) {
            ForgotPasswordButton(onClick = onNavigateToForgotPassword)
        }
        Spacer(modifier = Modifier.height(16.dp))
        ToggleRegisterLoginButton(
            isRegistering = isRegistering,
            onClick = { isRegistering = !isRegistering }
        )
    }
}
