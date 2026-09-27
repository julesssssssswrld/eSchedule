package com.example.eschedule.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.eschedule.ui.classes.ClassSheet
import com.example.eschedule.ui.classes.ClassesScreen
import com.example.eschedule.ui.classes.ViewClassSheet
import com.example.eschedule.ui.components.BottomNavBar
import com.example.eschedule.ui.components.TabRoute
import com.example.eschedule.ui.home.HomeScreen
import com.example.eschedule.ui.reminders.AddReminderSheet
import com.example.eschedule.ui.reminders.RemindersScreen
import com.example.eschedule.ui.reminders.ViewReminderSheet
import com.example.eschedule.ui.scan.ScanScreen
import com.example.eschedule.ui.settings.SettingsScreen

/**
 * Root composable.
 * Hosts the bottom navigation bar and swaps tab content.
 * Modal sheets are layered on top of the tab content.
 */
@Composable
fun AppScaffold() {
    var currentTab by remember { mutableStateOf<TabRoute>(TabRoute.Home) }

    // Sheet state holders — null = closed
    var showAddReminder by remember { mutableStateOf(false) }
    var viewReminderId by remember { mutableStateOf<String?>(null) }
    var editReminderId by remember { mutableStateOf<String?>(null) }

    var showAddClass by remember { mutableStateOf(false) }
    var viewClassId by remember { mutableStateOf<String?>(null) }
    var editClassId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onScanClick = { currentTab = TabRoute.Home /* Scan is its own flow */ },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentTab) {
                TabRoute.Home -> HomeScreen(
                    onClassClick = { viewClassId = it },
                    onReminderMoreClick = { viewReminderId = it },
                )
                TabRoute.Classes -> ClassesScreen(
                    onClassClick = { viewClassId = it },
                    onAddClass = { showAddClass = true },
                )
                TabRoute.Reminders -> RemindersScreen(
                    onAddReminder = { showAddReminder = true },
                    onReminderMoreClick = { viewReminderId = it },
                )
                TabRoute.Settings -> SettingsScreen(
                    onItemClick = { /* future: navigate to sub-settings */ },
                )
            }
        }
    }

    // ─── Reminder sheets ─────────────────────────────────────────────────────────
    if (showAddReminder) {
        AddReminderSheet(
            onDismiss = { showAddReminder = false },
            onSave = { _ -> showAddReminder = false },
        )
    }
    viewReminderId?.let { id ->
        ViewReminderSheet(
            reminderId = id,
            onDismiss = { viewReminderId = null },
            onEdit = {
                editReminderId = id
                viewReminderId = null
            },
            onDelete = { viewReminderId = null },
        )
    }
    editReminderId?.let { id ->
        AddReminderSheet(
            sheetTitle = "Edit Reminder",
            initialTitle = "Pag laba", // Will be pulled from repo later
            onDismiss = { editReminderId = null },
            onSave = { _ -> editReminderId = null },
        )
    }

    // ─── Class sheets ─────────────────────────────────────────────────────────────
    if (showAddClass) {
        ClassSheet(
            sheetTitle = "Add Class",
            onDismiss = { showAddClass = false },
            onSave = { _, _ -> showAddClass = false },
        )
    }
    viewClassId?.let { id ->
        ViewClassSheet(
            classId = id,
            onDismiss = { viewClassId = null },
            onEdit = {
                editClassId = id
                viewClassId = null
            },
            onDelete = { viewClassId = null },
        )
    }
    editClassId?.let { id ->
        ClassSheet(
            sheetTitle = "Edit Class",
            initialTitle = "IT102 - Mobile Application Development",
            initialSelectedDays = setOf(1, 3), // Tue & Thu
            onDismiss = { editClassId = null },
            onSave = { _, _ -> editClassId = null },
        )
    }
}
