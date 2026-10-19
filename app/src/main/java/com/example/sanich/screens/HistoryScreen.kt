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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: CalculationHistoryViewModel,
    bottomBar: @Composable () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(bottomBar = bottomBar) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color(0xFFFFC400))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("История", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (entries.isNotEmpty()) {
                    IconButton(onClick = { confirmClear = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Очистить историю")
                    }
                }
            }

            if (entries.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("История пока пуста", fontWeight = FontWeight.Bold)
                    Text("Завершённые расчёты появятся здесь и сохранятся после закрытия приложения", color = Color.Gray)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        entries.forEach { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(entry.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text(entry.time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        IconButton(onClick = { viewModel.delete(entry) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Удалить запись", tint = Color.Gray)
                                        }
                                    }
                                    Text(entry.method, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text(entry.result, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFF28C00))
                                    Text(entry.details, style = MaterialTheme.typography.bodySmall)
                                }
                            }
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
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Очистить историю?") },
            text = { Text("Все сохранённые расчёты будут удалены без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clear(); confirmClear = false }) { Text("Очистить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Отмена") }
            }
        )
    }
}
