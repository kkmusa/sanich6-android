package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.ForeignPumpCalculator
import java.util.Locale

@Composable
fun ForeignPumpScreen(historyViewModel: CalculationHistoryViewModel, onBack: () -> Unit) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var rate by remember { mutableStateOf("") }
    var baseFrequency by remember { mutableStateOf("60") }
    var targetFrequency by remember { mutableStateOf("50") }

    val calculation = runCatching {
        ForeignPumpCalculator.calculate(
            rate.toForeignDecimal() ?: error("Введите производительность DN"),
            baseFrequency.toForeignDecimal() ?: error("Введите исходную частоту"),
            targetFrequency.toForeignDecimal() ?: error("Введите требуемую частоту")
        )
    }
    val result = calculation.getOrNull()
    val resultText = result?.let {
        "${it.barrelsPerDay.prettyForeign()} барр./сут\n${it.cubicMetersPerDay.prettyForeign()} м³/сут"
    }
    val currentHistoryEntry = resultText?.let {
        HistoryEntry(
            title = "Иностранный УЭЦН — производительность",
            method = "Пересчёт по закону подобия",
            result = it,
            details = "DN/подача=$rate барр./сут; исходная частота=$baseFrequency Гц; расчётная частота=$targetFrequency Гц"
        )
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }
    PersistHistoryOnDispose(
        ::saveCurrentCalculation,
        debounceKey = result?.let { "$rate|$baseFrequency|$targetFrequency|${it.barrelsPerDay}" }
    )
    fun saveAndBack() { saveCurrentCalculation(); onBack() }
    BackHandler(onBack = ::saveAndBack)

    Scaffold { paddingValues ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(paddingValues)) {
            CalculationTopBar("Иностранные УЭЦН", ::saveAndBack)
            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(rate, { rate = it.filterForeignDecimal() }, label = { Text("Производительность DN, барр./сут") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next), keyboardActions = nextFieldKeyboardActions())
                OutlinedTextField(baseFrequency, { baseFrequency = it.filterForeignDecimal() }, label = { Text("Частота в обозначении, Гц") }, supportingText = { Text("Обычно для иностранных УЭЦН — 60 Гц") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next), keyboardActions = nextFieldKeyboardActions())
                OutlinedTextField(targetFrequency, { targetFrequency = it.filterForeignDecimal() }, label = { Text("Требуемая частота, Гц") }, supportingText = { Text("По умолчанию — 50 Гц, значение можно изменить") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done), keyboardActions = doneFieldKeyboardActions())
                ResultCardSimple(
                    result?.let { "Производительность при ${targetFrequency} Гц" } ?: "Производительность",
                    resultText,
                    calculation.exceptionOrNull()?.message
                )
                OutlinedButton(onClick = { rate = ""; baseFrequency = "60"; targetFrequency = "50" }, modifier = Modifier.fillMaxWidth()) { Text("Очистить") }
            }
        }
    }
}

private fun String.toForeignDecimal(): Double? = replace(',', '.').toDoubleOrNull()
private fun String.filterForeignDecimal(): String = filterIndexed { index, char ->
    char.isDigit() || ((char == '.' || char == ',') && index > 0 && take(index).none { it == '.' || it == ',' })
}
private fun Double.prettyForeign(): String = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
