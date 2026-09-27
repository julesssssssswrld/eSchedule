package com.example.eschedule.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.ui.components.EScheduleFab
import com.example.eschedule.ui.components.FilterChip
import com.example.eschedule.ui.components.PageTitle
import com.example.eschedule.ui.components.ReminderItemData
import com.example.eschedule.ui.components.ReminderListItem
import com.example.eschedule.ui.components.SectionHeader
import com.example.eschedule.ui.components.appShadow

private val FILTERS = listOf("All", "Today", "Week", "Month", "Completed")

@Composable
fun RemindersScreen(
    onAddReminder: () -> Unit,
    onReminderMoreClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeReminders = remember {
        listOf(
            ReminderItemData("r1", "Pag laba", "12:51 PM"),
            ReminderItemData("r2", "Do app dev project", "Tomorrow"),
            ReminderItemData("r3", "Make journal", "Sep 21"),
            ReminderItemData("r4", "Assassinate Jons", "Dec 17"),
        )
    }
    val completedReminders = remember {
        listOf(
            ReminderItemData("c1",  "Voiceover",          "10/18/24", isCompleted = true),
            ReminderItemData("c2",  "First Aid Video",    "10/16/24", isCompleted = true),
            ReminderItemData("c3",  "Tree planting",      "10/16/24", isCompleted = true),
            ReminderItemData("c4",  "Upaya im intro",     "10/9/24",  isCompleted = true),
            ReminderItemData("c5",  "NSTP",               "10/2/24",  isCompleted = true),
            ReminderItemData("c6",  "Intro",              "10/2/24",  isCompleted = true),
            ReminderItemData("c7",  "Comprog",            "9/25/24",  isCompleted = true),
            ReminderItemData("c8",  "Assignment sa NSTP", "9/18/24",  isCompleted = true),
            ReminderItemData("c9",  "Edit vid",           "9/2/24",   isCompleted = true),
            ReminderItemData("c10", "PerDev",             "11/21/23", isCompleted = true),
            ReminderItemData("c11", "Sex kit nigga",      "11/21/23", isCompleted = true),
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var checkedIds by remember { mutableStateOf(setOf<String>()) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            PageTitle(title = "Reminders")

            Spacer(Modifier.height(4.dp))

            // Filter chips — horizontally scrollable so they never wrap
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FILTERS.forEach { filter ->
                    FilterChip(
                        label = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Active reminders card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .appShadow(cornerRadius = 14.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Column {
                    activeReminders.forEach { reminder ->
                        ReminderListItem(
                            data = reminder.copy(isCompleted = checkedIds.contains(reminder.id)),
                            onCheckedChange = { checked ->
                                checkedIds = if (checked) checkedIds + reminder.id
                                else checkedIds - reminder.id
                            },
                            onMoreClick = { onReminderMoreClick(reminder.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Completed section
            if (selectedFilter == "All" || selectedFilter == "Completed") {
                SectionHeader(
                    title = "Completed",
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appShadow(cornerRadius = 14.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Column {
                        completedReminders.forEach { reminder ->
                            ReminderListItem(
                                data = reminder,
                                onCheckedChange = {},
                                onMoreClick = { onReminderMoreClick(reminder.id) },
                                showMoreButton = false,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(96.dp))
        }

        EScheduleFab(
            onClick = onAddReminder,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp),
        )
    }
}
