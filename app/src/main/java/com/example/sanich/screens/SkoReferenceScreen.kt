package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SkoReferenceScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold { paddingValues ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(paddingValues)) {
            CalculationTopBar("Соляно-кислотная обработка УЭЦН", onBack)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkoInfoCard("Кислотная композиция", "• Через НКТ — 1,5 м³ кислотной композиции.\n• Через затрубное пространство — 3 м³.\n• Применяются растворы 5%, 10% и 12%.\n• Для профилактики — 5%.\n• До 12% при расклинивании — только по решению ОРМФ.\n• Продавка — водным раствором ПАВ 0,1–1,0%.")
                SkoInfoCard("Справочные объёмы НКТ", "НКТ 60 — 2 л/м\nНКТ 73 — 3 л/м\nНКТ 89 — 4,5 л/м")
                SkoInfoCard("Варианты расчёта", "1. Прямая, работающий УЭЦН:\nVобр = Vнкт × Lэцн / 1000\n\n2. Прямая, нет подачи / клин:\nVобр = Vнкт × Lэцн / 1000 + 8 м³\n\n3. Обратная, работающий УЭЦН:\nVобр = 0,79 × Lэцн × (Dс² − Dнкт²) + 0,5\n\n4. Обратная, насос выключен:\nVобр = 0,79 × Dс² × Lэцн\n\nДля вариантов 3 и 4 диаметры используются в метрах.")
                SkoInfoCard("Ограничения по времени", "• Профилактическая промывка: кислота в зоне насоса — не более 20 минут.\n• Расклинивание без циркуляции — не более 60 минут.\n• При обратной обработке учитывается скорость оседания раствора 100–150 м/ч.")
                SkoInfoCard("Ограничения по проведению", "• Не более 3 СКО за скользящий месяц.\n• Не более 5 СКО за одну эксплуатацию ЭПО.\n• Повторная обработка — после оценки причины и эффективности предыдущих работ.")
                SkoInfoCard("Критерии применения", "• Снижение или отсутствие подачи.\n• Снижение подачи более чем на 5–20% от установившегося режима.\n• Остановка по причине «КЛИН».\n• Нет эффекта после промывки технологической жидкостью.\n• Подтверждённые солеотложения.\n• Частые остановки по ЗП при исправном наземном оборудовании.")
                SkoInfoCard("Когда СКО исключается", "• Нет подачи и отсутствует циркуляция при промывке.\n• Высокий динамический уровень.\n• Высокое давление или противодавление закачки.\n• Повторная обработка неэффективна при аномально низком пластовом давлении.")
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))) {
                    Text(
                        "Технология, концентрация кислоты, объём обработки и необходимость отключения УЭЦН определяются технологической службой и указываются в Плане-заказе.",
                        Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SkoInfoCard(title: String, text: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
