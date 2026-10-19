package com.example.sanich.screens

import com.example.sanich.ui.theme.CalculationResultYellow

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController

import com.example.sanich.calculator.FlowCalculator
import com.example.sanich.components.SelectionDialog
import com.example.sanich.data.Tables

@Composable
fun CalculatorScreen(
    navController: NavController,
    type: String
){

    var deltaH by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var hst by remember { mutableStateOf("") }

    val casings = listOf("139.7", "146", "168", "178")
    var selectedCasing by remember { mutableStateOf("168") }

    val tubings = listOf("60", "73", "89")
    var selectedTubing by remember { mutableStateOf("73") }

    var result by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showCasingSheet by remember { mutableStateOf(false) }
    var showTubingSheet by remember { mutableStateOf(false) }
    var showCasingDialog by remember { mutableStateOf(false) }
    var showTubingDialog by remember { mutableStateOf(false) }

    val isDrain = type == "drain"
    val isHermetic = type == "hermetic"
    val isAfterDrain = type == "after"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding().verticalScroll(rememberScrollState())
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Расчет притока",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            singleLine = true,
            value = deltaH,
            onValueChange = { deltaH = it },
            label = { Text("ΔH, м") },
            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = nextFieldKeyboardActions(),
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Время, мин") },
            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            singleLine = true,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = if (isDrain) ImeAction.Next else ImeAction.Done
            ),

            keyboardActions = if (isDrain) nextFieldKeyboardActions() else doneFieldKeyboardActions(),
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        if (isDrain) {

            OutlinedTextField(
                value = hst,
                onValueChange = { hst = it },
                label = { Text("Hст, м") },
                modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),

                keyboardActions = doneFieldKeyboardActions(),

                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black
                ),

                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color.Black
                )
            )
        }
        Text(
            text = "Эксплуатационная колонна",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            onClick = {
                showCasingDialog = true
            }
        ) {

            Text("Колонна: $selectedCasing мм")

        }

        if (showCasingDialog) {

            SelectionDialog(
                title = "Выберите колонну",
                items = casings.map { "$it мм" },

                onDismiss = {
                    showCasingDialog = false
                },

                onSelected = {

                    selectedCasing = it.replace(" мм","")
                    showCasingDialog = false

                }

            )

        }

        Text(
            text = "НКТ",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            onClick = {
                showTubingDialog = true
            }
        ) {

            Text("НКТ: $selectedTubing мм")

        }

        if (showTubingDialog) {

            SelectionDialog(
                title = "Выберите НКТ",
                items = tubings.map { "$it мм" },

                onDismiss = {
                    showTubingDialog = false
                },

                onSelected = {

                    selectedTubing = it.replace(" мм","")
                    showTubingDialog = false

                }

            )

        }



        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            onClick = {
                keyboardController?.hide()
                val dh = deltaH.replace(',', '.').toDoubleOrNull()
                val t = time.replace(',', '.').toDoubleOrNull()
                val hs = hst.replace(',', '.').toDoubleOrNull() ?: 0.0

                if (dh == null || t == null || t <= 0) {
                    result = "Проверьте введенные данные"
                    return@Button
                }

                val vkp = Tables.getVkp(
                    selectedCasing,
                    selectedTubing
                )

                val vnkt = Tables.getVnkt(
                    selectedTubing
                )

                val flow = when (type) {

                    "hermetic" -> {

                        FlowCalculator.hermetic(
                            deltaH = dh,
                            vkp = vkp,
                            time = t
                        )

                    }

                    "drain" -> {

                        FlowCalculator.drain(
                            deltaH = dh,
                            vkp = vkp,
                            vnkt = vnkt,
                            hst = hs,
                            time = t
                        )

                    }

                    else -> {

                        FlowCalculator.afterDrain(
                            deltaH = dh,
                            vkp = vkp,
                            vnkt = vnkt,
                            time = t
                        )

                    }

                }

                result = "$flow м³/сут"

            }
        ) {

            Text(
                text = "Рассчитать",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

        }

        Row(
            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                modifier = Modifier.weight(1f).keepVisibleAboveKeyboard(),
                onClick = {

                    deltaH = ""
                    time = ""
                    hst = ""
                    result = ""

                }
            ) {

                Text(
                    text = "Очистить",
                    color = Color.Black
                )

            }

            OutlinedButton(
                modifier = Modifier.weight(1f).keepVisibleAboveKeyboard(),
                onClick = {

                    navController.popBackStack()

                }
            ) {

                Text(
                    text = "Назад",
                    color = Color.Black
                )

            }

        }

        if (result.isNotEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CalculationResultYellow)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Приток",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = result,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                }

            }

        }

    }

}