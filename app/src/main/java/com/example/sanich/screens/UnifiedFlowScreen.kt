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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.calculator.AnnularVolumeCalculator
import com.example.sanich.calculator.FlowCalculator
import com.example.sanich.components.SelectionDialog
import com.example.sanich.data.ReferenceData
import com.example.sanich.ui.theme.CalculationResultYellow
import com.example.sanich.data.Tables
import java.util.Locale

enum class FlowType(val title: String) {
    HERMETIC("Герметичный ОК"),
    DRAIN("Негерметичный ОК в период слива"),
    AFTER_DRAIN("Негерметичный ОК после слива")
}

enum class VkpSource(val title: String) {
    TABLE("По справочнику"),
    GEOMETRY("По геометрии")
}

@Composable
fun UnifiedFlowScreen(
    historyViewModel: CalculationHistoryViewModel,
    onBack: () -> Unit
) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var flowType by remember { mutableStateOf(FlowType.HERMETIC) }
    var source by remember { mutableStateOf(VkpSource.TABLE) }
    var deltaH by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var hst by remember { mutableStateOf("") }

    var casingTable by remember { mutableStateOf("168") }
    var tubing by remember { mutableStateOf("73") }
    var casingOuter by remember { mutableStateOf(146.0) }
    var wall by remember { mutableStateOf(7.0) }

    var dialog by remember { mutableStateOf<String?>(null) }

    val innerDiameter = if (source == VkpSource.GEOMETRY) {
        runCatching { AnnularVolumeCalculator.casingInnerDiameter(casingOuter, wall) }.getOrNull()
    } else null

    val vkpResult = runCatching {
        when (source) {
            VkpSource.TABLE -> Tables.getVkp(casingTable, tubing).also {
                require(it > 0.0) { "Нет значения Vкп для выбранных размеров" }
            }
            VkpSource.GEOMETRY -> AnnularVolumeCalculator.volumePerMeter(
                tubingOuterDiameterMm = tubing.toDouble(),
                casingOuterDiameterMm = casingOuter,
                wallThicknessMm = wall
            )
        }
    }
    val vkp = vkpResult.getOrNull()
    val geometryError = vkpResult.exceptionOrNull()?.message

    val flowResult = runCatching {
        val dh = deltaH.toDecimal() ?: error("Введите ΔH")
        val minutes = time.toDecimal() ?: error("Введите время")
        val currentVkp = vkp ?: error(geometryError ?: "Не удалось определить Vкп")
        val vnkt = Tables.getVnkt(tubing)
        when (flowType) {
            FlowType.HERMETIC -> FlowCalculator.hermetic(dh, currentVkp, minutes)
            FlowType.DRAIN -> FlowCalculator.drain(
                deltaH = dh,
                vkp = currentVkp,
                vnkt = vnkt,
                hst = hst.toDecimal() ?: error("Введите Hст"),
                time = minutes
            )
            FlowType.AFTER_DRAIN -> FlowCalculator.afterDrain(dh, currentVkp, vnkt, minutes)
        }
    }
    val flow = flowResult.getOrNull()

    val currentHistoryEntry = flow?.let {
        HistoryEntry(
            title = flowType.title,
            method = source.title,
            result = "${it.pretty()} м³/сут",
            details = if (source == VkpSource.GEOMETRY) {
                "ΔH=$deltaH м; время=$time мин; НКТ=$tubing мм; D ЭК=${casingOuter.pretty()} мм; стенка ЭК=${wall.pretty()} мм; Vкп=${vkp?.volumeFormat()} м³/м"
            } else {
                "ΔH=$deltaH м; время=$time мин; ЭК=$casingTable мм; НКТ=$tubing мм; Vкп=${vkp?.volumeFormat()} м³/м"
            }
        )
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = flow?.let { "$flowType|$source|$deltaH|$time|$hst|$casingTable|$tubing|$casingOuter|$wall|$it" })

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
            CalculationTopBar(
                title = "Определение притока",
                onBack = ::saveAndBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactSelector("Вид расчёта", flowType.title) { dialog = "type" }
                CompactSelector("Источник Vкп", source.title) { dialog = "source" }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (source == VkpSource.TABLE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CompactSelector(
                                    label = "ЭК",
                                    value = "$casingTable мм",
                                    modifier = Modifier.weight(1f)
                                ) { dialog = "tableCasing" }
                                CompactSelector(
                                    label = "НКТ",
                                    value = "$tubing мм",
                                    modifier = Modifier.weight(1f)
                                ) { dialog = "tubing" }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CompactSelector(
                                    "НКТ",
                                    "$tubing мм",
                                    Modifier.weight(1f)
                                ) { dialog = "tubing" }
                                CompactSelector(
                                    "D ЭК",
                                    "${casingOuter.pretty()} мм",
                                    Modifier.weight(1f)
                                ) { dialog = "geometryCasing" }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CompactSelector(
                                    "Стенка ЭК",
                                    "${wall.pretty()} мм",
                                    Modifier.weight(1f)
                                ) { dialog = "wall" }
                            }
                        }

                        if (source == VkpSource.GEOMETRY) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dвнутр. ЭК: ${innerDiameter?.let { "${it.pretty()} мм" } ?: "—"}")
                                Text("Vкп: ${vkp?.let { it.volumeFormat() } ?: "—"} м³/м")
                            }
                            if (geometryError != null) {
                                Text(geometryError, color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text("Vкп: ${vkp?.volumeFormat() ?: "—"} м³/м")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberField("ΔH, м", deltaH, { deltaH = it }, Modifier.weight(1f), isLast = false)
                    NumberField("Время, мин", time, { time = it }, Modifier.weight(1f), isLast = flowType != FlowType.DRAIN)
                }
                if (flowType == FlowType.DRAIN) {
                    NumberField("Hст, м", hst, { hst = it }, Modifier.fillMaxWidth(), isLast = true)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (flow != null) CalculationResultYellow else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Приток", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = flow?.let { "${it.pretty()} м³/сут" }
                                ?: flowResult.exceptionOrNull()?.message.orEmpty()
                                    .ifBlank { "Заполните параметры" },
                            style = if (flow != null) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.bodyMedium,
                            fontWeight = if (flow != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (flow != null) Color(0xFF171717) else Color.Gray
                        )
                    }
                }
            }
        }
    }

    when (dialog) {
        "type" -> SelectionDialog(
            title = "Вид расчёта",
            items = FlowType.entries.map { it.title },
            onDismiss = { dialog = null },
            onSelected = { selected ->
                val newType = FlowType.entries.first { it.title == selected }
                if (newType != flowType) {
                    flowType = newType
                    deltaH = ""
                    time = ""
                    hst = ""
                }
                dialog = null
            }
        )
        "source" -> SelectionDialog(
            title = "Источник Vкп",
            items = VkpSource.entries.map { it.title },
            onDismiss = { dialog = null },
            onSelected = { selected ->
                source = VkpSource.entries.first { it.title == selected }
                dialog = null
            }
        )
        "tableCasing" -> optionDialog(
            "Эксплуатационная колонна",
            listOf("139.7", "146", "168", "178"),
            { dialog = null }
        ) { casingTable = it }
        "tubing" -> optionDialog(
            "Диаметр НКТ",
            ReferenceData.tubingDiametersMm,
            { dialog = null }
        ) { tubing = it }
        "geometryCasing" -> doubleOptionDialog(
            "Наружный диаметр ЭК",
            ReferenceData.casingOuterDiametersMm,
            { dialog = null }
        ) { selectedCasing ->
            casingOuter = selectedCasing
            val availableWalls = ReferenceData.wallThicknessesForCasing(selectedCasing)
            if (wall !in availableWalls) {
                wall = availableWalls.firstOrNull() ?: wall
            }
        }
        "wall" -> doubleOptionDialog(
            "Толщина стенки ЭК",
            ReferenceData.wallThicknessesForCasing(casingOuter),
            { dialog = null }
        ) { wall = it }
    }
}

