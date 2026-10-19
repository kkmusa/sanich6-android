package com.example.sanich.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun SpiritLevelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    var gravity by remember { mutableStateOf(floatArrayOf(0f, 0f, 9.81f)) }
    var received by remember { mutableStateOf(false) }
    var vertical by remember { mutableStateOf(false) }
    var tolerance by remember { mutableFloatStateOf(0.5f) }
    var zeroX by remember { mutableFloatStateOf(0f) }
    var zeroY by remember { mutableFloatStateOf(0f) }
    var zeroVertical by remember { mutableFloatStateOf(0f) }

    DisposableEffect(sensor) {
        if (sensor == null) onDispose { }
        else {
            val listener = object : SensorEventListener {
                private val filtered = FloatArray(3)
                private var initialized = false
                override fun onSensorChanged(event: SensorEvent) {
                    if (!initialized) {
                        for (i in 0..2) filtered[i] = event.values[i]
                        initialized = true
                    } else {
                        val alpha = if (sensor.type == Sensor.TYPE_GRAVITY) 0.30f else 0.08f
                        for (i in 0..2) filtered[i] += alpha * (event.values[i] - filtered[i])
                    }
                    gravity = filtered.copyOf()
                    received = true
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }

    // Angles relative to physical gravity, not to a user-defined zero.
    // A flat phone has gX = gY = 0; a vertical screen plane has gZ = 0.
    val gx = gravity[0].toDouble()
    val gy = gravity[1].toDouble()
    val gz = gravity[2].toDouble()
    val magnitude = sqrt(gx * gx + gy * gy + gz * gz).coerceAtLeast(0.001)
    val dx = Math.toDegrees(atan2(gx, abs(gz).coerceAtLeast(0.0001))).toFloat()
    val dy = Math.toDegrees(atan2(gy, abs(gz).coerceAtLeast(0.0001))).toFloat()
    // One-axis plumb measurement: rotation around the device X axis only.
    // The other axis is deliberately excluded from this angle.
    // Long side of the phone is parallel to its local Y axis.
    // A straight wall requires that this edge is parallel to gravity.
    // Unlike atan2(gZ, gY), this angle is invariant to rotation about Y:
    // turning the screen toward/away from the wall does not change the result.
    val dv = Math.toDegrees(atan2(sqrt(gx * gx + gz * gz), abs(gy))).toFloat()
    val calibratedX = dx - zeroX
    val calibratedY = dy - zeroY
    val calibratedVertical = dv - zeroVertical
    val aligned = if (vertical) abs(calibratedVertical) <= tolerance else abs(calibratedX) <= tolerance && abs(calibratedY) <= tolerance
    val green = Color(0xFF8CC63E)

    Scaffold { insets ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(insets)) {
            CalculationTopBar("Электронный уровень", onBack)
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vertical = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!vertical) Color(0xFFFFC400) else Color(0xFFFFE7A3))) { Text("Горизонталь", color = Color.Black) }
                Button(onClick = { vertical = true }, colors = ButtonDefaults.buttonColors(containerColor = if (vertical) Color(0xFFFFC400) else Color(0xFFFFE7A3))) { Text("Вертикаль", color = Color.Black) }
            }
            OutlinedButton(onClick = {
                if (vertical) zeroVertical = dv else { zeroX = dx; zeroY = dy }
            }, enabled = received) { Text("Калибровка нуля") }
            if (sensor == null) {
                Text("На устройстве нет подходящего датчика положения.")
            } else if (!received) {
                Text("Ожидание показаний датчика…")
            } else {
                Canvas(Modifier.fillMaxWidth().height(245.dp).background(Color(0xFFFFF3D6), RoundedCornerShape(20.dp))) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    if (vertical) {
                        val vialWidth = 52.dp.toPx()
                        val vialHeight = size.height * 0.82f
                        drawRoundRect(Color(0xFFFFD982), Offset(center.x - vialWidth / 2, center.y - vialHeight / 2), androidx.compose.ui.geometry.Size(vialWidth, vialHeight), androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()))
                        drawLine(Color(0xFF9E6700), Offset(center.x - vialWidth, center.y), Offset(center.x + vialWidth, center.y), 2.dp.toPx())
                        val bubbleY = (-calibratedVertical / 10f).coerceIn(-1f, 1f) * (vialHeight / 2 - 26.dp.toPx())
                        drawCircle(if (aligned) green else Color(0xFFFFC400), 19.dp.toPx(), center + Offset(0f, bubbleY))
                        drawCircle(Color(0xFF9E6700), 19.dp.toPx(), center + Offset(0f, bubbleY), style = Stroke(2.dp.toPx()))
                    } else {
                        val radius = min(size.width, size.height) * 0.38f
                        drawCircle(Color(0xFFD18B22), radius, center, style = Stroke(3.dp.toPx()))
                        drawCircle(Color(0xFFE6AF4D), radius * 0.48f, center, style = Stroke(2.dp.toPx()))
                        drawLine(Color(0xFFD18B22), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), 2.dp.toPx())
                        drawLine(Color(0xFFD18B22), Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), 2.dp.toPx())
                        val bubbleX = (-calibratedX / 10f).coerceIn(-1f, 1f) * radius * 0.75f
                        val bubbleY = (calibratedY / 10f).coerceIn(-1f, 1f) * radius * 0.75f
                        drawCircle(if (aligned) green else Color(0xFFFFC400), 22.dp.toPx(), center + Offset(bubbleX, bubbleY))
                    }
                }
                if (vertical) {
                    Text("Отклонение длинной грани: %.2f°".format(abs(calibratedVertical)), style = MaterialTheme.typography.titleLarge)
                    Text("Приложите длинную боковую грань к стене. Измеряется вертикальность линии контакта; для проверки стены в другой плоскости поверните телефон на стене.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Продольный наклон: %+.2f°".format(calibratedY), style = MaterialTheme.typography.titleMedium)
                    Text("Поперечный наклон: %+.2f°".format(calibratedX), style = MaterialTheme.typography.titleMedium)
                    Text("Положите телефон задней стороной на устойчивую плоскость. Выступ камеры или чехол могут искажать показания.", style = MaterialTheme.typography.bodySmall)
                }
                Text(if (aligned) "В пределах допуска" else "Требуется выравнивание", color = if (aligned) Color(0xFF388E3C) else Color(0xFFAA6500))
                Text("Допуск: %.1f°".format(tolerance))
                Slider(value = tolerance, onValueChange = { tolerance = it }, valueRange = 0.1f..2f, steps = 18)
            }
            Text("Показания ориентировочные. Для ответственных работ используйте допущенный измерительный прибор. Проверяйте устойчивость телефона и допустимость его использования на объекте.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
