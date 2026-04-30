package com.example.ledger.ui.tab

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ledger.data.AutoBill
import com.example.ledger.ui.component.AutoBillCard

fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver, "enabled_notification_listeners"
    ) ?: return false
    return enabledListeners.contains(context.packageName)
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(context.packageName)
}

@Composable
fun AutoRecordTab(
    pendingBills: List<AutoBill>,
    onDismiss: (AutoBill) -> Unit,
    onConvert: (AutoBill) -> Unit,
    onEdit: (AutoBill) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    var isNotificationEnabled by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    DisposableEffect(context) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isNotificationEnabled = isNotificationListenerEnabled(context)
                isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor("enabled_notification_listeners"), false, observer
        )
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES), false, observer
        )
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (!isNotificationEnabled) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = if (pendingBills.isEmpty()) 16.dp else 0.dp).height(176.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "打开通知读取，我就能在你付完钱的瞬间抓到账单",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
                        Text("去打开", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (!isAccessibilityEnabled) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = if (pendingBills.isEmpty()) 16.dp else 0.dp).height(176.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "微信/支付宝付完钱的瞬间，我就能抓到账单。\n说的就是无障碍服务，打开就行。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                        Text("去开启", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (pendingBills.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无新账单，看来今天还没花钱", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pendingBills, key = { it.id }) { bill ->
                    AutoBillCard(bill, onDismiss = { onDismiss(bill) }, onConvert = { onConvert(bill) }, onEdit = { onEdit(bill) })
                }
            }
        }
    }
}
