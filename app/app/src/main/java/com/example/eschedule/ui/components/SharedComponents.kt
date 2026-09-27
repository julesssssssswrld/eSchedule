package com.example.eschedule.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue

// ─── Class card ────────────────────────────────────────────────────────────────

data class ClassCardData(
    val id: String,
    val title: String,
    val timeRange: String,
    val location: String,
    val isExpandable: Boolean = false,
)

@Composable
fun ClassCard(
    data: ClassCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 14.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Blue left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .background(UepBlue, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = data.timeRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    letterSpacing = 0.sp,
                )
                Text(
                    text = data.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    letterSpacing = 0.sp,
                )
            }
            if (data.isExpandable) {
                Icon(
                    imageVector = AppIcons.Expand,
                    contentDescription = "Expand",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ─── Reminder list item ─────────────────────────────────────────────────────────

data class ReminderItemData(
    val id: String,
    val title: String,
    val dueLabel: String,
    val isCompleted: Boolean = false,
)

@Composable
fun ReminderListItem(
    data: ReminderItemData,
    onCheckedChange: (Boolean) -> Unit,
    onMoreClick: () -> Unit,
    showMoreButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = data.isCompleted,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                uncheckedColor = DividerColor,
                checkedColor = TextSecondary,
            ),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = data.title,
            fontSize = 14.sp,
            fontWeight = if (data.isCompleted) FontWeight.Normal else FontWeight.Medium,
            color = if (data.isCompleted) TextTertiary
                    else MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = data.dueLabel,
            fontSize = 12.sp,
            color = TextTertiary,
            letterSpacing = 0.sp,
        )
        if (showMoreButton) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = AppIcons.ThreeDot,
                contentDescription = "More",
                tint = TextTertiary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onMoreClick,
                    ),
            )
        }
    }
}

// ─── Section header ──────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = UepBlue,
        letterSpacing = (-0.3).sp,
        modifier = modifier,
    )
}

// ─── Filter chip ─────────────────────────────────────────────────────────────────

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg    = if (selected) UepBlue else Color.White
    val fg    = if (selected) Color.White else TextSecondary
    val bord  = if (selected) Color.Transparent else DividerColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(0.5.dp, bord, RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = fg,
            letterSpacing = 0.sp,
        )
    }
}

// ─── FAB ─────────────────────────────────────────────────────────────────────────

@Composable
fun EScheduleFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .appShadow(blur = 12.dp, spread = (-4).dp, cornerRadius = 24.dp)
            .background(Color(0xFF888888).copy(alpha = 0.35f), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Add,
            contentDescription = "Add",
            tint = Color.White,
            modifier = Modifier.size(26.dp),
        )
    }
}
