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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eschedule.theme.AppBackground
import com.example.eschedule.theme.NavActiveBlue
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

// ── Design tokens ──────────────────────────────────────────────────────────────
private val PillBg       = Color(0x33000000)        // black 20%
private val PillBorder   = Color(0x18FFFFFF)
private val ActiveTint   = NavActiveBlue             // #0088FF when selected
private val InactiveTint = Color.White               // plain white when idle

// Shared sizing so icon + label are always proportional across all items
private val NavIconSize  : Dp = 20.dp
private val NavLabelSize = 10.sp

// FAB geometry — cutout ring around yellow button
private val PillHeight   : Dp = 60.dp
private val FabSize      : Dp = 56.dp
private val FabRing      : Dp = 6.dp
private val FabRingSize  : Dp = FabSize + FabRing * 2   // 68dp
// Label box height: makes FAB centre land exactly at pill top
private val ScanLabelH   : Dp = PillHeight - FabRingSize / 2

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // ── Pill ──────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PillHeight)
                .appShadow(blur = 10.dp, spread = (-5).dp, cornerRadius = 30.dp)
                .background(PillBg, RoundedCornerShape(30.dp))
                .border(0.5.dp, PillBorder, RoundedCornerShape(30.dp))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left tabs — equal weight
            itemsLeft.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                    modifier = Modifier.weight(1f),
                )
            }

            // Centre gap — explicit width = FAB ring so the overlay aligns perfectly
            Spacer(Modifier.width(FabRingSize))

            // Right tabs — equal weight
            itemsRight.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Scan FAB (cutout ring + yellow button) ────────────────────────────
        // Column is bottom-aligned. Geometry:
        //   ScanLabelH (≈26dp) + FabRingSize (68dp) = ≈94dp from box bottom
        //   FabRing centre = ScanLabelH + FabRingSize/2 = 60dp = PillHeight ✓
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // AppBackground ring creates visual "cutout" in the pill
            Box(
                modifier = Modifier
                    .size(FabRingSize)
                    .background(AppBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(FabSize)
                        .appShadow(blur = 10.dp, spread = (-3).dp, cornerRadius = FabSize / 2)
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
                        modifier = Modifier.size(NavIconSize + 2.dp),
                    )
                }
            }
            // "Scan" label sits inside the pill gap at the same baseline as other labels
            Box(
                modifier = Modifier
                    .width(FabRingSize)
                    .height(ScanLabelH),
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
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) ActiveTint else InactiveTint,
        animationSpec = tween(durationMillis = 200),
        label = "tint_${item.label}",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp, horizontal = 2.dp),
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
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}
