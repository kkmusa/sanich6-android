package com.example.sanich.screens

import com.example.sanich.ui.theme.CalculationResultYellow

import com.example.sanich.data.Tables
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.sanich.calculator.FlowCalculator
import com.example.sanich.components.SelectionDialog
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun SensorCalculatorScreen(
    navController: NavController,
    type: String
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var selectedCasing by remember { mutableStateOf("168") }
    var showCasingDialog by remember { mutableStateOf(false) }
    var selectedTubing by remember { mutableStateOf("73") }
    var showTubingDialog by remember { mutableStateOf(false) }
    val casings = listOf(
        "139.7",
        "146",
        "168",
        "178"
    )
    val tubings = listOf(
        "60",
        "73",
        "89"
    )

    var depth by remember { mutableStateOf("") }
    var sensorPressure1 by remember { mutableStateOf("") }
    var sensorPressure2 by remember { mutableStateOf("") }
    var annulusPressure by remember { mutableStateOf("") }
    var density by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var flowResult by remember { mutableStateOf<Double?>(null) }
    var time by remember { mutableStateOf("") }
    fun parseNumber(value: String): Double {

        return value
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding().verticalScroll(rememberScrollState())
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Расчет притока по датчикам",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )


        OutlinedTextField(
            singleLine = true,

            value = depth,

            onValueChange = {
                depth = it
            },

            label = {
                Text("Глубина спуска, м")
            },

            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
            singleLine = true,

            value = sensorPressure1,

            onValueChange = {
                sensorPressure1 = it
            },

            label = {
                Text("Давление на приеме, МПа")
            },

            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
            singleLine = true,

            value = sensorPressure2,

            onValueChange = {
                sensorPressure2 = it
            },

            label = {
                Text("Датчик давления №2, МПа")
            },

            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
            singleLine = true,

            value = annulusPressure,

            onValueChange = {
                annulusPressure = it
            },

            label = {
                Text("Давление в затрубе, МПа")
            },

            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
            singleLine = true,

            value = density,

            onValueChange = {
                density = it
            },

            label = {
                Text("Плотность жидкости, кг/м³")
            },

            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
            singleLine = true,

            value = time,

            onValueChange = {
                time = it
            },

            label = {
                Text("Время исследования, мин")
            },

            modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
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

                errorMessage = null

                if (
                    depth.isBlank() ||
                    sensorPressure1.isBlank() ||
                    sensorPressure2.isBlank() ||
                    annulusPressure.isBlank() ||
                    density.isBlank() ||
                    time.isBlank()
                ) {

                    errorMessage = "Заполните все поля"

                    flowResult = null

                } else {

                    val vkp = Tables.getVkp(
                        selectedCasing,
                        selectedTubing
                    )

                    flowResult = FlowCalculator.sensorHermetic(

                        depth = parseNumber(depth),

                        sensorPressure1 = parseNumber(sensorPressure1),

                        sensorPressure2 = parseNumber(sensorPressure2),

                        annulusPressure = parseNumber(annulusPressure),

                        density = parseNumber(density),

                        casing = selectedCasing,

                        tubing = selectedTubing,

                        vkp = vkp,

                        time = parseNumber(time)



                    )

                }

            }

        ) {

            Text(
                text = "Рассчитать",
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

                    depth = ""
                    sensorPressure1 = ""
                    sensorPressure2 = ""
                    annulusPressure = ""
                    density = ""
                    time = ""

                    selectedCasing = "168"
                    selectedTubing = "73"

                }

            ) {

                Text("Очистить")

            }



            OutlinedButton(
                modifier = Modifier.weight(1f).keepVisibleAboveKeyboard(),

                onClick = {

                    navController.popBackStack()

                }

            ) {

                Text("Назад")

            }

        }
    Card(
        modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
        colors = CardDefaults.cardColors(containerColor = if (flowResult != null) CalculationResultYellow else Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )

    ) {

        if (flowResult != null) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Приток",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${flowResult} м³/сут",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

            }

        }

        if (errorMessage != null) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )

            }

        }

    }




    }

}