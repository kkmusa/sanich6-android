package com.example.sanich.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SelectionDialog(
    title: String,
    items: List<String>,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column {

                items.forEach { item ->

                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(item)
                            }
                            .padding(vertical = 16.dp)
                    )

                    HorizontalDivider()

                }

            }

        },

        confirmButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Закрыть")
            }

        }

    )

}