package vhdt.wiis.component

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vhdt.wiis.R
import java.io.OutputStream

sealed class BotBarScreen(val route: String, val title: String, val iconRes: Int) {
    object Calendar : BotBarScreen(NavRoutes.Calendar, "Lịch Trình", R.drawable.calendar_today_24px)
    object Data : BotBarScreen(NavRoutes.Data, "Dữ liệu", R.drawable.data_table_24px)
    object Setting : BotBarScreen(NavRoutes.Setting, "Cài Đặt", R.drawable.settings_24px)
}

@Composable
fun BotBar(navController: NavController) {
    val screens = listOf(
        BotBarScreen.Calendar,
        BotBarScreen.Data,
        BotBarScreen.Setting
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF639EEA),
            Color(0xFF1856BB)
        )
    )

    NavigationBar(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(gradientBrush),
        containerColor = Color.Transparent
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        screens.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                selected = isSelected,
                label = {
                    Text(
                        text = screen.title,
                        fontSize = if (isSelected) 13.sp else 11.sp,
                        fontWeight = if (isSelected) FontWeight.W800 else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconRes),
                        contentDescription = screen.title
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFFFFF),
                    selectedTextColor = Color(0xFFFFFFFF),
                    unselectedIconColor = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                    unselectedTextColor = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                    indicatorColor = Color(0xFFFFFFFF).copy(alpha = 0.25f)
                ),
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(NavRoutes.Calendar) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarLogoMenu(
    onMenuClick: () -> Unit = {}
) {
    var showDonateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wiis_logo),
                    contentDescription = "WIIS",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.padding(6.dp)
                )
            }
        },
        navigationIcon = {},
        actions = {
            IconButton(onClick = { showDonateDialog = true }) {
                Icon(
                    painter = painterResource(R.drawable.volunteer_activism_24px),
                    contentDescription = "Menu",
                    tint = Color(0xFF1F4BA1),
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )

    if (showDonateDialog) {
        AlertDialog(
            onDismissRequest = { showDonateDialog = false },
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Header

                    Text(
                        text = "Ủng Hộ WIIS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mọi đóng góp đều giúp ứng dụng phát triển ổn định hơn và có thêm nhiều tính năng mới trong tương lai. Cảm ơn bạn đã ủng hộ!",
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFF5F7FB),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.donation_qr),
                            contentDescription = "QR Donate",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Download Button
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    val success = saveQrToGallery(
                                        context,
                                        R.drawable.donation_qr
                                    )

                                    Toast.makeText(
                                        context,
                                        if (success) "Đã lưu ảnh QR"
                                        else "Không thể tải ảnh",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 8.dp, y = 8.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape
                                ),
                            shape = CircleShape,
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F4BA1)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.download_24px),
                                contentDescription = "Download",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Close Button
                    TextButton(
                        onClick = { showDonateDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF5B8DEF),
                                        Color(0xFF1F4BA1)
                                    )
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                    ) {
                        Text(
                            text = "Đóng",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        )
    }
}

suspend fun saveQrToGallery(context: Context, resId: Int): Boolean = withContext(Dispatchers.IO) {
    try {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        val filename = "WIIS_Donate_QR_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/WIIS_Donate")
            }
        }

        val imageUri: Uri? = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        imageUri?.let { uri ->
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            return@withContext true
        }
        return@withContext false
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext false
    }
}

@Preview
@Composable
fun BotBarPreview() {
    BotBar(navController = rememberNavController())
}

@Preview
@Composable
fun TopBarLogoMenuPreview() {
    TopBarLogoMenu()
}