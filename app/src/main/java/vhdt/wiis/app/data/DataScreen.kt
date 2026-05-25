package vhdt.wiis.app.data

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import vhdt.wiis.R
import vhdt.wiis.model.SyncState
import vhdt.wiis.data.remote.WebViewLoginScreen
import vhdt.wiis.model.SchoolPlugin
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DataScreen(
    navController: NavController,
    viewModel: DataVM = viewModel()
) {
    var showLoginWebView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.importPluginJson(it) } }

    // Memoize filteredPlugins để tránh recalculate mỗi frame
    val filteredPlugins = remember(searchQuery, viewModel.plugins) {
        viewModel.plugins.filter {
            it.schoolName.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(viewModel.syncState.value) {
        val state = viewModel.syncState.value
        if (state is SyncState.Error && state.message == "AUTH_REQUIRED") {
            showLoginWebView = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x00FAFAFA))
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            SyncButtonSection(
                viewModel = viewModel,
                onSyncClicked = {
                    if (viewModel.getJwtToken() == null) {
                        showLoginWebView = true
                    } else {
                        viewModel.syncSchedule()
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            StatsHeader(viewModel)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SyncWeeksSelector(viewModel)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "PLUGIN ĐÃ CÀI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm plugin...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search_24px),
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filteredPlugins.isEmpty()) {
            item {
                Text(
                    text = "Không tìm thấy plugin",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(filteredPlugins, key = { it.schoolName }) { plugin ->
                PluginCard(
                    name = plugin.schoolName,
                    location = plugin.loginUrl,
                    status = "Đang dùng",
                    statusColor = Color(0xFF22C55E)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Button(
                onClick = { launcher.launch("application/json") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(
                        width = 2.dp,
                        color = Color(0xFF1856BB),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1856BB)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF1856BB)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Import Plugin JSON",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // WebView
    val activePlugin = viewModel.plugins.firstOrNull()
    if (showLoginWebView && activePlugin != null) {
        WebViewLoginScreen(
            plugin = activePlugin,
            onTokenReceived = { token ->
                viewModel.saveJwtToken(token)
                showLoginWebView = false
                viewModel.syncSchedule()
            },
            onDismiss = {
                showLoginWebView = false
            }
        )
    }
}

@Composable
private fun SyncButtonSection(
    viewModel: DataVM,
    onSyncClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isPressed = remember { mutableStateOf(false) }
        val isSyncing = viewModel.syncState.value is SyncState.Loading

        // Scale animation — thu nhỏ khi bấm
        val scale by animateFloatAsState(
            targetValue = if (isPressed.value) 0.92f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "scale"
        )

        // Rotation animation — xoay khi đang sync
        val rotation by animateFloatAsState(
            targetValue = if (isSyncing) 360f else 0f,
            animationSpec = if (isSyncing)
                infiniteRepeatable(tween(1000, easing = LinearEasing))
            else
                tween(300),
            label = "rotation"
        )

        // Halo pulse khi đang sync
        val haloAlpha by animateFloatAsState(
            targetValue = if (isSyncing) 0.25f else 0.12f,
            animationSpec = infiniteRepeatable(
                tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "halo"
        )

        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A87A3).copy(alpha = haloAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF462BD7),
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed.value = true
                                tryAwaitRelease()
                                isPressed.value = false
                            },
                            onTap = { onSyncClicked() }
                        )
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sync_24px),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = viewModel.syncState.value) {
            is SyncState.Loading -> {
                Text(
                    text = "Đang đồng bộ...",
                    fontSize = 16.sp,
                    color = Color(0xFF1856BB),
                    fontWeight = FontWeight.SemiBold
                )
            }

            is SyncState.Success -> {
                Text(
                    text = "Đồng bộ thành công!",
                    fontSize = 16.sp,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.SemiBold
                )
            }

            is SyncState.Error -> {
                Text(
                    text = "Đồng bộ thất bại: ${state.message}",
                    fontSize = 16.sp,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.SemiBold
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun SyncWeeksSelector(viewModel: DataVM) {
    val weeks = viewModel.syncWeeks.value

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Phạm vi cập nhật",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    "$weeks tuần",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1856BB)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Slider(
                value = weeks.toFloat(),
                onValueChange = { viewModel.setSyncWeeks(it.toInt()) },
                valueRange = 1f..8f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF1856BB),
                    activeTrackColor = Color(0xFF1856BB),
                    inactiveTrackColor = Color(0xFFE2E8F0)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 tuần", fontSize = 10.sp, color = Color(0xFF94A3B8))
                Text("8 tuần", fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun StatsHeader(viewModel: DataVM) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Plugin",
            value = viewModel.pluginCount.value.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Sự kiện",
            value = viewModel.eventCount.value.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Báo thức",
            value = viewModel.alarmCount.value.toString(),
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
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1856BB)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun PluginCard(
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
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
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
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
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
