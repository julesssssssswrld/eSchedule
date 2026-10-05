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
import com.example.eschedule.ui.components.PageTitle
import com.example.eschedule.ui.components.ReminderItemData
import com.example.eschedule.ui.components.ReminderListItem
import com.example.eschedule.ui.components.SectionHeader
import com.example.eschedule.ui.components.appShadow

@Composable
fun HomeScreen(
    onClassClick: (String) -> Unit,
    onReminderMoreClick: (String) -> Unit,
    onReminderEditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            ),
            ClassCardData(
                id = "cs301",
                title = "CS301 - Operating Systems",
                timeRange = "2:00 PM - 3:30 PM",
                location = "CSFLD22",
            ),
            ClassCardData(
                id = "it201",
                title = "IT201 - Database Management",
                timeRange = "4:00 PM - 5:30 PM",
                location = "L2",
            ),
        )
    }
    val reminders = remember {
        listOf(
            ReminderItemData(id = "r1", title = "Pag laba",           dueLabel = "12:51 PM"),
            ReminderItemData(id = "r2", title = "Do app dev project", dueLabel = "Tomorrow"),
            ReminderItemData(id = "r3", title = "Make journal",        dueLabel = "Sep 21"),
            ReminderItemData(id = "r4", title = "Assassinate Jons",   dueLabel = "Dec 17"),
        )
    }
    var checkedIds by remember { mutableStateOf(setOf<String>()) }

    // Outer Column: title is FIXED (never scrolls), content scrolls below it
    Column(modifier = modifier.fillMaxSize()) {

        // ── Sticky page title ─────────────────────────────────────────────────
        PageTitle(
            title = "Dashboard",
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // ── Current class
            SectionHeader(title = "Current Class")
            Spacer(Modifier.height(8.dp))
            ClassCard(
                data = currentClass,
                onClick = { onClassClick(currentClass.id) },
            )

            Spacer(Modifier.height(16.dp))

            // ── Upcoming classes
            SectionHeader(title = "Upcoming Classes")
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                upcomingClasses.forEach { cls ->
                    ClassCard(
                        data = cls,
                        onClick = { onClassClick(cls.id) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Reminders
            SectionHeader(title = "Reminders")
            Spacer(Modifier.height(8.dp))
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
                            onEditClick = { onReminderEditClick(reminder.id) },
                            onDeleteClick = { /* TODO: hook to data layer */ },
                            showMoreButton = true,
                        )
                    }
                }
            }

            // Bottom spacing so content clears the floating nav bar
            Spacer(Modifier.height(120.dp))
        }
    }
}
