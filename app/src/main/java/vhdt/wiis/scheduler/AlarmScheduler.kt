package vhdt.wiis.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import vhdt.wiis.model.ScheduleItem
import java.security.MessageDigest

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
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm for item: ${item.id}", e)
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
    
    private fun generateRequestCode(itemId: String): Int {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(itemId.toByteArray())
        val hex = messageDigest.joinToString("") { "%02x".format(it) }
        return hex.hashCode()
    }
}
