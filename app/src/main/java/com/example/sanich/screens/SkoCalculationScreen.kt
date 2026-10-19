package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.SkoCalculator
import com.example.sanich.calculator.SkoVariant
import java.util.Locale

private data class SkoVariantUi(val value: SkoVariant, val title: String)

private val skoVariants = listOf(
    SkoVariantUi(SkoVariant.DIRECT_WORKING, "Прямая закачка — при снижении производительности"),
    SkoVariantUi(SkoVariant.DIRECT_NO_FLOW, "Прямая закачка — нет подачи / клин"),
    SkoVariantUi(SkoVariant.REVERSE_WORKING, "Обратная закачка — УЭЦН работает"),
    SkoVariantUi(SkoVariant.REVERSE_PUMP_OFF, "Обратная закачка — УЭЦН выключен")
)

@Composable
fun SkoCalculationScreen(
    historyViewModel: CalculationHistoryViewModel,
    onBack: () -> Unit
) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var variant by remember { mutableStateOf(SkoVariant.DIRECT_WORKING) }
    var depth by remember { mutableStateOf("") }
    var tubingSize by remember { mutableStateOf(73) }
    var casingOuterMm by remember { mutableStateOf(146.0) }
    var casingWallMm by remember { mutableStateOf(8.5) }
    var tubingOuterMm by remember { mutableStateOf("73") }

    val tubingLiters = when (tubingSize) {
        60 -> 2.0
        73 -> 3.0
        else -> 4.5
    }

    val calculation = runCatching {
        SkoCalculator.calculate(
            variant = variant,
            pumpDepthMeters = depth.skoDecimal() ?: error("Введите глубину спуска ЭЦН"),
            tubingLitersPerMeter = if (variant == SkoVariant.DIRECT_WORKING || variant == SkoVariant.DIRECT_NO_FLOW) tubingLiters else null,
            casingInnerDiameterMeters = (casingOuterMm - 2.0 * casingWallMm).div(1000.0),
            tubingOuterDiameterMeters = tubingOuterMm.skoDecimal()?.div(1000.0)
        )
    }
    val result = calculation.getOrNull()
    val selectedTitle = skoVariants.first { it.value == variant }.title

    val currentHistoryEntry = result?.let {
        HistoryEntry(
            title = "Объём СКО",
            method = selectedTitle,
            result = "${it.treatmentVolumeM3.skoPretty()} м³",
            details = buildString {
                append("Глубина=$depth м")
                if (variant == SkoVariant.DIRECT_WORKING || variant == SkoVariant.DIRECT_NO_FLOW) {
                    append("; НКТ=$tubingSize мм")
                } else {
                    append("; ЭК=${casingOuterMm.skoPretty()}×${casingWallMm.skoPretty()} мм; Dвн=${(casingOuterMm - 2*casingWallMm).skoPretty()} мм")
                    if (variant == SkoVariant.REVERSE_WORKING) append("; Dнар НКТ=$tubingOuterMm мм")
                }
            }
        )
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = result?.let { "$variant|$depth|$tubingSize|$casingOuterMm|$casingWallMm|$tubingOuterMm|${it.treatmentVolumeM3}" })
    fun saveAndBack() { saveCurrentCalculation(); onBack() }
    BackHandler(onBack = ::saveAndBack)

    Scaffold { paddingValues ->
        Column(
            Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(paddingValues)
        ) {
            CalculationTopBar("Объём СКО", ::saveAndBack)
            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Выберите вариант обработки", fontWeight = FontWeight.Bold)
                skoVariants.forEach { item ->
                    Card(
                        Modifier.fillMaxWidth().clickable { variant = item.value },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (variant == item.value) Color(0xFFFFF3CD) else Color.White)
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = variant == item.value, onClick = { variant = item.value })
                            Text(item.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                OutlinedTextField(
                    value = depth,
                    onValueChange = { depth = it.skoFilter() },
                    label = { Text("Глубина спуска ЭЦН, м") },
                    modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = if (variant == SkoVariant.DIRECT_WORKING || variant == SkoVariant.DIRECT_NO_FLOW) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = if (variant == SkoVariant.DIRECT_WORKING || variant == SkoVariant.DIRECT_NO_FLOW) doneFieldKeyboardActions() else nextFieldKeyboardActions()
                )

                if (variant == SkoVariant.DIRECT_WORKING || variant == SkoVariant.DIRECT_NO_FLOW) {
                    Text("НКТ", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(60, 73, 89).forEach { size ->
                            Card(
                                Modifier.weight(1f).clickable { tubingSize = size },
                                colors = CardDefaults.cardColors(containerColor = if (tubingSize == size) Color(0xFFFFC400) else Color.White)
                            ) { Text("$size мм", Modifier.padding(12.dp), fontWeight = FontWeight.Bold) }
                        }
                    }
                    Text("Справочный объём: ${tubingLiters.skoPretty()} л/м", color = Color.DarkGray)
                } else {
                    val selectedCasing = casingGost632.first { it.outside == casingOuterMm }
                    SimpleSkoDropdown(
                        label = "Наружный диаметр ЭК",
                        value = "${casingOuterMm.skoPretty()} мм",
                        options = casingGost632.map { it.outside to "${it.nominal} мм" },
                        onSelect = { outside ->
                            casingOuterMm = outside
                            casingWallMm = casingGost632.first { it.outside == outside }.walls.first()
                        }
                    )
                    SimpleSkoDropdown(
                        label = "Толщина стенки ЭК",
                        value = "${casingWallMm.skoPretty()} мм",
                        options = selectedCasing.walls.map { it to "${it.skoPretty()} мм" },
                        onSelect = { casingWallMm = it }
                    )
                    Text("Внутренний диаметр ЭК: ${(casingOuterMm - 2.0 * casingWallMm).skoPretty()} мм", fontWeight = FontWeight.Bold)
                    if (variant == SkoVariant.REVERSE_WORKING) {
                        OutlinedTextField(
                            value = tubingOuterMm,
                            onValueChange = { tubingOuterMm = it.skoFilter() },
                            label = { Text("Наружный диаметр НКТ, мм") },
                            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Done), keyboardActions = doneFieldKeyboardActions()
                        )
                    }
                }

                ResultCardSimple(
                    title = "Общий объём обработки",
                    result = result?.let { "${it.treatmentVolumeM3.skoPretty()} м³" },
                    error = calculation.exceptionOrNull()?.message
                )

                Text(
                    "Расчёт выполняется по формулам регламента. Технология, концентрация кислоты и окончательный объём определяются Планом-заказом и технологической службой.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

private fun String.skoDecimal(): Double? = replace(',', '.').toDoubleOrNull()
private fun String.skoFilter(): String = filterIndexed { index, char ->
    char.isDigit() || ((char == '.' || char == ',') && index > 0 && take(index).none { it == '.' || it == ',' })
}
private fun Double.skoPretty(): String = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')


@Composable
private fun SimpleSkoDropdown(label: String, value: String, options: List<Pair<Double, String>>, onSelect: (Double) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth().clickable { expanded = true }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Text(value, Modifier.padding(14.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(key); expanded = false })
            }
        }
    }
}
