package com.example.sanich.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PolicyYellow = Color(0xFFFFC400)
private val PolicyBlack = Color(0xFF171717)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFF6F6F6))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PolicyYellow)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = PolicyBlack
                    )
                }
                Text(
                    text = "Политика конфиденциальности",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PolicyBlack
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PolicyHeading("Политика конфиденциальности приложения «Оператор ДНГ»")
                PolicyParagraph("Дата вступления в силу: 20 августа 2026 года.")
                PolicyParagraph("Настоящая Политика конфиденциальности описывает порядок обработки информации при использовании мобильного приложения «Оператор ДНГ».")

                PolicySection("1. Сбор персональных данных")
                PolicyParagraph("Приложение «Оператор ДНГ» не требует регистрации пользователя и не запрашивает имя, номер телефона, адрес электронной почты, паспортные данные или иные персональные данные. Разработчик приложения не осуществляет целенаправленный сбор персональных данных пользователей.")

                PolicySection("2. Вводимые пользователем данные")
                PolicyParagraph("Данные, которые пользователь вводит для выполнения инженерных расчётов, используются исключительно для выполнения соответствующих вычислений. Введённые значения не передаются разработчику и третьим лицам. Если приложение сохраняет историю расчётов, такая информация хранится локально на устройстве пользователя.")

                PolicySection("3. Передача информации третьим лицам")
                PolicyParagraph("Приложение не передаёт пользовательские данные сторонним организациям, рекламным сетям или иным третьим лицам.")

                PolicySection("4. Реклама и аналитика")
                PolicyParagraph("Приложение не использует рекламные сервисы, сторонние системы аналитики и не осуществляет отслеживание пользователя в рекламных целях.")

                PolicySection("5. Доступ к функциям устройства")
                PolicyParagraph("Приложение использует только те возможности устройства, которые необходимы для его работы. Приложение не получает доступ к контактам, сообщениям, фотографиям, микрофону, камере или геолокации пользователя.")

                PolicySection("6. Хранение и безопасность")
                PolicyParagraph("Основные расчёты выполняются непосредственно на устройстве пользователя. Информация не отправляется на удалённые серверы разработчика. Сохранённая история расчётов, если она используется, хранится локально на устройстве.")

                PolicySection("7. Изменения политики")
                PolicyParagraph("Настоящая Политика конфиденциальности может обновляться при изменении функциональности приложения или требований законодательства. Актуальная версия политики доступна внутри приложения в разделе «О приложении».")

                PolicySection("8. Контактная информация")
                PolicyParagraph("По вопросам, связанным с работой приложения и настоящей Политикой конфиденциальности, пользователь может обратиться к разработчику по контактным данным, указанным на странице приложения в магазине приложений.")
            }
        }

        VerticalScrollIndicator(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 5.dp, top = 72.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun PolicyHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = PolicyBlack
    )
}

@Composable
private fun PolicySection(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = PolicyBlack
    )
}

@Composable
private fun PolicyParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF333333)
    )
}
