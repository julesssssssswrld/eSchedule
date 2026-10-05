package com.example.eschedule.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue

// ─────────────────────────────────────────────────────────────────────────────
// ClassCardData
// ─────────────────────────────────────────────────────────────────────────────

data class ClassCardData(
    val id: String,
    val title: String,
    val timeRange: String,
    val location: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// ClassCard — plain card, no internal expand state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ClassCard(
    data: ClassCardData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,         // null = non-interactive (stack layers)
    showExpandHint: Boolean = false,       // chevron shown only on top of stack
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 12.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) else Modifier
            ),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(vertical = 10.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Blue accent bar — dynamic height via fillMaxHeight()
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(UepBlue, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
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
            // Expand hint — only shown when explicitly requested
            if (showExpandHint) {
                Icon(
                    imageVector = AppIcons.Expand,
                    contentDescription = "Expand stack",
                    tint = TextTertiary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterVertically),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// StackedClassCards
//
// Each card uses Modifier.layout{} so it reports height = cardHeight + yOffset
// to the parent Box. This lets the Box grow to EXACTLY accommodate all peeking
// cards without needing a separate Spacer hack.
//
// placeRelativeWithLayer applies scale+alpha in the GPU layer without creating
// an isolated compositing group that would break Box’s z-ordering.
// ─────────────────────────────────────────────────────────────────────────────

private val PEEK_DP     : Dp    = 16.dp   // vertical peek per depth level
private val SCALE_STEP  : Float = 0.025f  // scale reduction per depth
private val ALPHA_STEP  : Float = 0.10f   // alpha reduction per depth
private const val ESTIMATED_CARD_H = 72f  // dp; used for expanded Y spacing
private const val EXPAND_GAP       = 12f  // dp gap between expanded cards

@Composable
fun StackedClassCards(
    cards: List<ClassCardData>,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    val tweenSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

    Box(modifier = modifier.fillMaxWidth()) {
        // Iterate in REVERSED order: last card drawn first (lowest z),
        // front card drawn last (highest z, naturally on top).
        cards.indices.reversed().forEach { index ->
            val depth = index  // 0 = front card

            val collapsedY = depth * PEEK_DP.value
            val expandedY  = depth * (ESTIMATED_CARD_H + EXPAND_GAP)

            val animY by animateFloatAsState(
                targetValue   = if (expanded) expandedY else collapsedY,
                animationSpec = tweenSpec,
                label         = "y_$index",
            )
            val animScale by animateFloatAsState(
                targetValue   = if (expanded) 1f else (1f - depth * SCALE_STEP),
                animationSpec = tweenSpec,
                label         = "scale_$index",
            )
            val animAlpha by animateFloatAsState(
                targetValue   = if (expanded) 1f else (1f - depth * ALPHA_STEP).coerceAtLeast(0.6f),
                animationSpec = tweenSpec,
                label         = "alpha_$index",
            )

            ClassCard(
                data           = cards[index],
                showExpandHint = index == 0 && cards.size > 1,
                onClick        = if (index == 0 && cards.size > 1) {
                    { expanded = !expanded }
                } else null,
                modifier       = Modifier
                    .zIndex((cards.size - index).toFloat())
                    .graphicsLayer {
                        scaleX = animScale
                        scaleY = animScale
                        alpha  = animAlpha
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val yPx = animY.dp.roundToPx()
                        // Report height = card height + y offset so the parent
                        // Box grows to exactly fit all stacked/expanded cards.
                        layout(placeable.width, placeable.height + yPx) {
                            // Place in layout space (not GPU layer) so the layout
                            // system tracks the real position and won't clip.
                            placeable.placeRelative(x = 0, y = yPx)
                        }
                    },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ReminderItemData + ReminderListItem
// ─────────────────────────────────────────────────────────────────────────────

data class ReminderItemData(
    val id: String,
    val title: String,
    val dueLabel: String,
    val isCompleted: Boolean = false,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReminderListItem(
    data: ReminderItemData,
    onCheckedChange: (Boolean) -> Unit,
    onMoreClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showMoreButton: Boolean = true,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = {
                    if (onEditClick != null || onDeleteClick != null) showMenu = true
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = data.isCompleted,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(20.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor   = UepBlue,
                    uncheckedColor = DividerColor,
                ),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = data.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = if (data.isCompleted) TextTertiary else Color(0xFF1A1A1A),
                textDecoration = if (data.isCompleted) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f),
                letterSpacing = 0.sp,
            )
            Text(
                text = data.dueLabel,
                fontSize = 12.sp,
                color = TextTertiary,
                letterSpacing = 0.sp,
            )
            if (showMoreButton) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onMoreClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.ThreeDot,
                        contentDescription = "More options",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Long-press context menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            if (onEditClick != null) {
                DropdownMenuItem(
                    text = { Text("Edit", fontSize = 14.sp) },
                    leadingIcon = { Icon(AppIcons.Edit, null, Modifier.size(18.dp)) },
                    onClick = { onEditClick(); showMenu = false },
                )
            }
            if (onDeleteClick != null) {
                DropdownMenuItem(
                    text = { Text("Delete", fontSize = 14.sp, color = Color(0xFFE53935)) },
                    leadingIcon = { Icon(AppIcons.Delete, null, Modifier.size(18.dp), tint = Color(0xFFE53935)) },
                    onClick = { onDeleteClick(); showMenu = false },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SectionHeader — small gray label used inside content areas
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        letterSpacing = 0.sp,
        modifier = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// PageTitle — consistent bold UepBlue page-level title across ALL screens
// 22sp, Bold. Container provides top=16dp via vertical=16 padding.
// This ensures pixel-perfect consistency regardless of screen.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PageTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = UepBlue,
        letterSpacing = 0.sp,
        modifier = modifier.padding(bottom = 12.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// FilterChip — used in RemindersScreen filter bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg     = if (selected) UepBlue else Color.White
    val fg     = if (selected) Color.White else TextSecondary
    val border = if (selected) Color.Transparent else DividerColor

    Box(
        modifier = modifier
            .appShadow(blur = 4.dp, spread = (-2).dp, cornerRadius = 20.dp)
            .background(bg, RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
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

// ─────────────────────────────────────────────────────────────────────────────
// EScheduleFab — floating action button used on Classes and Reminders screens
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EScheduleFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .appShadow(blur = 12.dp, spread = (-4).dp, cornerRadius = 26.dp)
            .background(Color(0x80000000), CircleShape)   // black 50%
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
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
