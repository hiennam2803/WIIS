package vhdt.wiis.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import vhdt.wiis.model.ScheduleItem
import java.security.MessageDigest
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    private const val EXTRA_ITEM_ID = "extra_item_id"
    private const val EXTRA_TITLE = "extra_title"
    private const val EXTRA_LOCATION = "extra_location"
    private const val EXTRA_START_TIME = "extra_start_time"
    
    fun scheduleAlarm(context: Context, item: ScheduleItem, minutesBefore: Int) {
        val triggerTime = item.startTime - (minutesBefore * 60 * 1000L)
        
        if (triggerTime <= System.currentTimeMillis()) {
            Log.w(TAG, "Trigger time is in the past for item: ${item.id}")
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = generateRequestCode(item.id)
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_TITLE, item.title)
            putExtra(EXTRA_LOCATION, item.location)
            putExtra(EXTRA_START_TIME, item.startTime)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Alarm scheduled for item: ${item.id} at $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "Lỗi bảo mật do thiếu quyền chính xác: ${item.id}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi trời ơi đất hỡi gì đó khác: ${item.id}", e)
        }
    }
    
    fun cancelAlarm(context: Context, itemId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = generateRequestCode(itemId)
        
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Alarm cancelled for item: $itemId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm for item: $itemId", e)
        }
    }
    
    fun scheduleAllAlarms(context: Context, items: List<ScheduleItem>, minutesBefore: Int) {
        items.forEach { item ->
            scheduleAlarm(context, item, minutesBefore)
        }
    }
    
    fun cancelAllAlarms(context: Context, items: List<ScheduleItem>) {
        items.forEach { item ->
            cancelAlarm(context, item.id)
        }
    }
    
    fun scheduleDayAlarms(
        context: Context,
        items: List<ScheduleItem>,
        minutesBefore: Int
    ) {
        val today = System.currentTimeMillis()
        val todayItems = items.filter { item ->
            item.startTime > today - (minutesBefore * 60 * 1000L)
        }
        todayItems.forEach { scheduleAlarm(context, it, minutesBefore) }
    }
    
    fun scheduleDailySummaryAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailySummaryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d(TAG, "Daily summary alarm scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling daily summary alarm", e)
        }
    }
    
    private fun generateRequestCode(itemId: String): Int {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(itemId.toByteArray())
        val hex = messageDigest.joinToString("") { "%02x".format(it) }
        return hex.hashCode()
    }
}
