package vhdt.wiis.app.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import vhdt.wiis.model.ScheduleItem
import vhdt.wiis.model.ScheduleType

@RequiresApi(Build.VERSION_CODES.O)
class CalendarVM : ViewModel() {
    val today = LocalDate.now()
    val selectedDate = mutableStateOf(today)
    val displayMonth = mutableStateOf(YearMonth.now())
    val expandedItems = mutableStateOf(setOf(2))

    val tasks = listOf(
        ScheduleItem(
            id = "task_1",
            title = "Họp phòng ban - Triển khai dự án mới",
            location = "Office",
            note = "Thảo luận với team design về UI mới của app WIIS. Nhớ chuẩn bị file báo cáo tiến độ.",
            startTime = today.atTime(8, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endTime = today.atTime(9, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            type = ScheduleType.UNIVERSITY
        ),
        ScheduleItem(
            id = "task_2",
            title = "Kiểm tra hệ thống - Backend API",
            location = "Office",
            note = "Fix bug lỗi endpoint đồng bộ lịch trình và tối ưu hóa tốc độ tải database.",
            startTime = today.atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endTime = today.atTime(15, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            type = ScheduleType.UNIVERSITY
        ),
        ScheduleItem(
            id = "task_3",
            title = "Gym time - Giải cứu thế giới",
            location = "Gym",
            note = "Bài tập chiến thần hôm nay: Deadlift 65kg, Bench Press nâng ngực cải thiện lõm. Đừng quên mang nước!",
            startTime = today.atTime(17, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endTime = today.atTime(18, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            type = ScheduleType.PERSONAL
        )
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun goToPreviousMonth() {
        displayMonth.value = displayMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        displayMonth.value = displayMonth.value.plusMonths(1)
    }

    fun toggleTaskExpanded(index: Int) {
        expandedItems.value = if (expandedItems.value.contains(index)) {
            expandedItems.value - index
        } else {
            expandedItems.value + index
        }
    }

    fun hasTasksOnDate(date: LocalDate): Boolean {
        return tasks.any { task ->
            val taskDate = Instant.ofEpochMilli(task.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            taskDate == date
        }
    }
}
