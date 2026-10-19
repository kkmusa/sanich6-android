package com.example.sanich.screens

import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private fun Double.displayHz(): String = String.format(java.util.Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorCoefficientScreen(historyViewModel: CalculationHistoryViewModel, onBack: () -> Unit) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var rpm by remember { mutableStateOf("") }
    var poles by remember { mutableStateOf("2") }
    var frequency by remember { mutableStateOf("") }
    var reverse by remember { mutableStateOf(false) }
    val n = rpm.replace(',', '.').toDoubleOrNull()
    val poleCount = poles.toIntOrNull()
    val f = frequency.replace(',', '.').toDoubleOrNull()
    val valid = n != null && n.isFinite() && n > 0 && poleCount != null && poleCount > 0 && poleCount % 2 == 0
    val base = if (valid) n!! * (poleCount!! / 2.0) / 60.0 else null
    val kf = base?.div(50.0)
    val result = if (kf != null && f != null && f.isFinite() && f >= 0) {
        if (reverse) f / kf else f * kf
    } else null
    val currentHistoryEntry = kf?.takeIf { it.isFinite() }?.let { coefficient ->
        HistoryEntry(title = "Коэффициент двигателя 2А", method = "Расчёт",
            result = buildString {
                append("КФ=${coefficient.displayHz()}")
                if (result != null && result.isFinite()) append("; ${if (reverse) "Обычная" else "Частота станции"}=${result.displayHz()} Гц")
            }, details = "Обороты=$rpm об/мин; полюсов=$poles" +
                (if (result != null && result.isFinite()) "; входная частота=$frequency Гц" else ""))
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)
    fun saveHistory() { latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) } }
    PersistHistoryOnDispose(::saveHistory, debounceKey = currentHistoryEntry?.let { "$rpm|$poles|$frequency|$reverse|${it.result}" })
    fun saveAndBack() { saveHistory(); onBack() }
    BackHandler(onBack = ::saveAndBack)
    Scaffold { insets ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(insets)) {
            CalculationTopBar("Коэффициент двигателя 2А габарита", ::saveAndBack)
            Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = rpm, onValueChange = { rpm = it }, label = { Text("Паспортные обороты, об/мин") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = poles, onValueChange = { poles = it }, label = { Text("Число полюсов 2p") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            if (!valid && (rpm.isNotBlank() || poles.isNotBlank())) {
                Text("Укажите положительные обороты и чётное число полюсов (2, 4, 6…).", color = MaterialTheme.colorScheme.error)
            }
            if (kf != null) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Базовая частота: ${base!!.displayHz()} Гц")
                        Text("Коэффициент КФ: ${kf.displayHz()}", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Text("Направление перевода", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !reverse, onClick = { reverse = false }, label = { Text("Обычная → станция") })
                FilterChip(selected = reverse, onClick = { reverse = true }, label = { Text("Станция → обычная") })
            }
            // Exchange the actual input and calculated output, then invert the direction.
            // Direction chips alone never modify input: prevents accidental cascading conversions.
            OutlinedButton(
                onClick = {
                    val converted = result
                    if (converted != null && converted.isFinite()) {
                        frequency = converted.displayHz()
                        reverse = !reverse
                    }
                },
                enabled = result != null && result.isFinite(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Поменять местами ↔")
            }
            OutlinedTextField(value = frequency, onValueChange = { frequency = it },
                label = { Text(if (reverse) "Частота станции, Гц" else "Обычная частота, Гц") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                modifier = Modifier.fillMaxWidth())
            if (result != null && result.isFinite()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (reverse) "Обычная частота" else "Частота для станции")
                        Text("${result.displayHz()} Гц", style = MaterialTheme.typography.headlineMedium)
                        Text(if (reverse) "Частота станции ÷ КФ" else "Обычная частота × КФ")
                    }
                }
            }
            Text("Расчёт по согласованной формуле КФ = n × (2p / 2) / 3000. Перед изменением частоты сверяйте допустимый диапазон с паспортом двигателя и настройками станции.",
                style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
