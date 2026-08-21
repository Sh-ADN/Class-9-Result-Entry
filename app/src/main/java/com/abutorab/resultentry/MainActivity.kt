package com.abutorab.resultentry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.abutorab.resultentry.data.SyncState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.abutorab.resultentry.data.StudentResult
import com.abutorab.resultentry.ui.MainViewModel
import com.abutorab.resultentry.ui.MainViewModelFactory
import com.abutorab.resultentry.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ResultApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        delay(2000L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value).alpha(alpha.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_result_icon_1787285063865),
                contentDescription = "App Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Class 9 Result Entry",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(factory = MainViewModelFactory)
) {
    val section by viewModel.section.collectAsState()
    val navController = rememberNavController()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (section != null) {
                    viewModel.syncNow()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val startDestination = "splash"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(onTimeout = {
                val nextDest = if (section == null) "setup" else "roster"
                navController.navigate(nextDest) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("setup") {
            SetupScreen(
                currentSection = section,
                onSectionSelected = { sec ->
                    viewModel.setSection(sec)
                    navController.navigate("roster") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }
        composable("roster") {
            RosterScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("setup") },
                onNavigateToCombinedResult = { navController.navigate("combined") }
            )
        }
        composable("combined") {
            com.abutorab.resultentry.ui.CombinedResultScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    currentSection: String?,
    onSectionSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select Section") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Please select your section:", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onSectionSelected("A") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Section A", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSectionSelected("B") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Section B", style = MaterialTheme.typography.titleMedium)
            }
            if (currentSection != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Currently selected: Section $currentSection")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCombinedResult: () -> Unit
) {
    val results by viewModel.studentResults.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentSection by viewModel.section.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val syncedCount = results.count { it.entry?.syncState == SyncState.SYNCED }
    val draftCount = results.count { it.entry?.syncState == SyncState.DRAFT }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Class 9 Result Entry")
                        Text(
                            "Synced: $syncedCount • Drafts: $draftCount • Total: ${results.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCombinedResult) {
                        Icon(Icons.Default.Assessment, contentDescription = "Combined Result")
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Roster")
                    }
                    IconButton(onClick = { viewModel.syncNow() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Now")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isRefreshing || isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Interactive Header for Section Selection & Instant Roll/Name Search
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = currentSection == "A",
                            onClick = { viewModel.setSection("A") },
                            label = { Text("Section A") }
                        )
                        FilterChip(
                            selected = currentSection == "B",
                            onClick = { viewModel.setSection("B") },
                            label = { Text("Section B") }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search Roll or Name...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(results, key = { it.roster.roll }) { result ->
                    QuickEditStudentRow(
                        result = result,
                        onSave = { tm, fc, g ->
                            viewModel.saveEntry(result.roster.roll, tm, fc, g)
                        },
                        showSnackbar = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditStudentRow(
    result: StudentResult,
    onSave: (Int, Int, Double?) -> Unit,
    showSnackbar: (String) -> Unit
) {
    var isEdited by remember(result.roster.roll) { mutableStateOf(false) }
    var saveStatus by remember(result.roster.roll) { mutableStateOf("idle") } // "idle", "saving", "saved"

    var totalMarks by remember(result.roster.roll) { mutableStateOf(result.entry?.totalMarks?.toString() ?: "") }
    var failedCount by remember(result.roster.roll) { mutableStateOf(result.entry?.failedCount) }
    var gpaDigits by remember(result.roster.roll) { 
        mutableStateOf(
            result.entry?.gpa?.let { gpaValue ->
                var str = gpaValue.toString()
                if (str.endsWith(".0")) str = str.substring(0, str.length - 2)
                str.replace(".", "")
            } ?: ""
        )
    }

    LaunchedEffect(result.entry) {
        if (!isEdited && result.entry != null) {
            totalMarks = result.entry.totalMarks.toString()
            failedCount = result.entry.failedCount
            gpaDigits = result.entry.gpa?.let { gpaValue ->
                var str = gpaValue.toString()
                if (str.endsWith(".0")) str = str.substring(0, str.length - 2)
                str.replace(".", "")
            } ?: ""
        }
    }

    LaunchedEffect(result.entry?.syncState) {
        if (result.entry?.syncState == SyncState.ERROR) {
            showSnackbar("Failed to sync roll ${result.roster.roll} — will retry.")
        }
    }

    val tmInt = totalMarks.toIntOrNull()
    val isMarksInvalid = totalMarks.isNotEmpty() && (tmInt == null || tmInt !in 0..1150)

    val gpaDouble = when (gpaDigits.length) {
        1 -> gpaDigits.toDoubleOrNull()
        2 -> "${gpaDigits[0]}.${gpaDigits[1]}".toDoubleOrNull()
        3 -> "${gpaDigits[0]}.${gpaDigits.substring(1, 3)}".toDoubleOrNull()
        else -> null
    }
    val isGpaInvalid = gpaDigits.isNotEmpty() && (gpaDouble == null || gpaDouble !in 0.0..5.0)

    // Debounced Auto-Save Loop (800ms) with reactive saveStatus feedback
    LaunchedEffect(totalMarks, failedCount, gpaDigits) {
        if (!isEdited) return@LaunchedEffect
        if (isMarksInvalid || isGpaInvalid) return@LaunchedEffect

        val tm = totalMarks.toIntOrNull()
        val fc = failedCount
        val g = gpaDouble

        if (tm != null && fc != null) {
            if (fc > 0 || (fc == 0 && g != null)) {
                saveStatus = "saving"
                delay(800)
                onSave(tm, fc, if (fc == 0) g else null)
                isEdited = false
                saveStatus = "saved"
                delay(2000)
                if (saveStatus == "saved") {
                    saveStatus = "idle"
                }
            }
        }
    }

    val currentData by rememberUpdatedState(Triple(totalMarks, failedCount, gpaDouble))
    val currentValidity by rememberUpdatedState(Pair(isMarksInvalid, isGpaInvalid))
    val currentIsEdited by rememberUpdatedState(isEdited)
    
    DisposableEffect(result.roster.roll) {
        onDispose {
            if (currentIsEdited) {
                val (tmStr, fc, g) = currentData
                val (marksInvalid, gpaInvalid) = currentValidity
                if (!marksInvalid && !gpaInvalid) {
                    val tm = tmStr.toIntOrNull()
                    if (tm != null && fc != null) {
                        if (fc > 0 || (fc == 0 && g != null)) {
                            onSave(tm, fc, if (fc == 0) g else null)
                            isEdited = false
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Roll ${result.roster.roll} — ${result.roster.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Reactive Status Indicator Badge (Saving... / Saved / Synced / Draft)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (saveStatus) {
                            "saving" -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Saving...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            "saved" -> {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Saved",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Saved",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                            else -> {
                                val (bgColor, textColor, text) = when (result.entry?.syncState) {
                                    SyncState.SYNCED -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Synced")
                                    SyncState.DRAFT -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Draft")
                                    SyncState.ERROR -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Sync Error")
                                    null -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Not Started")
                                }
                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = textColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = totalMarks,
                    onValueChange = { 
                        totalMarks = it
                        isEdited = true
                    },
                    label = { Text("Total Marks") },
                    placeholder = { Text("0-1150") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    isError = isMarksInvalid,
                    supportingText = { if (isMarksInvalid) Text("0-1150") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Failed Subjects:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Pill-style Numeric Selectors (0-9)
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                (0..9).forEach { number ->
                    FailedSubjectPill(
                        number = number,
                        isSelected = failedCount == number,
                        onClick = { 
                            failedCount = number
                            isEdited = true
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = failedCount == 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "GPA",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isGpaInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GpaInputBoxes(
                        value = gpaDigits,
                        onValueChange = { 
                            gpaDigits = it
                            isEdited = true
                        },
                        isError = isGpaInvalid
                    )
                    if (isGpaInvalid) {
                        Text(
                            text = "Valid GPA: 0.0 to 5.0",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FailedSubjectPill(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected && number == 0 -> Color(0xFFE8F5E9)
            isSelected -> Color(0xFFFFEBEE)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        label = "pillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected && number == 0 -> Color(0xFF2E7D32)
            isSelected -> Color(0xFFC62828)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "pillFg"
    )
    val borderColor = if (isSelected) {
        if (number == 0) Color(0xFF2E7D32) else Color(0xFFC62828)
    } else Color.Transparent

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 0.dp, borderColor),
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun GpaInputBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    val focusRequesters = remember { List(3) { FocusRequester() } }
    var pendingFocus by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pendingFocus) {
        pendingFocus?.let {
            focusRequesters[it].requestFocus()
            pendingFocus = null
        }
    }

    fun setDigit(index: Int, digit: Char) {
        val currentArray = CharArray(3) { i -> value.getOrElse(i) { ' ' } }
        currentArray[index] = digit
        onValueChange(String(currentArray).trimEnd())
    }

    fun clearDigit(index: Int) {
        val currentArray = CharArray(3) { i -> value.getOrElse(i) { ' ' } }
        currentArray[index] = ' '
        onValueChange(String(currentArray).trimEnd())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GpaSingleBox(
            char = value.getOrNull(0),
            isError = isError,
            focusRequester = focusRequesters[0],
            keyboardType = KeyboardType.Decimal,
            onValueChange = { newStr ->
                if (newStr.contains('.')) {
                    pendingFocus = 1
                } else {
                    val digit = newStr.replace(Regex("[^\\d]"), "").lastOrNull()
                    if (digit != null) setDigit(0, digit) else clearDigit(0)
                }
            },
            onBackspace = { /* box 0 has no earlier box */ }
        )
        Text(
            text = ".",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        GpaSingleBox(
            char = value.getOrNull(1),
            isError = isError,
            focusRequester = focusRequesters[1],
            onValueChange = { newStr ->
                val digit = newStr.replace(Regex("[^\\d]"), "").lastOrNull()
                if (digit != null) {
                    setDigit(1, digit)
                    pendingFocus = 2
                } else {
                    clearDigit(1)
                    pendingFocus = 0
                }
            },
            onBackspace = { pendingFocus = 0 }
        )
        Spacer(modifier = Modifier.width(8.dp))
        GpaSingleBox(
            char = value.getOrNull(2),
            isError = isError,
            focusRequester = focusRequesters[2],
            onValueChange = { newStr ->
                val digit = newStr.replace(Regex("[^\\d]"), "").lastOrNull()
                if (digit != null) {
                    setDigit(2, digit)
                } else {
                    clearDigit(2)
                    pendingFocus = 1
                }
            },
            onBackspace = { pendingFocus = 1 }
        )
    }
}

@Composable
fun GpaSingleBox(
    char: Char?,
    isError: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    val hasValue = char != null && char != ' '
    BasicTextField(
        value = if (hasValue) char.toString() else "",
        onValueChange = { onValueChange(it) },
        modifier = Modifier
            .size(56.dp)
            .border(
                width = if (hasValue) 2.dp else 1.dp,
                color = if (isError) MaterialTheme.colorScheme.error
                        else if (hasValue) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            )
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.Backspace && (!hasValue)) {
                    onBackspace()
                    true
                } else false
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                innerTextField()
            }
        }
    )
}
