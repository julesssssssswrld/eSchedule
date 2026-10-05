package com.example.eschedule.ui.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.ui.components.AppIcons
import com.example.eschedule.ui.components.appShadow
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    onDismiss: () -> Unit,
    onSave: (title: String) -> Unit,
    initialTitle: String = "",
    sheetTitle: String = "Add Reminder",
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(initialTitle) }

    // ── Date state ────────────────────────────────────────────────────────────
    val cal = remember { Calendar.getInstance() }
    var dateText by remember {
        mutableStateOf(
            "${monthName(cal.get(Calendar.MONTH))} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    dateText = "${monthName(m)} $d, $y"
                    showDatePicker = false
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
            ).apply { setOnDismissListener { showDatePicker = false } }.show()
        }
    }

    // ── Time state ────────────────────────────────────────────────────────────
    var timeText by remember { mutableStateOf("9:00 AM") }
    var timeEnabled by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    if (showTimePicker) {
        LaunchedEffect(Unit) {
            TimePickerDialog(
                context,
                { _, h, m ->
                    val amPm = if (h < 12) "AM" else "PM"
                    val h12  = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
                    timeText = "$h12:${m.toString().padStart(2, '0')} $amPm"
                    showTimePicker = false
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false,
            ).apply { setOnDismissListener { showTimePicker = false } }.show()
        }
    }

    var locationText by remember { mutableStateOf("") }
    var notesText    by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sheetTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = UepBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                SaveButton(onClick = { onSave(title) })
            }

            TitleField(value = title, onValueChange = { title = it })

            // Options card
            Text(text = "Options", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            OptionsCard {
                // Date row — tapping opens DatePickerDialog
                OptionRow(
                    label = "Date",
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { showDatePicker = true },
                        ) {
                            Text(dateText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Spacer(Modifier.width(4.dp))
                            Icon(AppIcons.Expand, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    },
                )
                OptionDivider()
                // Time row — tapping the expand icon opens TimePickerDialog
                OptionRow(
                    label = "Time",
                    subLabel = if (timeEnabled) timeText else null,
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (timeEnabled) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { showTimePicker = true },
                                ) {
                                    Icon(AppIcons.Expand, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                            Switch(
                                checked = timeEnabled,
                                onCheckedChange = { timeEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor   = Color.White,
                                    checkedTrackColor   = UepBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = TextTertiary,
                                ),
                            )
                        }
                    },
                )
            }

            // Location
            Text(text = "Location", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            GrowingInputRow(
                icon = { Icon(AppIcons.Location, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                placeholder = "Add Location",
                value = locationText,
                onValueChange = { locationText = it },
            )

            // Details
            Text(text = "Details", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            GrowingInputRow(
                icon = { Icon(AppIcons.Pencil, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                placeholder = "Notes",
                value = notesText,
                onValueChange = { notesText = it },
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── View Reminder (read-only) bottom sheet ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReminderSheet(
    reminderId: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = UepBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconActionButton(icon = AppIcons.Delete, contentDescription = "Delete", onClick = onDelete)
                Spacer(Modifier.width(8.dp))
                IconActionButton(icon = AppIcons.Edit,   contentDescription = "Edit",   onClick = onEdit)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DividerColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(32.dp))
                    Column {
                        Text("Pag laba", style = MaterialTheme.typography.titleMedium)
                        Text("Today",    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("12:51 PM", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            Text(text = "Location", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ReadOnlyInputRow(
                icon = { Icon(AppIcons.Location, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                value = "Location",
            )

            Text(text = "Details", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ReadOnlyInputRow(
                icon = { Icon(AppIcons.Pencil, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                value = "Notes",
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Reusable sub-components ─────────────────────────────────────────────────

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(UepBlue, RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Save", style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun TitleField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 10.dp)
            .background(Color.White, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Blue accent pill
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(52.dp)
                .background(UepBlue, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
        )
        // Breathing room after the pill
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF1A1A1A),
            ),
            singleLine = true,
            cursorBrush = SolidColor(UepBlue),
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp, top = 14.dp, bottom = 14.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text("Title", color = TextTertiary, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun OptionsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 10.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun OptionRow(
    label: String,
    subLabel: String? = null,
    trailingContent: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (subLabel != null) {
                Text(text = subLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        trailingContent()
    }
}

@Composable
private fun OptionDivider() {
    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
}

// Auto-growing text input with white card + shadow
@Composable
private fun GrowingInputRow(
    icon: @Composable () -> Unit,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 10.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.padding(top = 2.dp)) { icon() }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF1A1A1A), lineHeight = 20.sp),
            cursorBrush = SolidColor(UepBlue),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 2.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(placeholder, color = TextTertiary, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun ReadOnlyInputRow(
    icon: @Composable () -> Unit,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 10.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon()
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
    }
}

@Composable
private fun IconActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(TextTertiary.copy(alpha = 0.2f), RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun monthName(month: Int): String = listOf(
    "January","February","March","April","May","June",
    "July","August","September","October","November","December",
)[month]
