package vhdt.wiis.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import vhdt.wiis.app.calendar.CalendarScreen
import vhdt.wiis.app.data.DataScreen
import vhdt.wiis.app.setting.SettingScreen
import vhdt.wiis.app.splash.SplashScreen
import vhdt.wiis.R

object NavRoutes {
	const val Splash = "splash"
	const val Calendar = "calendar"
	const val Setting = "setting"
	const val Data = "data"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
	navController: NavHostController,
	modifier: Modifier = Modifier
) {
	val navBackStackEntry = navController.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry.value?.destination?.route
	val showBottomBar = currentRoute == NavRoutes.Calendar ||
		currentRoute == NavRoutes.Data ||
		currentRoute == NavRoutes.Setting

	Box(modifier = Modifier.fillMaxSize()) {
		if (showBottomBar) {
			Image(
				painter = painterResource(id = R.drawable.background),
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
		}

		Scaffold(
			containerColor = Color.Transparent,
			topBar = {
				if (showBottomBar) {
					TopBarLogoMenu()
				}
			},
			bottomBar = {
				if (showBottomBar) {
					BotBar(navController)
				}
			}
		) { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = NavRoutes.Splash,
				modifier = modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {

				composable(NavRoutes.Splash) {
					SplashRoute(navController)
				}

				composable(NavRoutes.Calendar) {
					CalendarScreen()
				}

				composable(NavRoutes.Setting) {
					SettingScreen(navController)
				}

				composable(NavRoutes.Data) {
					DataScreen(navController)
				}
			}
		}
	}
}

@Composable
private fun SplashRoute(
	navController: NavHostController
) {
	SplashScreen(navController)
}