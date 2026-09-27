package com.example.eschedule

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// --- Bottom-tab destinations ---
@Serializable data object HomeRoute : NavKey
@Serializable data object ClassesRoute : NavKey
@Serializable data object ScanRoute : NavKey
@Serializable data object RemindersRoute : NavKey
@Serializable data object SettingsRoute : NavKey

// --- Detail / modal destinations ---
@Serializable data class ViewReminderRoute(val reminderId: String) : NavKey
@Serializable data class AddReminderRoute(val prefillDate: String = "") : NavKey
@Serializable data class EditReminderRoute(val reminderId: String) : NavKey

@Serializable data class ViewClassRoute(val classId: String) : NavKey
@Serializable data class AddClassRoute(val fromScan: Boolean = false) : NavKey
@Serializable data class EditClassRoute(val classId: String) : NavKey
