package vhdt.wiis.app.calendar

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import vhdt.wiis.R
import vhdt.wiis.model.ScheduleItem
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ─── Shared colors ───────────────────────────────────────────────────────────
private val SchoolBlue   = Color(0xFF1e40af)
private val DeviceGreen  = Color(0xFF05992A)
private val SlateGray    = Color(0xFF94A3B8)

// ─── CalendarScreen ───────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: CalendarVM = viewModel()) {

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("CalendarScreen", "Calendar permission result: $isGranted")
        if (isGranted) {
            viewModel.loadDeviceCalendarEvents(context)
            viewModel.debugCheckCalendars(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshTasks()
        permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        MonthHeader(
            displayMonth = viewModel.displayMonth.value,
            onPrev    = { viewModel.goToPreviousMonth() },
            onNext    = { viewModel.goToNextMonth() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        MonthCalendarGrid(
            displayMonth          = viewModel.displayMonth.value,
            selectedDate          = viewModel.selectedDate.value,
            onDateSelected        = { viewModel.selectDate(it) },
            hasTasksOnDate        = { viewModel.hasTasksOnDate(it) },
            hasDeviceEventsOnDate = { viewModel.getDeviceEventsForDate(it).isNotEmpty() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        val schoolTaskCount = viewModel.getTasksForDate(viewModel.selectedDate.value).size
        val deviceTaskCount = viewModel.getDeviceEventsForDate(viewModel.selectedDate.value).size

        TodaySummaryCard(
            selectedDate = viewModel.selectedDate.value,
            taskCount    = schoolTaskCount + deviceTaskCount
        )

        Spacer(modifier = Modifier.height(8.dp))

        SectionLabel("LỊCH TRƯỜNG")

        Spacer(modifier = Modifier.height(12.dp))

        TimelineSection(viewModel)

        val deviceEvents = viewModel.getDeviceEventsForDate(viewModel.selectedDate.value)

        Spacer(modifier = Modifier.height(16.dp))

        SectionLabel("LỊCH ĐIỆN THOẠI")

        Spacer(modifier = Modifier.height(12.dp))

        if (deviceEvents.isEmpty()) {
            EmptyEventPlaceholder(text = "Không có lịch điện thoại")
        } else {
            DeviceEventsSection(viewModel = viewModel, events = deviceEvents)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ─── MonthHeader ─────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthHeader(
    displayMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment    = Alignment.CenterVertically
        ) {
            NavButton(iconRes = R.drawable.arrow_back_ios_24px, onClick = onPrev, iconPaddingStart = 4.dp)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "Tháng ${displayMonth.monthValue}",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = SchoolBlue
                )
                Text(
                    text       = "Năm ${displayMonth.year}",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF64748B)
                )
            }

            NavButton(iconRes = R.drawable.arrow_forward_ios_24px, onClick = onNext)
        }
    }
}

// ─── NavButton ────────────────────────────────────────────────────────────────
@Composable
private fun NavButton(
    iconRes: Int,
    onClick: () -> Unit,
    iconPaddingStart: androidx.compose.ui.unit.Dp = 0.dp
) {
    Surface(
        shape          = RoundedCornerShape(14.dp),
        color          = SchoolBlue,
        modifier       = Modifier
            .clickable { onClick() }
            .size(44.dp),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter           = painterResource(id = iconRes),
                contentDescription = null,
                tint              = Color.White,
                modifier          = Modifier
                    .size(20.dp)
                    .padding(start = iconPaddingStart)
            )
        }
    }
}

// ─── SectionLabel ─────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        fontSize   = 12.sp,
        fontWeight = FontWeight.Bold,
        color      = SlateGray,
        modifier   = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

// ─── EmptyEventPlaceholder ────────────────────────────────────────────────────
@Composable
private fun EmptyEventPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter           = painterResource(id = R.drawable.calendar_today_24px),
                contentDescription = null,
                tint              = SlateGray,
                modifier          = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = text,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = SlateGray
            )
        }
    }
}

