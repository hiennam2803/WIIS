package vhdt.wiis

import android.Manifest
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
import vhdt.wiis.scheduler.AlarmScheduler
import vhdt.wiis.scheduler.NotificationHelper
import vhdt.wiis.model.SchoolPlugin

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

		// Sửa lại chỗ này, xóa hết sạch chữ TODO() đi nha mày
		val testItem = ScheduleItem(
			id = "test_123",
			title = "Test báo thức chiến thần",
			location = "Phòng gym",
			startTime = System.currentTimeMillis() + (30 * 1000L), // Báo thức sau 30 giây
			note = "Húc tạ thôi anh em ơi", // Giả lập chuỗi rỗng hoặc chữ gì cũng được
			endTime = System.currentTimeMillis() + (60 * 60 * 1000L), // Giả lập thêm 1 tiếng
			type = ScheduleType.PERSONAL, // Loại cá nhân cho dễ test
			classCode = "CHIENTHAN101",
			fromPeriod = 1,
			toPeriod = 3,
			address = "Địa chỉ phòng gym"
		)

		// Gọi hàm đặt báo thức ngon lành cành đào, không sợ bị crash nữa
		AlarmScheduler.scheduleAlarm(this, testItem, 0)

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