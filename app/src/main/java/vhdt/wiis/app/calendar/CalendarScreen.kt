package vhdt.wiis.app.calendar

import android.Manifest
import android.os.Build
import android.app.TimePickerDialog
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import java.time.LocalDateTime
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import vhdt.wiis.R
import vhdt.wiis.model.ScheduleItem
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
// AlarmManager/Settings not required when using AlarmClock intents

// ─── Màu dùng chung cho InfoRow ───────────────────────────────────────────────
private val COLOR_SCHEDULE     = Color(0xAB1254BD)

private val COLOR_DEVICE      = Color(0xA312843A)
private val COLOR_ALARM_PURPLE = Color(0xFF9C27B0)
private val COLOR_ALARM_YELLOW = Color(0xFFEF9F27)

// Bell button colors (per design)
private val BELL_BG_YELLOW = Color(0xFFFFF8E6)
private val BELL_ICON_YELLOW = Color(0xFFEF9F27)
private val BELL_BG_PURPLE = Color(0xFFF3E8FF)
private val BELL_ICON_PURPLE = Color(0xFF7C3AED)
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: CalendarVM = viewModel()) {

    var showAddEventDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val selectedDateState = viewModel.selectedDate.value  // Explicitly observe state changes
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("CalendarScreen", "Calendar permission result: $isGranted")
        if (isGranted) {
            viewModel.loadDeviceCalendarEvents(context)
            viewModel.debugCheckCalendars(context)
        }
    }

    // Monitor selectedDate changes
    LaunchedEffect(selectedDateState) {
        Log.d("CalendarScreen", ">>> selectedDate changed to: ${selectedDateState.toStandardFormat()} (${selectedDateState.dayOfWeek})")
    }

    LaunchedEffect(Unit) {
        viewModel.refreshTasks()
        permissionLauncher.launch(Manifest.permission.READ_CALENDAR)

        // Using system Clock app for alarms; no exact-alarm permission required
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x00FFFFFF))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x00FFFFFF)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1856BB),
                        modifier = Modifier
                            .clickable { viewModel.goToPreviousMonth() }
                            .size(44.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back_ios_24px),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(start = 4.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Tháng ${viewModel.displayMonth.value.monthValue}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1856BB)
                        )
                        Text(
                            text = "Năm ${viewModel.displayMonth.value.year}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1856BB),
                        modifier = Modifier
                            .clickable { viewModel.goToNextMonth() }
                            .size(44.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MonthCalendarGrid(
                displayMonth = viewModel.displayMonth.value,
                selectedDate = selectedDateState,
                onDateSelected = { viewModel.selectDate(it) },
                hasTasksOnDate = { viewModel.hasTasksOnDate(it) },
                hasDeviceEventsOnDate = { viewModel.getDeviceEventsForDate(it).isNotEmpty() },
                hasAlarmOnDate = { viewModel.hasAlarmOnDate(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TodaySummaryCard(
                selectedDateState,
                viewModel.getTasksForDate(selectedDateState).size +
                        viewModel.getDeviceEventsForDate(selectedDateState).size
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LỊCH TRÌNH",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TimelineSection(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "LỊCH ĐIỆN THOẠI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val deviceEvents = viewModel.getDeviceEventsForDate(viewModel.selectedDate.value)
            var showDeviceAlarmDialog by remember {
                mutableStateOf<CalendarVM.DeviceScheduleItem?>(
                    null
                )
            }

            DeviceEventsSection(
                viewModel,
                deviceEvents,
                onBellClick = { showDeviceAlarmDialog = it }
            )

            if (showDeviceAlarmDialog != null) {
                DeviceAlarmDialog(
                    event = showDeviceAlarmDialog!!,
                    onDismiss = { showDeviceAlarmDialog = null },
                    onSave = { minutes ->
                        val settings = viewModel.getEventSettings(showDeviceAlarmDialog!!.id)
                        viewModel.saveEventSettings(
                            showDeviceAlarmDialog!!.id,
                            settings.copy(alarmEnabled = minutes > 0, minutesBefore = minutes),
                            deviceEventTitle = showDeviceAlarmDialog!!.title,
                            deviceEventStartTime = showDeviceAlarmDialog!!.startTime
                        )
                        showDeviceAlarmDialog = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
            containerColor = Color(0xEB1E70C7),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add_24px),
                contentDescription = "Thêm lịch",
                modifier = Modifier.size(24.dp)
            )
        }

        if (showAddEventDialog) {
            AddEventDialog(
                selectedDate = viewModel.selectedDate.value,
                onDismiss = { showAddEventDialog = false }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthCalendarGrid(
    displayMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    hasTasksOnDate: (LocalDate) -> Boolean,
    hasDeviceEventsOnDate: (LocalDate) -> Boolean,
    hasAlarmOnDate: (LocalDate) -> Boolean
) {
    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val firstDayOfMonth = displayMonth.atDay(1)
    val startDay = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong() - 1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x00FFFFFF)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text(
                            day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }
            }

            val weekRows = (0..5).map { weekIndex ->
                (0..6).map { dayIndex ->
                    startDay.plusDays((weekIndex * 7 + dayIndex).toLong())
                }
            }

            weekRows.forEach { weekDates ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    weekDates.forEach { date ->
                        key(date) {
                            val isCurrentMonth = date.month == displayMonth.month
                            val isSelected = date == selectedDate
                            val hasTask = hasTasksOnDate(date)
                            val hasDeviceEvents = hasDeviceEventsOnDate(date)
                            val isToday = date == LocalDate.now()
                            
                            // Store the date in a local variable to ensure correct closure capture
                            val clickedDate = date

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF1856BB)
                                            hasTask && isCurrentMonth -> Color(0xFFF0F8FF)
                                            else -> Color(0xFFFAFAFA)
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = isCurrentMonth) { 
                                        Log.d("CalendarScreen", "Date clicked: ${clickedDate.toString()} (year=${clickedDate.year}, month=${clickedDate.monthValue}, day=${clickedDate.dayOfMonth})")
                                        onDateSelected(clickedDate) 
                                    }
                                    .border(
                                        width = if (isToday && !isSelected) 2.dp else 0.dp,
                                        color = if (isToday && !isSelected) Color(0xFF1856BB) else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        date.dayOfMonth.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            isSelected -> Color.White
                                            isCurrentMonth -> Color(0xFF1E293B)
                                            else -> Color(0xFFCBD5E1)
                                        }
                                    )
                                    if (hasTask || hasDeviceEvents || hasAlarmOnDate(date)) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (hasDeviceEvents) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) Color.White else Color(0xFF22C55E)
                                                        )
                                                )
                                            }
                                            if (hasTask && (hasDeviceEvents || hasAlarmOnDate(date))) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            if (hasTask) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) Color.White else Color(0xFF1856BB)
                                                        )
                                                )
                                            }
                                            if (hasAlarmOnDate(date) && hasTask) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            if (hasAlarmOnDate(date)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) Color.White else Color(0xFFEF9F27)
                                                        )
                                                )
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TodaySummaryCard(selectedDate: LocalDate, taskCount: Int) {
    val dayOfWeekText = when (selectedDate.dayOfWeek.value) {
        1 -> "Thứ Hai"
        2 -> "Thứ Ba"
        3 -> "Thứ Tư"
        4 -> "Thứ Năm"
        5 -> "Thứ Sáu"
        6 -> "Thứ Bảy"
        else -> "Chủ Nhật"
    }
    val dateText = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3766CB)),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Color(0xFF14489C).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dayOfWeekText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = dateText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f)
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
                        text = taskCount.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "nhiệm vụ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimelineSection(viewModel: CalendarVM) {
    val tasksForSelectedDate = viewModel.getTasksForDate(viewModel.selectedDate.value)
    var showNotifDialog by remember { mutableStateOf<String?>(null) }

    if (tasksForSelectedDate.isEmpty()) {
        EmptyStateBox()
        return
    }

    showNotifDialog?.let { eventId ->
        val task = tasksForSelectedDate.find { it.id == eventId }
        if (task != null) {
            EventNotificationDialog(
                task = task,
                settings = viewModel.getEventSettings(eventId),
                onDismiss = { showNotifDialog = null },
                onSave = { settings -> viewModel.saveEventSettings(eventId, settings) }
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        tasksForSelectedDate.forEachIndexed { index, task ->
            key(task.id) {
                val isExpanded = viewModel.expandedItems.value.contains(index)
                val isLast = index == tasksForSelectedDate.lastIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1856BB))
                        )
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp)
                                    .background(Color(0xFF1856BB).copy(alpha = 0.3f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TimelineTaskCard(
                        time = Instant.ofEpochMilli(task.startTime)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm")),
                        endTime = Instant.ofEpochMilli(task.endTime)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm")),
                        title = task.title,
                        isExpanded = isExpanded,
                        task = task,
                        onToggle = { viewModel.toggleTaskExpanded(index) },
                        onBellClick = { showNotifDialog = task.id },
                        hasAlarm = viewModel.getEventSettings(task.id).alarmEnabled,
                        modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimelineTaskCard(
    time: String,
    endTime: String,
    title: String,
    isExpanded: Boolean,
    task: ScheduleItem,
    onToggle: () -> Unit,
    onBellClick: () -> Unit,
    hasAlarm: Boolean,
    modifier: Modifier = Modifier
) {
    val expandedContentRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isExpanded) {
        if (isExpanded) expandedContentRequester.bringIntoView()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x00FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.5.dp, if (isExpanded) Color(0xFF02368B).copy(alpha = 0.3f) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1856BB).copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = time,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1856BB)
                    )
                }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = CircleShape,
                    color = if (hasAlarm) BELL_BG_PURPLE else BELL_BG_YELLOW,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBellClick() }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(id = R.drawable.notifications_24px),
                            contentDescription = "Thông báo",
                            tint = if (hasAlarm) BELL_ICON_PURPLE else BELL_ICON_YELLOW,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) COLOR_SCHEDULE.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(
                                id = if (isExpanded) R.drawable.arrow_drop_up_24px
                                else R.drawable.arrow_drop_down_24px
                            ),
                            contentDescription = null,
                            tint = COLOR_SCHEDULE,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(expandedContentRequester)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(COLOR_SCHEDULE.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (task.classCode.isNotEmpty()) {
                        InfoRow(
                            icon = R.drawable.book_24px,
                            label = "Mã lớp",
                            value = task.classCode,
                            color = COLOR_SCHEDULE
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    if (task.fromPeriod > 0) {
                        InfoRow(
                            icon = R.drawable.book_ribbon_24px,
                            label = "Tiết học",
                            value = "Tiết: ${task.fromPeriod} - ${task.toPeriod}",
                            color = COLOR_SCHEDULE
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    InfoRow(
                        icon = R.drawable.alarm_24px,
                        label = "Thời gian",
                        value = "$time - $endTime",
                        color = COLOR_SCHEDULE
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    InfoRow(
                        icon = R.drawable.location_on_24px,
                        label = "Phòng học",
                        value = task.location,
                        color = COLOR_SCHEDULE
                    )

                    if (task.address.isNotEmpty() || task.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        InfoRow(
                            icon = R.drawable.school_24px,
                            label = "Cơ sở",
                            value = task.address.ifEmpty { task.note },
                            color = COLOR_SCHEDULE
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: Int,
    label: String,
    value: String,
    color: Color = Color(0xFF1856BB)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color)
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DeviceEventsSection(
    viewModel: CalendarVM,
    events: List<CalendarVM.DeviceScheduleItem>,
    onBellClick: (CalendarVM.DeviceScheduleItem) -> Unit
) {
    if (events.isEmpty()) {
        EmptyStateBox()
        return
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        events.forEachIndexed { index, event ->
            key(event.id) {
                val isExpanded = viewModel.expandedDeviceItems.value.contains(index)
                val isLast = index == events.lastIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF15860B))
                        )
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp)
                                    .background(Color(0xFF15860B).copy(alpha = 0.3f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    DeviceEventCard(
                        event = event,
                        isExpanded = isExpanded,
                        onToggle = { viewModel.toggleDeviceEventExpanded(index) },
                        onBellClick = { onBellClick(event) },
                        hasAlarm = viewModel.getEventSettings(event.id).alarmEnabled,
                        modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DeviceEventCard(
    event: CalendarVM.DeviceScheduleItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onBellClick: () -> Unit,
    hasAlarm: Boolean,
    modifier: Modifier = Modifier
) {
    val expandedContentRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isExpanded) {
        if (isExpanded) expandedContentRequester.bringIntoView()
    }

    val startTime = Instant.ofEpochMilli(event.startTime)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    val endTime = Instant.ofEpochMilli(event.endTime)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x00FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.5.dp,
            if (isExpanded) COLOR_DEVICE.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x9CA5E0A1))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = startTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF11570D)
                    )
                }

                Text(
                    text = event.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = CircleShape,
                    color = if (hasAlarm) BELL_BG_PURPLE else BELL_BG_YELLOW,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBellClick() }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(id = R.drawable.notifications_24px),
                            contentDescription = "Báo thức",
                            tint = if (hasAlarm) BELL_ICON_PURPLE else BELL_ICON_YELLOW,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) COLOR_DEVICE.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(
                                id = if (isExpanded) R.drawable.arrow_drop_up_24px
                                else R.drawable.arrow_drop_down_24px
                            ),
                            contentDescription = null,
                            tint = COLOR_DEVICE,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(expandedContentRequester)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(COLOR_DEVICE.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow(
                        icon = R.drawable.alarm_24px,
                        label = "Thời gian",
                        value = "$startTime - $endTime",
                        color = COLOR_DEVICE
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    if (event.calendarName.isNotEmpty()) {
                        InfoRow(
                            icon = R.drawable.calendar_today_24px,
                            label = "Lịch",
                            value = event.calendarName,
                            color = COLOR_DEVICE
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyStateBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x00FFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.calendar_today_24px),
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Không có lịch trình",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventNotificationDialog(
    task: ScheduleItem,
    settings: CalendarVM.EventNotificationSettings,
    onDismiss: () -> Unit,
    onSave: (CalendarVM.EventNotificationSettings) -> Unit
) {
    var alarmEnabled by remember { mutableStateOf(settings.alarmEnabled) }
    var minutesBefore by remember { mutableStateOf(settings.minutesBefore.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Cài đặt thông báo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            task.title,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                painter = painterResource(id = R.drawable.close_24px),
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Notification toggle hidden in dialog (value preserved from settings)

                // Switch 2 — Báo thức
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    painter = painterResource(id = R.drawable.alarm_24px),
                                    contentDescription = null,
                                    tint = Color(0xFFEF9F27),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "Báo thức",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "Rung + âm thanh",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Switch(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF9F27)
                        )
                    )
                }

                if (alarmEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
                        border = BorderStroke(1.dp, Color(0xFFEF9F27).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Báo trước",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    "${minutesBefore.toInt()} phút",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF9F27)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = minutesBefore,
                                onValueChange = { minutesBefore = it },
                                valueRange = 5f..120f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFEF9F27),
                                    activeTrackColor = Color(0xFFEF9F27),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("5 phút", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("120 phút", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSave(
                            CalendarVM.EventNotificationSettings(
                                alarmEnabled = alarmEnabled,
                                minutesBefore = minutesBefore.toInt()
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1856BB))
                ) {
                    Text("Lưu cài đặt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DeviceAlarmDialog(
    event: CalendarVM.DeviceScheduleItem,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val settings = remember { mutableStateOf(60) } // default 60 minutes
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Cài đặt báo thức",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            event.title,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                painter = painterResource(id = R.drawable.close_24px),
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Alarm slider settings
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFEF9F27).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Báo trước",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "${settings.value} phút",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF9F27)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = settings.value.toFloat(),
                            onValueChange = { settings.value = it.toInt() },
                            valueRange = 5f..120f,
                            steps = 22,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFEF9F27),
                                activeTrackColor = Color(0xFFEF9F27),
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("5 phút", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("120 phút", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSave(settings.value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF9F27))
                ) {
                    Text("Lưu cài đặt", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf(8) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(9) }
    var endMinute by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Thêm lịch",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            fontSize = 12.sp,
                            color = Color(0xFF1856BB),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                painter = painterResource(id = R.drawable.close_24px),
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tiêu đề
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề *", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mô tả
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Giờ bắt đầu & kết thúc
                Text(
                    "Thời gian",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bắt đầu
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF2FF))
                                .border(1.dp, Color(0xFF1856BB).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    TimePickerDialog(ctx, { _, h, m ->
                                        startHour = h; startMinute = m
                                    }, startHour, startMinute, true).show()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "%02d:%02d".format(startHour, startMinute),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1856BB)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Bắt đầu",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Mũi tên
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Kết thúc
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF2FF))
                                .border(1.dp, Color(0xFF1856BB).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    TimePickerDialog(ctx, { _, h, m ->
                                        endHour = h; endMinute = m
                                    }, endHour, endMinute, true).show()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "%02d:%02d".format(endHour, endMinute),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1856BB)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Kết thúc",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nút Thêm
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val startMillis = selectedDate
                                .atTime(startHour, startMinute)
                                .atZone(ZoneId.systemDefault())
                                .toInstant().toEpochMilli()
                            val endMillis = selectedDate
                                .atTime(endHour, endMinute)
                                .atZone(ZoneId.systemDefault())
                                .toInstant().toEpochMilli()

                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, title)
                                putExtra(CalendarContract.Events.DESCRIPTION, description)
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                            }
                            ctx.startActivity(intent)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (title.isNotBlank()) Color(0xFF1856BB)
                        else Color(0xFFE2E8F0)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_24px),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Thêm vào lịch",
                        fontWeight = FontWeight.Bold,
                        color = if (title.isNotBlank()) Color.White else Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Nút Hủy
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Hủy", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun CalendarScreenPreview() {
    CalendarScreen()
}