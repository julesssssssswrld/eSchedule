package com.example.eschedule.ui.reminders

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.ui.components.AppIcons
import com.example.eschedule.ui.components.appShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    onDismiss: () -> Unit,
    onSave: (title: String) -> Unit,
    initialTitle: String = "",
    sheetTitle: String = "Add Reminder",
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf(initialTitle) }
    var dateText by remember { mutableStateOf("September 11, 2026") }
    var timeText by remember { mutableStateOf("9:00 AM") }
    var timeEnabled by remember { mutableStateOf(false) }
    var locationText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

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
            // Header row
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

            // Title field with left accent border
            TitleField(value = title, onValueChange = { title = it })

            // Options card
            Text(
                text = "Options",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            OptionsCard {
                // Date row
                OptionRow(
                    label = "Date",
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(dateText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Spacer(Modifier.width(4.dp))
                            Icon(AppIcons.Expand, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    },
                )
                OptionDivider()
                // Time row
                OptionRow(
                    label = "Time",
                    subLabel = timeText,
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(AppIcons.Expand, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = timeEnabled,
                                onCheckedChange = { timeEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = UepBlue,
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
            PlaceholderInputRow(
                icon = { Icon(AppIcons.Location, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                placeholder = "Add Location",
                value = locationText,
                onValueChange = { locationText = it },
            )

            // Details
            Text(text = "Details", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            PlaceholderInputRow(
                icon = { Icon(AppIcons.Pencil, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                placeholder = "Notes",
                value = notesText,
                onValueChange = { notesText = it },
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── View Reminder (read-only) bottom sheet ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReminderSheet(
    reminderId: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

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
            // Header
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
                IconActionButton(icon = AppIcons.Edit, contentDescription = "Edit", onClick = onEdit)
            }

            // Reminder detail with left border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DividerColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(1.5.dp, TextSecondary, RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Pag laba", style = MaterialTheme.typography.titleMedium)
                        Text("Today", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("12:51 PM", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            // Location
            Text(text = "Location", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ReadOnlyInputRow(
                icon = { Icon(AppIcons.Location, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                value = "Location",
            )

            // Details
            Text(text = "Details", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            ReadOnlyInputRow(
                icon = { Icon(AppIcons.Pencil, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                value = "Notes",
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Reusable sub-components ─────────────────────────────────────────────────────

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
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(52.dp)
                .background(UepBlue, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Title", color = TextTertiary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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

@Composable
private fun PlaceholderInputRow(
    icon: @Composable () -> Unit,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, DividerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon()
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium,
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
            .border(0.5.dp, DividerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
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
