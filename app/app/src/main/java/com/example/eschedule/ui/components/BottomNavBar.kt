package com.example.eschedule.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.theme.UepYellow

/** Tab destinations for the main bottom nav. */
sealed interface TabRoute {
    data object Home : TabRoute
    data object Classes : TabRoute
    data object Reminders : TabRoute
    data object Settings : TabRoute
}

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val tab: TabRoute,
)

// Frosted glass white — enough transparency to feel floating
private val GlassWhite = Color(0xE6FFFFFF) // ~90% opaque white

@Composable
fun BottomNavBar(
    currentTab: TabRoute,
    onTabSelected: (TabRoute) -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemsLeft = remember {
        listOf(
            NavItem("Home",    AppIcons.Home,    TabRoute.Home),
            NavItem("Classes", AppIcons.Classes, TabRoute.Classes),
        )
    }
    val itemsRight = remember {
        listOf(
            NavItem("Reminders", AppIcons.Reminder, TabRoute.Reminders),
            NavItem("Settings",  AppIcons.Settings,  TabRoute.Settings),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // ── Glassmorphism pill ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .appShadow(blur = 10.dp, spread = (-5).dp, cornerRadius = 32.dp)
                .background(GlassWhite, RoundedCornerShape(32.dp))
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(32.dp),
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsLeft.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                    modifier = Modifier.weight(1f),
                )
            }

            // Gap for floating scan button
            Spacer(Modifier.weight(1f))

            itemsRight.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Floating scan FAB ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .size(56.dp)
                .appShadow(blur = 12.dp, spread = (-4).dp, cornerRadius = 28.dp)
                .background(UepYellow, CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onScanClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Scan,
                contentDescription = "Scan",
                tint = UepBlue,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun NavTabItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) UepBlue else TextTertiary,
        animationSpec = tween(durationMillis = 180),
        label = "nav_tint",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
            letterSpacing = 0.sp,
        )
    }
}
