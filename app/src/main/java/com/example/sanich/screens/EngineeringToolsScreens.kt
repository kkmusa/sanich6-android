package com.example.sanich.screens

import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.example.sanich.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

private data class StationGuide(val name: String, val ramp: String, val rich: String)

private val stationGuides = listOf(
    StationGuide("Электон-05Ф",
        "Разгон: перенеси параметры, выданные технологом. Для программного изменения частоты не путай темп изменения частоты (Гц/ч) с временем ступени.",
        "РИЧ: введи верхнюю и нижнюю частоты и времена каждой ступени по заданию технолога. РИЧ — не «встряхивание». После запуска проконтролируй первый полный цикл."),
    StationGuide("Эталон ЧР",
        "Разгон: задай параметры разгона по технологическому заданию и контролируй фактическую частоту.",
        "РИЧ: Fверх / Fниз / Tверх / Tниз. После запуска проконтролируй переходы и первый полный цикл."),
    StationGuide("Триол АК-06 / УМКА",
        "Разгон: используй сценарий разгона станции и параметры технолога.",
        "РИЧ: режим работы по таймеру со второй частотой. Задай две частоты и времена. Не путай с режимом встряхивания."),
    StationGuide("ИРЗ ЧР / УМКА (Сириус)",
        "Разгон: используй настройки выбранного контроллера; перед подтверждением проверь единицы времени.",
        "РИЧ: Fверх / Fниз / Tверх / Tниз; обязательно проверь единицы времени на панели и первый цикл."),
    StationGuide("Борец-15 — КАСКАД-НТ",
        "Разгон: отдельная настройка КАСКАД-НТ; не переноси пункты меню с АРГУС. Контроллер может оперировать частотой вращения вала насоса.",
        "РИЧ: верхняя/нижняя ступени и времена задаются по технологическому заданию. Учитывай, что отображаемый параметр может быть частотой вращения вала насоса."),
    StationGuide("Борец-15 — АРГУС",
        "Разгон: отдельная настройка АРГУС; не переноси пункты меню с КАСКАД-НТ.",
        "РИЧ: Fверх / Fниз / Tверх / Tниз по заданию технолога; проконтролируй первый цикл."),
    StationGuide("Орион-03 — двухстрочный дисплей",
        "Разгон: используй меню Орион-03 и параметры технолога.",
        "РИЧ: верхняя/нижняя частота и времена; перед запуском сверяй отображаемые единицы и проконтролируй первый цикл.")
)

