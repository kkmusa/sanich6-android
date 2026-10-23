package com.example.sanich.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class HelpData(val intro:String,val formulas:List<String>,val terms:List<String>,val notes:List<String> = emptyList())
private fun helpFor(title:String):HelpData? = when {
 title.contains("Разгон",true)||title.contains("РИЧ",true)||title.contains("Помощник",true)||title.contains("Отбор проб",true)||title.contains("Электронный уровень",true)->null
 title.contains("температур",true)->HelpData("Уставка максимальной температуры ТМС после выхода УЭЦН на установившийся режим. Берётся более жёсткое из двух ограничений.",listOf("T₁ = Tраб × 1,05","T₂ = Tмакс × 0,85","Tуставки = min(T₁; T₂)"),listOf("Tраб — фактическая установившаяся температура, °C.","Tмакс — максимально допустимая температура, °C."),listOf("Не следует механически принимать предельно допустимую температуру пластовой жидкости за безопасную температуру ПЭД."))
 title.contains("Потенциал",true)->HelpData("Оценивает среднесуточный потенциал скважины при циклической работе с учётом задержки появления подачи.",listOf("Qпот = (Q × (Tраб − Tподачи)) / (Tраб + Tост)"),listOf("Q — дебит работающей УЭЦН, м³/сут.","Tраб — время работы; Tподачи — задержка подачи; Tост — остановка."),listOf("Все времена вводятся в одинаковых единицах; Tподачи < Tраб."))
 title.contains("уров",true)->HelpData("Определяет уровень жидкости по глубине спуска, давлениям и плотности.",listOf("Hур = Hсп − 10 × (Pд − Pз) / ρ"),listOf("Hсп — глубина спуска насоса, м.","Pд — давление на приёме/датчике, ат.","Pз — давление в затрубе, ат.","ρ — плотность жидкости, г/см³."),listOf("Если задано «Удлинение», приложение сначала вычитает его из глубины спуска."))
 title.equals("Напор на частоте",true)->HelpData("Пересчёт паспортного напора при 50 Гц на заданную рабочую частоту.",listOf("H = H₅₀ × (f / 50)²"),listOf("H — напор на рабочей частоте, м.","H₅₀ — паспортный напор при 50 Гц, м.","f — рабочая частота, Гц."),listOf("Пример: 2100 × (54 / 50)² = 2449,44 м.","Квадратичная зависимость для той же установки."))
 title.contains("частот",true)->HelpData("Оценивает необходимую частоту запуска УЭЦН при заданном уровне жидкости и напоре.",listOf("f = 50 × √((40 × 9,81 + Hур) / Hэцн)"),listOf("f — частота запуска, Гц.","Hур — уровень жидкости, м.","Hэцн — напор УЭЦН, м."),listOf("Расчётную частоту сверяют с технологическим заданием и допустимым диапазоном конкретной установки."))
 title.equals("Необходимый приток",true)->HelpData("Выберите диаметр насоса, мощность двигателя, параметры обсадной колонны и толщину стенки. Приложение покажет подходящие насосы. Выберите насос, чтобы получить необходимый приток для охлаждения ПЭД при номинальной загрузке. При необходимости укажите фактический рабочий ток и рассчитайте приток при загрузке ниже номинальной.",emptyList(),emptyList())
 title.contains("приток",true)->HelpData("Определяет приток по изменению уровня за измеренный интервал. В приложении предусмотрены три состояния эксплуатационной колонны.",listOf("Герметичный ОК: Q = (ΔH × Vкп × 1440) / t","Негерметичный ОК, слив: Q = ((ΔH × Vкп − Vнкт × Hст) × 1440) / t","После слива: Q = (ΔH × (Vкп + Vнкт) × 1440) / t"),listOf("ΔH — изменение уровня, м; Vкп — объём кольцевого пространства, м³/м.","Vнкт — удельный объём НКТ, м³/м; Hст — высота столба, м; t — время, мин.","1440 — минут в сутках."),listOf("Vкп берётся из справочника либо рассчитывается по геометрии."))
 title.contains("дебит",true)||title.contains("Ручной замер",true)->HelpData("",listOf("Q = Vл × 1440 / (tмин × 1000)"),listOf("Q — дебит, м³/сут.","Vл — объём жидкости, л.","tмин — время замера, мин."))
 title.contains("отпай",true)->HelpData("Подбирает напряжение отпайки ТМПН с учётом ПЭД, кабеля, температуры и типа СУ. Для СУ без ЧРП и с ЧРП формулы различаются.",listOf("Kₜ = 1 + 0,0043 × (Tпл − 20)","ΔUкаб = √3 × Iпэд × cosφ × (L/S) × ρкаб × Kₜ","Без ЧРП: Uбаза = (Uвх.ТМПН / Uсети) × Uпэд","С ЧРП: Uбаза = (Uвх.ТМПН / Umax.ЧРП) × (fmax / fном) × Uпэд × Kф","Uотп = Uбаза + ΔUкаб"),listOf("Iпэд — ток ПЭД; L/S — длина и сечение кабеля; ρкаб — сопротивление материала.","Kф = 1,08 при выходном фильтре, иначе 1."),listOf("Выбирается ближайшая доступная отпайка в большую сторону."))
 title.contains("РПД",true)->HelpData("Рассчитывает цикл периодической откачки/накопления и ориентировочные давления отключения и повторного запуска по методике технологической инструкции.",listOf("V = ((Qэцн − Qпр) × Tр) / 1440","Tн = V × 1440 / Qпр","ΔL = V / (¼π × (D² − d²))","ρж = ρн × (1 − B) + ρв × B","ΔPпр = ΔL × ρж × 9,81 / 101300","Pоткл = Pпр − ΔPпр","Pзап = Pпр"),listOf("Qпр — приток; Qэцн — подача насоса; Tр — время работы.","D — внутренний диаметр ЭК; d — наружный диаметр НКТ.","B — обводнённость в долях; Pпр — проектное давление на приёме."),listOf("Конечные уставки проверяются по действующей редакции регламента и единицам давления конкретной СУ."))
 title.contains("ПР",true)||title.contains("УПР",true)||title.contains("АПВ",true)->HelpData("Переводит режим эксплуатации в циклический, сохраняя требуемый потенциал и учитывая непроизводительное время после запуска.",listOf("Tпол = Tцикл × Qпот / Qэцн","Tраб = Tпол + Tподачи","Tост = Tцикл − Tраб","АПВ: Tцикл,min = (Tподачи + Tслива) / (1 − Qпот/Qэцн)"),listOf("Qпот — потенциал; Qэцн — дебит работающей УЭЦН.","Tподачи — время до подачи; Tслива — время слива/отстоя."),listOf("Минимальный цикл АПВ в приложении округляется вверх до целого часа."))
 else -> null
}
@Composable
fun CalculationTopBar(title: String, onBack: () -> Unit) {
    var showHelp by remember { mutableStateOf(false) }
    val help = helpFor(title)
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().background(Color(0xFFFFC400)).padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад", tint = Color(0xFF171717)) }
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF171717))
        if (help != null) {
            IconButton(onClick = { showHelp = true }) { Text("?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        }
    }
    if (showHelp && help != null) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Понятно") } },
            title = { Text("Как выполняется расчёт") },
            text = { Column(Modifier.fillMaxWidth().heightIn(max=560.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                if (!title.contains("Ручной замер", true)) { Text(help.intro); if (help.formulas.isNotEmpty()) Text("Формулы",fontWeight=FontWeight.Bold) }
                help.formulas.forEach { f -> Card(shape=RoundedCornerShape(12.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF3C4))) { FormulaDisplay(f) } }
                if (help.terms.isNotEmpty()) Text("Обозначения",fontWeight=FontWeight.Bold); help.terms.forEach { Text("• $it") }; if(!title.contains("Ручной замер",true) && help.notes.isNotEmpty()){ Text("Важно",fontWeight=FontWeight.Bold); help.notes.forEach { Text("• $it") } }
            } }
        )
    }
}

