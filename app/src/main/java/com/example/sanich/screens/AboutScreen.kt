package com.example.sanich.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanich.R

private val AboutYellow = Color(0xFFFFC400)
private val AboutBlack = Color(0xFF171717)

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFF6F6F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AboutYellow)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = AboutBlack
                )
            }
            Text(
                text = "О приложении",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AboutBlack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.brand_logo_full),
                contentDescription = "Логотип Оператор ДНГ",
                modifier = Modifier.fillMaxWidth(0.56f),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Инженерные расчёты",
                modifier = Modifier.padding(top = 18.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Расчёты выполняются локально на устройстве.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "Версия 2.8.1",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                onClick = onPrivacyPolicyClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AboutYellow,
                    contentColor = AboutBlack
                )
            ) {
                Icon(Icons.Default.PrivacyTip, contentDescription = null)
                Text(
                    text = "Политика конфиденциальности",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
