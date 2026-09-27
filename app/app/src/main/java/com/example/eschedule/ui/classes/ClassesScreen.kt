package com.example.eschedule.ui.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eschedule.theme.ClassTeal
import com.example.eschedule.theme.CurrentTimeIndicator
import com.example.eschedule.theme.DividerColor
import com.example.eschedule.theme.TextSecondary
import com.example.eschedule.theme.TextTertiary
import com.example.eschedule.theme.UepBlue
import com.example.eschedule.ui.components.EScheduleFab
import com.example.eschedule.ui.components.SectionHeader
import com.example.eschedule.ui.components.appShadow
import java.util.Calendar

// ── Grid constants ─────────────────────────────────────────────────────────────
private val DAYS         = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val HOUR_HEIGHT  = 60.dp
private val LABEL_W      = 60.dp     // wide enough for "12:00 PM"
private const val TIME_START     = 0   // midnight — full 24h
private const val TIME_END       = 24
private const val SCROLL_TO_HOUR = 7   // default view: 7 AM

/** One block on the weekly schedule grid */
data class ClassBlock(
    val id: String,
    val subjectCode: String,
    val dayIndex: Int,          // 0 = Mon … 6 = Sun
    val startHour: Float,       // e.g. 8.0 for 8:00 AM
    val durationHours: Float,
)

@Composable
fun ClassesScreen(
    onClassClick: (String) -> Unit,
    onAddClass: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val blocks = remember {
        listOf(
            ClassBlock("sia101_mon", "SIA101",    0, 8f,  1f),
            ClassBlock("it102_tue",  "IT102",     1, 8f,  1f),
            ClassBlock("sia101_wed", "SIA101",    2, 8f,  1f),
            ClassBlock("ge_wed",     "GE ELEC 1", 2, 9f,  1f),
            ClassBlock("im101_tue",  "IM101",     1, 10f, 2f),
            ClassBlock("im101_wed",  "IM101",     2, 10f, 2f),
            ClassBlock("it102_thu",  "IT102",     3, 8f,  1f),
            ClassBlock("sia101_thu", "SIA101",    3, 10f, 3f),
            ClassBlock("ipt101_mon", "IPT101",    0, 13f, 1f),
            ClassBlock("net102_tue", "NET102",    1, 14f, 1f),
            ClassBlock("its101_wed", "ITS101",    2, 14f, 2f),
            ClassBlock("net102_thu", "NET102",    3, 14f, 1f),
            ClassBlock("its101_mon", "ITS101",    0, 15f, 2f),
            ClassBlock("its101_thu", "ITS101",    3, 15f, 2f),
            ClassBlock("net102_mon", "NET102",    0, 16f, 3f),
            ClassBlock("im101_wed2", "IM101",     2, 16f, 3f),
        )
    }

    // Current time in fractional hours (e.g. 12.5 = 12:30 PM)
    val currentHour = remember {
        val cal = Calendar.getInstance()
        cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        val targetPx = ((SCROLL_TO_HOUR - TIME_START) * HOUR_HEIGHT.value).toInt()
        scrollState.scrollTo(targetPx)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Title ─────────────────────────────────────────────────────────
            SectionHeader(
                title = "Class Schedule",
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
            )

            // ── Sticky day-header row ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(Color(0xFFF7F7F7)),
            ) {
                Spacer(Modifier.width(LABEL_W))
                DAYS.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.sp,
                        )
                    }
                }
            }

            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

            // ── Scrollable 24h grid ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)     // ← screen margins for the grid
                        .verticalScroll(scrollState),
                ) {
                    WeeklyGrid(
                        blocks = blocks,
                        currentHour = currentHour,
                        onBlockClick = onClassClick,
                    )
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        EScheduleFab(
            onClick = onAddClass,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Weekly grid — renders inside the parent Row (needs RowScope for weight())
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun androidx.compose.foundation.layout.RowScope.WeeklyGrid(
    blocks: List<ClassBlock>,
    currentHour: Float,
    onBlockClick: (String) -> Unit,
) {
    val hours      = TIME_START until TIME_END
    val totalHours = hours.count()
    val gridH      = HOUR_HEIGHT * totalHours

    // ── Time label column ─────────────────────────────────────────────────────
    Column(modifier = Modifier.width(LABEL_W)) {
        hours.forEach { hour ->
            Box(
                modifier = Modifier.height(HOUR_HEIGHT),
                contentAlignment = Alignment.TopEnd,     // text sits ON the divider line
            ) {
                Text(
                    text = hourLabel(hour),
                    fontSize = 9.sp,
                    color = TextTertiary,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(end = 8.dp, top = 2.dp),
                )
            }
        }
    }

    // ── Day columns + current-time overlay ───────────────────────────────────
    Box(
        modifier = Modifier
            .weight(1f)
            .height(gridH),
    ) {
        // Hour divider lines
        Column(modifier = Modifier.fillMaxSize()) {
            hours.forEach { _ ->
                Box(modifier = Modifier.height(HOUR_HEIGHT)) {
                    HorizontalDivider(
                        color = DividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.align(Alignment.TopStart),
                    )
                }
            }
        }

        // Day columns with class blocks
        Row(modifier = Modifier.fillMaxSize()) {
            DAYS.forEachIndexed { dayIndex, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    blocks.filter { it.dayIndex == dayIndex }.forEach { block ->
                        val topDp   = (block.startHour - TIME_START) * HOUR_HEIGHT.value
                        val blockH  = block.durationHours * HOUR_HEIGHT.value

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = topDp.dp + 2.dp, start = 1.dp, end = 1.dp)
                                .height((blockH - 2f).dp)
                                .appShadow(blur = 6.dp, spread = (-3).dp, cornerRadius = 4.dp)
                                .background(ClassTeal, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onBlockClick(block.id) },
                            contentAlignment = Alignment.TopStart,
                        ) {
                            Text(
                                text = block.subjectCode,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = 0.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }

        // ── Current time indicator ─────────────────────────────────────────
        // Only visible if current time is within the 24h grid
        if (currentHour in TIME_START.toFloat()..TIME_END.toFloat()) {
            val offsetDp = ((currentHour - TIME_START) * HOUR_HEIGHT.value).dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetDp),
            ) {
                // Red dot at left edge
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CurrentTimeIndicator, CircleShape)
                        .align(Alignment.CenterStart),
                )
                // Red horizontal line across full width
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 4.dp)           // starts just after the dot
                        .background(CurrentTimeIndicator)
                        .align(Alignment.CenterStart),
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun hourLabel(hour: Int): String = when {
    hour == 0  -> "12:00 AM"
    hour < 12  -> "$hour:00 AM"
    hour == 12 -> "12:00 PM"
    else       -> "${hour - 12}:00 PM"
}
