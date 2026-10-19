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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.LevelCalculator
import com.example.sanich.ui.theme.CalculationResultYellow
import java.util.Locale

@Composable
fun LevelScreen(historyViewModel: CalculationHistoryViewModel, onBack: () -> Unit) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var depth by remember { mutableStateOf("") }
    var pz by remember { mutableStateOf("") }
    var pd by remember { mutableStateOf("") }
    var density by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf("") }

    val calculation = runCatching {
        LevelCalculator.calculate(
            (depth.toDecimal() ?: error("Заполните все параметры")) -
                (extension.toDecimal() ?: 0.0),
            pz.toDecimal() ?: error("Заполните все параметры"),
            pd.toDecimal() ?: error("Заполните все параметры"),
            density.toDecimal() ?: error("Заполните все параметры")
        )
    }
    val result = calculation.getOrNull()

    val currentHistoryEntry = result?.let {
        HistoryEntry(
            title = "Уровень жидкости",
            method = "Автоматический расчёт",
            result = "${it.prettyLevel()} м",
            details = "Глубина=$depth м; удлинение=${extension.ifBlank { "нет" }}; Pз=$pz; Pд=$pd; плотность=$density г/см³"
        )
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = result?.let { "$depth|$pz|$pd|$density|$it" })

    fun saveAndBack() {
        saveCurrentCalculation()
        onBack()
    }
    BackHandler(onBack = ::saveAndBack)

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .padding(paddingValues)
        ) {
            CalculationTopBar("Расчёт уровня жидкости", ::saveAndBack)
            Column(
                modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(depth, { depth = it.filterDecimalInput() }, label = { Text("Глубина спуска насоса, м") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Next), keyboardActions = nextFieldKeyboardActions())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(pz, { pz = it.filterDecimalInput() }, label = { Text("Pз") }, modifier = Modifier.weight(1f).keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Next), keyboardActions = nextFieldKeyboardActions())
                    OutlinedTextField(pd, { pd = it.filterDecimalInput() }, label = { Text("Pд") }, modifier = Modifier.weight(1f).keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Next), keyboardActions = nextFieldKeyboardActions())
                }
                OutlinedTextField(density, { density = it.filterDecimalInput() }, label = { Text("Плотность жидкости, г/см³") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Next), keyboardActions = nextFieldKeyboardActions())
                OutlinedTextField(extension, { extension = it.filterDecimalInput() }, label = { Text("Удлинение, м") }, supportingText = { Text("Необязательно. Если поле заполнено, значение вычитается из глубины спуска.") }, modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Done), keyboardActions = doneFieldKeyboardActions())
                ResultCardSimple("Уровень жидкости", result?.let { "${it.prettyLevel()} м" }, calculation.exceptionOrNull()?.message)
                OutlinedButton(onClick = { depth=""; pz=""; pd=""; density=""; extension="" }, modifier = Modifier.fillMaxWidth()) { Text("Очистить") }
            }
        }
    }
}

@Composable
fun ResultCardSimple(title: String, result: String?, error: String?) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (result != null) CalculationResultYellow else Color.White)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(result ?: error.orEmpty(), style = if (result != null) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium, fontWeight = if (result != null) FontWeight.Bold else FontWeight.Normal, color = if (result != null) Color(0xFF171717) else Color.Gray)
        }
    }
}
private fun String.toDecimal(): Double? = replace(',', '.').toDoubleOrNull()
private fun String.filterDecimalInput(): String = filterIndexed { index, char -> char.isDigit() || ((char == '.' || char == ',') && index > 0 && take(index).none { it == '.' || it == ',' }) }
private fun Double.prettyLevel(): String = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
