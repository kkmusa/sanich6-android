package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.FrequencyCalculator
import java.util.Locale

@Composable
fun FrequencyScreen(historyViewModel: CalculationHistoryViewModel, onBack: () -> Unit) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var level by remember { mutableStateOf("") }
    var head by remember { mutableStateOf("") }
    val calculation = runCatching { FrequencyCalculator.calculate(level.toFrequencyDecimal() ?: error("Заполните все параметры"), head.toFrequencyDecimal() ?: error("Заполните все параметры")) }
    val result = calculation.getOrNull()

    val currentHistoryEntry = result?.let {
        HistoryEntry(
            title = "Необходимая частота запуска",
            method = "Автоматический расчёт",
            result = "${it.prettyFrequency()} Гц",
            details = "Уровень=$level м; напор=$head м"
        )
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = result?.let { "$level|$head|$it" })

    fun saveAndBack() {
        saveCurrentCalculation()
        onBack()
    }
    BackHandler(onBack = ::saveAndBack)

    Scaffold { paddingValues ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(paddingValues)) {
            CalculationTopBar("Необходимая частота запуска", ::saveAndBack)
            Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(level, { level = it.filterFrequencyDecimal() }, label = { Text("Уровень жидкости, м") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Next), keyboardActions = nextFieldKeyboardActions())
                OutlinedTextField(head, { head = it.filterFrequencyDecimal() }, label = { Text("Напор УЭЦН, м") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Done), keyboardActions = doneFieldKeyboardActions())
                ResultCardSimple("Необходимая частота запуска", result?.let { "${it.prettyFrequency()} Гц" }, calculation.exceptionOrNull()?.message)
                OutlinedButton(onClick = { level=""; head="" }, modifier = Modifier.fillMaxWidth()) { Text("Очистить") }
            }
        }
    }
}
private fun String.toFrequencyDecimal(): Double? = replace(',', '.').toDoubleOrNull()
private fun String.filterFrequencyDecimal(): String = filterIndexed { index, char -> char.isDigit() || ((char == '.' || char == ',') && index > 0 && take(index).none { it == '.' || it == ',' }) }
private fun Double.prettyFrequency(): String = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
