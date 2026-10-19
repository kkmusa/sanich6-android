package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.sanich.R
import java.util.Locale
import kotlin.math.roundToInt

private data class OperatorStep(val title: String, val description: String, val image: Int?)
private fun formatNumber(value: Double): String = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
private fun minutesClock(value: Double): String {
    val total = value.roundToInt()
    return "${total / 60} ч ${String.format(Locale.US, "%02d", total % 60)} мин"
}
private fun secondsClock(value: Double): String {
    val total = value.roundToInt()
    return "${total / 60} мин ${String.format(Locale.US, "%02d", total % 60)} с"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorModeFlowScreen(isRamp: Boolean, onBack: () -> Unit) {
    var start by rememberSaveable { mutableStateOf("") }
    var end by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("") }
    var delta by rememberSaveable { mutableStateOf("") }
    var stepMinutes by rememberSaveable { mutableStateOf("") }
    var high by rememberSaveable { mutableStateOf("") }
    var low by rememberSaveable { mutableStateOf("") }
    var highMinutes by rememberSaveable { mutableStateOf("") }
    var lowMinutes by rememberSaveable { mutableStateOf("") }
    var station by rememberSaveable { mutableStateOf("Борец КАСКАД-НТ") }
    var expanded by remember { mutableStateOf(false) }
    var viewing by rememberSaveable { mutableStateOf(false) }
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val stations = listOf("Борец КАСКАД-НТ", "Эталон ЧР", "ЭЛЕКТОН", "ОРИОН-01", "ТРИОЛ (УМКА)")
    val number: (String) -> Double? = { it.replace(',', '.').toDoubleOrNull()?.takeIf { n -> n.isFinite() } }
    val sv = number(start); val ev = number(end); val rv = number(rate)
    val dv = number(delta); val sm = number(stepMinutes)
    val hv = number(high); val lv = number(low)
    val hm = number(highMinutes); val lm = number(lowMinutes)
    val uv = lm?.let { (it + 1.0) * 60.0 }
    val valid = if (isRamp) sv != null && ev != null && ev > sv && sv > 0 &&
        dv != null && dv > 0 && sm != null && sm > 0
    else hv != null && lv != null && hv > lv && lv > 0 && hm != null && hm > 0 && lm != null && lm > 0
    val supported = station in stations

    fun steps(): List<OperatorStep> {
        val st = formatNumber(sv ?: 0.0); val en = formatNumber(ev ?: 0.0)
        val rt = formatNumber(if (sm != null && sm > 0) (dv ?: 0.0) * 3600.0 / sm else 0.0); val hi = formatNumber(hv ?: 0.0); val lo = formatNumber(lv ?: 0.0)
        val htime = formatNumber(hm ?: 0.0); val ltime = formatNumber(lm ?: 0.0)
        return when {
            isRamp && station == "ОРИОН-01" -> listOf(
                OperatorStep("Управление режимами", "В корневом каталоге открыть «Управление режимами».", R.drawable.orion_ramp_01),
                OperatorStep("Программное изменение частоты", "Открыть «Программное изменение частоты».", R.drawable.orion_ramp_02),
                OperatorStep("Конечная частота", "Установить НАШУ конечную частоту: $en Гц.", R.drawable.orion_ramp_03),
                OperatorStep("Начальная частота", "Установить НАШУ начальную частоту: $st Гц.", R.drawable.orion_ramp_04),
                OperatorStep("Темп изменения", "Установить НАШ темп изменения: $rt Гц/ч.", R.drawable.orion_ramp_05),
                OperatorStep("Включение", "Вернуться к «Программное изменение частоты» и выбрать «Вкл».", R.drawable.orion_ramp_06)
            )
            isRamp && station == "ЭЛЕКТОН" -> listOf(
                OperatorStep("Технологические настройки", "В главном меню открыть «6. Технол. настройки».", R.drawable.elekton_ramp_01),
                OperatorStep("Программное изменение частоты", "Открыть «6.5 Прогр. изм. част.».", R.drawable.elekton_ramp_02),
                OperatorStep("Начальная частота", "Параметр 230: установить НАШУ начальную частоту $st Гц.", R.drawable.elekton_ramp_03),
                OperatorStep("Конечная частота и темп", "Параметр 231: $en Гц. Параметр 232: $rt Гц/ч.", R.drawable.elekton_ramp_04),
                OperatorStep("Включение", "Параметр 229 «Прогр. изм. част.» установить «Вкл».", R.drawable.elekton_ramp_05)
            )
            isRamp && station == "Борец КАСКАД-НТ" -> {
                val totalMinutes = ((ev!! - sv!!) / dv!! * sm!! / 60.0)
                listOf(
                    OperatorStep("Вход в меню", "Нажать «Ввод / Enter».", R.drawable.borets_cascade_01),
                    OperatorStep("Уставки", "Открыть «Уставки».", R.drawable.borets_cascade_02),
                    OperatorStep("Уставки другие", "Открыть «Уставки другие».", R.drawable.borets_cascade_03),
                    OperatorStep("Начальная частота", "В «Nн разг» установить $st Гц.", R.drawable.borets_cascade_04),
                    OperatorStep("Время разгона", "Установить НАШЕ время разгона ${formatNumber(totalMinutes)} мин.", R.drawable.borets_cascade_05),
                    OperatorStep("Способ изменения", "Выбрать «время».", R.drawable.borets_cascade_06),
                    OperatorStep("Программное изменение", "Выбрать «1раз».", R.drawable.borets_cascade_07),
                    OperatorStep("Конечная частота", "В первом пункте «N» установить $en Гц.", R.drawable.borets_cascade_n_final),
                    OperatorStep("Главный экран", "Вернуться на главный экран и проверить индикацию программы.", R.drawable.borets_cascade_09)
                )
            }
            isRamp && station == "Эталон ЧР" -> listOf(
                OperatorStep("Параметры ЧРП", "Зайти в «Параметры ЧРП».", R.drawable.etalon_new_ramp_01),
                OperatorStep("Прогр. изм F", "Зайти в «Прогр. изм F».", R.drawable.etalon_new_ramp_02),
                OperatorStep("Частоты", "Установить начальную $st Гц и конечную $en Гц.", R.drawable.etalon_new_ramp_03),
                OperatorStep("Шаг по регл.", "Установить шаг ${formatNumber(dv!!)} Гц.", R.drawable.etalon_new_ramp_04),
                OperatorStep("Задерж. по регл.", "Установить задержку ${formatNumber(sm!!)} с.", R.drawable.etalon_new_ramp_05),
                OperatorStep("Вывод на режим", "Выбрать «Однокр».", R.drawable.etalon_new_ramp_06)
            )
            isRamp && station == "ТРИОЛ (УМКА)" -> {
                val totalSeconds = (ev!! - sv!!) / dv!! * sm!!
                val minutes = (totalSeconds / 60.0).roundToInt()
                val duration = String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60)
                listOf(
                    OperatorStep("Режимы работы", "Зайти в «Режимы работы».", R.drawable.triol_ramp_01),
                    OperatorStep("Программный режим", "Зайти в «Программный режим».", R.drawable.triol_ramp_02),
                    OperatorStep("Начальная частота", "Установить $st Гц.", R.drawable.triol_ramp_03),
                    OperatorStep("Конечная частота", "Установить $en Гц.", R.drawable.triol_ramp_04),
                    OperatorStep("Время на переход", "Установить время $duration (ч:мин).", R.drawable.triol_ramp_05),
                    OperatorStep("Регулятор техн. параметра", "Вернуться назад и открыть «Регулятор техн. параметра».", R.drawable.triol_ramp_06),
                    OperatorStep("Поддерживаемый параметр", "Выбрать «Прогр. F».", R.drawable.triol_ramp_07)
                )
            }
            !isRamp && station == "Эталон ЧР" -> listOf(
                OperatorStep("Уставки и защиты", "Зайти в «Уставки и защиты».", R.drawable.etalon_new_rich_01),
                OperatorStep("Уставки", "Зайти в «Уставки».", R.drawable.etalon_new_rich_02),
                OperatorStep("Время работы", "Повышенная: $htime мин; пониженная: $ltime мин.", R.drawable.etalon_new_rich_03),
                OperatorStep("Нижняя частота", "«Уставка низк. F»: $lo Гц.", R.drawable.etalon_new_rich_04),
                OperatorStep("Токовые защиты", "Нажать «Отмена» и зайти в «Токовые защиты».", R.drawable.etalon_new_rich_05),
                OperatorStep("Задержка отключения", "Установить ${formatNumber(uv!!)} с.", R.drawable.etalon_new_rich_06),
                OperatorStep("Уставки", "Нажать «Отмена» и зайти в «Уставки».", R.drawable.etalon_new_rich_07),
                OperatorStep("Пониженная частота", "Выбрать «Да».", R.drawable.etalon_new_rich_08)
            )
            !isRamp && station == "ТРИОЛ (УМКА)" -> listOf(
                OperatorStep("Защиты", "Зайти в «Защиты».", R.drawable.triol_rich_01),
                OperatorStep("ЗСП", "Зайти в «ЗСП».", R.drawable.triol_rich_02),
                OperatorStep("Время ЗСП", "Установить ${formatNumber(uv!!)} с.", R.drawable.triol_rich_03),
                OperatorStep("Режимы работы", "Дважды нажать стрелку влево и зайти в «Режимы работы».", R.drawable.triol_rich_05),
                OperatorStep("Работа по таймеру", "Зайти в «Работа по таймеру».", R.drawable.triol_rich_06),
                OperatorStep("Работа по программе", "Выбрать «С 2 частотой» вместо «С остановом».", R.drawable.triol_rich_07),
                OperatorStep("Время на Fзад", "Установить $htime мин.", R.drawable.triol_rich_08),
                OperatorStep("Время на F2", "Установить $ltime мин.", R.drawable.triol_rich_09),
                OperatorStep("Частота F2", "Установить $lo Гц.", R.drawable.triol_rich_10)
            )
            station == "ОРИОН-01" -> listOf(
                OperatorStep("Защиты", "В корневом каталоге открыть «Защиты».", R.drawable.orion_rich_01),
                OperatorStep("Недогрузка", "Открыть «Недогрузка».", R.drawable.orion_rich_02),
                OperatorStep("Время срабатывания", "Установить НАШЕ время срабатывания недогрузки ${formatNumber(uv!!)} с.", R.drawable.orion_rich_03),
                OperatorStep("Управление режимами", "Дважды нажать стрелку влево и открыть «Управление режимами».", R.drawable.orion_rich_04),
                OperatorStep("Две частоты", "Открыть «Режим работы на двух частотах».", R.drawable.orion_rich_05),
                OperatorStep("Рабочая частота", "Установить НАШУ рабочую частоту $hi Гц.", R.drawable.orion_rich_06),
                OperatorStep("Пониженная частота", "Установить НАШУ пониженную частоту $lo Гц.", R.drawable.orion_rich_07),
                OperatorStep("Время на пониженной", "Установить НАШЕ время $ltime мин.", R.drawable.orion_rich_08),
                OperatorStep("Время на рабочей", "Установить НАШЕ время $htime мин.", R.drawable.orion_rich_09),
                OperatorStep("Включение", "Кнопкой «−» вернуться к «Режим двух частот» и выбрать «Вкл».", R.drawable.orion_rich_10)
            )
            station == "ЭЛЕКТОН" -> listOf(
                OperatorStep("Уставки и защиты", "Открыть «Уставки и защиты».", R.drawable.elekton_rich_01),
                OperatorStep("Недогруз двигателя", "Параметр 103: установить НАШЕ время ${secondsClock(uv!!)}.", R.drawable.elekton_rich_02),
                OperatorStep("Технологические настройки", "Открыть «Технол. настройки».", R.drawable.elekton_rich_03),
                OperatorStep("Периодический режим", "Открыть «6.2 Периодич. режим».", R.drawable.elekton_rich_04),
                OperatorStep("Времена работы", "Параметр 209: ${minutesClock(hm!!)}; параметр 210: ${minutesClock(lm!!)}.", R.drawable.elekton_rich_05),
                OperatorStep("Выходная частота останова", "Параметр 218: установить НАШУ пониженную частоту $lo Гц.", R.drawable.elekton_rich_06),
                OperatorStep("Работа по программе", "Параметр 208: выбрать «Режим с измен. част.».", R.drawable.elekton_rich_07)
            )
            station == "Борец КАСКАД-НТ" -> listOf(
                OperatorStep("Вход в меню", "Нажать «Ввод / Enter».", R.drawable.borets_cascade_01),
                OperatorStep("Уставки", "Открыть «Уставки».", R.drawable.borets_cascade_02),
                OperatorStep("Уставки I", "Открыть «Уставки I».", R.drawable.borets_rich_03),
                OperatorStep("ЗСП", "Установить ЗСП ${formatNumber((lm!! + 1.0) * 60.0)} с. ", R.drawable.borets_rich_04),
                OperatorStep("Уставки другие", "Открыть «Уставки другие».", R.drawable.borets_cascade_03),
                OperatorStep("Пониженная частота", "В «NminПРГ» установить $lo Гц.", R.drawable.borets_rich_06),
                OperatorStep("ПРГ пауза", "Установить НАШЕ время на пониженной частоте $ltime мин.", R.drawable.borets_rich_06),
                OperatorStep("ПРГ работа", "Установить НАШЕ время на рабочей частоте $htime мин и включить ПРГ.", R.drawable.borets_rich_08)
            )
            else -> listOf(OperatorStep("Алгоритм не подтверждён", "Для этой станции пошаговый алгоритм пока не подтверждён. Не переносите настройки другой станции.", null))
        }
    }
    BackHandler { if (viewing) { viewing = false; stepIndex = 0 } else onBack() }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(padding)) {
            CalculationTopBar(if (viewing) "$station — ${if (isRamp) "Разгон" else "РИЧ"}" else if (isRamp) "Разгон" else "РИЧ") {
                if (viewing) { viewing = false; stepIndex = 0 } else onBack()
            }
            if (!viewing) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Введите свои значения", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    @Composable fun Field(label: String, value: String, change: (String) -> Unit) {
                        OutlinedTextField(value = value, onValueChange = change, label = { Text(label) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    if (isRamp) {
                        Field("Начальная частота, Гц", start) { start = it }
                        Field("Конечная частота, Гц", end) { end = it }
                        Field("Шаг изменения частоты, Гц", delta) { delta = it }
                        Field("Время одного шага, с", stepMinutes) { stepMinutes = it }
                    } else {
                        Field("Рабочая / повышенная частота, Гц", high) { high = it }
                        Field("Пониженная частота, Гц", low) { low = it }
                        Field("Время на рабочей частоте, мин", highMinutes) { highMinutes = it }
                        Field("Время на пониженной частоте, мин", lowMinutes) { lowMinutes = it }
                        if (lm != null) Text("Время срабатывания недогрузки (автоматически): ${formatNumber((lm + 1.0) * 60.0)} с")
                    }
                    Text("Станция управления", fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(value = station, onValueChange = {}, readOnly = true,
                            label = { Text("Выберите станцию") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            stations.forEach { name -> DropdownMenuItem(text = { Text(name) }, onClick = { station = name; expanded = false }) }
                        }
                    }
                    if (!valid) Text("Заполните поля корректными значениями. Частоты и времена должны быть положительными.", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { stepIndex = 0; viewing = true }, enabled = valid && supported, modifier = Modifier.fillMaxWidth()) {
                        Text("Перейти к инструкции")
                    }
                }
            } else {
                val list = steps()
                val item = list[stepIndex.coerceIn(0, list.lastIndex)]
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Шаг ${stepIndex + 1} из ${list.size}", fontWeight = FontWeight.Bold)
                    Text(item.title, style = MaterialTheme.typography.titleLarge)
                    item.image?.let { Image(painterResource(it), contentDescription = item.title,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp), contentScale = ContentScale.Fit) }
                    Text(item.description, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { if (stepIndex == 0) viewing = false else stepIndex-- }) { Text("Предыдущий") }
                        Button(onClick = { if (stepIndex == list.lastIndex) viewing = false else stepIndex++ }) {
                            Text(if (stepIndex == list.lastIndex) "К параметрам" else "Далее")
                        }
                    }
                }
            }
        }
    }
}