@Composable
fun StationModesScreen(onBack: () -> Unit) {
    var station by remember { mutableStateOf<StationGuide?>(null) }
    var mode by remember { mutableStateOf<String?>(null) }
    BackHandler { when { mode != null -> mode = null; station != null -> station = null; else -> onBack() } }
    Scaffold { pv ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(pv)) {
            CalculationTopBar(mode ?: station?.name ?: "Разгон и РИЧ") {
                when { mode != null -> mode = null; station != null -> station = null; else -> onBack() }
            }
            Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    station == null -> {
                        Text("Выберите станцию управления", fontWeight = FontWeight.Bold)
                        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) { stationGuides.forEach { s -> ChoiceCard(s.name) { station = s } } }
                    }
                    mode == null -> {
                        Text("Выберите действие", fontWeight = FontWeight.Bold)
                        ChoiceCard("Разгон") { mode = "Разгон" }
                        ChoiceCard("РИЧ — режим изменения частоты") { mode = "РИЧ — режим изменения частоты" }
                    }
                    else -> {
                        if (mode!!.startsWith("РПД") && station!!.name == "ИРЗ ЧР / УМКА (Сириус)") {
                            IrzRpdGuide()
                        } else if (mode!!.startsWith("РПД")) {
                            UnconfirmedRichGuide(station!!.name, "Для РПД расчёт уставок уже добавлен отдельным калькулятором. Точный путь по меню этой станции будет показан только после подтверждения руководством именно этой версии контроллера.")
                        } else if (mode == "Разгон" && station!!.name == "ИРЗ ЧР / УМКА (Сириус)") {
                            IrzRampGuide()
                        } else if (mode == "Разгон" && station!!.name == "Орион-03 — двухстрочный дисплей") {
                            OrionPhotoRampGuide()
                        } else if (mode == "Разгон" && station!!.name == "Триол АК-06 / УМКА") {
                            TriolRampGuide()
                        } else if (mode == "Разгон" && station!!.name == "Борец-15 — КАСКАД-НТ") {
                            CascadeRampGuide()
                        } else if (mode == "Разгон" && station!!.name == "Электон-05Ф") {
                            ElektonRampGuide()
                        } else if (mode == "Разгон" && station!!.name == "Эталон ЧР") {
                            EtalonRampGuide()
                        } else if (mode != "Разгон" && station!!.name == "Орион-03 — двухстрочный дисплей") {
                            OrionRichGuide()
                        } else if (mode != "Разгон" && station!!.name == "Борец-15 — КАСКАД-НТ") {
                            CascadeRichGuide()
                        } else if (mode != "Разгон" && station!!.name == "Эталон ЧР") {
                            EtalonRichGuide()
                        } else if (mode != "Разгон" && station!!.name == "Триол АК-06 / УМКА") {
                            TriolRichGuide()
                        } else if (mode != "Разгон" && station!!.name == "Электон-05Ф") {
                            ElektonRichGuide()
                        } else if (mode != "Разгон" && station!!.name == "ИРЗ ЧР / УМКА (Сириус)") {
                            UnconfirmedRichGuide("ИРЗ ЧР / УМКА (Сириус)", "РИЧ пока не добавлен: точный алгоритм чередования двух частот с двумя независимыми выдержками по документации не подтверждён.")
                        } else if (mode != "Разгон" && station!!.name.startsWith("Борец-15")) {
                            UnconfirmedRichGuide(station!!.name, "РИЧ пока не добавлен: найденный программный режим описывает работу/останов, а не подтверждённое чередование двух частот.")
                        } else {
                            val text = if (mode == "Разгон") station!!.ramp else station!!.rich
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(station!!.name, fontWeight = FontWeight.Bold)
                                    Text(text)
                                    if (mode != "Разгон") Text("Контроль: на нижней поддерживающей частоте проверь отсутствие подачи, если это предусмотрено заданием технолога.", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun RichInputs(title: String, content: @Composable (Double, Double, Double, Double) -> Unit) {
    var fHigh by remember { mutableStateOf("") }; var tHigh by remember { mutableStateOf("") }
    var fLow by remember { mutableStateOf("") }; var tLow by remember { mutableStateOf("") }
    val fh=fHigh.decimal(); val th=tHigh.decimal(); val fl=fLow.decimal(); val tl=tLow.decimal()
    Card(colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text(title,fontWeight=FontWeight.Bold); Text("Введите задание технолога: две частоты и время работы на каждой.")
        DecimalField(fHigh,{fHigh=it},"Рабочая / верхняя частота, Гц"); DecimalField(tHigh,{tHigh=it},"Время на верхней частоте, мин")
        DecimalField(fLow,{fLow=it},"Пониженная частота, Гц"); DecimalField(tLow,{tLow=it},"Время на пониженной частоте, мин")
        if(fh!=null && th!=null && fl!=null && tl!=null) content(fh,th,fl,tl)
    }}
}


@Composable private fun OrionPhotoRampGuide() {
    var viewing by remember { mutableStateOf(false) }
    val steps = listOf(
        EtalonStep("Управление режимами", "В корневом каталоге найти «Управление режимами».", R.drawable.orion_ramp_01),
        EtalonStep("Программное изменение частоты", "Открыть «Программное изменение частоты».", R.drawable.orion_ramp_02),
        EtalonStep("Конечная частота", "Листать вправо и установить конечную частоту, Гц.", R.drawable.orion_ramp_03),
        EtalonStep("Начальная частота", "Установить начальную частоту, Гц.", R.drawable.orion_ramp_04),
        EtalonStep("Темп изменения", "Установить темп изменения частоты в Гц/ч.", R.drawable.orion_ramp_05),
        EtalonStep("Включение", "Вернуться к первому пункту «Программное изменение частоты» и установить «Вкл». На фото показано «Откл» до включения.", R.drawable.orion_ramp_06)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) EtalonStepViewer(steps,onExit={viewing=false})
    else Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("ОРИОН — Разгон",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        Text("6 шагов с фотографиями. Уставки вводятся по заданию технолога; темп в Гц/ч.")
        Button(onClick={viewing=true},modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
    }
}
@Composable private fun ElektonRampGuide() {
    var viewing by remember { mutableStateOf(false) }
    val steps = listOf(
        EtalonStep("Технологические настройки", "В главном меню открыть «6. Технол. настройки».", R.drawable.elekton_ramp_01),
        EtalonStep("Программное изменение частоты", "Открыть «6.5 Прогр. изм. част.».", R.drawable.elekton_ramp_02),
        EtalonStep("Начальная частота", "Параметр 230 «Начальная част. пуска»: установить начальную частоту, Гц.", R.drawable.elekton_ramp_03),
        EtalonStep("Конечная частота и темп", "Установить 231 «Конечная част.» (Гц) и 232 «Темп изменения частоты» (Гц/ч).", R.drawable.elekton_ramp_04),
        EtalonStep("Включить программу", "Вернуться к 229 «Прогр. изм. част.» и выбрать «Вкл». На фото показано «Откл» до включения.", R.drawable.elekton_ramp_05)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) EtalonStepViewer(steps,onExit={viewing=false})
    else Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("ЭЛЕКТОН — Разгон",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        Text("5 шагов с фотографиями; конечная частота и темп на одном экране.")
        Button(onClick={viewing=true},modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
    }
}

@Composable private fun OrionRichGuide() {
    var viewing by remember { mutableStateOf(false) }
    val steps = listOf(
        EtalonStep("Защиты", "В корневом каталоге открыть «Защиты».", R.drawable.orion_rich_01),
        EtalonStep("Недогрузка", "Найти пункт «Недогрузка».", R.drawable.orion_rich_02),
        EtalonStep("Время срабатывания", "Найти «Недогрузка время срабатывания» и установить значение по заданию технолога.", R.drawable.orion_rich_03),
        EtalonStep("Управление режимами", "Дважды нажать стрелку влево для возврата в меню и открыть «Управление режимами».", R.drawable.orion_rich_04),
        EtalonStep("Режим двух частот", "Найти «Режим работы на двух частотах» и войти в этот раздел.", R.drawable.orion_rich_05),
        EtalonStep("Рабочая частота", "Установить рабочую частоту по заданию технолога.", R.drawable.orion_rich_06),
        EtalonStep("Пониженная частота", "Установить пониженную частоту по заданию технолога.", R.drawable.orion_rich_07),
        EtalonStep("Время на пониженной", "Установить время работы на пониженной частоте.", R.drawable.orion_rich_08),
        EtalonStep("Время на повышенной", "Установить время работы на повышенной частоте.", R.drawable.orion_rich_09),
        EtalonStep("Включить режим", "Кнопкой «−» вернуться к первому пункту «Режим двух частот» и выбрать «Вкл».", R.drawable.orion_rich_10)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) EtalonStepViewer(steps, onExit={viewing=false})
    else Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("ОРИОН — РИЧ",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        Text("Пошаговая инструкция по меню контроллера. Уставки берутся из задания технолога.")
        Button(onClick={viewing=true},modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
    }
}
@Composable private fun EtalonRichGuide() {
    var high by remember { mutableStateOf("") }; var low by remember { mutableStateOf("") }
    var highTime by remember { mutableStateOf("") }; var lowTime by remember { mutableStateOf("") }
    var viewing by remember { mutableStateOf(false) }
    val h=high.decimal(); val l=low.decimal(); val ht=highTime.decimal(); val lt=lowTime.decimal()
    val valid=h!=null && l!=null && ht!=null && lt!=null && h.isFinite() && l.isFinite() && ht.isFinite() && lt.isFinite() && h>l && l>0 && ht>0 && lt>0
    val steps=listOf(
        EtalonStep("Уставки и защиты", "Зайти в «Уставки и защиты».", R.drawable.etalon_new_rich_01),
        EtalonStep("Уставки", "Зайти в «Уставки».", R.drawable.etalon_new_rich_02),
        EtalonStep("Время работы", "Установить время на повышенной частоте — ${ht?.pretty() ?: "—"} мин; на пониженной — ${lt?.pretty() ?: "—"} мин.", R.drawable.etalon_new_rich_03),
        EtalonStep("Нижняя частота", "В пункте «Уставка низк. F» установить ${l?.pretty() ?: "—"} Гц.", R.drawable.etalon_new_rich_04),
        EtalonStep("Токовые защиты", "Нажать «Отмена» и зайти в «Токовые защиты».", R.drawable.etalon_new_rich_05),
        EtalonStep("Задержка отключения", "Установить задержку отключения — ${lt?.let { ((it + 1.0) * 60.0).pretty() } ?: "—"} сек.", R.drawable.etalon_new_rich_06),
        EtalonStep("Уставки", "Нажать «Отмена» и зайти в «Уставки».", R.drawable.etalon_new_rich_07),
        EtalonStep("Пониженная частота", "В пункте «Пониженная частота» выбрать «Да».", R.drawable.etalon_new_rich_08)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) {
        EtalonStepViewer(steps, onExit={viewing=false})
    } else {
        val focus=LocalFocusManager.current; val keyboard=LocalSoftwareKeyboardController.current
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Эталон ЧР — РИЧ",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
            Text("Введите четыре параметра из задания технолога")
            DecimalField(high,{high=it},"Повышенная частота, Гц")
            DecimalField(low,{low=it},"Пониженная частота, Гц")
            DecimalField(highTime,{highTime=it},"Время на повышенной, мин")
            DecimalField(lowTime,{lowTime=it},"Время на пониженной, мин",onDone={focus.clearFocus();keyboard?.hide();if(valid)viewing=true})
            Button(onClick={focus.clearFocus();keyboard?.hide();viewing=true},enabled=valid,modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
            if(!valid) Text(
                when {
                    high.isBlank() || low.isBlank() || highTime.isBlank() || lowTime.isBlank() -> "Заполните все четыре обязательных поля."
                    h == null || l == null || ht == null || lt == null -> "Введите корректные числа (точка или запятая)."
                    h <= 0 || l <= 0 -> "Частоты должны быть больше нуля."
                    h <= l -> "Повышенная частота должна быть больше пониженной."
                    ht <= 0 || lt <= 0 -> "Время на каждой частоте должно быть больше нуля."
                    else -> "Проверьте введённые значения."
                }, color=MaterialTheme.colorScheme.error
            )
        }
    }
}
@Composable private fun TriolRichGuide() {
    var high by remember { mutableStateOf("") }
    var low by remember { mutableStateOf("") }
    var highTime by remember { mutableStateOf("") }
    var lowTime by remember { mutableStateOf("") }
    var viewing by remember { mutableStateOf(false) }
    val h=high.decimal(); val l=low.decimal(); val ht=highTime.decimal(); val lt=lowTime.decimal()
    val valid=h!=null && l!=null && ht!=null && lt!=null && h.isFinite() && l.isFinite() && ht.isFinite() && lt.isFinite() && h>l && l>0 && ht>0 && lt>0
    val zspMinutes = lt?.plus(1.0)
    val steps=listOf(
        EtalonStep("Защиты", "Зайти в «Защиты».", R.drawable.triol_rich_01),
        EtalonStep("ЗСП", "Зайти в «ЗСП».", R.drawable.triol_rich_02),
        EtalonStep("Время ЗСП", "Установить время срабатывания ЗСП — ${zspMinutes?.times(60)?.pretty() ?: "—"} сек.", R.drawable.triol_rich_03),
        EtalonStep("Режимы работы", "Дважды нажать стрелку влево и зайти в «Режимы работы».", R.drawable.triol_rich_05),
        EtalonStep("Работа по таймеру", "Зайти в «Работа по таймеру».", R.drawable.triol_rich_06),
        EtalonStep("Работа по программе", "В пункте «Работа по прог.» выбрать «С 2 частотой» вместо «С остановом».", R.drawable.triol_rich_07),
        EtalonStep("Время на Fзад", "Установить время работы на повышенной частоте — ${ht?.pretty() ?: "—"} мин.", R.drawable.triol_rich_08),
        EtalonStep("Время на F2", "Установить время работы на пониженной частоте — ${lt?.pretty() ?: "—"} мин.", R.drawable.triol_rich_09),
        EtalonStep("Частота F2", "Установить пониженную частоту — ${l?.pretty() ?: "—"} Гц.", R.drawable.triol_rich_10)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) {
        EtalonStepViewer(steps, onExit={viewing=false})
    } else {
        val focus=LocalFocusManager.current; val keyboard=LocalSoftwareKeyboardController.current
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            DecimalField(high,{high=it},"Повышенная частота, Гц")
            DecimalField(low,{low=it},"Пониженная частота, Гц")
            DecimalField(highTime,{highTime=it},"Время на повышенной, мин")
            DecimalField(lowTime,{lowTime=it},"Время на пониженной, мин",onDone={focus.clearFocus();keyboard?.hide();if(valid)viewing=true})
            Button(onClick={focus.clearFocus();keyboard?.hide();viewing=true},enabled=valid,modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
            if(!valid) Text("Введите корректные значения: повышенная частота больше пониженной, частоты и времена больше нуля.",color=MaterialTheme.colorScheme.error)
        }
    }
}
@Composable private fun ElektonRichGuide() {
    var viewing by remember { mutableStateOf(false) }
    val steps = listOf(
        EtalonStep("Уставки и защиты", "Открыть раздел «Уставки и защиты».", R.drawable.elekton_rich_01),
        EtalonStep("Время недогруза", "В пункте «Недограз двиг. время» настроить ЗСП по заданию технолога.", R.drawable.elekton_rich_02),
        EtalonStep("Технологические настройки", "Вернуться в меню и открыть «Технол. настройки».", R.drawable.elekton_rich_03),
        EtalonStep("Периодический режим", "Выбрать «6.2 Периодич. режим» и нажать «ВВОД».", R.drawable.elekton_rich_04),
        EtalonStep("Времена работы", "Установить время повышенной и пониженной частоты. На фото: 209 «Время работы по прог.» и 210 «Время останова по прог.»; значения вводятся по заданию технолога.", R.drawable.elekton_rich_05),
        EtalonStep("Выходная частота останова", "Найти «218 Вых. частота остан.» и установить пониженную частоту по заданию технолога. На фото показано 45,00 Гц — это пример, а не обязательная уставка.", R.drawable.elekton_rich_06),
        EtalonStep("Работа по программе", "В пункте «208 Работа по прог.» выбрать «Режим с измен. част.».", R.drawable.elekton_rich_07)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) EtalonStepViewer(steps, onExit={viewing=false})
    else Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("ЭЛЕКТОН — РИЧ",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        Text("7 шагов с фотографиями предоставленных экранов. Числовые значения на фото — примеры, не готовые уставки.")
        Button(onClick={viewing=true},modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
    }
}
@Composable private fun UnconfirmedRichGuide(name:String, reason:String) {
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF3CD))) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(name,fontWeight=FontWeight.Bold); Text(reason); Text("Неподтверждённые параметры оператору не показываем.",fontWeight=FontWeight.Bold)
    }}
}

@Composable
private fun IrzRampGuide() {
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var delta by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }
    var showGuide by remember { mutableStateOf(false) }

    val startV = start.decimal()
    val endV = end.decimal()
    val deltaV = delta.decimal()
    val secondsV = seconds.decimal()
    val rate = if (deltaV != null && secondsV != null && deltaV > 0 && secondsV > 0) deltaV * 3600.0 / secondsV else null
    val irzRate = rate?.let { kotlin.math.round(it * 100.0) / 100.0 }
    val actualSeconds = if (deltaV != null && irzRate != null && irzRate > 0) deltaV / irzRate * 3600.0 else null

    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("ИРЗ — программное изменение частоты", fontWeight = FontWeight.Bold)
            Text("Введите задание технолога. Приложение переведёт его в темп изменения частоты, Гц/ч.")
            DecimalField(start, { start = it }, "Начальная частота, Гц")
            DecimalField(end, { end = it }, "Конечная частота, Гц")
            DecimalField(delta, { delta = it }, "Изменение частоты за один интервал, Гц")
            DecimalField(seconds, { seconds = it }, "Время интервала, сек")

            if (rate != null && irzRate != null) {
                HorizontalDivider()
                Text("Расчёт", fontWeight = FontWeight.Bold)
                Text("Требуемый темп: ${String.format(Locale.US, "%.4f", rate)} Гц/ч")
                Text("Значение с шагом 0,01: ${String.format(Locale.US, "%.2f", irzRate)} Гц/ч", fontWeight = FontWeight.Bold)
                actualSeconds?.let { Text("Расчётное время на ${deltaV!!.pretty()} Гц при округлении: ${it.toInt()} сек") }
                Button(onClick = { showGuide = !showGuide }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showGuide) "Скрыть инструкцию" else "Как настроить ИРЗ")
                }
            }
        }
    }

    if (showGuide && startV != null && endV != null && irzRate != null) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Путь по меню", fontWeight = FontWeight.Bold)
                Text("Уставки и настройки → Технологические настройки → Программное изменение частоты")
                HorizontalDivider()
                Text("Что выставить", fontWeight = FontWeight.Bold)
                Text("255  Начальная частота пуска → ${String.format(Locale.US, "%.2f", startV)} Гц")
                Text("256  Конечная частота → ${String.format(Locale.US, "%.2f", endV)} Гц")
                Text("257  Темп изменения частоты → ${String.format(Locale.US, "%.2f", irzRate)} Гц/ч")
                Text("258  Начальная уставка недогруза → проверить по действующей карте/уставкам; приложение её не подставляет автоматически.")
                Text("254  Режим программного изменения → выбрать требуемый технологом режим только после проверки остальных параметров.")
                HorizontalDivider()
                Text("Важно: номера и названия пунктов зависят от версии ПО контроллера. Перед изменением уставок сверьте их с РЭ именно вашей станции и действующей технологической картой.", fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
private fun OrionRampGuide() {
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var delta by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }
    var showGuide by remember { mutableStateOf(false) }

    val startV = start.decimal()
    val endV = end.decimal()
    val deltaV = delta.decimal()
    val secondsV = seconds.decimal()
    val rate = if (deltaV != null && secondsV != null && deltaV > 0 && secondsV > 0) deltaV * 3600.0 / secondsV else null
    val orionRate = rate?.let { kotlin.math.round(it * 100.0) / 100.0 }
    val actualSeconds = if (deltaV != null && orionRate != null && orionRate > 0) deltaV / orionRate * 3600.0 else null
    val rateInRange = orionRate != null && orionRate in 0.01..600.0

    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("ОРИОН-03 6.2.93 — программное изменение частоты", fontWeight = FontWeight.Bold)
            Text("Введите задание технолога. Для П_1.4.02 ОРИОН использует темп в Гц/час с шагом 0,01.")
            DecimalField(start, { start = it }, "Начальная частота, Гц")
            DecimalField(end, { end = it }, "Конечная частота, Гц")
            DecimalField(delta, { delta = it }, "Изменение частоты за один интервал, Гц")
            DecimalField(seconds, { seconds = it }, "Время интервала, сек")

            if (rate != null && orionRate != null) {
                HorizontalDivider()
                Text("Расчёт", fontWeight = FontWeight.Bold)
                Text("Требуемый темп: ${String.format(Locale.US, "%.4f", rate)} Гц/ч")
                Text("П_1.4.02 → ${String.format(Locale.US, "%.2f", orionRate)} Гц/ч", fontWeight = FontWeight.Bold)
                actualSeconds?.let { Text("Фактическое время на ${deltaV!!.pretty()} Гц после округления: ${it.toInt()} сек") }
                if (!rateInRange) {
                    Text("Полученный темп вне диапазона ОРИОН 0,01…600,00 Гц/ч. Такой расчёт вводить нельзя.", fontWeight = FontWeight.Bold)
                } else {
                    Button(onClick = { showGuide = !showGuide }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (showGuide) "Скрыть инструкцию" else "Как настроить ОРИОН-03")
                    }
                }
            }
        }
    }

    if (showGuide && startV != null && endV != null && orionRate != null && rateInRange) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Куда нажимать", fontWeight = FontWeight.Bold)
                Text("1. На основном экране нажмите МЕНЮ.")
                Text("2. Кнопками + / − выберите «1. УПРАВЛЕНИЕ РЕЖИМАМИ» и нажмите ↵.")
                Text("3. Кнопками + / − выберите «1.4 ПРОГРАММНОЕ ИЗМЕНЕНИЕ ЧАСТОТЫ» и нажмите ↵.")
                HorizontalDivider()
                Text("Что выставить", fontWeight = FontWeight.Bold)
                Text("П_1.4.03  НАЧАЛЬНАЯ ЧАСТОТА → ${String.format(Locale.US, "%.2f", startV)} Гц")
                Text("П_1.4.04  КОНЕЧНАЯ ЧАСТОТА → ${String.format(Locale.US, "%.2f", endV)} Гц")
                Text("П_1.4.02  ТЕМП ПРОГРАММНОГО ИЗМЕНЕНИЯ ЧАСТОТЫ → ${String.format(Locale.US, "%.2f", orionRate)} Гц/ч")
                Text("После проверки: П_1.4.01  ПРОГРАММНОЕ ИЗМЕНЕНИЕ ЧАСТОТЫ → ВКЛ", fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Text("Редактирование числа", fontWeight = FontWeight.Bold)
                Text("↵ — начать редактирование / сохранить; + и − — изменить значение; ← — перейти между разрядами. МЕНЮ — отмена изменения/выход в основной режим.")
                HorizontalDivider()
                Text("Важно", fontWeight = FontWeight.Bold)
                Text("Если ПЭД уже работает, ОРИОН сначала перейдёт от текущей частоты к П_1.4.03 обычным темпом разгона/торможения, затем выполнит программный переход к П_1.4.04.")
                Text("После завершения П_1.4.01 автоматически станет ОТКЛ. Ручное или дистанционное изменение уставки частоты во время выполнения прерывает программный режим.")
                Text("Инструкция относится к ОРИОН-03 с ПО КСУ 6.2.93. Перед изменением уставок сверяйте версию ПО и действующее технологическое задание.", fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
private fun TriolRampGuide() {
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var delta by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }
    var viewing by remember { mutableStateOf(false) }
    val sv=start.decimal(); val ev=end.decimal()
    val dv=delta.decimal(); val sec=seconds.decimal()
    val totalSeconds=if(sv!=null && ev!=null && dv!=null && sec!=null && ev>sv && dv>0 && sec>0) (ev-sv)/dv*sec else null
    val roundedMinutes=totalSeconds?.let { kotlin.math.round(it/60.0).toLong() }
    val duration=roundedMinutes?.let { "%02d:%02d".format(Locale.US,it/60,it%60) } ?: "—"
    val valid=sv!=null && ev!=null && dv!=null && sec!=null && sv.isFinite() && ev.isFinite() && dv.isFinite() && sec.isFinite() && sv>0 && ev>sv && dv>0 && sec>0 && totalSeconds!=null && totalSeconds.isFinite() && roundedMinutes!=null && roundedMinutes>0
    val steps=listOf(
        EtalonStep("Режимы работы", "Зайти в «Режимы работы».", R.drawable.triol_ramp_01),
        EtalonStep("Программный режим", "Зайти в «Программный режим».", R.drawable.triol_ramp_02),
        EtalonStep("Начальная частота", "Установить частоту ${sv?.pretty() ?: "—"} Гц.", R.drawable.triol_ramp_03),
        EtalonStep("Конечная частота", "Установить частоту ${ev?.pretty() ?: "—"} Гц.", R.drawable.triol_ramp_04),
        EtalonStep("Время на переход", "Установить время на переход $duration.", R.drawable.triol_ramp_05),
        EtalonStep("Регулятор техн. параметра", "Вернуться назад стрелкой влево и зайти в «Регулятор техн. параметра».", R.drawable.triol_ramp_06),
        EtalonStep("Поддерживаемый параметр", "В пункте «Поддерж. параметр» выбрать «Прогр. F».", R.drawable.triol_ramp_07)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) {
        EtalonStepViewer(steps,onExit={viewing=false})
    } else {
        val focus=LocalFocusManager.current; val keyboard=LocalSoftwareKeyboardController.current
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            DecimalField(start,{start=it},"Начальная частота, Гц")
            DecimalField(end,{end=it},"Конечная частота, Гц")
            DecimalField(delta,{delta=it},"Шаг, Гц")
            DecimalField(seconds,{seconds=it},"Время шага, сек",onDone={focus.clearFocus();keyboard?.hide();if(valid)viewing=true})
            Button(onClick={focus.clearFocus();keyboard?.hide();viewing=true},enabled=valid,modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
            if(!valid) Text("Проверьте частоты, шаг и время шага.",color=MaterialTheme.colorScheme.error)
        }
    }
}

// КАСКАД-НТ: операторские шаги; оригинальные фото без обрезки и ретуши.
@Composable private fun CascadeRampGuide() {
    var start by remember { mutableStateOf("") }; var end by remember { mutableStateOf("") }
    var delta by remember { mutableStateOf("") }; var seconds by remember { mutableStateOf("") }
    var viewing by remember { mutableStateOf(false) }
    val sv=start.decimal(); val ev=end.decimal(); val dv=delta.decimal(); val sec=seconds.decimal()
    val valid=sv!=null && ev!=null && dv!=null && sec!=null && sv.isFinite() && ev.isFinite() && dv.isFinite() && sec.isFinite() && sv>0 && ev>sv && dv>0 && sec>0
    val total=if(valid) (ev!!-sv!!)/dv!!*sec!! else null
    val duration=if(total!=null && total.isFinite() && total>0) total.pretty() else "—"
    val steps=listOf(
        EtalonStep("Вход в меню", "Нажать «Ввод / Enter».", R.drawable.borets_cascade_01),
        EtalonStep("Уставки", "Выбрать «Уставки» и нажать Enter.", R.drawable.borets_cascade_02),
        EtalonStep("Уставки другие", "Выбрать «Уставки другие» и нажать Enter.", R.drawable.borets_cascade_03),
        EtalonStep("Nн разг", "Листать вправо до «Nн разг». Установить начальную частоту ${sv?.pretty() ?: "—"} Гц.", R.drawable.borets_cascade_04),
        EtalonStep("Время разгона", "Нажать стрелку влево один раз и установить время разгона. Расчётное время разгона: ${total?.pretty() ?: "—"} мин.", R.drawable.borets_cascade_05),
        EtalonStep("Способ изм.N", "Оставить значение «время».", R.drawable.borets_cascade_06),
        EtalonStep("Прог.изм.N", "Установить значение «1раз».", R.drawable.borets_cascade_07),
        EtalonStep("Конечная частота N", "Нажать «Отмена», затем «Ввод». В начале «Уставки другие» выбрать первый параметр «N» и установить ${ev?.pretty() ?: "—"} Гц.", R.drawable.borets_cascade_n_final),
        EtalonStep("Главный экран", "Вернуться на главный экран; проверить появление символа программы изменения частоты.", R.drawable.borets_cascade_09)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) EtalonStepViewer(steps,onExit={viewing=false})
    else {
        val focus=LocalFocusManager.current; val keyboard=LocalSoftwareKeyboardController.current
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            DecimalField(start,{start=it},"Начальная частота, Гц")
            DecimalField(end,{end=it},"Конечная частота, Гц")
            DecimalField(delta,{delta=it},"Шаг, Гц")
            DecimalField(seconds,{seconds=it},"Время шага, мин",onDone={focus.clearFocus();keyboard?.hide();if(valid && total!=null && total.isFinite()) viewing=true})
            Button(onClick={focus.clearFocus();keyboard?.hide();viewing=true},enabled=valid && total!=null && total.isFinite() && total>0,modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
            if(!valid) Text("Проверьте частоты, шаг и время шага. Конечная частота должна быть больше начальной.",color=MaterialTheme.colorScheme.error)
        }
    }
}

@Composable private fun CascadeRichGuide() {
    var high by remember { mutableStateOf("") }; var low by remember { mutableStateOf("") }
    var highTime by remember { mutableStateOf("") }; var lowTime by remember { mutableStateOf("") }
    var viewing by remember { mutableStateOf(false) }
    val h=high.decimal(); val l=low.decimal(); val ht=highTime.decimal(); val lt=lowTime.decimal()
    val valid=h!=null && l!=null && ht!=null && lt!=null && h.isFinite() && l.isFinite() && ht.isFinite() && lt.isFinite() && h>l && l>0 && ht>0 && lt>0
    val zsp=lt?.let { (it+1.0)*60.0 }
    val steps=listOf(
        EtalonStep("Вход в меню", "Нажать «Ввод / Enter».", R.drawable.borets_cascade_01),
        EtalonStep("Уставки", "Выбрать «Уставки» и нажать Enter.", R.drawable.borets_cascade_02),
        EtalonStep("Уставки I", "Зайти в «Уставки I».", R.drawable.borets_rich_03),
        EtalonStep("ЗдОтк ЗСП", "Листать вправо до «ЗдОтк ЗСП». Расчётное время срабатывания ЗСП: ${zsp?.pretty() ?: "—"} сек.", R.drawable.borets_rich_04),
        EtalonStep("Уставки другие", "Нажать «Отмена» и перейти в «Уставки другие».", R.drawable.borets_cascade_03),
        EtalonStep("NminПРГ", "Листать вправо до «NminПРГ». Установить минимальную частоту ${l?.pretty() ?: "—"} Гц.", R.drawable.borets_rich_06),
        EtalonStep("ПРГ пауза", "Листать влево до «ПРГ пауза». Установить время на пониженной частоте ${lt?.pretty() ?: "—"} мин.", R.drawable.borets_rich_06),
        EtalonStep("ПРГ работа и включение", "В «ПРГ работа» установить время на повышенной частоте ${ht?.pretty() ?: "—"} мин. Параметр «ПРГ» перевести в «Вкл».", R.drawable.borets_rich_08)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) {
        EtalonStepViewer(steps,onExit={viewing=false})
    } else {
        val focus=LocalFocusManager.current; val keyboard=LocalSoftwareKeyboardController.current
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            DecimalField(high,{high=it},"Повышенная частота, Гц")
            DecimalField(low,{low=it},"Пониженная частота, Гц")
            DecimalField(highTime,{highTime=it},"Время на повышенной, мин")
            DecimalField(lowTime,{lowTime=it},"Время на пониженной, мин",onDone={focus.clearFocus();keyboard?.hide();if(valid)viewing=true})
            Button(onClick={focus.clearFocus();keyboard?.hide();viewing=true},enabled=valid,modifier=Modifier.fillMaxWidth()) { Text("Показать инструкцию") }
            if(!valid) Text("Повышенная частота должна быть больше пониженной; время на каждой частоте должно быть положительным.",color=MaterialTheme.colorScheme.error)
        }
    }
}

