package vhdt.wiis.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import vhdt.wiis.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "wiis_schedule_channel"
    private const val CHANNEL_NAME = "Lịch học WIIS"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo lịch học từ ứng dụng WIIS"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }
    
    fun showNotification(
        context: Context,
        title: String,
        location: String,
        startTime: Long,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale("vi", "VN"))
            val timeString = timeFormat.format(Date(startTime))
            
            val notificationTitle = "⏰ $title"
            val notificationBody = "Bắt đầu lúc $timeString tại $location"
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(notificationBody)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .build()
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Notification shown: $notificationTitle")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }
}