/** Render divisions as real stacked fractions throughout all calculation help dialogs.
 * This is presentation-only; calculations remain unchanged.
 */
@Composable
fun FormulaDisplay(formula: String) {
    val eq = formula.indexOf(" = ")
    val left = if (eq >= 0) formula.substring(0, eq + 3) else ""
    val expression = if (eq >= 0) formula.substring(eq + 3) else formula
    if (formula == "H = H₅₀ × (f / 50)²") {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("H = H₅₀ × (", fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("f", fontWeight = FontWeight.Bold)
                HorizontalDivider(Modifier.width(28.dp), thickness = 1.5.dp, color = MaterialTheme.colorScheme.onSurface)
                Text("50", fontWeight = FontWeight.Bold)
            }
            Text(")²", fontWeight = FontWeight.Bold)
        }
    } else if (formula.startsWith("f = 50 × √((40 × 9,81 + Hур) / Hэцн)")) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("f = 50 × √(", fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f, fill = false), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("40 × 9,81 + Hур", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                HorizontalDivider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.onSurface)
                Text("Hэцн", fontWeight = FontWeight.Bold)
            }
            Text(")", fontWeight = FontWeight.Bold)
        }
    } else {
        CardFormulaRow(left, expression)
    }
}

@Composable
private fun CardFormulaRow(left: String, expression: String) {
    // Explicitly separate multiplicative factors from a fraction, avoiding
    // misleading rendering such as (A / B) × C as A / (B × C).
    val slash = expression.indexOf('/')
    val before = if (slash >= 0) expression.substring(0, slash).trim() else expression
    val after = if (slash >= 0) expression.substring(slash + 1).trim() else ""
    val numerator: String
    val denominator: String
    val suffix: String
    val prefix: String
    if (slash >= 0) {
        val prefixMatch = Regex("^(.*?)(\\([^()]*\\)|[A-Za-zА-Яа-яΔρ][A-Za-zА-Яа-я0-9Δρ₀-₉]*)$").find(before)
        prefix = if (prefixMatch != null && prefixMatch.groupValues[1].trim().endsWith("×")) prefixMatch.groupValues[1].trim() + " " else ""
        numerator = (if (prefix.isNotEmpty()) prefixMatch!!.groupValues[2] else before).removeSurrounding("(", ")")
        val factorMatch = Regex("^(.*?)(?:\\s+×\\s+)([^×]+)$").find(after)
        suffix = if (factorMatch != null && !after.startsWith("(")) " × " + factorMatch.groupValues[2].trim() else ""
        denominator = (if (suffix.isNotEmpty()) factorMatch!!.groupValues[1].trim() else after).removeSurrounding("(", ")").removeSurrounding("[", "]")
    } else { prefix=""; numerator=""; denominator=""; suffix="" }
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (left.isNotEmpty()) Text(left, fontWeight = FontWeight.Bold)
        if (slash < 0) {
            Text(expression, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        } else {
            if (prefix.isNotEmpty()) Text(prefix, fontWeight = FontWeight.Bold)
            Column(modifier = Modifier.weight(1f, fill = false), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(numerator, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                HorizontalDivider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.onSurface)
                Text(denominator, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            if (suffix.isNotEmpty()) Text(suffix, fontWeight = FontWeight.Bold)
        }
    }
}