private data class EtalonStep(val title:String, val instruction:String, val photo:Int?)

@Composable private fun EtalonStepViewer(steps:List<EtalonStep>, onExit:()->Unit) {
    var index by remember { mutableIntStateOf(0) }
    val step=steps[index]
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=12.dp, vertical=8.dp),
        verticalArrangement=Arrangement.spacedBy(10.dp)
    ) {
        Text("Шаг ${index+1} из ${steps.size}",fontWeight=FontWeight.Bold)
        Row(Modifier.fillMaxWidth().height(8.dp),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
            steps.forEachIndexed { i, _ ->
                Box(Modifier.weight(1f).fillMaxHeight().background(
                    if(i<=index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ))
            }
        }
        Text(step.title,style=MaterialTheme.typography.titleLarge)
        if(step.photo != null) {
            Image(
                painter=painterResource(step.photo),
                contentDescription="Фото контроллера: ${step.title}",
                modifier=Modifier.fillMaxWidth().aspectRatio(1.6f),
                contentScale=androidx.compose.ui.layout.ContentScale.Fit
            )
            Text("Это лишь пример фото контроллера! Вводи свои значения!",style=MaterialTheme.typography.bodySmall,fontWeight=FontWeight.Bold)
        } else {
            Text("Фотография этого шага пока не предоставлена.",style=MaterialTheme.typography.bodySmall)
        }
        Text(step.instruction,fontWeight=FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            OutlinedButton(onClick={ if(index==0) { onExit() } else { index -= 1 } }) { Text("Назад") }
            Button(onClick={ if(index==steps.lastIndex) { onExit() } else { index += 1 } }) {
                Text(if(index==steps.lastIndex) "К параметрам" else "Далее")
            }
        }
    }
}

