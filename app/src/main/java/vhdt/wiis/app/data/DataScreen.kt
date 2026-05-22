package vhdt.wiis.app.data

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import vhdt.wiis.R

@Composable
fun DataScreen(
    navController: NavController,
    viewModel: DataVM = viewModel()
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

        // Stats Header
        StatsHeader(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Installed Plugins Section
        InstalledPluginsSection(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Sync Buttons
        SyncButtonsSection(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Contributions Section
        ContributionsSection()
    }
}

@Composable
private fun StatsHeader(viewModel: DataVM) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF5B7FDF))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Plugin",
            value = "2",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Sự kiện",
            value = "12",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Báo thức",
            value = "5",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f),
        modifier = modifier
            .height(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun InstalledPluginsSection(viewModel: DataVM) {
    Column {
        Text(
            text = "PLUGIN ĐÃ CÀI",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Plugin 1
        PluginCard(
            icon = R.drawable.ic_launcher_foreground,
            name = "Đại học Bách Khoa",
            location = "hcmut.edu.vn - v2",
            status = "Tăng dụng",
            statusColor = Color(0xFF22C55E)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Plugin 2
        PluginCard(
            icon = R.drawable.ic_launcher_foreground,
            name = "Gym Aloha",
            location = "nhập key local",
            status = "Thỏ ỏng",
            statusColor = Color(0xFFEAB308)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Import Button
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1856BB)
            ),
            border = BorderDefaults.none.apply {
                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF1856BB)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "+ Import Plugin JSON",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PluginCard(
    icon: Int,
    name: String,
    location: String,
    status: String,
    statusColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF5B7FDF),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = location,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SyncButtonsSection(viewModel: DataVM) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ĐỒNG BỘ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        SyncButton(
            title = "Cập nhật lịch học",
            description = "Lấy lịch từ trường",
            onClick = { /* TODO */ }
        )

        SyncButton(
            title = "Đồng bộ lịch điện thoại",
            description = "Xuất sang Google Calendar",
            onClick = { /* TODO */ }
        )
    }
}

@Composable
private fun SyncButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
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

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1856BB)
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "Cập nhật",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ContributionsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ĐÓNG GÓP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        ContributionCard(
            icon = R.drawable.ic_launcher_foreground,
            title = "Cấp nhật",
            description = "lịch học",
            location = "HCMUT - tương lai",
            badge = "Cấp nhật"
        )

        ContributionCard(
            icon = R.drawable.ic_launcher_foreground,
            title = "Đồng bộ",
            description = "lịch điện thoại",
            location = "Google Calendar\nXuất sang\n27/05/2026 - 08:30",
            badge = "Đồng bộ"
        )
    }
}

@Composable
private fun ContributionCard(
    icon: Int,
    title: String,
    description: String,
    location: String,
    badge: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
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
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5B7FDF)
                    )
                }
                Text(
                    text = location,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 14.sp
                )
            }

            Button(
                onClick = { /* TODO */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1F5FE)
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1856BB)
                )
            }
        }
    }
}

object BorderDefaults {
    val none = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
}


//@Preview
//@Composable
//fun DataScreenPreview() {
//    DataScreen(
//    )
//}
