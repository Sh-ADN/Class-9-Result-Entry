package com.abutorab.resultentry

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.abutorab.resultentry.data.SyncState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
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
                painter = painterResource(id = R.drawable.app_icon_perfect_pro_1787968956161),
                contentDescription = "App Logo",
                contentScale = ContentScale.Fit,
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
                Text("Section A — জনাব মেজবা উদ্দিন", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSectionSelected("B") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Section B — জনাব অনন্য দেবনাথ", style = MaterialTheme.typography.titleMedium)
            }
            if (currentSection != null) {
                val teacherName = if (currentSection == "A") "জনাব মেজবা উদ্দিন" else "জনাব অনন্য দেবনাথ"
                Spacer(modifier = Modifier.height(32.dp))
                Text("Currently selected: Section $currentSection ($teacherName)")
            }
        }
    }
}

fun Int.toBengaliDigits(): String {
    val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return this.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")
}

enum class ConsiderationCategory {
    MERIT,
    FIRST,
    SECOND,
    SPECIAL
}

data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

@Composable
fun CategoryCounterBadge(
    title: String,
    count: Int,
    isSelected: Boolean,
    containerColor: Color,
    contentColor: Color,
    selectedContainerColor: Color,
    selectedContentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isSelected) selectedContainerColor else containerColor
    val textCol = if (isSelected) selectedContentColor else contentColor
    val borderCol = if (isSelected) selectedContentColor.copy(alpha = 0.85f) else contentColor.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                color = textCol,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = count.toBengaliDigits(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                color = textCol,
                maxLines = 1
            )
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
    val sectionResults by viewModel.sectionResults.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentSection by viewModel.section.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val syncedCount = remember(sectionResults) { sectionResults.count { it.entry?.syncState == SyncState.SYNCED } }
    val draftCount = remember(sectionResults) { sectionResults.count { it.entry?.syncState == SyncState.DRAFT } }
    val notStartedCount = remember(sectionResults) { sectionResults.count { it.entry == null } }
    
    val meritCount = remember(sectionResults) { sectionResults.count { it.entry != null && it.entry.failedCount == 0 } }
    val firstCount = remember(sectionResults) { sectionResults.count { it.entry != null && it.entry.failedCount == 1 } }
    val secondCount = remember(sectionResults) { sectionResults.count { it.entry != null && it.entry.failedCount == 2 } }
    val specialCount = remember(sectionResults) { sectionResults.count { it.entry != null && it.entry.failedCount >= 3 } }
    
    var showOnlyNotStarted by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ConsiderationCategory?>(null) }
    var focusedStudentRoll by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(currentSection) {
        selectedCategory = null
        showOnlyNotStarted = false
        focusedStudentRoll = null
    }

    val rankMap = remember(sectionResults) {
        val enteredStudents = sectionResults
            .filter { it.entry != null }
            .sortedWith(
                compareByDescending<StudentResult> { it.entry?.totalMarks ?: 0 }
                    .thenBy { it.roster.roll }
            )
        enteredStudents.mapIndexed { index, student ->
            student.roster.roll to (index + 1)
        }.toMap()
    }

    val displayResults = remember(results, showOnlyNotStarted, selectedCategory) {
        val filtered = when {
            showOnlyNotStarted -> results.filter { it.entry == null }
            selectedCategory == ConsiderationCategory.MERIT -> results.filter { it.entry != null && it.entry.failedCount == 0 }
            selectedCategory == ConsiderationCategory.FIRST -> results.filter { it.entry != null && it.entry.failedCount == 1 }
            selectedCategory == ConsiderationCategory.SECOND -> results.filter { it.entry != null && it.entry.failedCount == 2 }
            selectedCategory == ConsiderationCategory.SPECIAL -> results.filter { it.entry != null && it.entry.failedCount >= 3 }
            else -> results
        }
        if (selectedCategory != null) {
            filtered.sortedWith(
                compareByDescending<StudentResult> { it.entry?.totalMarks ?: 0 }
                    .thenBy { it.roster.roll }
            )
        } else {
            filtered
        }
    }

    val isDark = isSystemInDarkTheme()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showSnackbarMsg: (String) -> Unit = remember {
        { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
    }
    
    var showExitDialog by remember { mutableStateOf(false) }
    var isActionsExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit App") },
            text = { Text("Are you sure you want to exit the app?") },
            confirmButton = { 
                TextButton(
                    onClick = { 
                        showExitDialog = false
                        (context as? android.app.Activity)?.finish() 
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = { 
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Class 9 Result Entry",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            if (currentSection != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Section $currentSection",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Synced badge
                            Surface(
                                color = if (isDark) Color(0xFF102A43) else Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF1D4ED8) else Color(0xFF90CAF9))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (isDark) Color(0xFF60A5FA) else Color(0xFF1976D2), CircleShape)
                                    )
                                    Text(
                                        text = "Synced: $syncedCount",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF0D47A1)
                                    )
                                }
                            }
                            
                            // Draft badge
                            Surface(
                                color = if (isDark) Color(0xFF38230B) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFFB45309) else Color(0xFFFFB74D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (isDark) Color(0xFFFBBF24) else Color(0xFFF57C00), CircleShape)
                                    )
                                    Text(
                                        text = "Drafts: $draftCount",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFFE65100)
                                    )
                                }
                            }

                            // Pending badge
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Pending: $notStartedCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Collapsible arrow button (‹ when hidden, › when expanded)
                        Surface(
                            shape = CircleShape,
                            color = if (isActionsExpanded) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isActionsExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
                            ),
                            tonalElevation = 2.dp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            IconButton(
                                onClick = { isActionsExpanded = !isActionsExpanded },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("toggle_actions_arrow_button")
                            ) {
                                Icon(
                                    imageVector = if (isActionsExpanded) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                                    contentDescription = if (isActionsExpanded) "Hide tools" else "Show tools",
                                    tint = if (isActionsExpanded) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Smoothly animated action icons
                        AnimatedVisibility(
                            visible = isActionsExpanded,
                            enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(
                                expandFrom = Alignment.Start,
                                animationSpec = tween(260)
                            ),
                            exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(
                                shrinkTowards = Alignment.Start,
                                animationSpec = tween(220)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val csvBuilder = java.lang.StringBuilder()
                                        csvBuilder.append("Roll,Name,Total Marks,Failed Count,GPA\n")
                                        results.forEach { res ->
                                            val entry = res.entry
                                            if (entry != null) {
                                                val gpaStr = entry.gpa?.toString() ?: ""
                                                csvBuilder.append("${res.roster.roll},\"${res.roster.name}\",${entry.totalMarks},${entry.failedCount},$gpaStr\n")
                                            }
                                        }
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TITLE, "Section $currentSection Results")
                                            putExtra(Intent.EXTRA_TEXT, csvBuilder.toString())
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Export Section $currentSection"))
                                    },
                                    modifier = Modifier.size(38.dp).testTag("action_export_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Export", modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = onNavigateToCombinedResult,
                                    modifier = Modifier.size(38.dp).testTag("action_combined_button")
                                ) {
                                    Icon(Icons.Default.Assessment, contentDescription = "Combined Result", modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.refreshData() },
                                    modifier = Modifier.size(38.dp).testTag("action_refresh_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Roster", modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.syncNow() },
                                    modifier = Modifier.size(38.dp).testTag("action_sync_button")
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync Now", modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = onNavigateToSettings,
                                    modifier = Modifier.size(38.dp).testTag("action_settings_button")
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
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

            // Interactive Compact Header for Section Selection, Live Consideration Summary & Search
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)) {
                    // Row 1: Teacher Name + Compact Missing Filter Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val teacherName = if (currentSection == "A") "জনাব মেজবা উদ্দিন" else if (currentSection == "B") "জনাব অনন্য দেবনাথ" else ""
                        if (teacherName.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "শ্রেণি শিক্ষক: $teacherName",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    if (showOnlyNotStarted) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (showOnlyNotStarted) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(11.dp)
                                )
                                .clickable {
                                    showOnlyNotStarted = !showOnlyNotStarted
                                    if (showOnlyNotStarted) selectedCategory = null
                                }
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(
                                            if (showOnlyNotStarted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = "Missing: $notStartedCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                                    color = if (showOnlyNotStarted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Row 2: Fixed Live Consideration Category Summary Bar (মেধা, ১ম, ২য়, বিশেষ)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CategoryCounterBadge(
                            title = "মেধা",
                            count = meritCount,
                            isSelected = selectedCategory == ConsiderationCategory.MERIT,
                            containerColor = if (isDark) Color(0xFF1B3822) else Color(0xFFE8F5E9),
                            contentColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF1B5E20),
                            selectedContainerColor = Color(0xFF2E7D32),
                            selectedContentColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = if (selectedCategory == ConsiderationCategory.MERIT) null else ConsiderationCategory.MERIT
                                if (selectedCategory != null) showOnlyNotStarted = false
                            }
                        )
                        CategoryCounterBadge(
                            title = "১ম",
                            count = firstCount,
                            isSelected = selectedCategory == ConsiderationCategory.FIRST,
                            containerColor = if (isDark) Color(0xFF102A43) else Color(0xFFE1F5FE),
                            contentColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF0277BD),
                            selectedContainerColor = Color(0xFF0288D1),
                            selectedContentColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = if (selectedCategory == ConsiderationCategory.FIRST) null else ConsiderationCategory.FIRST
                                if (selectedCategory != null) showOnlyNotStarted = false
                            }
                        )
                        CategoryCounterBadge(
                            title = "২য়",
                            count = secondCount,
                            isSelected = selectedCategory == ConsiderationCategory.SECOND,
                            containerColor = if (isDark) Color(0xFF382D16) else Color(0xFFFFF8E1),
                            contentColor = if (isDark) Color(0xFFFFCC80) else Color(0xFFE65100),
                            selectedContainerColor = Color(0xFFF57C00),
                            selectedContentColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = if (selectedCategory == ConsiderationCategory.SECOND) null else ConsiderationCategory.SECOND
                                if (selectedCategory != null) showOnlyNotStarted = false
                            }
                        )
                        CategoryCounterBadge(
                            title = "বিশেষ",
                            count = specialCount,
                            isSelected = selectedCategory == ConsiderationCategory.SPECIAL,
                            containerColor = if (isDark) Color(0xFF3B1629) else Color(0xFFFCE4EC),
                            contentColor = if (isDark) Color(0xFFF48FB1) else Color(0xFF880E4F),
                            selectedContainerColor = Color(0xFFC2185B),
                            selectedContentColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = if (selectedCategory == ConsiderationCategory.SPECIAL) null else ConsiderationCategory.SPECIAL
                                if (selectedCategory != null) showOnlyNotStarted = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Row 3: Compact Instant Search Bar
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Search Roll or Name...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.setSearchQuery("") },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear Search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = selectedCategory != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val catTitle = when (selectedCategory) {
                                    ConsiderationCategory.MERIT -> "মেধাতালিকা (০ ফেল)"
                                    ConsiderationCategory.FIRST -> "১ম বিবেচনা (১ ফেল)"
                                    ConsiderationCategory.SECOND -> "২য় বিবেচনা (২ ফেল)"
                                    ConsiderationCategory.SPECIAL -> "বিশেষ বিবেচনা (৩+ ফেল)"
                                    null -> ""
                                }
                                Text(
                                    text = "$catTitle • নম্বর অনুসারে সাজানো (Rank #)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "✕ রিসেট",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { selectedCategory = null }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (displayResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when {
                                        selectedCategory != null -> "এই বিবেচনা ক্যাটাগরিতে কোনো শিক্ষার্থী নেই"
                                        showOnlyNotStarted -> "সব শিক্ষার্থীর ফলাফল ইতিমধ্যে এন্ট্রি করা হয়েছে"
                                        searchQuery.isNotBlank() -> "কোনো মিল পাওয়া যায়নি"
                                        else -> "কোনো শিক্ষার্থী পাওয়া যায়নি"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (selectedCategory != null || showOnlyNotStarted || searchQuery.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = {
                                        selectedCategory = null
                                        showOnlyNotStarted = false
                                        viewModel.setSearchQuery("")
                                    }) {
                                        Text("ফিল্টার রিসেট করুন")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = displayResults,
                        key = { _, it -> it.roster.roll },
                        contentType = { _, _ -> "student_row" }
                    ) { index, result ->
                        val serialRank = if (selectedCategory != null) rankMap[result.roster.roll] else null
                        QuickEditStudentRow(
                            result = result,
                            serialRank = serialRank,
                            isTargetFocus = focusedStudentRoll == result.roster.roll,
                            onFocusConsumed = {
                                if (focusedStudentRoll == result.roster.roll) {
                                    focusedStudentRoll = null
                                }
                            },
                            hasPrevious = index > 0,
                            hasNext = index < displayResults.size - 1,
                            onNavigatePrev = {
                                if (index > 0) {
                                    val prevRoll = displayResults[index - 1].roster.roll
                                    focusedStudentRoll = prevRoll
                                    scope.launch {
                                        listState.animateScrollToItem((index - 1).coerceAtLeast(0))
                                    }
                                }
                            },
                            onAutoAdvance = {
                                if (index < displayResults.size - 1) {
                                    val nextRoll = displayResults[index + 1].roster.roll
                                    focusedStudentRoll = nextRoll
                                    scope.launch {
                                        listState.animateScrollToItem(index + 1)
                                    }
                                } else {
                                    showSnackbarMsg("শাখার শেষ শিক্ষার্থীর ফলাফল সম্পন্ন হয়েছে! 🎉")
                                }
                            },
                            onSave = viewModel::saveEntry,
                            showSnackbar = showSnackbarMsg
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditStudentRow(
    result: StudentResult,
    serialRank: Int? = null,
    isTargetFocus: Boolean = false,
    onFocusConsumed: () -> Unit = {},
    hasPrevious: Boolean = false,
    hasNext: Boolean = false,
    onNavigatePrev: () -> Unit = {},
    onAutoAdvance: () -> Unit = {},
    onSave: (Int, Int, Int, Double?) -> Unit,
    showSnackbar: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
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

    val marksFocusRequester = remember { FocusRequester() }
    val gpaFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isTargetFocus) {
        if (isTargetFocus) {
            delay(60)
            try {
                marksFocusRequester.requestFocus()
            } catch (_: Exception) {}
            onFocusConsumed()
        }
    }

    LaunchedEffect(result.entry) {
        if (!isEdited && result.entry != null) {
            val newTm = result.entry.totalMarks.toString()
            val newFc = result.entry.failedCount
            val newGpa = result.entry.gpa?.let { gpaValue ->
                var str = gpaValue.toString()
                if (str.endsWith(".0")) str = str.substring(0, str.length - 2)
                str.replace(".", "")
            } ?: ""
            if (totalMarks != newTm) totalMarks = newTm
            if (failedCount != newFc) failedCount = newFc
            if (gpaDigits != newGpa) gpaDigits = newGpa
        }
    }

    if (result.entry?.syncState == SyncState.ERROR) {
        LaunchedEffect(Unit) {
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

    val commitSaveNow: () -> Boolean = {
        val tm = totalMarks.toIntOrNull()
        val fc = failedCount
        val g = gpaDouble

        if (tm != null && !isMarksInvalid && fc != null) {
            if (fc > 0 || (fc == 0 && g != null && !isGpaInvalid)) {
                onSave(result.roster.roll, tm, fc, if (fc == 0) g else null)
                isEdited = false
                saveStatus = "saved"
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun handleAdvance() {
        if (isEdited) {
            val tm = totalMarks.toIntOrNull()
            if (isMarksInvalid) {
                showSnackbar("সঠিক মোট নম্বর দিন (০-১১৫০)")
                return
            }
            if (failedCount == null) {
                if (tm != null) {
                    failedCount = 0
                    isEdited = true
                    gpaFocusRequester.requestFocus()
                    showSnackbar("ফেল বিষয় ০ (পাস) ধরা হয়েছে — জিপিএ দিন")
                    return
                }
            } else if (failedCount == 0 && (gpaDouble == null || isGpaInvalid)) {
                gpaFocusRequester.requestFocus()
                showSnackbar("অনুগ্রহ করে বৈধ জিপিএ দিন (০.০ - ৫.০)")
                return
            }
            commitSaveNow()
        }
        onAutoAdvance()
    }

    // Debounced Auto-Save Loop (800ms) - ONLY active when user has modified data, avoiding coroutine churn during scrolling
    if (isEdited) {
        LaunchedEffect(totalMarks, failedCount, gpaDigits) {
            if (isMarksInvalid || isGpaInvalid) return@LaunchedEffect

            val tm = totalMarks.toIntOrNull()
            val fc = failedCount
            val g = gpaDouble

            if (tm != null && fc != null) {
                if (fc > 0 || (fc == 0 && g != null)) {
                    saveStatus = "saving"
                    delay(800)
                    onSave(result.roster.roll, tm, fc, if (fc == 0) g else null)
                    isEdited = false
                    saveStatus = "saved"
                    delay(2000)
                    if (saveStatus == "saved") {
                        saveStatus = "idle"
                    }
                }
            }
        }
    }

    // Keep latest edit values for onDispose save without creating Triple/Pair allocations on every frame
    val onSaveLatest by rememberUpdatedState(onSave)
    val latestValues = remember(result.roster.roll) {
        object {
            var edited = false
            var tmStr = ""
            var fc: Int? = null
            var g: Double? = null
            var marksInvalid = false
            var gpaInvalid = false
        }
    }
    latestValues.edited = isEdited
    latestValues.tmStr = totalMarks
    latestValues.fc = failedCount
    latestValues.g = gpaDouble
    latestValues.marksInvalid = isMarksInvalid
    latestValues.gpaInvalid = isGpaInvalid
    
    DisposableEffect(result.roster.roll) {
        onDispose {
            if (latestValues.edited && !latestValues.marksInvalid && !latestValues.gpaInvalid) {
                val tm = latestValues.tmStr.toIntOrNull()
                val fc = latestValues.fc
                val g = latestValues.g
                if (tm != null && fc != null) {
                    if (fc > 0 || (fc == 0 && g != null)) {
                        onSaveLatest(result.roster.roll, tm, fc, if (fc == 0) g else null)
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (serialRank != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "#$serialRank",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "Roll ${result.roster.roll} — ${result.roster.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Status Indicator Badge (Saving... / Saved / Synced / Draft / Not Started)
                    // Status Indicator Badge + Auto-Advance Action Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (saveStatus) {
                            "saving" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                val (bgColor, textColor, borderColor, dotColor, text) = when (result.entry?.syncState) {
                                    SyncState.SYNCED -> if (isDark) {
                                        Quint(Color(0xFF102A43), Color(0xFF93C5FD), Color(0xFF1D4ED8), Color(0xFF60A5FA), "Synced")
                                    } else {
                                        Quint(Color(0xFFE3F2FD), Color(0xFF0D47A1), Color(0xFF90CAF9), Color(0xFF1976D2), "Synced")
                                    }
                                    SyncState.DRAFT -> if (isDark) {
                                        Quint(Color(0xFF38230B), Color(0xFFFDE68A), Color(0xFFB45309), Color(0xFFFBBF24), "Draft")
                                    } else {
                                        Quint(Color(0xFFFFF3E0), Color(0xFFBF360C), Color(0xFFFFB74D), Color(0xFFF57C00), "Draft")
                                    }
                                    SyncState.ERROR -> if (isDark) {
                                        Quint(Color(0xFF3B1219), Color(0xFFFECDD3), Color(0xFFBE123C), Color(0xFFF43F5E), "Sync Error")
                                    } else {
                                        Quint(Color(0xFFFFEBEE), Color(0xFFB71C1C), Color(0xFFEF9A9A), Color(0xFFD32F2F), "Sync Error")
                                    }
                                    null -> Quint(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        MaterialTheme.colorScheme.outline,
                                        "Not Started"
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(dotColor, CircleShape)
                                        )
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }

                        // Auto-Advance Next Action Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isEdited) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                1.dp,
                                if (isEdited) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { handleAdvance() }
                                .testTag("auto_advance_button_${result.roster.roll}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (isEdited) "Save & Next" else "Next",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isEdited) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Student",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isEdited) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (failedCount == null || failedCount == 0) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (failedCount == null) {
                                failedCount = 0
                                isEdited = true
                                gpaFocusRequester.requestFocus()
                            } else if (failedCount == 0) {
                                gpaFocusRequester.requestFocus()
                            } else {
                                handleAdvance()
                            }
                        },
                        onDone = {
                            handleAdvance()
                        }
                    ),
                    modifier = Modifier
                        .width(120.dp)
                        .focusRequester(marksFocusRequester)
                        .testTag("marks_input_${result.roster.roll}"),
                    singleLine = true,
                    isError = isMarksInvalid,
                    supportingText = if (isMarksInvalid) { { Text("0-1150") } } else null
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Failed Subjects:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Efficient 5x2 grid of numeric selectors (0-9) - avoids heavy dynamic FlowRow measure cycles
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (0..4).forEach { number ->
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (5..9).forEach { number ->
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
            }

            AnimatedVisibility(
                visible = failedCount == 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "GPA",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGpaInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GpaInputBoxes(
                        value = gpaDigits,
                        onValueChange = { 
                            gpaDigits = it
                            isEdited = true
                        },
                        isError = isGpaInvalid,
                        firstBoxFocusRequester = gpaFocusRequester,
                        onComplete = {
                            handleAdvance()
                        }
                    )
                    if (isGpaInvalid) {
                        Text(
                            text = "Valid GPA: 0.0 to 5.0",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
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
    val backgroundColor = when {
        isSelected && number == 0 -> Color(0xFFE8F5E9)
        isSelected -> Color(0xFFFFEBEE)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = when {
        isSelected && number == 0 -> Color(0xFF2E7D32)
        isSelected -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = if (isSelected) {
        if (number == 0) Color(0xFF2E7D32) else Color(0xFFC62828)
    } else Color.Transparent

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isSelected) Modifier.border(1.5.dp, borderColor, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

@Composable
fun GpaInputBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    firstBoxFocusRequester: FocusRequester? = null,
    onComplete: () -> Unit = {}
) {
    val focusRequesters = remember { List(3) { FocusRequester() } }
    val box0Requester = firstBoxFocusRequester ?: focusRequesters[0]
    var pendingFocus by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    if (pendingFocus != null) {
        LaunchedEffect(pendingFocus) {
            pendingFocus?.let {
                if (it == 0) box0Requester.requestFocus() else focusRequesters[it].requestFocus()
                pendingFocus = null
            }
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
            focusRequester = box0Requester,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            onValueChange = { newStr ->
                if (newStr.contains('.')) {
                    pendingFocus = 1
                } else {
                    val digit = newStr.replace(Regex("[^\\d]"), "").lastOrNull()
                    if (digit != null) {
                        setDigit(0, digit)
                        pendingFocus = 1
                    } else {
                        clearDigit(0)
                    }
                }
            },
            onImeAction = { pendingFocus = 1 },
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
            imeAction = ImeAction.Next,
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
            onImeAction = { pendingFocus = 2 },
            onBackspace = { pendingFocus = 0 }
        )
        Spacer(modifier = Modifier.width(8.dp))
        GpaSingleBox(
            char = value.getOrNull(2),
            isError = isError,
            focusRequester = focusRequesters[2],
            imeAction = ImeAction.Done,
            onValueChange = { newStr ->
                val digit = newStr.replace(Regex("[^\\d]"), "").lastOrNull()
                if (digit != null) {
                    setDigit(2, digit)
                    scope.launch {
                        delay(250)
                        onComplete()
                    }
                } else {
                    clearDigit(2)
                    pendingFocus = 1
                }
            },
            onImeAction = {
                onComplete()
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
    keyboardType: KeyboardType = KeyboardType.Number,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
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
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
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
