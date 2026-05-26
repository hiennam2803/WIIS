package vhdt.wiis.scheduler

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
        private const val EXTRA_ITEM_ID = "extra_item_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_LOCATION = "extra_location"
        private const val EXTRA_START_TIME = "extra_start_time"
    }
    
    @RequiresPermission(Manifest.permission.VIBRATE)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val location = intent.getStringExtra(EXTRA_LOCATION) ?: ""
        val startTime = intent.getLongExtra(EXTRA_START_TIME, 0L)
        
        Log.d(TAG, "AlarmReceiver triggered for: $title")
        
        // Create notification channel first
        NotificationHelper.createNotificationChannel(context)
        
        // Read settings from SharedPreferences
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationEnabled = prefs.getBoolean("notificationEnabled", true)
        val quietHoursEnabled = prefs.getBoolean("quietHoursEnabled", true)
        val quietHoursStart = prefs.getString("quietHoursStart", "22:00") ?: "22:00"
        val quietHoursEnd = prefs.getString("quietHoursEnd", "07:00") ?: "07:00"
        val vibrationEnabled = prefs.getBoolean("vibrationEnabled", false)
        
        if (!notificationEnabled) {
            Log.d(TAG, "Notifications are disabled")
            return
        }
        
        // Check quiet hours
        if (isInQuietHours(quietHoursEnabled, quietHoursStart, quietHoursEnd)) {
            Log.d(TAG, "In quiet hours, notification suppressed")
            return
        }
        
        // Use itemId hash as notification ID to ensure uniqueness
        val notificationId = itemId.hashCode()
        NotificationHelper.showNotification(context, title, location, startTime, notificationId)
        
        // Trigger vibration if enabled
        if (vibrationEnabled) {
            triggerVibration(context)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isInQuietHours(quietHoursEnabled: Boolean, quietHoursStart: String, quietHoursEnd: String): Boolean {
        if (!quietHoursEnabled) return false
        
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val quietStart = LocalTime.parse(quietHoursStart, formatter)
        val quietEnd = LocalTime.parse(quietHoursEnd, formatter)
        
        return if (quietStart.isBefore(quietEnd)) {
            now.isAfter(quietStart) && now.isBefore(quietEnd)
        } else {
            now.isAfter(quietStart) || now.isBefore(quietEnd)
        }
    }
    
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun triggerVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
            Log.d(TAG, "Vibration triggered")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering vibration", e)
        }
    }
}
