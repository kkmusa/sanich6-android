package com.example.sanich.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.HeadAtFrequencyCalculator
import java.util.Locale
import java.util.UUID

@Composable
fun HeadAtFrequencyScreen(historyViewModel: CalculationHistoryViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("head_at_frequency", Context.MODE_PRIVATE) }
    var head by rememberSaveable { mutableStateOf(prefs.getString("head", "") ?: "") }
    var frequency by rememberSaveable { mutableStateOf(prefs.getString("frequency", "") ?: "") }
    val sessionKey = rememberSaveable { UUID.randomUUID().toString() }
    LaunchedEffect(head, frequency) {
        prefs.edit().putString("head", head).putString("frequency", frequency).apply()
    }
    val calculation = runCatching {
        HeadAtFrequencyCalculator.calculate(
            head.replace(',', '.').toDoubleOrNull() ?: error("Введите напор при 50 Гц"),
            frequency.replace(',', '.').toDoubleOrNull() ?: error("Введите рабочую частоту")
        )
    }
    val result = calculation.getOrNull()
    val historyEntry by rememberUpdatedState(result?.let {
        HistoryEntry(
            title = "Напор на частоте",
            method = "Квадратичная зависимость от 50 Гц",
            result = "${it.prettyHead()} м",
            details = "Напор при 50 Гц=$head м; частота=$frequency Гц"
        )
    })
    fun save() { historyEntry?.let { historyViewModel.saveSession(sessionKey, it) } }
    PersistHistoryOnDispose(::save, debounceKey = result?.let { "$head|$frequency|$it" })
    fun saveAndBack() { save(); onBack() }
    BackHandler(onBack = ::saveAndBack)
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(padding)) {
            CalculationTopBar("Напор на частоте", ::saveAndBack)
            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = head, onValueChange = { head = it },
                    label = { Text("Паспортный напор при 50 Гц, м") },
                    modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = nextFieldKeyboardActions()
                )
                OutlinedTextField(
                    value = frequency, onValueChange = { frequency = it },
                    label = { Text("Рабочая частота, Гц") },
                    modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = doneFieldKeyboardActions()
                )
                ResultCardSimple("Напор", result?.let { "${it.prettyHead()} м" }, calculation.exceptionOrNull()?.message)
                OutlinedButton(onClick = { head = ""; frequency = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Очистить")
                }
            }
        }
    }
}

private fun Double.prettyHead(): String = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
