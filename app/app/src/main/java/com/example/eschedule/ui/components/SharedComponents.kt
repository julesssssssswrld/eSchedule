package com.example.eschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue

// ─── Stacked class cards ───────────────────────────────────────────────────────
// Shows upcoming cards in a fanned stack. Tap to expand/collapse.

private val PeekHeight : Dp = 10.dp   // how much each card behind peeks

@Composable
fun StackedClassCards(
    cards: List<ClassCardData>,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    if (expanded) {
        // ── Expanded: all cards shown in a column ──────────────────────────
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cards.forEach { card ->
                ClassCard(
                    data = card.copy(isExpandable = false),
                    onClick = {
                        if (cards.size > 1) expanded = false
                        onCardClick(card.id)
                    },
                )
            }
        }
    } else {
        // ── Collapsed: cards fanned as a stack ────────────────────────────
        // Cards are drawn LAST-to-FIRST so index-0 is on top.
        // Each subsequent card is offset downward by PeekHeight,
        // creating a "peeking" visual under the top card.
        // Total stack height = card height + (n-1) * PeekHeight.
        Box(
            modifier = modifier
                .fillMaxWidth()
                // Reserve space for peeking cards below
                .padding(bottom = ((cards.size - 1) * PeekHeight.value).dp),
        ) {
            cards.reversed().forEachIndexed { reversedIdx, card ->
                val cardIdx = cards.size - 1 - reversedIdx  // 0 = top card
                val scale   = 1f - cardIdx * 0.025f         // slight scale down per layer
                val alpha   = 1f - cardIdx * 0.25f          // fade out lower layers

                ClassCard(
                    data = if (cardIdx == 0) card.copy(isExpandable = cards.size > 1)
                           else card.copy(isExpandable = false),
                    onClick = { if (cards.size > 1) expanded = true else onCardClick(card.id) },
                    modifier = Modifier
                        .offset(y = (cardIdx * PeekHeight.value).dp)
                        .graphicsLayer {
                            scaleX = scale
                            this.alpha = alpha
                        },
                )
            }
        }
    }
}


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
            .appShadow(cornerRadius = 12.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
    ) {
        // IntrinsicSize.Min makes the Row adopt the height of its tallest child
        // so the blue accent bar always matches the card content height
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(vertical = 10.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(UepBlue, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    text = data.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    letterSpacing = 0.sp,
                    lineHeight = 18.sp,
                )
                Text(
                    text = data.timeRange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    letterSpacing = 0.sp,
                    lineHeight = 16.sp,
                )
                Text(
                    text = data.location,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    letterSpacing = 0.sp,
                    lineHeight = 16.sp,
                )
            }
            if (data.isExpandable) {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = "Expand",
                    tint = TextTertiary,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterVertically),
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
            .padding(vertical = 9.dp, horizontal = 2.dp),
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
        Spacer(Modifier.width(10.dp))
        Text(
            text = data.title,
            fontSize = 13.sp,
            fontWeight = if (data.isCompleted) FontWeight.Normal else FontWeight.Medium,
            color = if (data.isCompleted) TextTertiary else Color(0xFF1A1A1A),
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = data.dueLabel,
            fontSize = 11.sp,
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
                    .size(14.dp)
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
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = UepBlue,
        letterSpacing = (-0.2).sp,
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
    val bg   = if (selected) UepBlue else Color.White
    val fg   = if (selected) Color.White else TextSecondary
    val bord = if (selected) Color.Transparent else DividerColor

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
            modifier = Modifier.size(24.dp),
        )
    }
}
