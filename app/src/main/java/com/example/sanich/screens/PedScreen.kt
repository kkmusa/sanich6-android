package com.example.sanich.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import com.example.sanich.PedUiState
import com.example.sanich.PedViewModel
import com.example.sanich.data.PedEntity
import com.example.sanich.ui.theme.CalculationResultYellow

@Composable
fun PedScreen(
    viewModel: PedViewModel,
    historyViewModel: CalculationHistoryViewModel,
    onBack: () -> Unit
) {
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.reset()
        }
    }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentHistoryEntry = state.selectedPed?.let { ped ->
        state.calculatedFlow?.let { flow ->
            HistoryEntry(
                title = "Необходимый приток",
                method = "По фактической загрузке ПЭД",
                result = "%.2f м³/сут".format(flow),
                details = "ПЭД=${ped.name}; D=${state.selectedDiameter} мм; мощность=${state.selectedPower} кВт; ЭК=${state.selectedCasing} мм; стенка=${state.selectedWall} мм; рабочий ток=${state.workingCurrent} А; номинальный ток=${state.nominalCurrent} А; номинальный приток=%.2f м³/сут".format(ped.flow)
            )
        }
    }
    val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)

    fun saveCurrentCalculation() {
        latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey, it) }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = state.calculatedFlow?.let { "${state.selectedPed?.name}|${state.selectedDiameter}|${state.selectedPower}|${state.selectedCasing}|${state.selectedWall}|${state.workingCurrent}|${state.nominalCurrent}|$it" })

    fun saveAndBack() {
        saveCurrentCalculation()
        onBack()
    }
    BackHandler(onBack = ::saveAndBack)

    LaunchedEffect(Unit) {
        viewModel.reset()
        viewModel.initialize()
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PedContent(
            state = state,
            onBack = ::saveAndBack,
            onDiameterSelected = viewModel::selectDiameter,
            onPowerSelected = viewModel::selectPower,
            onCasingSelected = viewModel::selectCasing,
            onWallSelected = viewModel::selectWall,
            onPedSelected = viewModel::selectPed,
            onWorkingCurrentChanged = viewModel::updateWorkingCurrent,
            onNominalCurrentChanged = viewModel::updateNominalCurrent,
            onReset = viewModel::reset
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PedContent(
    state: PedUiState,
    onBack: () -> Unit,
    onDiameterSelected: (Int) -> Unit,
    onPowerSelected: (Int) -> Unit,
    onCasingSelected: (Int) -> Unit,
    onWallSelected: (Double) -> Unit,
    onPedSelected: (PedEntity) -> Unit,
    onWorkingCurrentChanged: (String) -> Unit,
    onNominalCurrentChanged: (String) -> Unit,
    onReset: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        CalculationTopBar(title = "Необходимый приток", onBack = onBack)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressHeader(currentStep = state.currentStep, totalSteps = 5)

                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Исходные данные",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        SelectionGrid(
                            state = state,
                            onDiameterSelected = onDiameterSelected,
                            onPowerSelected = onPowerSelected,
                            onCasingSelected = onCasingSelected,
                            onWallSelected = onWallSelected
                        )
                    }
                }

                when {
                    state.isLoading -> Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                    state.selectedPed != null -> PedDetails(
                        state = state,
                        onWorkingCurrentChanged = onWorkingCurrentChanged,
                        onNominalCurrentChanged = onNominalCurrentChanged
                    )
                    state.results.isNotEmpty() -> PedResults(
                        results = state.results,
                        onPedSelected = onPedSelected
                    )
                    state.selectedWall != null -> Text(
                        text = "Подходящие ПЭД не найдены",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (state.selectedWall != null) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Начать заново") }
                }
                Spacer(Modifier.height(8.dp))
            }

            VerticalScrollIndicator(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 3.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun SelectionGrid(
    state: PedUiState,
    onDiameterSelected: (Int) -> Unit,
    onPowerSelected: (Int) -> Unit,
    onCasingSelected: (Int) -> Unit,
    onWallSelected: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.weight(1f)) {
            DiameterStep(
                diameters = state.diameters,
                selected = state.selectedDiameter,
                enabled = state.selectedDiameter == null && !state.isLoading,
                onSelect = onDiameterSelected
            )
        }
        Box(Modifier.weight(1f)) {
            state.selectedDiameter?.let {
                PowerStep(
                    powers = state.powers,
                    selected = state.selectedPower,
                    enabled = state.selectedPower == null && !state.isLoading,
                    onSelect = onPowerSelected
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.weight(1f)) {
            state.selectedPower?.let {
                CasingStep(
                    casings = state.casings,
                    selected = state.selectedCasing,
                    enabled = state.selectedCasing == null && !state.isLoading,
                    onSelect = onCasingSelected
                )
            }
        }
        Box(Modifier.weight(1f)) {
            state.selectedCasing?.let {
                WallStep(
                    walls = state.walls,
                    selected = state.selectedWall,
                    enabled = state.selectedWall == null && !state.isLoading,
                    onSelect = onWallSelected
                )
            }
        }
    }
}

@Composable
private fun PedResults(
    results: List<PedEntity>,
    onPedSelected: (PedEntity) -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Найдено ПЭД: ${results.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            results.forEach { ped ->
                ResultCard(ped = ped, onClick = { onPedSelected(ped) })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PedDetails(
    state: PedUiState,
    onWorkingCurrentChanged: (String) -> Unit,
    onNominalCurrentChanged: (String) -> Unit
) {
    val ped = state.selectedPed ?: return
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = ped.name,
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactValueCard(
                title = "Производитель",
                value = ped.manufacturer,
                modifier = Modifier.weight(1f)
            )
            CompactValueCard(
                title = "Номинальный приток",
                value = "%.2f м³/сут".format(ped.flow),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurrentInput(
                label = "Рабочий ток, А",
                value = state.workingCurrent,
                imeAction = ImeAction.Next,
                onValueChange = onWorkingCurrentChanged,
                onAction = { focusManager.moveFocus(FocusDirection.Right) },
                modifier = Modifier.weight(1f)
            )
            CurrentInput(
                label = "Номинальный ток, А",
                value = state.nominalCurrent,
                imeAction = ImeAction.Done,
                onValueChange = onNominalCurrentChanged,
                onAction = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                },
                modifier = Modifier.weight(1f)
            )
        }

        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = CalculationResultYellow
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Необходимый приток",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "при фактической загрузке ПЭД",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = state.calculatedFlow?.let { "%.2f м³/сут".format(it) } ?: "—",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun CompactValueCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.keepVisibleAboveKeyboard(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFFF6F6F6)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun CurrentInput(
    label: String,
    value: String,
    imeAction: ImeAction,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.keepVisibleAboveKeyboard(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        keyboardActions = if (imeAction == ImeAction.Done) {
            KeyboardActions(onDone = { onAction() })
        } else {
            KeyboardActions(onNext = { onAction() })
        }
    )
}
