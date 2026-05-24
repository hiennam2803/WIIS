package vhdt.wiis.app.calendar

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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
            .background(Color(0x00FFF0F0))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header với nút prev/next
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2EEFF)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1856BB),
                modifier = Modifier
                    .clickable { viewModel.goToPreviousMonth() }
                    .padding(8.dp)
                    .size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back_ios_24px),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Tháng ${viewModel.displayMonth.value.monthValue} / ${viewModel.displayMonth.value.year}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1856BB),
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1856BB),
                modifier = Modifier
                    .clickable { viewModel.goToNextMonth() }
                    .padding(8.dp)
                    .size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        MonthCalendarGrid(
            displayMonth = viewModel.displayMonth.value,
            selectedDate = viewModel.selectedDate.value,
            onDateSelected = { viewModel.selectDate(it) },
            hasTasksOnDate = { viewModel.hasTasksOnDate(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        TodaySummaryCard(
            viewModel.selectedDate.value,
            viewModel.getTasksForDate(viewModel.selectedDate.value).size
        )

        Spacer(modifier = Modifier.height(24.dp))

        TimelineSection(viewModel)
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

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFFFF)),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dayLabels.forEach { day ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    Text(
                        day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        repeat(6) { weekIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(7) { dayIndex ->
                    val date = startDay.plusDays((weekIndex * 7 + dayIndex).toLong())
                    val isCurrentMonth = date.month == displayMonth.month
                    val isSelected = date == selectedDate

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> Color(0xFF205FC6)
                                    else -> Color(0x00FFFFFF)
                                }
                            )
                            .clickable(enabled = isCurrentMonth) { onDateSelected(date) }
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
                                    isCurrentMonth -> Color(0xFF131B24)
                                    else -> Color(0xFF9FADBF)
                                }
                            )
                            if (hasTasksOnDate(date)) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.White else Color(0xFF0868EF)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            if (weekIndex < 5) {
                Spacer(modifier = Modifier.height(4.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE)),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFF93C5FD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1856BB))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$dayOfWeekText, $dateText - $taskCount nhiệm vụ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimelineSection(viewModel: CalendarVM) {
    val tasksForSelectedDate = viewModel.getTasksForDate(viewModel.selectedDate.value)

    Column(modifier = Modifier.fillMaxWidth()) {
        tasksForSelectedDate.forEachIndexed { index, task ->
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
                    modifier = Modifier.width(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1856BB))
                    )
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(Color(0xFFCBD5E1))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // FIX 3: truyền đầy đủ task + endTime
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
                    modifier = Modifier.padding(bottom = if (isLast) 0.dp else 20.dp)
                )
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge giờ
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1856BB).copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = time,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1856BB)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    painter = painterResource(
                        id = if (isExpanded) R.drawable.arrow_drop_up_24px
                        else R.drawable.arrow_drop_down_24px
                    ),
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // FIX 1: dùng Box thay Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mã lớp
                if (task.classCode.isNotEmpty()) {
                    InfoRow(icon = "📋", label = "Mã lớp", value = task.classCode)
                }

                // Tiết học
                if (task.fromPeriod > 0) {
                    InfoRow(
                        icon = "📚",
                        label = "Tiết học",
                        value = "Tiết: ${task.fromPeriod} - ${task.toPeriod}"
                    )
                }

                // FIX 2: dùng $time thay $startTime
                InfoRow(
                    icon = "⏰",
                    label = "Thời gian",
                    value = "$time - $endTime"
                )

                // Phòng học
                InfoRow(
                    icon = "📍",
                    label = "Phòng học",
                    value = task.location
                )

                // Địa chỉ / Cơ sở
                if (task.address.isNotEmpty()) {
                    InfoRow(icon = "🏫", label = "Cơ sở", value = task.address)
                } else if (task.note.isNotEmpty()) {
                    InfoRow(icon = "🏫", label = "Cơ sở", value = task.note)
                }
            }
        }
    }
}

// FIX 4: thêm InfoRow function
@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = Color(0xFF334155)
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