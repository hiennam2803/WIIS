package vhdt.wiis.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.serialization.json.Json
import vhdt.wiis.model.ScheduleItem

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                Log.d(TAG, "Device boot completed, rescheduling alarms")
                
                // Load saved schedule items
                val regularPrefs = context.getSharedPreferences("schedule_data", Context.MODE_PRIVATE)
                val jsonString = regularPrefs.getString("schedule_items", null)
                
                if (!jsonString.isNullOrEmpty()) {
                    val json = Json { ignoreUnknownKeys = true }
                    val items: List<ScheduleItem> = try {
                        json.decodeFromString(jsonString)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing schedule items", e)
                        return
                    }
                    
                    val appPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                    val minutesBefore = appPrefs.getInt("notifyMinutesBefore", 60)
                    
                    // Re-schedule all future alarms
                    val futureItems = items.filter { it.startTime > System.currentTimeMillis() }
                    AlarmScheduler.scheduleAllAlarms(context, futureItems, minutesBefore)
                    Log.d(TAG, "Re-scheduled ${futureItems.size} alarms")
                }
                
                // Re-schedule daily summary alarm
                AlarmScheduler.scheduleDailySummaryAlarm(context)
                Log.d(TAG, "Daily summary alarm re-scheduled")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in onReceive", e)
            }
        }
    }
}
