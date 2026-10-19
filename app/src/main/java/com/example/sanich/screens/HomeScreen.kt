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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanich.R

private val BrandYellow = Color(0xFFFFC400)
private val BrandOrange = Color(0xFFF28C00)
private val BrandBlack = Color(0xFF171717)


private val HIcon: ImageVector by lazy {
    Builder(name = "LevelRuler", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(BrandOrange)) {
            moveTo(7f,2f); lineTo(10f,2f); lineTo(10f,22f); lineTo(7f,22f); close()
            moveTo(10f,4f); lineTo(17f,4f); lineTo(17f,6f); lineTo(10f,6f); close()
            moveTo(10f,9f); lineTo(15f,9f); lineTo(15f,11f); lineTo(10f,11f); close()
            moveTo(10f,14f); lineTo(17f,14f); lineTo(17f,16f); lineTo(10f,16f); close()
            moveTo(10f,19f); lineTo(15f,19f); lineTo(15f,21f); lineTo(10f,21f); close()
        }
    }.build()
}
private val PIcon: ImageVector by lazy {
    Builder(name = "PotentialP", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(BrandOrange)) {
            moveTo(5f, 3f); lineTo(13f, 3f); curveTo(22f, 3f, 22f, 14f, 13f, 14f)
            lineTo(8f, 14f); lineTo(8f, 21f); lineTo(5f, 21f); close()
        }
    }.build()
}

private val TwoAIcon: ImageVector by lazy {
    Builder(name = "2A", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(BrandOrange)) {
            moveTo(2f,7f); curveTo(2f,1f,13f,1f,13f,7f); curveTo(13f,10f,9f,12f,6f,16f)
            lineTo(13f,16f); lineTo(13f,19f); lineTo(2f,19f); lineTo(2f,16f)
            curveTo(5f,12f,10f,9f,10f,7f); curveTo(10f,4f,5f,4f,5f,7f); close()
            moveTo(17f,5f); lineTo(20f,5f); lineTo(24f,19f); lineTo(21f,19f)
            lineTo(20.3f,16f); lineTo(16.7f,16f); lineTo(16f,19f); lineTo(13f,19f); close()
        }
    }.build()
}

@Composable
fun HomeScreen(
    onLevelClick: () -> Unit,
    onSpiritLevelClick: () -> Unit,
    onFrequencyClick: () -> Unit,
    onFlowClick: () -> Unit,
    onManualDebitClick: () -> Unit,
    onTapCalculationClick: () -> Unit,
    onPrToUprClick: () -> Unit,
    onUprToApvClick: () -> Unit,
    onSkoClick: () -> Unit,
    onStationModesClick: () -> Unit,
    onRampClick: () -> Unit,
    onRichClick: () -> Unit,
    onMotorCoefficientClick: () -> Unit,
    onTemperatureSetpointClick: () -> Unit,
    onWellPotentialClick: () -> Unit,
    onRpdClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandYellow)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.brand_logo_full),
                        contentDescription = "Логотип Оператор ДНГ",
                        modifier = Modifier
                            .fillMaxWidth(0.44f)
                            .padding(top = 2.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Расчёт параметров скважины",
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )
                    Text(
                        text = "Выберите необходимый расчёт",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandBlack.copy(alpha = 0.75f)
                    )
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuCard(
                        title = "Ручной замер дебита",
                        subtitle = "Перевод литров и времени замера в м³/сут",
                        iconRes = R.drawable.ic_manual_debit,
                        onClick = onManualDebitClick
                    )
                    MenuCard(
                        title = "Динамический уровень",
                        subtitle = "Определение уровня по замерам",
                        icon = HIcon,
                        onClick = onLevelClick
                    )
                    MenuCard(
                        title = "Необходимая частота запуска",
                        subtitle = "Расчёт частоты УЭЦН",
                        icon = Icons.Default.Speed,
                        onClick = onFrequencyClick
                    )
                    MenuCard(
                        title = "Потенциал скважины",
                        subtitle = "Ожидаемая суточная добыча с учётом режима и задержки подачи",
                        iconText = "Q",
                        onClick = onWellPotentialClick
                    )
                    MenuCard(
                        title = "Разгон",
                        subtitle = "Сначала ввод параметров, затем выбор станции",
                        icon = Icons.Default.TrendingUp,
                        onClick = onRampClick
                    )
                    MenuCard(
                        title = "РИЧ",
                        subtitle = "Режим изменения частоты",
                        iconText = "FF",
                        onClick = onRichClick
                    )
                    MenuCard(
                        title = "Коэффициент двигателя 2А габарита",
                        subtitle = "Расчёт КФ и перевод обычной частоты в частоту станции",
                        iconText = "2А",
                        onClick = onMotorCoefficientClick
                    )
                    MenuCard(
                        title = "Расчёт отпайки ТМПН",
                        subtitle = "Для СУ без ЧРП и для СУ с ЧРП",
                        icon = Icons.Default.Bolt,
                        onClick = onTapCalculationClick
                    )
                    MenuCard(
                        title = "Электронный уровень",
                        subtitle = "Горизонталь и вертикаль по датчикам телефона",
                        icon = Icons.Default.Architecture,
                        onClick = onSpiritLevelClick
                    )
                }
            }

            VerticalScrollIndicator(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 3.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    iconText: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (iconText != null) {
                if (iconText == "FF") {
                    Row(modifier = Modifier.width(34.dp), verticalAlignment = Alignment.Bottom) {
                        Text("F", color = BrandOrange, fontWeight = FontWeight.Medium, fontSize = 23.sp)
                        Text("F", color = BrandOrange, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                } else Text(iconText, color = BrandOrange, fontWeight = if (iconText == "2А") FontWeight.Medium else FontWeight.Bold, fontSize = if (iconText == "2А") 18.sp else 20.sp, maxLines = 1, modifier = Modifier.width(36.dp))
            } else if (iconRes != null) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .width(34.dp)
                        .height(34.dp),
                    contentScale = ContentScale.Fit
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = BrandOrange)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = BrandBlack)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = BrandOrange)
        }
    }
}