@Composable private fun EtalonRampGuide() {
    var start by remember { mutableStateOf("") }; var end by remember { mutableStateOf("") }
    var delta by remember { mutableStateOf("") }; var seconds by remember { mutableStateOf("") }
    var viewing by remember { mutableStateOf(false) }
    val sv=start.decimal(); val ev=end.decimal(); val dv=delta.decimal(); val sec=seconds.decimal()
    val valid=sv!=null && ev!=null && dv!=null && sec!=null && sv.isFinite() && ev.isFinite() && dv.isFinite() && sec.isFinite() && dv>0 && sec>0 && sv>0 && ev>0
    val steps=listOf(
        EtalonStep("Параметры ЧРП", "Зайти в «Параметры ЧРП».", R.drawable.etalon_new_ramp_01),
        EtalonStep("Прогр. изм F", "Зайти в «Прогр. изм F».", R.drawable.etalon_new_ramp_02),
        EtalonStep("Начальная и конечная частоты", "Установить начальную частоту ${sv?.pretty() ?: "—"} Гц и конечную ${ev?.pretty() ?: "—"} Гц.", R.drawable.etalon_new_ramp_03),
        EtalonStep("Шаг по регл.", "Установить шаг ${dv?.pretty() ?: "—"} Гц.", R.drawable.etalon_new_ramp_04),
        EtalonStep("Задерж. по регл.", "Установить задержку ${sec?.pretty() ?: "—"} сек.", R.drawable.etalon_new_ramp_05),
        EtalonStep("Вывод на режим", "В пункте «Вывод на режим» выбрать «Однокр».", R.drawable.etalon_new_ramp_06)
    )
    BackHandler(enabled=viewing) { viewing=false }
    if(viewing) {
        EtalonStepViewer(steps,onExit={viewing=false})
    } else {
        val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Эталон ЧР — Разгон",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
            DecimalField(start,{start=it},"Начальная частота, Гц")
            DecimalField(end,{end=it},"Конечная частота, Гц")
            DecimalField(delta,{delta=it},"Шаг регулирования, Гц")
            DecimalField(seconds,{seconds=it},"Задержка регулирования, с",onDone={focus.clearFocus();keyboard?.hide();if(valid)viewing=true})
            Button(onClick={focus.clearFocus();keyboard?.hide();viewing=true},enabled=valid,modifier=Modifier.fillMaxWidth()) {Text("Показать инструкцию")}
            if(!valid) Text(
                when {
                    start.isBlank() || end.isBlank() || delta.isBlank() || seconds.isBlank() -> "Заполните все четыре параметра разгона."
                    sv == null || ev == null || dv == null || sec == null -> "Введите корректные числа (точка или запятая)."
                    sv <= 0 || ev <= 0 -> "Частоты должны быть больше нуля."
                    dv <= 0 -> "Шаг регулирования должен быть больше нуля."
                    sec <= 0 -> "Задержка регулирования должна быть больше нуля."
                    else -> "Проверьте введённые значения."
                }, color=MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun formatDuration(seconds: Double): String {
    val total = kotlin.math.round(seconds).toLong(); val h=total/3600; val m=(total%3600)/60; val s=total%60
    return if(s==0L) String.format(Locale.US,"%02d:%02d",h,m) else String.format(Locale.US,"%02d:%02d:%02d",h,m,s)
}

@Composable private fun ChoiceCard(title: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Text(title, Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TemperatureSetpointScreen(historyViewModel: CalculationHistoryViewModel, onBack: () -> Unit) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var working by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("Д1") }
    val max = when (group) { "Д1" -> 120.0; "Д2" -> 150.0; else -> 170.0 }
    val a = working.decimal()?.times(1.05)
    val b = max * 0.85
    val result = if (a != null && b != null) minOf(a,b) else null
    val currentHistoryEntry = result?.takeIf { it.isFinite() }?.let {
        HistoryEntry(title = "Уставка температуры ТМС", method = "Расчёт",
            result = "${it.pretty()} °C", details = "Tраб=$working °C; группа=$group; Tмакс=${max.toInt()} °C")
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)
    fun saveHistory() { latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) } }
    PersistHistoryOnDispose(::saveHistory, debounceKey = result?.let { "$working|$group|$it" })
    fun saveAndBack() { saveHistory(); onBack() }
    BackHandler(onBack = ::saveAndBack)
    Scaffold { pv -> Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(pv)) {
        CalculationTopBar("Настройка температуры ТМС", ::saveAndBack)
        Column(Modifier.padding(14.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("После выхода на установившийся режим. Уставка = меньшее из Tраб × 1,05 и Tмакс × 0,85.")
            DecimalField(working, {working=it}, "Рабочая температура Tраб, °C")
            Text("Группа исполнения двигателя")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Д1" to 120, "Д2" to 150, "Д3" to 170).forEach { (name, temp) ->
                    FilterChip(selected = group == name, onClick = { group = name },
                        label = { Text("$name — $temp °C") })
                }
            }
            Text("Максимально допустимая температура: ${max.toInt()} °C")
            ResultCardSimple("Уставка максимальной температуры", result?.let { "${it.pretty()} °C" }, null)
        }
    }}
}

