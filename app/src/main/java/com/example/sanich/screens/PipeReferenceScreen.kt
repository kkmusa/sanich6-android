package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

internal data class PipeSize(val nominal: String, val outside: Double, val walls: List<Double>)

internal val casingGost632 = listOf(
    PipeSize("114", 114.0, listOf(5.2, 5.7, 6.4, 7.4, 8.6, 10.2)),
    PipeSize("127", 127.0, listOf(5.6, 6.4, 7.5, 9.2, 10.7)),
    PipeSize("140", 140.0, listOf(6.2, 7.0, 7.7, 9.2, 10.5)),
    PipeSize("146", 146.0, listOf(6.5, 7.0, 7.7, 8.5, 9.5, 10.7)),
    PipeSize("168", 168.0, listOf(7.3, 8.0, 8.9, 10.6, 12.1)),
    PipeSize("178", 178.0, listOf(5.9, 6.9, 8.1, 9.2, 10.4, 11.5, 12.7, 13.7, 15.0)),
    PipeSize("194", 194.0, listOf(7.6, 8.3, 9.5, 10.9, 12.7, 15.1))
)

private val tubingGost633 = listOf(
    PipeSize("60", 60.3, listOf(5.0)),
    PipeSize("73", 73.0, listOf(5.5, 7.0)),
    PipeSize("89", 88.9, listOf(6.5, 8.0))
)

@Composable
internal fun PipeReferenceScreen(title: String, standard: String, pipes: List<PipeSize>, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val scroll = rememberScrollState()
    Scaffold { pv ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(pv)) {
            CalculationTopBar(title, onBack)
            Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(standard, fontWeight = FontWeight.Bold)
                pipes.forEach { pipe ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("${pipe.nominal} мм", fontWeight = FontWeight.Bold)
                            pipe.walls.forEach { wall ->
                                val inner = pipe.outside - 2 * wall
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Стенка ${wall.prettyPipe()} мм")
                                    Text("Dвн ${inner.prettyPipe()} мм")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CasingGostReferenceScreen(onBack: () -> Unit) = PipeReferenceScreen(
    title = "Обсадные трубы",
    standard = "ГОСТ 632-80 — трубы обсадные и муфты к ним. В справочнике приведены используемые в приложении ЭК 114–194 мм; 219 мм не включена.",
    pipes = casingGost632,
    onBack = onBack
)

@Composable
fun TubingGostReferenceScreen(onBack: () -> Unit) = PipeReferenceScreen(
    title = "НКТ",
    standard = "ГОСТ 633-80 — трубы насосно-компрессорные и муфты к ним. Для НКТ 60, 73 и 89 мм показаны наружный диаметр, толщины стенок и внутренний диаметр.",
    pipes = tubingGost633,
    onBack = onBack
)

private fun Double.prettyPipe(): String = String.format(Locale.US, "%.1f", this).replace(".0", "")
