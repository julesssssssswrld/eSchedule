package com.example.eschedule.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
// Expanded:  cards slide out vertically to their natural positions.
// Tapping the TOP card toggles the state; cards underneath are non-interactive
// so they never accidentally navigate to a detail view.
// ─────────────────────────────────────────────────────────────────────────────

private val PEEK_DP     : Dp = 10.dp   // peeking gap between stacked cards
private val SCALE_STEP  : Float = 0.025f
private val ALPHA_STEP  : Float = 0.20f

@Composable
fun StackedClassCards(
    cards: List<ClassCardData>,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    // Animate each card's Y offset and alpha individually for a smooth slide
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium,
    )
    val springDp = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium,
    )

    // Reserve height: collapsed = topCard + peek * (n-1) below it
    // Expanded = cards * (card height ~ from IntrinsicSize) + 8dp gaps
    // We let Compose measure naturally — just control offsets
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        cards.forEachIndexed { index, card ->
            // index 0 = top card of the stack (drawn last = on top)

            // ── Collapsed positions ──────────────────────────────────────────
            val collapsedOffsetY = (index * PEEK_DP.value)
            val collapsedScale   = 1f - index * SCALE_STEP
            val collapsedAlpha   = 1f - index * ALPHA_STEP

            // ── Expanded positions ───────────────────────────────────────────
            // We need each card's "natural" height to space them correctly.
            // Since Compose cards auto-size, we use a fixed estimated height
            // (card content ~60dp + padding = ~78dp) plus 8dp gap.
            val estimatedCardH = 78f
            val expandedOffsetY = index * (estimatedCardH + 8f)

            val targetOffsetY = if (expanded) expandedOffsetY else collapsedOffsetY
            val targetScale   = if (expanded) 1f              else collapsedScale
            val targetAlpha   = if (expanded) 1f              else collapsedAlpha

            val animOffsetY by animateFloatAsState(
                targetValue   = targetOffsetY,
                animationSpec = springSpec,
                label         = "offset_$index",
            )
            val animScale by animateFloatAsState(
                targetValue   = targetScale,
                animationSpec = springSpec,
                label         = "scale_$index",
            )
            val animAlpha by animateFloatAsState(
                targetValue   = targetAlpha,
                animationSpec = springSpec,
                label         = "alpha_$index",
            )

            ClassCard(
                data           = card,
                showExpandHint = index == 0 && cards.size > 1,
                // Only the TOP card is interactive (toggles the stack)
                onClick        = if (index == 0 && cards.size > 1) {
                    { expanded = !expanded }
                } else null,
                modifier = Modifier
                    .offset(y = animOffsetY.dp)
                    .graphicsLayer {
                        scaleX = animScale
                        alpha  = animAlpha
                    },
            )
        }

        // Invisible spacer to reserve the full expanded/collapsed height
        // so content below doesn't overlap
        val requiredHeight = if (expanded) {
            val estimatedCardH = 78f
            (cards.size * estimatedCardH + (cards.size - 1) * 8f).dp
        } else {
            // collapsed: top card height + peeking offsets
            (78f + (cards.size - 1) * PEEK_DP.value).dp
        }
        val animHeight by animateDpAsState(
            targetValue   = requiredHeight,
            animationSpec = springDp,
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
// SectionHeader — used on every screen for consistent hierarchy
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
            .background(UepBlue, CircleShape)
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
