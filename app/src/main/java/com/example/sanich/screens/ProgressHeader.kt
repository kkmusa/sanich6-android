package com.example.sanich.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressHeader(
    currentStep: Int,
    totalSteps: Int
) {

    val progress = currentStep.toFloat() / totalSteps.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Необходимый приток",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Шаг $currentStep из $totalSteps",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}