package com.example.eschedule.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
// Collapsed: cards fanned in a depth stack (scale + alpha + Y offset).
//   index 0 = TOP (highest zIndex), subsequent cards peek behind it.
// Expanded:  cards slide out to natural column positions.
// Tapping the TOP card toggles; background cards are non-interactive.
// ─────────────────────────────────────────────────────────────────────────────

private val PEEK_DP     : Dp    = 16.dp   // vertical peek per card behind
private val SCALE_STEP  : Float = 0.03f   // per-layer scale reduction
private val ALPHA_STEP  : Float = 0.12f   // per-layer opacity reduction
private const val ESTIMATED_CARD_H = 78f  // approximate card height in dp

@Composable
fun StackedClassCards(
    cards: List<ClassCardData>,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    // Smooth iOS-like easing — no bounce
    val tweenSpec = tween<Float>(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    val tweenDp   = tween<Dp>(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)

    Box(modifier = modifier.fillMaxWidth()) {
        // Draw cards in REVERSE order so that card 0 is drawn LAST = visually on top.
        // zIndex ensures correct hit testing regardless of draw order.
        cards.indices.reversed().forEach { index ->
            val card = cards[index]
            val depth = index  // 0 = front card, 1 = first behind, ...

            // Collapsed: each card peeks below the one in front
            val collapsedY     = depth * PEEK_DP.value
            val collapsedScale = 1f - depth * SCALE_STEP
            val collapsedAlpha = (1f - depth * ALPHA_STEP).coerceAtLeast(0.3f)

            // Expanded: spaced out vertically with a comfortable gap
            val expandedY = depth * (ESTIMATED_CARD_H + 12f)

            val animY by animateFloatAsState(
                targetValue   = if (expanded) expandedY else collapsedY,
                animationSpec = tweenSpec,
                label         = "y_$index",
            )
            val animScale by animateFloatAsState(
                targetValue   = if (expanded) 1f else collapsedScale,
                animationSpec = tweenSpec,
                label         = "scale_$index",
            )
            val animAlpha by animateFloatAsState(
                targetValue   = if (expanded) 1f else collapsedAlpha,
                animationSpec = tweenSpec,
                label         = "alpha_$index",
            )

            ClassCard(
                data           = card,
                showExpandHint = index == 0 && cards.size > 1,
                onClick        = if (index == 0 && cards.size > 1) {
                    { expanded = !expanded }
                } else null,
                modifier = Modifier
                    .zIndex((cards.size - index).toFloat())  // card 0 = highest z
                    .offset(y = animY.dp)
                    .graphicsLayer {
                        scaleX = animScale
                        alpha  = animAlpha
                    },
            )
        }

        // Invisible spacer to reserve correct height so nothing below overlaps
        val requiredHeight = if (expanded) {
            (cards.size * ESTIMATED_CARD_H + (cards.size - 1) * 12f).dp
        } else {
            (ESTIMATED_CARD_H + (cards.size - 1) * PEEK_DP.value).dp
        }
        val animHeight by animateDpAsState(
            targetValue   = requiredHeight,
            animationSpec = tweenDp,
            label         = "stack_height",
        )
        Spacer(Modifier.height(animHeight))
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

@Composable
fun ReminderListItem(
    data: ReminderItemData,
    onCheckedChange: (Boolean) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    showMoreButton: Boolean = true,
) {
    Row(
        modifier = modifier
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
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
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
            .background(Color(0x99000000), CircleShape)   // black 60% — matches nav pill
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
