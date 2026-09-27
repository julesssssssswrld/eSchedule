package com.example.eschedule.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eschedule.ui.components.ClassCard
import com.example.eschedule.ui.components.ClassCardData
import com.example.eschedule.ui.components.ReminderItemData
import com.example.eschedule.ui.components.ReminderListItem
import com.example.eschedule.ui.components.SectionHeader
import com.example.eschedule.ui.components.appShadow

@Composable
fun HomeScreen(
    onClassClick: (String) -> Unit,
    onReminderMoreClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stub data — will be replaced by ViewModel/repository later
    val currentClass = remember {
        ClassCardData(
            id = "it102",
            title = "IT102 - Mobile Application Development",
            timeRange = "8:00 AM - 9:00 AM",
            location = "CSFLD22",
        )
    }
    val upcomingClasses = remember {
        listOf(
            ClassCardData(
                id = "sia101",
                title = "SIA101 - System Integration and Architecture",
                timeRange = "10:00 AM - 1:00 PM",
                location = "L4",
                isExpandable = true,
            ),
        )
    }
    val reminders = remember {
        listOf(
            ReminderItemData(id = "r1", title = "Pag laba", dueLabel = "12:51 PM"),
            ReminderItemData(id = "r2", title = "Do app dev project", dueLabel = "Tomorrow"),
            ReminderItemData(id = "r3", title = "Make journal", dueLabel = "Sep 21"),
            ReminderItemData(id = "r4", title = "Assassinate Jons", dueLabel = "Dec 17"),
        )
    }
    var checkedIds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp), // space between sections
    ) {
        // ── Current class ──────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "Current Class")
            ClassCard(data = currentClass, onClick = { onClassClick(currentClass.id) })
        }

        // ── Upcoming classes ───────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "Upcoming Classes")
            upcomingClasses.forEach { cls ->
                ClassCard(
                    data = cls,
                    onClick = { onClassClick(cls.id) },
                )
            }
        }

        // ── Reminders ──────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "Reminders")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .appShadow(cornerRadius = 14.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Column {
                    reminders.forEach { reminder ->
                        ReminderListItem(
                            data = reminder.copy(isCompleted = checkedIds.contains(reminder.id)),
                            onCheckedChange = { checked ->
                                checkedIds = if (checked) checkedIds + reminder.id
                                else checkedIds - reminder.id
                            },
                            onMoreClick = { onReminderMoreClick(reminder.id) },
                            showMoreButton = false,
                        )
                    }
                }
            }
        }

        // Bottom spacing so content clears the floating nav bar
        Spacer(Modifier.height(88.dp))
    }
}
