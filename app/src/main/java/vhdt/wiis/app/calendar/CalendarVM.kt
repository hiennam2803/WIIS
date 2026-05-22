package vhdt.wiis.app.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate
import java.time.YearMonth
import vhdt.wiis.model.ScheduleItem

//data class TaskItem(
//    val date: LocalDate,
//    val time: String,
//    val title: String,
//    val details: String
//)

@RequiresApi(Build.VERSION_CODES.O)
class CalendarVM : ViewModel() {
    val today = LocalDate.now()
    val selectedDate = mutableStateOf(today)
    val displayMonth = mutableStateOf(YearMonth.now())
    val expandedItems = mutableStateOf(setOf(2))

    val tasks = listOf(
        TaskItem(
            date = today,
            time = "08:30",
            title = "Họp phòng ban - Triển khai dự án mới",
            details = "Thảo luận với team design về UI mới của app WIIS. Nhớ chuẩn bị file báo cáo tiến độ."
        ),
        TaskItem(
            date = today,
            time = "14:00",
            title = "Kiểm tra hệ thống - Backend API",
            details = "Fix bug lỗi endpoint đồng bộ lịch trình và tối ưu hóa tốc độ tải database."
        ),
        TaskItem(
            date = today,
            time = "17:00",
            title = "Gym time - Giải cứu thế giới",
            details = "Bài tập chiến thần hôm nay: Deadlift 65kg, Bench Press nâng ngực cải thiện lõm. Đừng quên mang nước!"
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
        return tasks.any { it.date == date }
    }
}
