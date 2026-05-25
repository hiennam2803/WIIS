package vhdt.wiis.app.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import vhdt.wiis.R
import vhdt.wiis.component.NavRoutes

@Composable
fun SplashScreen(
	navController: NavController
) {
	val composition by rememberLottieComposition(
		LottieCompositionSpec.RawRes(R.raw.splash)
	)

	val progress by animateLottieCompositionAsState(
		composition = composition,
		iterations = 1
	)
	val hasNavigated = remember { mutableStateOf(false) }

	LaunchedEffect(key1 = progress) {
		if (!hasNavigated.value && progress >= 1.0f) {
			hasNavigated.value = true
			navController.navigate(NavRoutes.Calendar) {
				popUpTo(navController.graph.startDestinationId) {
					inclusive = true
				}
				launchSingleTop = true
			}
		}
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.White)
	) {

		LottieAnimation(
			composition = composition,
			progress = { progress },
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Crop
		)
	}
}
