package vhdt.wiis.app.data

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun SyncWeeksSelector(
    weeks: Int,
    onWeeksChange: (Int) -> Unit
) {
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
                onValueChange = { onWeeksChange(it.toInt()) },
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

@Preview(showBackground = true)
@Composable
fun SyncWeeksSelectorPreview() {
    SyncWeeksSelector(4, onWeeksChange = {})
}