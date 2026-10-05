package com.example.eschedule.ui.classes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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

private val DAY_LABELS = listOf("S", "M", "T", "W", "Th", "F", "S")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSheet(
    sheetTitle: String,
    initialTitle: String = "",
    initialSelectedDays: Set<Int> = emptySet(),
    onDismiss: () -> Unit,
    onSave: (title: String, selectedDays: Set<Int>) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(initialTitle) }
    var selectedDays by remember { mutableStateOf(initialSelectedDays) }

    // ── Date pickers ─────────────────────────────────────────────────────────
    val cal = remember { Calendar.getInstance() }
    var fromText by remember { mutableStateOf("August 1, 2026") }
    var untilText by remember { mutableStateOf("December 17, 2026") }
    var showFromPicker  by remember { mutableStateOf(false) }
    var showUntilPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d -> fromText = "${monthName(m)} $d, $y"; showFromPicker = false },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
            ).apply { setOnDismissListener { showFromPicker = false } }.show()
        }
    }
    if (showUntilPicker) {
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d -> untilText = "${monthName(m)} $d, $y"; showUntilPicker = false },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
            ).apply { setOnDismissListener { showUntilPicker = false } }.show()
        }
    }

    // ── Time pickers ─────────────────────────────────────────────────────────
    var startsText by remember { mutableStateOf("7:00 AM") }
    var endsText   by remember { mutableStateOf("9:00 AM") }
    var showStartsPicker by remember { mutableStateOf(false) }
    var showEndsPicker   by remember { mutableStateOf(false) }

    if (showStartsPicker) {
        LaunchedEffect(Unit) {
            TimePickerDialog(
                context,
                { _, h, m -> startsText = formatTime(h, m); showStartsPicker = false },
                7, 0, false,
            ).apply { setOnDismissListener { showStartsPicker = false } }.show()
        }
    }
    if (showEndsPicker) {
        LaunchedEffect(Unit) {
            TimePickerDialog(
                context,
                { _, h, m -> endsText = formatTime(h, m); showEndsPicker = false },
                9, 0, false,
            ).apply { setOnDismissListener { showEndsPicker = false } }.show()
        }
    }

    var locationText by remember { mutableStateOf("") }
    var descText     by remember { mutableStateOf("") }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sheetTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = UepBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                ClassSaveButton(onClick = { onSave(title, selectedDays) })
            }

            ClassTitleField(value = title, onValueChange = { title = it })

            Text(text = "Options", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ClassOptionsCard {
                ClassTimeRow("Starts", startsText) { showStartsPicker = true }
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                ClassTimeRow("Ends", endsText) { showEndsPicker = true }
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                ClassTimeRow("From", fromText) { showFromPicker = true }
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                ClassTimeRow("Until", untilText) { showUntilPicker = true }
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

                // Repeat day selector — evenly distributed
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Repeat",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        DAY_LABELS.forEachIndexed { index, label ->
                            val isSelected = selectedDays.contains(index)
                            DayChip(
                                label = label,
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) selectedDays - index
                                    else selectedDays + index
                                },
                            )
                        }
                    }
                }
            }

            Text(text = "Location", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ClassGrowingInputRow(
                icon = { Icon(AppIcons.Location, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                placeholder = "Add Location",
                value = locationText,
                onValueChange = { locationText = it },
            )

            Text(text = "Description / Additional Information", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ClassGrowingInputRow(
                icon = { Icon(AppIcons.Pencil, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                placeholder = "Description",
                value = descText,
                onValueChange = { descText = it },
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── View Class bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewClassSheet(
    classId: String,
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
                    text = "Class Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = UepBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                ClassIconAction(icon = AppIcons.Delete, description = "Delete", onClick = onDelete)
                Spacer(Modifier.width(8.dp))
                ClassIconAction(icon = AppIcons.Edit,   description = "Edit",   onClick = onEdit)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(68.dp)
                        .background(UepBlue, RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("IT102 - Mobile App Development", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Repeats every Tuesday & Thursday", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("8:00 AM - 9:00 AM",               style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Text(text = "Location", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ClassReadOnlyRow(
                icon = { Icon(AppIcons.Location, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                value = "CSFLD 22",
            )

            Text(text = "Description / Additional Information", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ClassReadOnlyRow(
                icon = { Icon(AppIcons.Pencil, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                value = "Class ID: 0012764, Instructor: B.PTL2",
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Private helpers ─────────────────────────────────────────────────────────

@Composable
private fun ClassSaveButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(UepBlue, RoundedCornerShape(50))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Save", style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun ClassTitleField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 10.dp)
            .background(Color.White, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(52.dp)
                .background(UepBlue, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF1A1A1A)),
            singleLine = true,
            cursorBrush = SolidColor(UepBlue),
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp, top = 14.dp, bottom = 14.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) Text("Title", color = TextTertiary, fontSize = 16.sp)
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun ClassOptionsCard(content: @Composable () -> Unit) {
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
private fun ClassTimeRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        ) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.width(4.dp))
            Icon(AppIcons.Expand, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg        = if (selected) UepBlue else Color.White
    val border    = if (selected) UepBlue else TextTertiary
    val textColor = if (selected) Color.White else TextTertiary

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(bg, CircleShape)
            .border(1.dp, border, CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

@Composable
private fun ClassGrowingInputRow(
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
                    if (value.isEmpty()) Text(placeholder, color = TextTertiary, fontSize = 14.sp)
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun ClassReadOnlyRow(icon: @Composable () -> Unit, value: String) {
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
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun ClassIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(TextTertiary.copy(alpha = 0.2f), RoundedCornerShape(50))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun monthName(month: Int): String = listOf(
    "January","February","March","April","May","June",
    "July","August","September","October","November","December",
)[month]

private fun formatTime(h: Int, m: Int): String {
    val amPm = if (h < 12) "AM" else "PM"
    val h12  = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
    return "$h12:${m.toString().padStart(2, '0')} $amPm"
}