@Composable
fun TechnologistScreen(
    onFlowClick: () -> Unit,
    onPrToUprClick: () -> Unit,
    onUprToApvClick: () -> Unit,
    onSkoClick: () -> Unit,
    onTemperatureSetpointClick: () -> Unit,
    onRpdClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    Scaffold(bottomBar = bottomBar) { pv ->
        Box(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(pv)) {
            Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
                Column(Modifier.fillMaxWidth().background(BrandYellow)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.brand_logo_full),
                        contentDescription = "Логотип Саныч-6",
                        modifier = Modifier
                            .fillMaxWidth(0.34f)
                            .padding(top = 2.dp, bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text("Технолог", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = BrandBlack)
                    Text("Инженерные расчёты", color = BrandBlack.copy(alpha = 0.75f))
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuCard(
                        title = "Расчёт РПД",
                        subtitle = "Откачка/накопление и уставки давления на приёме",
                        icon = Icons.Default.Autorenew,
                        onClick = onRpdClick
                    )
                    MenuCard(
                        title = "Определение притока",
                        subtitle = "Три режима ОК, Vкп по таблице или геометрии",
                        iconRes = R.drawable.ic_well_inflow,
                        onClick = onFlowClick
                    )
                    MenuCard(
                        title = "Перевод ПР в УПР",
                        subtitle = "Расчёт времени работы и остановки",
                        icon = Icons.Default.Autorenew,
                        onClick = onPrToUprClick
                    )
                    MenuCard(
                        title = "Перевод УПР в АПВ",
                        subtitle = "Проверка цикла с учётом времени слива",
                        icon = Icons.Default.Autorenew,
                        onClick = onUprToApvClick
                    )
                    MenuCard(
                        title = "Объём СКО",
                        subtitle = "Четыре варианта расчёта кислотной обработки",
                        icon = Icons.Default.Science,
                        onClick = onSkoClick
                    )
                    MenuCard(
                        title = "Настройка температуры ТМС",
                        subtitle = "Расчёт уставки по рабочей и максимальной температуре",
                        icon = Icons.Default.Thermostat,
                        onClick = onTemperatureSetpointClick
                    )
                }
            }
            VerticalScrollIndicator(scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
                    .padding(end = 3.dp, top = 8.dp, bottom = 8.dp))
        }
    }
}
