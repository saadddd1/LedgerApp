package com.example.ledger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ledger.data.AppDatabase
import com.example.ledger.data.AuthSession
import com.example.ledger.network.ApiClient
import com.example.ledger.network.SendCodeRequest
import com.example.ledger.network.SyncDownloadResponse
import com.example.ledger.network.SyncUploadRequest
import com.example.ledger.network.VerifyCodeRequest
import com.example.ledger.viewmodel.ItemViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVip: () -> Unit
) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getDatabase(context) }
    val viewModel: ItemViewModel = viewModel(factory = ItemViewModel.Factory(db))

    var pendingRestore by remember { mutableStateOf(false) }
    var pendingSyncData by remember { mutableStateOf<SyncDownloadResponse?>(null) }

    // Countdown timer
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    // Restore dialog after login
    if (pendingRestore) {
        AlertDialog(
            onDismissRequest = { pendingRestore = false; onNavigateBack() },
            title = { Text("发现云端备份") },
            text = { Text("云端有你的备份数据，是否恢复到本地？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        pendingSyncData?.dataJson?.let { viewModel.importAllData(it) }
                        AuthSession.updateLastSyncTime(pendingSyncData?.updatedAt ?: System.currentTimeMillis())
                    }
                    pendingRestore = false
                    onNavigateBack()
                }) { Text("恢复") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = false; onNavigateBack() }) { Text("不用了") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("登录", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "关闭")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ledger.ui.theme.FrostedBar)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("把账本搬上云端", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 40.dp, bottom = 8.dp))
            Text("换手机、删软件，账都不会丢", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 40.dp))

            OutlinedTextField(value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
                label = { Text("手机号") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(12.dp))

            // Send code button
            OutlinedButton(
                onClick = {
                    if (phone.length != 11) { message = "手机号得是11位吧"; return@OutlinedButton }
                    scope.launch {
                        isSending = true
                        try {
                            ApiClient.apiService.sendCode(SendCodeRequest(phone))
                            countdown = 60
                            message = "验证码已发送（开发模式用 123456）"
                        } catch (e: Exception) {
                            message = "发验证码失败：${e.message}"
                        } finally { isSending = false }
                    }
                },
                enabled = phone.length == 11 && countdown == 0 && !isSending,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                if (isSending) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(
                    if (countdown > 0) "${countdown}秒后再发" else "发送验证码",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = code, onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                label = { Text("验证码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (phone.length != 11 || code.length < 4) {
                        message = "手机号和验证码总得填对吧"; return@Button
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            val response = ApiClient.apiService.verifyCode(
                                VerifyCodeRequest(phone, code)
                            )
                            AuthSession.login(response.token, response.isVip, response.userId, response.vipExpireAt)

                            if (response.isVip) {
                                try {
                                    val syncData = ApiClient.apiService.downloadSyncData("Bearer ${response.token}")
                                    if (syncData.success && syncData.dataJson != null) {
                                        pendingSyncData = syncData
                                        pendingRestore = true
                                        isLoading = false
                                        return@launch
                                    } else {
                                        // No cloud data, auto-upload local data as first backup
                                        val json = viewModel.exportAllData()
                                        ApiClient.apiService.uploadSyncData(
                                            "Bearer ${response.token}",
                                            SyncUploadRequest(json)
                                        )
                                        AuthSession.updateLastSyncTime(System.currentTimeMillis())
                                    }
                                } catch (_: Exception) { }
                            }
                            message = "登录成功"
                            onNavigateBack()
                        } catch (e: Exception) {
                            message = "登录失败，验证码对了吗？"
                        } finally { isLoading = false }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("登录", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (message != null) {
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(message ?: "") }
                LaunchedEffect(message) { delay(3000); message = null }
            }
        }
    }
}
