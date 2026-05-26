package vhdt.wiis.scheduler

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.serialization.json.Json
import vhdt.wiis.model.ScheduleItem
import vhdt.wiis.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class DailySummaryReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "DailySummaryReceiver"
        private const val CHANNEL_ID = "wiis_schedule_channel"
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        try {
            NotificationHelper.createNotificationChannel(context)
            
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val notificationEnabled = prefs.getBoolean("notificationEnabled", true)
            
            if (!notificationEnabled) {
                Log.d(TAG, "Notifications disabled")
                return
            }
            
            val regularPrefs = context.getSharedPreferences("schedule_data", Context.MODE_PRIVATE)
            val json = Json { ignoreUnknownKeys = true }
            val jsonString = regularPrefs.getString("schedule_items", null) ?: return
            
            val items: List<ScheduleItem> = try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing schedule items", e)
                return
            }
            
            val today = LocalDate.now()
            val todayItems = items.filter { item ->
                val itemDate = Instant.ofEpochMilli(item.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                itemDate == today
            }.sortedBy { it.startTime }
            
            if (todayItems.isEmpty()) {
                Log.d(TAG, "No items for today")
                return
            }
            
            val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale("vi", "VN"))
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dayOfWeekText = today.format(dayOfWeekFormatter)
            val dateText = today.format(dateFormatter)
            
            val summaryTitle = "Lịch hôm nay - $dayOfWeekText"
            val summaryBody = "${todayItems.size} buổi học/tập hôm nay"
            
            val inboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle(summaryTitle)
            inboxStyle.setSummaryText("$dayOfWeekText, $dateText")
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale("vi", "VN"))
            todayItems.forEach { item ->
                val timeStr = timeFormat.format(Date(item.startTime))
                val line = "$timeStr - ${item.title} (${item.location})"
                inboxStyle.addLine(line)
            }
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(summaryTitle)
                .setContentText(summaryBody)
                .setStyle(inboxStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .build()
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(8888, notification)
            Log.d(TAG, "Summary notification shown for $todayItems.size items")
            
            // Schedule individual alarms for today's items
            val minutesBefore = prefs.getInt("notifyMinutesBefore", 60)
            todayItems.forEach { item ->
                AlarmScheduler.scheduleAlarm(context, item, minutesBefore)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive", e)
        }
    }
}
