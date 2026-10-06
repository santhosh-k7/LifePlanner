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
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.LifePlannerTheme
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
    val currentTab by viewModel.currentTab.collectAsState()
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
        enabled = showQuickAdd || selectedTaskForDetail != null || taskToReschedule != null ||
                isFocusVisible || activeRunningRoutine != null || isSearchActive || showSettings
    ) {
        when {
            showQuickAdd -> viewModel.showQuickAdd.value = false
            selectedTaskForDetail != null -> viewModel.selectedTaskForDetail.value = null
            taskToReschedule != null -> viewModel.taskToReschedule.value = null
            isFocusVisible -> viewModel.closeFocusScreen()
            activeRunningRoutine != null -> viewModel.closeRoutineRunner()
            isSearchActive -> viewModel.isSearchActive.value = false
            showSettings -> viewModel.showSettingsDialog.value = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✨ LifePlanner",
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
                            tint = MaterialTheme.colorScheme.primary
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.TODAY,
                    onClick = { viewModel.currentTab.value = AppTab.TODAY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.TODAY) Icons.Filled.Today else Icons.Outlined.Today,
                            contentDescription = "Today Tab"
                        )
                    },
                    label = { Text("Today") },
                    modifier = Modifier.testTag("nav_tab_today")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.UPCOMING,
                    onClick = { viewModel.currentTab.value = AppTab.UPCOMING },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.UPCOMING) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Upcoming Tab"
                        )
                    },
                    label = { Text("Upcoming") },
                    modifier = Modifier.testTag("nav_tab_upcoming")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.ROUTINES,
                    onClick = { viewModel.currentTab.value = AppTab.ROUTINES },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.ROUTINES) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                            contentDescription = "Routines Tab"
                        )
                    },
                    label = { Text("Routines") },
                    modifier = Modifier.testTag("nav_tab_routines")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.REVIEW,
                    onClick = { viewModel.currentTab.value = AppTab.REVIEW },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.REVIEW) Icons.Filled.Insights else Icons.Outlined.Insights,
                            contentDescription = "Review Tab"
                        )
                    },
                    label = { Text("Review") },
                    modifier = Modifier.testTag("nav_tab_review")
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openQuickAdd() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(56.dp)
                    .testTag("fab_quick_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Add Task",
                    modifier = Modifier.size(28.dp)
                )
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
                AppTab.UPCOMING -> UpcomingScreen(viewModel = viewModel)
                AppTab.ROUTINES -> RoutinesHabitsScreen(viewModel = viewModel)
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
