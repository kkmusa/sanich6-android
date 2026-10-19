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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.CableMaterial
import com.example.sanich.calculator.ControlStationMode
import com.example.sanich.calculator.MotorType
import com.example.sanich.calculator.TapCalculator
import java.util.Locale

@Composable
fun TapCalculationScreen(
    historyViewModel: CalculationHistoryViewModel,
    onBack: () -> Unit
) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var mode by remember { mutableStateOf(ControlStationMode.WITHOUT_VFD) }
    var transformerInput by remember { mutableStateOf("380") }
    var actualSupply by remember { mutableStateOf("380") }
    var motorVoltage by remember { mutableStateOf("") }
    var motorCurrent by remember { mutableStateOf("") }
    var cableLength by remember { mutableStateOf("") }
    var cableSection by remember { mutableStateOf("16") }
    var temperature by remember { mutableStateOf("80") }
    var material by remember { mutableStateOf(CableMaterial.COPPER) }
    var motorType by remember { mutableStateOf(MotorType.ASYNCHRONOUS) }
    var vfdMaxVoltage by remember { mutableStateOf("380") }
    var maxFrequency by remember { mutableStateOf("60") }
    var nominalFrequency by remember { mutableStateOf("50") }
    var hasOutputFilter by remember { mutableStateOf(false) }

    // Explicit focus chain: Next always jumps to the next NUMBER field,
    // even when chips/switches are located between numeric inputs.
    val transformerInputFocus = remember { FocusRequester() }
    val actualSupplyFocus = remember { FocusRequester() }
    val motorVoltageFocus = remember { FocusRequester() }
    val motorCurrentFocus = remember { FocusRequester() }
    val cableLengthFocus = remember { FocusRequester() }
    val cableSectionFocus = remember { FocusRequester() }
    val temperatureFocus = remember { FocusRequester() }
    val vfdVoltageFocus = remember { FocusRequester() }
    val maxFrequencyFocus = remember { FocusRequester() }
    val nominalFrequencyFocus = remember { FocusRequester() }

    val calculation = runCatching {
        TapCalculator.calculate(
            mode = mode,
            transformerNominalInputVoltage = transformerInput.decimal() ?: error("Заполните входное напряжение ТМПН"),
            actualSupplyVoltage = actualSupply.decimal() ?: error("Заполните фактическое напряжение сети"),
            motorNominalVoltage = motorVoltage.decimal() ?: error("Заполните напряжение ПЭД"),
            motorNominalCurrent = motorCurrent.decimal() ?: error("Заполните ток ПЭД"),
            cableLengthMeters = cableLength.decimal() ?: error("Заполните длину кабеля"),
            cableSectionMm2 = cableSection.decimal() ?: error("Заполните сечение кабеля"),
            formationTemperatureC = temperature.decimal() ?: error("Заполните температуру пласта"),
            cableMaterial = material,
            motorType = motorType,
            vfdMaxOutputVoltage = vfdMaxVoltage.decimal(),
            maxFrequencyHz = maxFrequency.decimal(),
            motorNominalFrequencyHz = nominalFrequency.decimal(),
            hasOutputFilter = hasOutputFilter
        )
    }
    val result = calculation.getOrNull()

    val currentHistoryEntry = result?.let {
        HistoryEntry(
            title = "Отпайка ТМПН",
            method = if (mode == ControlStationMode.WITH_VFD) "СУ с ЧРП" else "СУ без ЧРП",
            result = "${it.requiredVoltage.prettyTap()} В",
            details = "Потери=${it.voltageLoss.prettyTap()} В; UПЭД=$motorVoltage В; IПЭД=$motorCurrent А; кабель=$cableLength м/$cableSection мм²"
        )
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = result?.let { "$mode|$transformerInput|$actualSupply|$motorVoltage|$motorCurrent|$cableLength|$cableSection|$temperature|$material|$motorType|$vfdMaxVoltage|$maxFrequency|$nominalFrequency|$hasOutputFilter|${it.requiredVoltage}" })

    fun saveAndBack() {
        saveCurrentCalculation()
        onBack()
    }
    BackHandler(onBack = ::saveAndBack)

    Scaffold { paddingValues ->
        Column(
            Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(paddingValues)
        ) {
            CalculationTopBar("Расчёт отпайки ТМПН", ::saveAndBack)
            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text("Тип станции управления", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(mode == ControlStationMode.WITHOUT_VFD, { mode = ControlStationMode.WITHOUT_VFD }, { Text("Без ЧРП") })
                    FilterChip(mode == ControlStationMode.WITH_VFD, { mode = ControlStationMode.WITH_VFD }, { Text("С ЧРП") })
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(transformerInput, { transformerInput = it.tapInput() }, "Uвх ТМПН, В", Modifier.weight(1f), transformerInputFocus, actualSupplyFocus)
                    NumberField(actualSupply, { actualSupply = it.tapInput() }, "U сети, В", Modifier.weight(1f), actualSupplyFocus, motorVoltageFocus)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(motorVoltage, { motorVoltage = it.tapInput() }, "U ПЭД, В", Modifier.weight(1f), motorVoltageFocus, motorCurrentFocus)
                    NumberField(motorCurrent, { motorCurrent = it.tapInput() }, "I ПЭД, А", Modifier.weight(1f), motorCurrentFocus, cableLengthFocus)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(cableLength, { cableLength = it.tapInput() }, "Кабель, м", Modifier.weight(1f), cableLengthFocus, cableSectionFocus)
                    NumberField(cableSection, { cableSection = it.tapInput() }, "Сечение, мм²", Modifier.weight(1f), cableSectionFocus, temperatureFocus)
                }
                NumberField(temperature, { temperature = it.tapInput() }, "Температура пласта, °C", Modifier.fillMaxWidth(), temperatureFocus, if (mode == ControlStationMode.WITH_VFD) vfdVoltageFocus else null)

                Text("Материал кабеля", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(material == CableMaterial.COPPER, { material = CableMaterial.COPPER }, { Text("Медь") })
                    FilterChip(material == CableMaterial.ALUMINUM, { material = CableMaterial.ALUMINUM }, { Text("Алюминий") })
                }
                Text("Тип ПЭД", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(motorType == MotorType.ASYNCHRONOUS, { motorType = MotorType.ASYNCHRONOUS }, { Text("Асинхронный") })
                    FilterChip(motorType == MotorType.PERMANENT_MAGNET, { motorType = MotorType.PERMANENT_MAGNET }, { Text("Вентильный") })
                }

                if (mode == ControlStationMode.WITH_VFD) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(vfdMaxVoltage, { vfdMaxVoltage = it.tapInput() }, "Uвых max ЧРП, В", Modifier.weight(1f), vfdVoltageFocus, maxFrequencyFocus)
                        NumberField(maxFrequency, { maxFrequency = it.tapInput() }, "Fmax, Гц", Modifier.weight(1f), maxFrequencyFocus, nominalFrequencyFocus)
                    }
                    NumberField(nominalFrequency, { nominalFrequency = it.tapInput() }, "Номинальная частота ПЭД, Гц", Modifier.fillMaxWidth(), nominalFrequencyFocus, null)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Выходной фильтр в составе НЭО", style = MaterialTheme.typography.bodyMedium)
                            Text("Учитывать коэффициент 1,08 для встроенного или внешнего выходного фильтра", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                        Switch(checked = hasOutputFilter, onCheckedChange = { hasOutputFilter = it })
                    }
                }

                ResultCardSimple(
                    title = "Расчётное напряжение отпайки",
                    result = result?.let { "${it.requiredVoltage.prettyTap()} В\nПотери в кабеле: ${it.voltageLoss.prettyTap()} В" },
                    error = calculation.exceptionOrNull()?.message
                )
                Text(
                    "Выберите ближайшую доступную отпайку в большую сторону.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    focusRequester: FocusRequester,
    nextFocusRequester: FocusRequester?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.focusRequester(focusRequester).keepVisibleAboveKeyboard(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (nextFocusRequester == null) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
        ),
        keyboardActions = if (nextFocusRequester == null) {
            doneFieldKeyboardActions()
        } else {
            KeyboardActions(onNext = { nextFocusRequester.requestFocus() })
        }
    )
}

private fun String.decimal(): Double? = replace(',', '.').toDoubleOrNull()
private fun String.tapInput(): String = filterIndexed { index, char ->
    char.isDigit() || (char == '-' && index == 0) || ((char == '.' || char == ',') && take(index).none { it == '.' || it == ',' })
}
private fun Double.prettyTap(): String = String.format(Locale.US, "%.1f", this).trimEnd('0').trimEnd('.')
