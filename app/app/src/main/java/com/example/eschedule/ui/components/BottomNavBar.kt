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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.theme.UepYellow

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

// Black at 20% — dark frosted glass
private val PillBg       = Color(0x33000000)
private val PillBorder   = Color(0x22FFFFFF)
private val ActiveTint   = Color.White
private val InactiveTint = Color.White.copy(alpha = 0.45f)

// Uniform icon + label dimensions so everything looks the same size
private val NavIconSize   = 20.dp
private val NavLabelSize  = 10.sp
private val NavItemWidth  = 64.dp   // fixed width keeps columns identical

@Composable
fun BottomNavBar(
    currentTab: TabRoute,
    onTabSelected: (TabRoute) -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemsLeft = remember {
        listOf(
            NavItem("Home",    AppIcons.Home,     TabRoute.Home),
            NavItem("Classes", AppIcons.Classes,  TabRoute.Classes),
        )
    }
    val itemsRight = remember {
        listOf(
            NavItem("Reminders", AppIcons.Reminder, TabRoute.Reminders),
            NavItem("Settings",  AppIcons.Settings,  TabRoute.Settings),
        )
    }

    // Total pill height
    val pillHeight = 60.dp
    // Scan FAB diameter — flush with pill top; 8dp protrudes above
    val fabSize = 48.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // ── Dark glassmorphism pill ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillHeight)
                .appShadow(blur = 10.dp, spread = (-5).dp, cornerRadius = 30.dp)
                .background(PillBg, RoundedCornerShape(30.dp))
                .border(0.5.dp, PillBorder, RoundedCornerShape(30.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsLeft.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                )
            }

            // Centre gap — exact same width as the FAB so layout is symmetric
            Spacer(Modifier.width(fabSize))

            itemsRight.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                )
            }
        }

        // ── Scan FAB — centred, protrudes just above pill top ─────────────────
        // Column bottom aligns with Box bottom (same as pill bottom).
        // Scan label (~18dp) + FAB (48dp) = 66dp > pillHeight(60dp)
        // → FAB top sits 6dp above pill top ✓
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(fabSize)
                    .appShadow(blur = 10.dp, spread = (-3).dp, cornerRadius = fabSize / 2)
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
                    modifier = Modifier.size(NavIconSize),
                )
            }
            // Label matches exact height of other tab labels so alignment is uniform
            Box(
                modifier = Modifier.height((pillHeight - fabSize) / 2 + 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Scan",
                    fontSize = NavLabelSize,
                    fontWeight = FontWeight.Normal,
                    color = InactiveTint,
                    letterSpacing = 0.sp,
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) ActiveTint else InactiveTint,
        animationSpec = tween(durationMillis = 180),
        label = "nav_tint_${item.label}",
    )

    Column(
        modifier = Modifier
            .width(NavItemWidth)
            .clip(RoundedCornerShape(10.dp))
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
            modifier = Modifier.size(NavIconSize),
        )
        Text(
            text = item.label,
            fontSize = NavLabelSize,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
            letterSpacing = 0.sp,
        )
    }
}
