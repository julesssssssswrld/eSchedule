package com.example.eschedule.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.ui.components.AppIcons
import com.example.eschedule.ui.components.PageTitle
import com.example.eschedule.ui.components.appShadow

private data class SettingsItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun SettingsScreen(
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val generalItems = remember {
        listOf(
            SettingsItem("Account Sync",              AppIcons.Reminder,  "account_sync"),
            SettingsItem("Widgets",                   AppIcons.Classes,   "widgets"),
            SettingsItem("Notification & Reminder",   AppIcons.Reminder,  "notifications"),
            SettingsItem("Theme",                     AppIcons.Settings,  "theme"),
        )
    }
    val aboutItems = remember {
        listOf(
            SettingsItem("Language",     AppIcons.Location,  "language"),
            SettingsItem("Feedback",     AppIcons.Edit,      "feedback"),
            SettingsItem("Share App",    AppIcons.Add,       "share"),
            SettingsItem("Follow Us",    AppIcons.Home,      "follow"),
            SettingsItem("Privacy Policy", AppIcons.Settings, "privacy"),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PageTitle(title = "Settings")

        Spacer(Modifier.height(4.dp))

        SettingsGroupLabel("General")
        SettingsGroup(items = generalItems, onItemClick = onItemClick)

        Spacer(Modifier.height(12.dp))

        SettingsGroupLabel("About")
        SettingsGroup(items = aboutItems, onItemClick = onItemClick)

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsGroupLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsGroup(
    items: List<SettingsItem>,
    onItemClick: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(cornerRadius = 14.dp)
            .background(Color.White, RoundedCornerShape(12.dp)),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                SettingsRow(item = item, onClick = { onItemClick(item.route) })
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        color = DividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    item: SettingsItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = UepBlue,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}
