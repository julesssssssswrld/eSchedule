package com.example.eschedule.ui.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.example.eschedule.theme.ClassTeal
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.ui.components.EScheduleFab
import com.example.eschedule.ui.components.SectionHeader
import com.example.eschedule.ui.components.appShadow

private val DAYS = listOf("Mon", "Tue", "Wed", "Thur", "Fri", "Sat", "Sun")

/** One block on the weekly grid */
data class ClassBlock(
    val id: String,
    val subjectCode: String,
    val dayIndex: Int,          // 0 = Mon … 6 = Sun
    val startHour: Float,       // e.g. 8.0 for 8:00 AM
    val durationHours: Float,   // e.g. 1.0 for a 1-hour class
)

private val HOUR_HEIGHT_DP = 60.dp
private val LABEL_WIDTH_DP = 64.dp
private val TIME_START = 6   // 6 AM
private val TIME_END   = 21  // 9 PM

@Composable
fun ClassesScreen(
    onClassClick: (String) -> Unit,
    onAddClass: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stub schedule data
    val blocks = remember {
        listOf(
            ClassBlock("sia101_mon", "SIA101", 0, 8f, 1f),
            ClassBlock("it102_tue",  "IT102",  1, 8f, 1f),
            ClassBlock("sia101_wed", "SIA101", 2, 8f, 1f),
            ClassBlock("ge_wed",     "GE ELEC 1", 2, 9f, 1f),
            ClassBlock("im101_tue",  "IM101",  1, 10f, 2f),
            ClassBlock("im101_wed",  "IM101",  2, 10f, 2f),
            ClassBlock("it102_thu",  "IT102",  3, 8f, 1f),
            ClassBlock("sia101_thu", "SIA101", 3, 10f, 3f),
            ClassBlock("ipt101_mon", "IPT101", 0, 13f, 1f),
            ClassBlock("net102_tue", "NET102", 1, 14f, 1f),
            ClassBlock("its101_wed", "ITS101", 2, 14f, 2f),
            ClassBlock("net102_thu", "NET102", 3, 14f, 1f),
            ClassBlock("its101_mon", "ITS101", 0, 15f, 2f),
            ClassBlock("its101_thu", "ITS101", 3, 15f, 2f),
            ClassBlock("net102_mon", "NET102", 0, 16f, 3f),
            ClassBlock("im101_wed2", "IM101",  2, 16f, 3f),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title
            Text(
                text = "Class Schedule",
                style = MaterialTheme.typography.headlineMedium,
                color = UepBlue,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )

            // Scrollable weekly grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    WeeklyGrid(
                        blocks = blocks,
                        onBlockClick = onClassClick,
                    )
                    Spacer(Modifier.height(96.dp))
                }
            }
        }

        // FAB
        EScheduleFab(
            onClick = onAddClass,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 88.dp),
        )
    }
}

@Composable
private fun WeeklyGrid(
    blocks: List<ClassBlock>,
    onBlockClick: (String) -> Unit,
) {
    val hours = TIME_START until TIME_END

    Row(modifier = Modifier.fillMaxWidth()) {
        // Time labels column
        Column(modifier = Modifier.width(LABEL_WIDTH_DP)) {
            Spacer(Modifier.height(32.dp)) // header spacer
            hours.forEach { hour ->
                Box(
                    modifier = Modifier.height(HOUR_HEIGHT_DP),
                    contentAlignment = Alignment.TopStart,
                ) {
                    val label = when {
                        hour == 12 -> "12:00 PM"
                        hour < 12  -> "${hour}:00 AM"
                        else       -> "${hour - 12}:00 PM"
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                    )
                }
            }
        }

        // Day columns
        DAYS.forEachIndexed { dayIndex, dayLabel ->
            Column(modifier = Modifier.weight(1f)) {
                // Day header
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }

                // Hour rows with class blocks
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HOUR_HEIGHT_DP * hours.count()),
                ) {
                    // Hour dividers
                    hours.forEachIndexed { i, _ ->
                        HorizontalDivider(
                            color = DividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = HOUR_HEIGHT_DP * i),
                        )
                    }

                    // Class blocks for this day
                    blocks.filter { it.dayIndex == dayIndex }.forEach { block ->
                        val topOffset = (block.startHour - TIME_START) * HOUR_HEIGHT_DP.value
                        val blockHeight = block.durationHours * HOUR_HEIGHT_DP.value
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = topOffset.dp, start = 1.dp, end = 1.dp)
                                .height(blockHeight.dp)
                                .appShadow(blur = 6.dp, spread = (-3).dp, cornerRadius = 6.dp)
                                .background(ClassTeal, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onBlockClick(block.id) },
                            contentAlignment = Alignment.TopStart,
                        ) {
                            Text(
                                text = block.subjectCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(3.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
