package com.example.ledger.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ledger.data.AppDatabase
import com.example.ledger.data.AuthSession
import com.example.ledger.data.AutoBill
import com.example.ledger.data.ExpenseRecord
import com.example.ledger.data.Item
import com.example.ledger.data.RecurringExpense
import com.example.ledger.ui.dialog.*
import com.example.ledger.ui.tab.AutoRecordTab
import com.example.ledger.ui.tab.ExpenseTab
import com.example.ledger.ui.tab.ItemListTab
import com.example.ledger.ui.tab.OverviewTab
import com.example.ledger.ui.theme.FrostedBar
import com.example.ledger.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPersonalCenter: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val viewModel: ItemViewModel = viewModel(factory = ItemViewModel.Factory(db))
    val items by viewModel.items.collectAsState()
    val pendingBills by viewModel.pendingBills.collectAsState()
    val allBills by viewModel.allBills.collectAsState()
    val expenseRecords by viewModel.expenseRecords.collectAsState()
    val activeTemplates by viewModel.activeTemplates.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()

    val isLoggedIn by AuthSession.isLoggedIn.collectAsState()
    val syncEvent by AuthSession.syncEvent.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToSell by remember { mutableStateOf<Item?>(null) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var billToConvert by remember { mutableStateOf<AutoBill?>(null) }
    var billToEdit by remember { mutableStateOf<AutoBill?>(null) }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var showRecurringListDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseRecord?>(null) }
    var templateToEdit by remember { mutableStateOf<RecurringExpense?>(null) }

    val pagerState = rememberPagerState(pageCount = { 4 })

    // Notification permission for Android 13+
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val check = androidx.core.content.ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
            if (check != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("账本", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToPersonalCenter) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "个人中心",
                            tint = if (isLoggedIn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = com.example.ledger.ui.theme.FrostedBar
                ),
                actions = {
                    if (pagerState.currentPage == 0) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Outlined.Add, contentDescription = "添加", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                    if (pagerState.currentPage == 2) {
                        IconButton(onClick = { showAddExpenseDialog = true }) {
                            Icon(Icons.Outlined.Add, contentDescription = "记一笔", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = FrostedBar) {
                val tabs = listOf(
                    Triple("家当", Icons.Filled.Inventory2, "我的家当"),
                    Triple("记账", Icons.AutoMirrored.Filled.ReceiptLong, "自动记账"),
                    Triple("开销", Icons.Outlined.Payments, "生活开销"),
                    Triple("总账", Icons.Filled.Assessment, "算总账")
                )
                tabs.forEachIndexed { index, (title, icon, desc) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = desc) },
                        label = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ItemListTab(
                        items = items,
                        onDelete = { viewModel.deleteItem(it) },
                        onEdit = { itemToEdit = it },
                        onSell = { itemToSell = it }
                    )
                    1 -> AutoRecordTab(
                        pendingBills = pendingBills,
                        onDismiss = { viewModel.dismissAutoBill(it) },
                        onConvert = { billToConvert = it },
                        onEdit = { billToEdit = it },
                        context = context
                    )
                    2 -> ExpenseTab(
                        expenseRecords = expenseRecords,
                        thisMonthTotal = com.example.ledger.domain.usecase.CalculateStatistics.calculate(
                            items, allBills, expenseRecords, activeTemplates
                        ).thisMonthLivingExpenses,
                        onDelete = { viewModel.deleteExpenseRecord(it) },
                        onEdit = { expenseToEdit = it },
                        onManageRecurring = { showRecurringListDialog = true }
                    )
                    3 -> OverviewTab(
                        items = items,
                        allBills = allBills,
                        expenseRecords = expenseRecords,
                        activeTemplates = activeTemplates
                    )
                }
            }

            if (syncEvent != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) { Text(syncEvent ?: "") }
                LaunchedEffect(syncEvent) { kotlinx.coroutines.delay(2000); AuthSession.clearSyncEvent() }
            }
        }
    }

    // Item dialogs
    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, price, date, _ -> viewModel.addItem(name, price, date, 0.0); showAddDialog = false }
        )
    }
    itemToEdit?.let { item ->
        EditItemDialog(item = item, onDismiss = { itemToEdit = null }, onEdit = { n, p, d -> viewModel.updateItemDetails(item, n, p, d); itemToEdit = null })
    }
    itemToSell?.let { item ->
        SellItemDialog(item = item, onDismiss = { itemToSell = null }, onSell = { sp, sd -> viewModel.sellItem(item, sp, sd); itemToSell = null })
    }
    billToConvert?.let { bill ->
        ConvertBillDialog(bill = bill, onDismiss = { billToConvert = null }, onConvert = { n, _ -> viewModel.convertBillToItem(bill, n, 0.0); billToConvert = null })
    }
    billToEdit?.let { bill ->
        EditBillDialog(bill = bill, onDismiss = { billToEdit = null }, onEdit = { mn, a, t -> viewModel.updateBillDetails(bill, mn, a, t); billToEdit = null })
    }

    // Expense dialogs
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { name, amount, date, category, note ->
                viewModel.addExpenseRecord(name, amount, date, category, note)
                showAddExpenseDialog = false
            }
        )
    }
    expenseToEdit?.let { record ->
        EditExpenseDialog(
            record = record,
            onDismiss = { expenseToEdit = null },
            onEdit = { name, amount, date, category, note ->
                viewModel.updateExpenseRecord(record, name, amount, date, category, note)
                expenseToEdit = null
            }
        )
    }
    if (showAddRecurringDialog) {
        RecurringExpenseDialog(
            onDismiss = { showAddRecurringDialog = false },
            onAdd = { name, amount, category, period, day, startMonth, deposit, note ->
                viewModel.addRecurringExpense(name, amount, category, period, day, startMonth, deposit, note)
                showAddRecurringDialog = false
            },
            onEdit = { _, _, _, _, _, _, _, _ -> }
        )
    }
    templateToEdit?.let { template ->
        RecurringExpenseDialog(
            template = template,
            onDismiss = { templateToEdit = null },
            onAdd = { _, _, _, _, _, _, _, _ -> },
            onEdit = { _, name, amount, category, period, day, startMonth, note ->
                viewModel.updateRecurringExpense(template, name, amount, category, period, day, startMonth, note)
                templateToEdit = null
            }
        )
    }
    if (showRecurringListDialog) {
        RecurringExpenseListDialog(
            templates = allTemplates,
            onDismiss = { showRecurringListDialog = false },
            onToggle = { viewModel.toggleRecurringExpense(it) },
            onDelete = { viewModel.deleteRecurringExpense(it.id) },
            onEdit = {
                templateToEdit = it
                showRecurringListDialog = false
            }
        )
    }
}
