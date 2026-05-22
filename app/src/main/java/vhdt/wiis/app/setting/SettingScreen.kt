package vhdt.wiis.app.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import vhdt.wiis.R

@Composable
fun SettingScreen(
    navController: NavController,
    viewModel: SettingVM = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x00FFF0F0))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Alarm Section
        SettingSection(title = "BÁOÁO THỨC") {
            ToggleSettingItem(
                icon = R.drawable.ic_launcher_foreground,
                title = "Bật báo thức",
                description = "Gửi thông báo trước giờ học",
                isEnabled = viewModel.settings.value.alarmEnabled,
                onToggle = { viewModel.toggleAlarm(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NumericSettingItem(
                icon = R.drawable.ic_launcher_foreground,
                title = "Báo trước",
                value = viewModel.settings.value.notifyMinutesBefore,
                unit = "phút",
                onValueChange = { viewModel.setNotifyMinutesBefore(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NumericSettingItem(
                icon = R.drawable.ic_launcher_foreground,
                title = "Nhắc lại",
                value = viewModel.settings.value.snoozeMinutes,
                unit = "phút",
                onValueChange = { viewModel.setSnoozeMinutes(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notification Section
        SettingSection(title = "THÔNG BÁO") {
            ToggleSettingItem(
                icon = R.drawable.ic_launcher_foreground,
                title = "Bật thông báo",
                description = "Hiển thị thông báo trên màn hình",
                isEnabled = viewModel.settings.value.notificationEnabled,
                onToggle = { viewModel.toggleNotification(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ToggleSettingItem(
                icon = R.drawable.ic_launcher_foreground,
                title = "Giờ im lặng",
                description = "Không hiển thông báo trong thời gian này",
                isEnabled = viewModel.settings.value.quietHoursEnabled,
                onToggle = { viewModel.toggleQuietHours(it) }
            )

            if (viewModel.settings.value.quietHoursEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                TimeRangeSettingItem(
                    icon = R.drawable.ic_launcher_foreground,
                    title = "Bắt đầu - Kết thúc",
                    startTime = viewModel.settings.value.quietHoursStart,
                    endTime = viewModel.settings.value.quietHoursEnd,
                    onStartTimeChange = { viewModel.setQuietHoursStart(it) },
                    onEndTimeChange = { viewModel.setQuietHoursEnd(it) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ToggleSettingItem(
                icon = R.drawable.ic_launcher_foreground,
                title = "Rung khi báo",
                description = "Thiết bị rung khi có thông báo",
                isEnabled = viewModel.settings.value.vibrationEnabled,
                onToggle = { viewModel.toggleVibration(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Section
        SettingSection(title = "DỮ LIỆU") {
            DeleteDataButton(
                onDelete = { viewModel.deleteAllData() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Version
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WhyIsSunday v1.0.0 - build 1",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}

@Composable
private fun ToggleSettingItem(
    icon: Int,
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isEnabled) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFDEE5F4),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color(0xFF5B7FDF),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF5B7FDF),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE2E8F0)
                )
            )
        }
    }
}

@Composable
private fun NumericSettingItem(
    icon: Int,
    title: String,
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFDEE5F4),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color(0xFF5B7FDF),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5FE),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEE5F4))
            ) {
                Text(
                    text = "$value $unit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1856BB),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSettingItem(
    icon: Int,
    title: String,
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFDEE5F4),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color(0xFF5B7FDF),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5FE),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEE5F4))
            ) {
                Text(
                    text = "$startTime-$endTime",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1856BB),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteDataButton(
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDelete() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEDD5),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Xóa toàn bộ dữ liệu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Plugin, lịch, alarm",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

