package com.example.ledger.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ledger.network.SyncUploadRequest
import com.example.ledger.network.VerifyCodeRequest
import androidx.compose.material3.MaterialTheme
import com.example.ledger.viewmodel.ItemViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3_600_000 -> "${diff / 60_000}分钟前"
        diff < 86_400_000 -> "${diff / 3_600_000}小时前"
        else -> "${diff / 86_400_000}天前"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalCenterScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isLoggedIn by AuthSession.isLoggedIn.collectAsState()
    val token by AuthSession.token.collectAsState()
    val lastSyncTime by AuthSession.lastSyncTime.collectAsState()

    val db = remember { AppDatabase.getDatabase(context) }
    val viewModel: ItemViewModel = viewModel(factory = ItemViewModel.Factory(db))

    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // File export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.exportAllData()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    message = "导出成功"
                } catch (e: Exception) {
                    message = "导出失败：${e.message}"
                }
            }
        }
    }

    // File import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                    if (json.isNotBlank()) {
                        viewModel.importAllData(json)
                        message = "导入成功"
                    } else {
                        message = "文件为空"
                    }
                } catch (e: Exception) {
                    message = "导入失败：${e.message}"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ledger.ui.theme.FrostedBar)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLoggedIn) {
                LoginSection(
                    scope = scope,
                    isLoading = isLoading,
                    onLoadingChange = { isLoading = it },
                    onMessage = { message = it },
                    viewModel = viewModel
                )
            } else {
                AccountSection(
                    lastSyncTime = lastSyncTime,
                    scope = scope,
                    isLoading = isLoading,
                    onLoadingChange = { isLoading = it },
                    onMessage = { message = it },
                    viewModel = viewModel
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            // Export/Import section (available to all)
            Text("数据备份", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        exportLauncher.launch("ledger_backup_${System.currentTimeMillis()}.json")
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Upload, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("导出数据", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("导入数据", fontSize = 14.sp)
                }
            }

            // Logout button
            if (isLoggedIn) {
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = {
                    AuthSession.logout()
                    message = "已退出登录"
                }) {
                    Text("退出登录", color = MaterialTheme.colorScheme.error)
                }
            }

            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Snackbar(modifier = Modifier.padding(horizontal = 16.dp)) { Text(message ?: "") }
                LaunchedEffect(message) { delay(3000); message = null }
            }
        }
    }
}

@Composable
private fun LoginSection(
    scope: kotlinx.coroutines.CoroutineScope,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onMessage: (String?) -> Unit,
    viewModel: ItemViewModel
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }

    LaunchedEffect(countdown) {
        if (countdown > 0) { delay(1000); countdown-- }
    }

    Text("把账本搬上云端", fontSize = 22.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp))
    Text("登录后可使用云端同步", fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 32.dp))

    OutlinedTextField(value = email, onValueChange = { email = it.trim().take(64) },
        label = { Text("邮箱") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = {
            if (!email.contains("@")) { onMessage("请输入正确的邮箱地址"); return@OutlinedButton }
            scope.launch {
                isSending = true
                try {
                    ApiClient.apiService.sendCode(SendCodeRequest(email))
                    countdown = 60
                    onMessage("验证码已发送")
                } catch (e: Exception) {
                    onMessage("发验证码失败：${e.message}")
                } finally { isSending = false }
            }
        },
        enabled = email.contains("@") && countdown == 0 && !isSending,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(44.dp)
    ) {
        if (isSending) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        else Text(if (countdown > 0) "${countdown}秒后再发" else "发送验证码", fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(value = code, onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
        label = { Text("验证码") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
            if (!email.contains("@") || code.length < 4) {
                onMessage("邮箱和验证码总得填对吧"); return@Button
            }
            scope.launch {
                onLoadingChange(true)
                try {
                    val response = ApiClient.apiService.verifyCode(VerifyCodeRequest(email, code))
                    AuthSession.login(response.token, response.isVip, response.userId, response.vipExpireAt)

                    try {
                        val syncData = ApiClient.apiService.downloadSyncData("Bearer ${response.token}")
                        if (syncData.success && syncData.dataJson != null) {
                            viewModel.importAllData(syncData.dataJson)
                            AuthSession.updateLastSyncTime(syncData.updatedAt ?: System.currentTimeMillis())
                        } else {
                            val json = viewModel.exportAllData()
                            ApiClient.apiService.uploadSyncData("Bearer ${response.token}", SyncUploadRequest(json))
                            AuthSession.updateLastSyncTime(System.currentTimeMillis())
                        }
                    } catch (_: Exception) { }
                    onMessage("登录成功")
                } catch (e: Exception) {
                    onMessage("登录失败，验证码对了吗？")
                } finally { onLoadingChange(false) }
            }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
        else Text("登录", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AccountSection(
    lastSyncTime: Long?,
    scope: kotlinx.coroutines.CoroutineScope,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onMessage: (String?) -> Unit,
    viewModel: ItemViewModel
) {
    val token by AuthSession.token.collectAsState()

    // Sync status card
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("☁️", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("云端同步", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("数据自动备份到服务器", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (lastSyncTime != null) {
                    Icon(Icons.Outlined.CloudDone, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("最近同步 · ${formatRelativeTime(lastSyncTime)}", fontSize = 13.sp, color = Color(0xFF4CAF50))
                } else {
                    Icon(Icons.Outlined.Cloud, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("等待首次同步", fontSize = 13.sp, color = Color(0xFFFF9800))
                }
            }
        }
    }

    // Manual sync button
    OutlinedButton(
        onClick = {
            scope.launch {
                onLoadingChange(true)
                try {
                    val json = viewModel.exportAllData()
                    ApiClient.apiService.uploadSyncData("Bearer $token", SyncUploadRequest(json))
                    AuthSession.updateLastSyncTime(System.currentTimeMillis())
                    onMessage("同步成功")
                } catch (e: Exception) {
                    onMessage("同步失败：${e.message}")
                } finally { onLoadingChange(false) }
            }
        },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().height(44.dp).padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        else {
            Icon(Icons.Outlined.Cloud, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("立即同步", fontSize = 14.sp)
        }
    }
}
