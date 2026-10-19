package com.example.sanich.screens


import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PowerStep(
    powers: List<Int>,
    selected: Int?,
    enabled: Boolean,
    onSelect: (Int) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Мощность ПЭД",
                    style = MaterialTheme.typography.titleSmall
                )

                if (selected != null) {

                    if (selected != null) {

                        Text("✅")

                    }

                }

            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onClick = {
                    expanded = true
                }
            ) {

                Text(
                    text = selected?.let { "$it кВт" } ?: "Выберите"
                )

            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                powers.forEach { value ->

                    DropdownMenuItem(
                        text = {
                            Text("$value кВт")
                        },
                        onClick = {

                            expanded = false

                            onSelect(value)

                        }
                    )

                }

            }

        }

    }

}