// ─── MonthCalendarGrid ────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthCalendarGrid(
    displayMonth          : YearMonth,
    selectedDate          : LocalDate,
    onDateSelected        : (LocalDate) -> Unit,
    hasTasksOnDate        : (LocalDate) -> Boolean,
    hasDeviceEventsOnDate : (LocalDate) -> Boolean
) {
    val dayLabels     = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val firstDay      = displayMonth.atDay(1)
    val startDay      = firstDay.minusDays(firstDay.dayOfWeek.value.toLong() - 1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent),
        border   = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Day-of-week header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Color(0x83011A9E), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xBE000000).copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                dayLabels.forEach { day ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text(day, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }

            // Date cells
            val weekRows = (0..5).map { w -> (0..6).map { d -> startDay.plusDays((w * 7 + d).toLong()) } }

            weekRows.forEach { weekDates ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    weekDates.forEach { date ->
                        key(date) {
                            val isCurrentMonth   = date.month == displayMonth.month
                            val isSelected       = date == selectedDate
                            val hasTask          = hasTasksOnDate(date)
                            val hasDeviceEvents  = hasDeviceEventsOnDate(date)
                            val isToday          = date == LocalDate.now()

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isSelected                     -> SchoolBlue
                                            hasTask && isCurrentMonth      -> Color(0xFFF0F8FF)
                                            else                           -> Color(0xFFFAFAFA)
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = isCurrentMonth) { onDateSelected(date) }
                                    .border(
                                        width  = if (isToday && !isSelected) 2.dp else 0.dp,
                                        color  = if (isToday && !isSelected) SchoolBlue else Color.Transparent,
                                        shape  = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier            = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text       = date.dayOfMonth.toString(),
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = when {
                                            isSelected     -> Color.White
                                            isCurrentMonth -> Color(0xFF1E293B)
                                            else           -> Color(0xFFCBD5E1)
                                        }
                                    )

                                    if (hasTask || hasDeviceEvents) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            modifier              = Modifier.fillMaxWidth()
                                        ) {
                                            if (hasDeviceEvents) {
                                                EventDot(color = if (isSelected) Color.White else DeviceGreen)
                                            }
                                            if (hasTask && hasDeviceEvents) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            if (hasTask) {
                                                EventDot(color = if (isSelected) Color.White else SchoolBlue)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── EventDot ─────────────────────────────────────────────────────────────────
@Composable
private fun EventDot(color: Color) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// ─── TodaySummaryCard ─────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TodaySummaryCard(selectedDate: LocalDate, taskCount: Int) {
    val dayOfWeekText = when (selectedDate.dayOfWeek.value) {
        1    -> "Thứ Hai"
        2    -> "Thứ Ba"
        3    -> "Thứ Tư"
        4    -> "Thứ Năm"
        5    -> "Thứ Sáu"
        6    -> "Thứ Bảy"
        else -> "Chủ Nhật"
    }
    val dateText = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Card(
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF3766CB)),
        modifier  = Modifier
            .fillMaxWidth()
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border    = BorderStroke(1.dp, Color(0xFF14489C).copy(alpha = 0.3f))
    ) {
        Row(
            modifier             = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = dayOfWeekText,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Text(
                    text       = dateText,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White.copy(alpha = 0.8f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = taskCount.toString(),
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                    Text(
                        text       = "nhiệm vụ",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─── TimelineSection (school) ─────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimelineSection(viewModel: CalendarVM) {
    val tasks = viewModel.getTasksForDate(viewModel.selectedDate.value)

    if (tasks.isEmpty()) {
        EmptyEventPlaceholder(text = "Không có lịch trường")
        return
    }

    TimelineColumn(
        count   = tasks.size,
        dotColor = SchoolBlue
    ) { index ->
        val task       = tasks[index]
        val isExpanded = viewModel.expandedItems.value.contains(index)

        TimelineTaskCard(
            time       = task.startTime.toTimeString(),
            endTime    = task.endTime.toTimeString(),
            title      = task.title,
            isExpanded = isExpanded,
            task       = task,
            onToggle   = { viewModel.toggleTaskExpanded(index) },
            modifier   = Modifier.padding(bottom = if (index == tasks.lastIndex) 0.dp else 16.dp)
        )
    }
}

// ─── DeviceEventsSection ──────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DeviceEventsSection(
    viewModel : CalendarVM,
    events    : List<CalendarVM.DeviceScheduleItem>
) {
    TimelineColumn(
        count    = events.size,
        dotColor = DeviceGreen
    ) { index ->
        val event      = events[index]
        val isExpanded = viewModel.expandedDeviceItems.value.contains(index)

        key(event.id) {
            DeviceEventCard(
                event      = event,
                isExpanded = isExpanded,
                onToggle   = { viewModel.toggleDeviceEventExpanded(index) },
                modifier   = Modifier.padding(bottom = if (index == events.lastIndex) 0.dp else 16.dp)
            )
        }
    }
}

// ─── TimelineColumn ───────────────────────────────────────────────────────────
/**
 * Shared timeline scaffold: draws the dot + vertical connector line,
 * then calls [content] for each item.
 */
@Composable
private fun TimelineColumn(
    count    : Int,
    dotColor : Color,
    content  : @Composable (index: Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(count) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Dot + line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.width(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    if (index < count - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(dotColor.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                content(index)
            }
        }
    }
}

// ─── TimelineTaskCard (school) ────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimelineTaskCard(
    time       : String,
    endTime    : String,
    title      : String,
    isExpanded : Boolean,
    task       : ScheduleItem,
    onToggle   : () -> Unit,
    modifier   : Modifier = Modifier
) {
    EventCard(
        accentColor = SchoolBlue,
        timeBadge   = time,
        title       = title,
        isExpanded  = isExpanded,
        onToggle    = onToggle,
        modifier    = modifier
    ) {
        if (task.classCode.isNotEmpty()) {
            InfoRow(icon = R.drawable.book_24px, label = "Mã lớp", value = task.classCode)
            Spacer(modifier = Modifier.height(2.dp))
        }
        if (task.fromPeriod > 0) {
            InfoRow(
                icon  = R.drawable.book_ribbon_24px,
                label = "Tiết học",
                value = "Tiết: ${task.fromPeriod} - ${task.toPeriod}"
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        InfoRow(icon = R.drawable.alarm_24px, label = "Thời gian", value = "$time - $endTime")
        Spacer(modifier = Modifier.height(2.dp))
        InfoRow(icon = R.drawable.location_on_24px, label = "Phòng học", value = task.location)
        if (task.address.isNotEmpty() || task.note.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            InfoRow(
                icon  = R.drawable.school_24px,
                label = "Cơ sở",
                value = task.address.ifEmpty { task.note }
            )
        }
    }
}

// ─── DeviceEventCard ──────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DeviceEventCard(
    event      : CalendarVM.DeviceScheduleItem,
    isExpanded : Boolean,
    onToggle   : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val startTime = event.startTime.toTimeString()
    val endTime   = event.endTime.toTimeString()

    EventCard(
        accentColor = DeviceGreen,
        timeBadge   = startTime,
        title       = event.title,
        isExpanded  = isExpanded,
        onToggle    = onToggle,
        modifier    = modifier
    ) {
        InfoRow(icon = R.drawable.alarm_24px, label = "Thời gian", value = "$startTime - $endTime", iconBgColor = DeviceGreen)
        if (event.calendarName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            InfoRow(icon = R.drawable.calendar_today_24px, label = "Lịch", value = event.calendarName, iconBgColor = DeviceGreen)
        }
    }
}

// ─── EventCard ────────────────────────────────────────────────────────────────
/**
 * Generic expandable event card shared by school & device events.
 * [accentColor] drives the time badge, border, and expand-icon tint.
 * [expandedContent] is shown only when [isExpanded] is true.
 */
@Composable
private fun EventCard(
    accentColor     : Color,
    timeBadge       : String,
    title           : String,
    isExpanded      : Boolean,
    onToggle        : () -> Unit,
    modifier        : Modifier = Modifier,
    expandedContent : @Composable ColumnScope.() -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isExpanded) {
        if (isExpanded) bringIntoViewRequester.bringIntoView()
    }

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(
            1.5.dp,
            if (isExpanded) accentColor.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { onToggle() }
                .padding(14.dp)
        ) {
            // Header row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Time badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text       = timeBadge,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = accentColor
                    )
                }

                Text(
                    text       = title,
                    modifier   = Modifier.weight(1f),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1E293B),
                    maxLines   = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Surface(
                    shape    = CircleShape,
                    color    = if (isExpanded) accentColor.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter           = painterResource(
                                id = if (isExpanded) R.drawable.arrow_drop_up_24px
                                else            R.drawable.arrow_drop_down_24px
                            ),
                            contentDescription = null,
                            tint              = accentColor,
                            modifier          = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expanded detail
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(accentColor.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    expandedContent()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// ─── InfoRow ──────────────────────────────────────────────────────────────────
@Composable
private fun InfoRow(
    icon        : Int,
    label       : String,
    value       : String,
    iconBgColor : Color = Color(0xFF7AA3E7)
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment    = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier         = Modifier
                .clip(CircleShape)
                .background(iconBgColor)
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter           = painterResource(id = icon),
                contentDescription = null,
                modifier          = Modifier.size(14.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
            Text(text = value, fontSize = 13.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Extension helpers ────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
private fun Long.toTimeString(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

// ─── Preview ──────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun CalendarScreenPreview() {
    CalendarScreen()
}