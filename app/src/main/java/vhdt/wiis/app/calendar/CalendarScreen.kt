package vhdt.wiis.app.calendar

import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.YearMonth


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: CalendarVM = viewModel()) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x00FFFFFF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header với nút prev/next - Premium Design
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
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_ios_24px),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(start = 4.dp) // Đẩy icon sang phải để cân bằng với nút next
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
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
            selectedDate = viewModel.selectedDate.value,
            onDateSelected = { viewModel.selectDate(it) },
            hasTasksOnDate = { viewModel.hasTasksOnDate(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TodaySummaryCard(
            viewModel.selectedDate.value,
            viewModel.getTasksForDate(viewModel.selectedDate.value).size
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

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthCalendarGrid(
    displayMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    hasTasksOnDate: (LocalDate) -> Boolean
) {
    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val firstDayOfMonth = displayMonth.atDay(1)
    val startDay = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong() - 1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Day labels header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Calendar dates
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
                        key(date) {  // ADD KEY ĐỂ TỐI ƯU RECOMPOSE
                            val isCurrentMonth = date.month == displayMonth.month
                            val isSelected = date == selectedDate
                            val hasTask = hasTasksOnDate(date)
                            val isToday = date == LocalDate.now()

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
                                    .clickable(enabled = isCurrentMonth) { onDateSelected(date) }
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
                                    if (hasTask) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) Color.White else Color(0xFF1856BB)
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3766CB)
        ),
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

    if (tasksForSelectedDate.isEmpty()) {
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
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        tasksForSelectedDate.forEachIndexed { index, task ->
            key(task.id) {  // ADD KEY BASED ON TASK ID
                val isExpanded = viewModel.expandedItems.value.contains(index)
                val isLast = index == tasksForSelectedDate.lastIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Timeline indicator
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
    modifier: Modifier = Modifier
) {
    val expandedContentRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            expandedContentRequester.bringIntoView()
        }
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
                // Time badge
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

                // Title
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Expand icon
                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) Color(0xFF1856BB).copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(
                                id = if (isExpanded) R.drawable.arrow_drop_up_24px
                                else R.drawable.arrow_drop_down_24px
                            ),
                            contentDescription = null,
                            tint = Color(0xFF1856BB),
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

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF1856BB).copy(alpha = 0.1f))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Details
                    if (task.classCode.isNotEmpty()) {
                        InfoRow(icon = R.drawable.book_24px, label = "Mã lớp", value = task.classCode)
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    if (task.fromPeriod > 0) {
                        InfoRow(
                            icon = R.drawable.book_ribbon_24px,
                            label = "Tiết học",
                            value = "Tiết: ${task.fromPeriod} - ${task.toPeriod}"
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    InfoRow(
                        icon = R.drawable.alarm_24px,
                        label = "Thời gian",
                        value = "$time - $endTime"
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    InfoRow(
                        icon = R.drawable.location_on_24px,
                        label = "Phòng học",
                        value = task.location
                    )

                    if (task.address.isNotEmpty() || task.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        InfoRow(
                            icon = R.drawable.school_24px,
                            label = "Cơ sở",
                            value = task.address.ifEmpty { task.note }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// Improved InfoRow with better styling
@Composable
private fun InfoRow(icon: Int, label: String, value: String) {
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
                .background(Color(0xFF7AA3E7))
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
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
@Preview
@Composable
fun CalendarScreenPreview() {
    CalendarScreen()
}