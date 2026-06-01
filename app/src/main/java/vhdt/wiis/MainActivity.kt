package vhdt.wiis

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import vhdt.wiis.component.NavGraph
import vhdt.wiis.model.ScheduleItem
import vhdt.wiis.model.ScheduleType
import vhdt.wiis.scheduler.NotificationHelper

class MainActivity : ComponentActivity() {
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }


    private fun checkXiaomiWhitelist() {
        if (isXiaomiDevice()) {
            startActivity(Intent().apply {
                component = ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                )
            })
        }
    }

    private fun isXiaomiDevice(): Boolean {
        return Build.MANUFACTURER.lowercase().contains("xiaomi") || 
               Build.BRAND.lowercase().contains("xiaomi")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create notification channel on app startup
        NotificationHelper.createNotificationChannel(this)
        
        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // No exact-alarm permission required when using AlarmClock intents

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}