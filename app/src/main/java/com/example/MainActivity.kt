package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannels(this)

        setContent {
            val plannerViewModel: LifePlannerViewModel = viewModel()
            val themeMode by plannerViewModel.themeMode.collectAsState()

            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            LifePlannerTheme(darkTheme = isDark) {
                val context = LocalContext.current
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { /* Result handled */ }

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                LifePlannerApp(viewModel = plannerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifePlannerApp(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val currentTab by viewModel.currentTab.collectAsState()
    val showCreateTask by viewModel.showCreateTaskDialog.collectAsState()
    val createTaskDate by viewModel.createTaskInitialDate.collectAsState()
    val showQuickAdd by viewModel.showQuickAdd.collectAsState()
    val selectedTaskForDetail by viewModel.selectedTaskForDetail.collectAsState()
    val taskToReschedule by viewModel.taskToReschedule.collectAsState()
    val isFocusVisible by viewModel.isFocusScreenVisible.collectAsState()
    val activeRunningRoutine by viewModel.activeRunningRoutine.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()
    val showRoutineFinished by viewModel.showRoutineFinishedDialog.collectAsState()

    // Handle back button for sub-sheets
    BackHandler(
        enabled = showCreateTask || showQuickAdd || selectedTaskForDetail != null || taskToReschedule != null ||
                isFocusVisible || activeRunningRoutine != null || isSearchActive || showSettings
    ) {
        when {
            showCreateTask -> viewModel.closeCreateTask()
            showQuickAdd -> viewModel.showQuickAdd.value = false
            selectedTaskForDetail != null -> viewModel.selectedTaskForDetail.value = null
            taskToReschedule != null -> viewModel.taskToReschedule.value = null
            isFocusVisible -> viewModel.closeFocusScreen()
            activeRunningRoutine != null -> viewModel.closeRoutineRunner()
            isSearchActive -> viewModel.isSearchActive.value = false
            showSettings -> viewModel.showSettingsDialog.value = false
        }
    }

    val glassNavContainerColor = if (isDark) Color(0xDD0D0F18) else Color(0xEEFFFFFF)
    val glassBorderBrush = if (isDark) {
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            listOf(Color.White.copy(alpha = 0.25f), Color(0x336366F1), Color.White.copy(alpha = 0.1f))
        )
    } else {
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            listOf(Color.White.copy(alpha = 0.95f), Color(0x444F46E5), Color.White.copy(alpha = 0.6f))
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .liquidGlassBackground(isDark),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6366F1).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Text(
                            text = "LifePlanner",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Quick Focus button
                    IconButton(
                        onClick = { viewModel.openFocusMode(null) },
                        modifier = Modifier.testTag("top_bar_focus_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "Open Focus Timer",
                            tint = Color(0xFF6366F1)
                        )
                    }

                    // Review / Reflection button
                    IconButton(
                        onClick = { viewModel.currentTab.value = AppTab.REVIEW },
                        modifier = Modifier.testTag("top_bar_review_btn")
                    ) {
                        Icon(
                            imageVector = if (currentTab == AppTab.REVIEW) Icons.Filled.Insights else Icons.Outlined.Insights,
                            contentDescription = "Weekly Review",
                            tint = if (currentTab == AppTab.REVIEW) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Search button
                    IconButton(
                        onClick = { viewModel.isSearchActive.value = true },
                        modifier = Modifier.testTag("top_bar_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search Tasks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Settings button
                    IconButton(
                        onClick = { viewModel.showSettingsDialog.value = true },
                        modifier = Modifier.testTag("top_bar_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = glassNavContainerColor
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        brush = glassBorderBrush,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("main_bottom_nav")
                    .drawBehind {
                        drawLine(
                            brush = glassBorderBrush,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                containerColor = glassNavContainerColor,
                tonalElevation = 0.dp
            ) {
                // Tab 1: Today
                NavigationBarItem(
                    selected = currentTab == AppTab.TODAY,
                    onClick = { viewModel.currentTab.value = AppTab.TODAY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.TODAY) Icons.Filled.Today else Icons.Outlined.Today,
                            contentDescription = "Today Tab"
                        )
                    },
                    label = { Text("Today", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_today")
                )

                // Tab 2: Notepad (Urgent / Important Notes)
                NavigationBarItem(
                    selected = currentTab == AppTab.NOTEPAD,
                    onClick = { viewModel.currentTab.value = AppTab.NOTEPAD },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.NOTEPAD) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                            contentDescription = "Notepad Tab"
                        )
                    },
                    label = { Text("Notepad", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_notepad")
                )

                // Tab 3: LeetCode Progress
                NavigationBarItem(
                    selected = currentTab == AppTab.LEETCODE,
                    onClick = { viewModel.currentTab.value = AppTab.LEETCODE },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.LEETCODE) Icons.Filled.Code else Icons.Outlined.Code,
                            contentDescription = "LeetCode Tab"
                        )
                    },
                    label = { Text("LeetCode", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_leetcode")
                )

                // Tab 4: Upcoming
                NavigationBarItem(
                    selected = currentTab == AppTab.UPCOMING,
                    onClick = { viewModel.currentTab.value = AppTab.UPCOMING },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.UPCOMING) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Upcoming Tab"
                        )
                    },
                    label = { Text("Upcoming", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_upcoming")
                )

                // Tab 5: Routines
                NavigationBarItem(
                    selected = currentTab == AppTab.ROUTINES,
                    onClick = { viewModel.currentTab.value = AppTab.ROUTINES },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.ROUTINES) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                            contentDescription = "Routines Tab"
                        )
                    },
                    label = { Text("Routines", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_routines")
                )
            }
        },
        floatingActionButton = {
            if (currentTab != AppTab.NOTEPAD && currentTab != AppTab.LEETCODE) {
                FloatingActionButton(
                    onClick = { viewModel.openCreateTask() },
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(56.dp)
                        .testTag("fab_create_task")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Task",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.TODAY -> TodayScreen(viewModel = viewModel)
                AppTab.NOTEPAD -> NotepadScreen(viewModel = viewModel)
                AppTab.LEETCODE -> LeetCodeScreen(viewModel = viewModel)
                AppTab.UPCOMING -> UpcomingScreen(viewModel = viewModel)
                AppTab.ROUTINES -> RoutinesScreen(viewModel = viewModel)
                AppTab.REVIEW -> ReviewScreen(viewModel = viewModel)
            }

            // Full-screen overlay: Focus Mode
            AnimatedVisibility(
                visible = isFocusVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FocusModeScreen(viewModel = viewModel)
            }
        }
    }

    // Complete Task Filling Page Modal
    if (showCreateTask) {
        CreateTaskDialog(
            initialDate = createTaskDate,
            viewModel = viewModel,
            onDismiss = { viewModel.closeCreateTask() }
        )
    }

    // Quick Add Modal Bottom Sheet
    if (showQuickAdd) {
        QuickAddBottomSheet(viewModel = viewModel)
    }

    // Task Detail Dialog
    selectedTaskForDetail?.let { taskWithSubtasks ->
        TaskDetailDialog(
            taskWithSubtasks = taskWithSubtasks,
            viewModel = viewModel,
            onDismiss = { viewModel.selectedTaskForDetail.value = null }
        )
    }

    // Reschedule Bottom Sheet
    taskToReschedule?.let { task ->
        RescheduleDialog(
            task = task,
            viewModel = viewModel,
            onDismiss = { viewModel.taskToReschedule.value = null }
        )
    }

    // Search Dialog
    if (isSearchActive) {
        SearchDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.isSearchActive.value = false }
        )
    }

    // Settings Dialog
    if (showSettings) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showSettingsDialog.value = false }
        )
    }

    // Routine Runner Dialog
    activeRunningRoutine?.let { routineWithSteps ->
        val stepIdx by viewModel.currentRoutineStepIndex.collectAsState()
        val secRemaining by viewModel.routineStepRemainingSeconds.collectAsState()
        val isTimerRunning by viewModel.isRoutineTimerActive.collectAsState()

        RoutineRunnerDialog(
            routineWithSteps = routineWithSteps,
            currentStepIndex = stepIdx,
            secondsRemaining = secRemaining,
            isTimerRunning = isTimerRunning,
            onPauseResumeTimer = { viewModel.pauseResumeRoutineTimer() },
            onNextStep = { viewModel.nextRoutineStep() },
            onClose = { viewModel.closeRoutineRunner() }
        )
    }

    // Routine Celebration Dialog
    if (showRoutineFinished) {
        AlertDialog(
            onDismissRequest = { viewModel.closeRoutineRunner() },
            title = {
                Text(
                    text = "🎉 Routine Complete!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Great job completing your routine today! Every small win builds positive momentum.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.closeRoutineRunner() },
                    modifier = Modifier.testTag("dismiss_routine_celebration")
                ) {
                    Text("Continue")
                }
            }
        )
    }
}
