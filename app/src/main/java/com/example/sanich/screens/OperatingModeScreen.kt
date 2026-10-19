package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.OperatingModeCalculator
import java.util.Locale

enum class OperatingModeConversion { PR_TO_UPR, UPR_TO_APV }

@Composable
fun OperatingModeScreen(
    conversion: OperatingModeConversion,
    historyViewModel: CalculationHistoryViewModel,
    onBack: () -> Unit
) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var rate by remember { mutableStateOf("") }
    var potential by remember { mutableStateOf("") }
    var supplyTime by remember { mutableStateOf("") }
    var drainTime by remember { mutableStateOf("60") }
    var cycle by remember { mutableStateOf(if (conversion == OperatingModeConversion.PR_TO_UPR) "60" else "360") }

    val calculation = runCatching {
        val q = rate.modeDecimal() ?: error("Заполните дебит УЭЦН")
        val a = potential.modeDecimal() ?: error("Заполните потенциал")
        val supply = supplyTime.modeDecimal() ?: error("Заполните время появления подачи")
        val selectedCycle = cycle.modeDecimal() ?: error("Заполните выбранный цикл")
        when (conversion) {
            OperatingModeConversion.PR_TO_UPR -> OperatingModeCalculator.toUpr(q, a, supply, selectedCycle)
            OperatingModeConversion.UPR_TO_APV -> OperatingModeCalculator.toApv(
                q, a, supply,
                drainTime.modeDecimal() ?: error("Заполните время слива"),
                selectedCycle
            )
        }
    }

    val currentHistoryEntry = calculation.getOrNull()?.let { value ->
        val resultText = when (value) {
            is com.example.sanich.calculator.UprResult -> "${value.totalRunMinutes.modeMinutes()} / ${(cycle.modeDecimal()!! - value.totalRunMinutes.modeMinutes()).toInt()} мин"
            is com.example.sanich.calculator.ApvResult -> if (value.cycleIsSuitable) "Работа ${value.totalRunMinutes.modeMinutes()} мин; отстой ${(cycle.modeDecimal()!! - value.totalRunMinutes.modeMinutes()).toInt()} мин; минимальный цикл ${value.minimumRequiredCycleMinutes} мин" else null
            else -> null
        }
        resultText?.let {
            HistoryEntry(
                title = if (conversion == OperatingModeConversion.PR_TO_UPR) "Перевод ПР → УПР" else "Перевод УПР → АПВ",
                method = "Автоматический расчёт",
                result = it,
                details = "Дебит=$rate; потенциал=$potential; подача=$supplyTime мин; слив=$drainTime мин; цикл=$cycle мин"
            )
        }
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = calculation.getOrNull()?.let { "$conversion|$rate|$potential|$supplyTime|$drainTime|$cycle|$it" })

    fun saveAndBack() {
        saveCurrentCalculation()
        onBack()
    }
    BackHandler(onBack = ::saveAndBack)

    val title = if (conversion == OperatingModeConversion.PR_TO_UPR) "Перевод ПР в УПР" else "Перевод УПР в АПВ"
    Scaffold { paddingValues ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(paddingValues)) {
            CalculationTopBar(title, ::saveAndBack)
            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeField(rate, { rate = it.modeInput() }, "Дебит, м³/сут", Modifier.weight(1f))
                    ModeField(potential, { potential = it.modeInput() }, "Потенциал, м³/сут", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeField(supplyTime, { supplyTime = it.modeInput() }, "Подача, мин", Modifier.weight(1f))
                    ModeField(cycle, { cycle = it.modeInput() }, "Цикл, мин", Modifier.weight(1f), isLast = conversion == OperatingModeConversion.PR_TO_UPR)
                }
                if (conversion == OperatingModeConversion.UPR_TO_APV) {
                    ModeField(drainTime, { drainTime = it.modeInput() }, "Время слива, мин", Modifier.fillMaxWidth(), isLast = true)
                }

                when (val value = calculation.getOrNull()) {
                    is com.example.sanich.calculator.UprResult -> ResultCardSimple(
                        "Рекомендуемый режим УПР",
                        "${value.totalRunMinutes.modeMinutes()} / ${(cycle.modeDecimal()!! - value.totalRunMinutes.modeMinutes()).toInt()} мин\nПолезная работа: ${value.productiveRunMinutes.modeMinutes()} мин",
                        null
                    )
                    is com.example.sanich.calculator.ApvResult -> {
                        if (value.cycleIsSuitable) {
                            ResultCardSimple(
                                "Рекомендуемый режим АПВ",
                                "Работа: ${value.totalRunMinutes.modeMinutes()} мин\nОтстой: ${(cycle.modeDecimal()!! - value.totalRunMinutes.modeMinutes()).toInt()} мин\nВремя слива: ${drainTime.ifBlank { "0" }} мин\nМинимальный цикл: ${value.minimumRequiredCycleMinutes} мин\nВыбранный цикл подходит",
                                null
                            )
                        } else {
                            ResultCardSimple(
                                "Выбранный цикл не подходит",
                                "Минимально допустимый цикл: ${value.minimumRequiredCycleMinutes} мин",
                                null
                            )
                            Button(
                                onClick = { cycle = value.minimumRequiredCycleMinutes.toString() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Использовать ${value.minimumRequiredCycleMinutes} мин")
                            }
                        }
                    }
                    else -> ResultCardSimple("Результат", null, calculation.exceptionOrNull()?.message)
                }

                Text(
                    if (conversion == OperatingModeConversion.PR_TO_UPR)
                        "Время работы = цикл × потенциал / дебит + время появления подачи. Остаток цикла — время остановки."
                    else
                        "Минимальный цикл рассчитывается независимо от выбранного: (подача + слив) / (1 − потенциал / дебит), затем округляется вверх до 60 минут. Отстой считается для выбранного цикла.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun ModeField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier, isLast: Boolean = false) {
    OutlinedTextField(
        value, onValueChange, label = { Text(label) }, modifier = modifier.keepVisibleAboveKeyboard(), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = if (isLast) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next),
        keyboardActions = if (isLast) doneFieldKeyboardActions() else nextFieldKeyboardActions()
    )
}
private fun String.modeDecimal(): Double? = replace(',', '.').toDoubleOrNull()
private fun String.modeInput(): String = filterIndexed { index, char -> char.isDigit() || ((char == '.' || char == ',') && index > 0 && take(index).none { it == '.' || it == ',' }) }
private fun Double.modePretty(): String = String.format(Locale.US, "%.1f", this).trimEnd('0').trimEnd('.')

private fun Double.modeMinutes(): Int = kotlin.math.round(this).toInt()
