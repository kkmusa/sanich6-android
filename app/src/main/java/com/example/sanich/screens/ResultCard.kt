package com.example.sanich.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sanich.data.PedEntity

@Composable
fun ResultCard(
    ped: PedEntity,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = ped.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Производитель:"
            )

            Text(
                text = ped.manufacturer
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Приток:"
            )

            Text(
                text = "%.2f".format(ped.flow)
            )

        }

    }

}