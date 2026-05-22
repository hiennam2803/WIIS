package vhdt.wiis.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import vhdt.wiis.R

sealed class BotBarScreen(val route: String, val title: String, val iconRes: Int) {
    object Calendar : BotBarScreen("calendar", "Lịch Trình", R.drawable.calendar_today_24px)
    object Data : BotBarScreen("data", "Dữ liệu", R.drawable.data_table_24px)
    object Setting : BotBarScreen("setting", "Cài Đặt", R.drawable.settings_24px)
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
                            popUpTo(navController.graph.startDestinationId) {
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

@Preview
@Composable
fun BotBarPreview() {
    BotBar(navController = rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarLogoMenu(
    onMenuClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        clip = false
                    )
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
            IconButton(onClick = onMenuClick) {
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
}

@Preview
@Composable
fun TopBarLogoMenuPreview() {
    TopBarLogoMenu()
}