package vhdt.wiis.app.setting

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import vhdt.wiis.R

// ── Design tokens ─────────────────────────────────────────────────────────────
private val Primary        = Color(0xFF1856BB)
private val PrimaryLight   = Color(0xFFE8F0FE)
private val Purple         = Color(0xFF7C3AED)
private val PurpleLight    = Color(0xFFEDE9FE)
private val Danger         = Color(0xFFEF4444)
private val DangerLight    = Color(0xFFFFECEC)
private val CardBg         = Color(0xFFFFFFFF)
private val PageBg         = Color(0x00FFFFFF)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF94A3B8)
private val Divider        = Color(0xFFF1F5F9)
// ──────────────────────────────────────────────────────────────────────────────

private fun safeDrawable(context: Context, name: String): Int {
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (id != 0) id else R.drawable.ic_launcher_foreground
}

@Composable
fun SettingScreen(
    navController: NavController,
    viewModel: SettingVM = viewModel()
) {
    val ctx = LocalContext.current
    val settings = viewModel.settings.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Cài đặt",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tuỳ chỉnh ứng dụng",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section: THÔNG BÁO ────────────────────────────────────────────
            SectionLabel("THÔNG BÁO")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Thông báo toggle
                    SettingToggleRow(
                        iconRes = safeDrawable(ctx, "notifications_24px"),
                        iconBg = PrimaryLight,
                        iconTint = Primary,
                        title = "Thông báo",
                        subtitle = "Hiển thị nhắc nhở trên thiết bị",
                        checked = settings.notificationEnabled,
                        trackColor = Primary,
                        onChecked = { viewModel.toggleNotification(it) }
                    )

                    HorizontalDivider(color = Divider, thickness = 1.dp)

                    // Giờ im lặng toggle
                    SettingToggleRow(
                        iconRes = safeDrawable(ctx, "alarm_24px"),
                        iconBg = PurpleLight,
                        iconTint = Purple,
                        title = "Giờ im lặng",
                        subtitle = "Tắt thông báo trong khoảng thời gian",
                        checked = settings.quietHoursEnabled,
                        trackColor = Purple,
                        onChecked = { viewModel.toggleQuietHours(it) }
                    )

                    // Time range picker (animate expand)
                    AnimatedVisibility(
                        visible = settings.quietHoursEnabled,
                        enter = expandVertically(animationSpec = tween(250)),
                        exit = shrinkVertically(animationSpec = tween(200))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFAFBFF))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Khoảng thời gian im lặng",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TimePickerBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Bắt đầu",
                                    time = settings.quietHoursStart,
                                    accentColor = Purple,
                                    onTimePicked = { viewModel.setQuietHoursStart(it) }
                                )
                                TimePickerBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Kết thúc",
                                    time = settings.quietHoursEnd,
                                    accentColor = Purple,
                                    onTimePicked = { viewModel.setQuietHoursEnd(it) }
                                )
                            }
                        }
                    }
                }
            }


            // ── Footer version ────────────────────────────────────────────────
            Text(
                text = "WhyIsSunday v1.0.0",
                fontSize = 11.sp,
                color = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

}

// ── Reusable composables ───────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 6.dp)
    )
}

@Composable
private fun SettingToggleRow(
    iconRes: Int,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    trackColor: Color,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onChecked(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedTrackColor = trackColor,
                uncheckedTrackColor = Color(0xFFE2E8F0),
                uncheckedThumbColor = Color.White
            )
        )
    }
}

@Composable
private fun TimePickerBox(
    modifier: Modifier = Modifier,
    label: String,
    time: String,
    accentColor: Color,
    onTimePicked: (String) -> Unit
) {
    val ctx = LocalContext.current

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.08f), accentColor.copy(alpha = 0.04f))
                    )
                )
                .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val parts = time.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 22
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    TimePickerDialog(ctx, { _, hour, minute ->
                        val hh = if (hour < 10) "0$hour" else "$hour"
                        val mm = if (minute < 10) "0$minute" else "$minute"
                        onTimePicked("$hh:$mm")
                    }, h, m, true).show()
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = time,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