@Composable
fun WellPotentialScreen(historyViewModel: CalculationHistoryViewModel, onBack: () -> Unit) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    var q by remember { mutableStateOf("") }
    var on by remember { mutableStateOf("") }
    var off by remember { mutableStateOf("") }
    var delay by remember { mutableStateOf("") }
    val qv=q.decimal(); val ton=on.decimal(); val toff=off.decimal(); val td=if(delay.isBlank()) 0.0 else delay.decimal()
    val result = if (qv!=null && ton!=null && toff!=null && td!=null && qv>=0 && ton>0 && toff>=0 && td>=0 && td<ton)
        qv * (ton-td)/(ton+toff) else null
    val currentHistoryEntry = result?.takeIf { it.isFinite() }?.let {
        HistoryEntry(title = "Потенциал скважины", method = "Расчёт",
            result = "${it.pretty()} м³/сут",
            details = "Дебит=$q м³/сут; работа=$on мин; простой=$off мин; задержка=${td?.pretty()} мин")
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)
    fun saveHistory() { latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) } }
    PersistHistoryOnDispose(::saveHistory, debounceKey = result?.let { "$q|$on|$off|$delay|$it" })
    fun saveAndBack() { saveHistory(); onBack() }
    BackHandler(onBack = ::saveAndBack)
    Scaffold { pv -> Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(pv)) {
        CalculationTopBar("Потенциал скважины", ::saveAndBack)
        Column(Modifier.padding(14.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ожидаемая суточная добыча с учётом цикла и задержки появления подачи.")
            DecimalField(q,{q=it},"Непрерывный дебит, м³/сут")
            DecimalField(on,{on=it},"Время работы, мин")
            DecimalField(off,{off=it},"Время простоя, мин")
            DecimalField(delay,{delay=it},"Задержка подачи после запуска, мин")
            ResultCardSimple("Ожидаемая добыча", result?.let { "${it.pretty()} м³/сут" }, if (td!=null && ton!=null && td>=ton) "Задержка должна быть меньше времени работы" else null)
        }
    }}
}