@Composable
private fun CompactSelector(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(modifier = modifier.keepVisibleAboveKeyboard(), onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, maxLines = 1, color = Color.Black)
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    isLast: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filterDecimal()) },
        label = { Text(label) },
        modifier = modifier.keepVisibleAboveKeyboard(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (isLast) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
        ),
        keyboardActions = if (isLast) doneFieldKeyboardActions() else nextFieldKeyboardActions()
    )
}

@Composable
private fun optionDialog(
    title: String,
    values: List<String>,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    SelectionDialog(
        title = title,
        items = values.map { "$it мм" },
        onDismiss = onDismiss,
        onSelected = {
            onSelected(it.removeSuffix(" мм"))
            onDismiss()
        }
    )
}

@Composable
private fun doubleOptionDialog(
    title: String,
    values: List<Double>,
    onDismiss: () -> Unit,
    onSelected: (Double) -> Unit
) {
    SelectionDialog(
        title = title,
        items = values.map { "${it.pretty()} мм" },
        onDismiss = onDismiss,
        onSelected = { text ->
            onSelected(text.removeSuffix(" мм").replace(',', '.').toDouble())
            onDismiss()
        }
    )
}

private fun String.toDecimal(): Double? = replace(',', '.').toDoubleOrNull()

private fun String.filterDecimal(): String = filterIndexed { index, char ->
    char.isDigit() || ((char == '.' || char == ',') && index > 0 && take(index).none { it == '.' || it == ',' })
}

private fun Double.pretty(): String = if (this % 1.0 == 0.0) {
    toInt().toString()
} else {
    String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
}

private fun Double.volumeFormat(): String = String.format(Locale.US, "%.6f", this).trimEnd('0').trimEnd('.')
