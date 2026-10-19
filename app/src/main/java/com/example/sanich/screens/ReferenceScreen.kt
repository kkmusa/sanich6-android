package com.example.sanich.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanich.R

private data class ReferenceInfo(val title: String, val text: String)

@Composable
fun ReferenceScreen(
    onVnrClick: () -> Unit,
    onPedReferenceClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onSkoReferenceClick: () -> Unit,
    onCasingGostClick: () -> Unit,
    onTubingGostClick: () -> Unit,
    onAboutClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    var info by remember { mutableStateOf<ReferenceInfo?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(bottomBar = bottomBar) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Справочник",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFC400))
                        .padding(16.dp)
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                ReferenceCard(
                    title = "Регламент ВНР",
                    subtitle = "Полная технологическая инструкция, оглавление и поиск",
                    icon = Icons.Default.Info,
                    onClick = onVnrClick,
                    showChevron = true
                )
                ReferenceCard(
                    title = "Аварийные остановки и нештатные режимы",
                    subtitle = "Алгоритмы действий по производственным ситуациям",
                    icon = Icons.Default.ErrorOutline,
                    onClick = onEmergencyClick,
                    showChevron = true
                )
                ReferenceCard(
                    title = "Необходимый приток для охлаждения ПЭД",
                    subtitle = "Расчёт притока при номинальной и фактической загрузке",
                    imageRes = R.drawable.ic_ped_cooling,
                    onClick = onPedReferenceClick,
                    showChevron = true
                )
                ReferenceCard(
                    title = "Соляно-кислотная обработка УЭЦН",
                    subtitle = "Формулы, объёмы, ограничения и критерии СКО",
                    icon = Icons.Default.Science,
                    onClick = onSkoReferenceClick,
                    showChevron = true
                )
                ReferenceCard(
                    title = "Уставки защиты от срыва подачи",
                    subtitle = "Рекомендуемые значения ЗСП по загрузке ПЭД",
                    icon = Icons.Default.ElectricBolt,
                    onClick = {
                        info = ReferenceInfo(
                            "Уставки ЗСП",
                            "Загрузка ПЭД 70% и более — 85–88%.\n\n" +
                                "Загрузка ПЭД от 50 до 70% — 90–92%.\n\n" +
                                "Загрузка ПЭД менее 50% — 92–95%."
                        )
                    }
                )
                ReferenceCard(
                    title = "Температурные группы ПЭД",
                    subtitle = "Допустимая температура пластовой жидкости",
                    icon = Icons.Default.Thermostat,
                    onClick = {
                        info = ReferenceInfo(
                            "Температурные группы ПЭД",
                            "Д1 (первая группа) — до 120 °C.\n\nД2 (вторая группа) — до 150 °C.\n\nД3 (третья группа) — до 170 °C."
                        )
                    }
                )
                ReferenceCard(
                    title = "Работа ПЭД при недостаточном притоке",
                    subtitle = "Допустимое время работы и охлаждения",
                    icon = Icons.Default.Schedule,
                    onClick = {
                        info = ReferenceInfo(
                            "Работа ПЭД при недостаточном притоке",
                            "До 32 кВт включительно:\nне более 2 часов работы + 1,5 часа охлаждения.\n\n" +
                                "Более 32 до 45 кВт включительно:\nне более 1 часа работы + 1,5 часа охлаждения.\n\n" +
                                "Свыше 45 кВт:\nне более 30 минут работы + 1,5 часа охлаждения."
                        )
                    }
                )
                ReferenceCard(
                    title = "Данные при передаче запуска ВНР",
                    subtitle = "Справочная информация для передачи запуска на пульт",
                    icon = Icons.Default.Info,
                    onClick = {
                        info = ReferenceInfo(
                            "Данные при передаче запуска ВНР",
                            "1)Фактическое время запуска\n" +
                                "2)куст/скважина\n" +
                                "3)установка\n" +
                                "4)Глубина спуска (формуляр или паспорт вторая страница)\n" +
                                "5)Частота на прямом и обратном\n" +
                                "6)токи на прямом и  обратном\n" +
                                "7)загрузка на прямом и обратном\n" +
                                "8)зсп\n" +
                                "9)статический уровень (считай сам по датчикам)\n" +
                                "10)давление затрубное буферное линейное\n" +
                                "11)кто выводит  станция управления (паспорт или на су )\n" +
                                "12)ОК  (паспорт)\n" +
                                "13)ПЭД — габарит, мощность и номинальное напряжение\n" +
                                "14)длина кабеля (на первой странице паспорта внизу )\n" +
                                "15)диаметр сечения кабеля (на первой странице паспорта внизу)\n" +
                                "16)косинус отпайка напряжение\n" +
                                "17)косинус на обратном вращении\n" +
                                "18)активная мощность и выходное напряжение\n" +
                                "19)номинальные и холостые токи\n" +
                                "20)ТМС\n" +
                                "21)изоляция и звезда ( электромонтеры пишут на формуляре обычно)"
                        )
                    }
                )
                ReferenceCard(
                    title = "Обсадные трубы — ГОСТ 632-80",
                    subtitle = "Диаметры ЭК, толщины стенок и внутренние диаметры",
                    icon = Icons.Default.Info,
                    onClick = onCasingGostClick
                )
                ReferenceCard(
                    title = "НКТ — ГОСТ 633-80",
                    subtitle = "НКТ 60, 73, 89 мм: стенки и внутренние диаметры",
                    icon = Icons.Default.Info,
                    onClick = onTubingGostClick
                )
                ReferenceCard(
                    title = "О приложении",
                    subtitle = "Версия, сведения и политика конфиденциальности",
                    icon = Icons.Default.Info,
                    onClick = onAboutClick
                )
                }
            }

            VerticalScrollIndicator(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 7.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }

    info?.let { selected ->
        AlertDialog(
            onDismissRequest = { info = null },
            title = { Text(selected.title, fontWeight = FontWeight.Bold) },
            text = { Text(selected.text, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = { info = null }) { Text("Закрыть") }
            }
        )
    }
}

@Composable
private fun ReferenceCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    imageRes: Int? = null,
    onClick: () -> Unit,
    showChevron: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (imageRes != null) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.width(28.dp).height(28.dp),
                    contentScale = ContentScale.Fit
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color(0xFFF28C00))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (showChevron) Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
