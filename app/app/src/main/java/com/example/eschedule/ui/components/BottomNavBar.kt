package com.example.eschedule.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.eschedule.ClassesRoute
import com.example.eschedule.HomeRoute
import com.example.eschedule.RemindersRoute
import com.example.eschedule.ScanRoute
import com.example.eschedule.SettingsRoute
import com.example.eschedule.theme.AppBackground
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.theme.UepYellow

/** The route currently displayed in the main tab area. */
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

@Composable
fun BottomNavBar(
    currentTab: TabRoute,
    onTabSelected: (TabRoute) -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember {
        listOf(
            NavItem("Home", AppIcons.Home, TabRoute.Home),
            NavItem("Classes", AppIcons.Classes, TabRoute.Classes),
        )
    }
    val itemsRight = remember {
        listOf(
            NavItem("Reminders", AppIcons.Reminder, TabRoute.Reminders),
            NavItem("Settings", AppIcons.Settings, TabRoute.Settings),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // The pill-shaped nav bar background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(32.dp))
                .background(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(32.dp),
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left items
            items.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                    modifier = Modifier.weight(1f),
                )
            }

            // Center scan button placeholder (actual button floats above)
            Box(modifier = Modifier.weight(1f))

            // Right items
            itemsRight.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Floating scan FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .size(60.dp)
                .shadow(6.dp, CircleShape)
                .background(UepYellow, CircleShape)
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
                modifier = Modifier.size(28.dp),
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
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) UepBlue else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "nav_tab_tint",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = iconTint,
        )
    }
}