@Composable private fun DecimalField(value:String, onValue:(String)->Unit, label:String, onDone:(()->Unit)?=null) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value, { onValue(it.filter { c -> c.isDigit() || c==',' || c=='.' }) },
        label={Text(label)}, modifier=Modifier.fillMaxWidth().keepVisibleAboveKeyboard(), singleLine=true,
        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal, imeAction=if(onDone==null) ImeAction.Next else ImeAction.Done),
        keyboardActions=KeyboardActions(
            onNext={ focus.moveFocus(FocusDirection.Next) },
            onDone={ onDone?.invoke() ?: focus.moveFocus(FocusDirection.Next) }
        )
    )
}

private fun String.decimal()=replace(',','.').toDoubleOrNull()
private fun Double.pretty()=String.format(Locale.US,"%.2f",this).trimEnd('0').trimEnd('.')


@Composable
private fun IrzRpdGuide() {
    Card(colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("ИРЗ-500 — РПД по давлению на приёме",fontWeight=FontWeight.Bold)
        Text("Используйте предварительно рассчитанные Pоткл и Pзап из калькулятора «Расчёт РПД».")
        Text("1. Откройте группу уставок/защит ТМС.")
        Text("2. Найдите «Давл.на входе ЭЦН МИН» и установите рассчитанное давление отключения Pоткл.")
        Text("3. «Давл.на входе ЭЦН время» — задайте требуемую задержку отключения по защите.")
        Text("4. «Давл.на входе тип АПВ» → НОРМАЛИЗ., если повторный запуск должен происходить после возврата давления в допустимый диапазон.")
        Text("5. «Давл.на входе зад.АПВ» — задержка АПВ; сверить с технологическим регламентом периодического режима.")
        Text("6. «Давл.на входе кол-во АПВ» — допустимое количество повторных включений по утверждённой настройке.")
        HorizontalDivider()
        Text("По руководству ИРЗ-500: минимальное давление на приёме 0…99,990 МПа; задержка отключения 0…60 с; тип АПВ НОРМАЛИЗ./КОМБИНИР./ВРЕМЯ; задержка АПВ 1…3000 мин; количество АПВ 0…65535.",style=MaterialTheme.typography.bodySmall)
        Text("Не подставляйте давление в атмосферах в поле, если контроллер отображает МПа: сначала проверьте выбранные единицы давления.",fontWeight=FontWeight.Bold)
    }}
}
