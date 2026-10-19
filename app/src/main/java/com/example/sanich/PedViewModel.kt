package com.example.sanich

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanich.data.PedEntity
import com.example.sanich.data.PedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PedUiState(
    val diameters: List<Int> = emptyList(),
    val powers: List<Int> = emptyList(),
    val casings: List<Int> = emptyList(),
    val walls: List<Double> = emptyList(),
    val results: List<PedEntity> = emptyList(),
    val selectedDiameter: Int? = null,
    val selectedPower: Int? = null,
    val selectedCasing: Int? = null,
    val selectedWall: Double? = null,
    val selectedPed: PedEntity? = null,
    val workingCurrent: String = "",
    val nominalCurrent: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val currentStep: Int
        get() = when {
            selectedDiameter == null -> 1
            selectedPower == null -> 2
            selectedCasing == null -> 3
            selectedWall == null -> 4
            else -> 5
        }

    val calculatedFlow: Double?
        get() {
            val ped = selectedPed ?: return null
            val work = workingCurrent.replace(',', '.').toDoubleOrNull() ?: return null
            val nominal = nominalCurrent.replace(',', '.').toDoubleOrNull() ?: return null
            if (work < 0.0 || nominal <= 0.0) return null
            val ratio = work / nominal
            return ped.flow * ratio * ratio
        }
}

class PedViewModel(
    private val repository: PedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedUiState())
    val uiState: StateFlow<PedUiState> = _uiState.asStateFlow()

    fun initialize() {
        if (_uiState.value.diameters.isNotEmpty() || _uiState.value.isLoading) {
            return
        }

        loadData(
            request = { repository.getDiameters() },
            reduce = { values: List<Int> ->
                copy(diameters = values)
            }
        )
    }

    fun selectDiameter(diameter: Int) {
        _uiState.update {
            it.copy(
                selectedDiameter = diameter,
                selectedPower = null,
                selectedCasing = null,
                selectedWall = null,
                selectedPed = null,
                powers = emptyList(),
                casings = emptyList(),
                walls = emptyList(),
                results = emptyList(),
                workingCurrent = "",
                nominalCurrent = "",
                errorMessage = null
            )
        }
        loadData(
            request = { repository.getPowers(diameter) },
            reduce = { values ->
                copy(powers = values)
            }
        )
    }

    fun selectPower(power: Int) {
        val diameter = _uiState.value.selectedDiameter ?: return
        _uiState.update {
            it.copy(
                selectedPower = power,
                selectedCasing = null,
                selectedWall = null,
                selectedPed = null,
                casings = emptyList(),
                walls = emptyList(),
                results = emptyList(),
                workingCurrent = "",
                nominalCurrent = "",
                errorMessage = null
            )
        }
        loadData(
            request = { repository.getCasings(diameter, power) },
            reduce = { values ->
                copy(casings = values)
            }
        )
    }

    fun selectCasing(casing: Int) {
        val state = _uiState.value
        val diameter = state.selectedDiameter ?: return
        val power = state.selectedPower ?: return
        _uiState.update {
            it.copy(
                selectedCasing = casing,
                selectedWall = null,
                selectedPed = null,
                walls = emptyList(),
                results = emptyList(),
                workingCurrent = "",
                nominalCurrent = "",
                errorMessage = null
            )
        }
        loadData(
            request = { repository.getWalls(diameter, power, casing) },
            reduce = { values ->
                copy(walls = values)
            }
        )
    }

    fun selectWall(wall: Double) {
        val state = _uiState.value
        val diameter = state.selectedDiameter ?: return
        val power = state.selectedPower ?: return
        val casing = state.selectedCasing ?: return
        _uiState.update {
            it.copy(
                selectedWall = wall,
                selectedPed = null,
                results = emptyList(),
                workingCurrent = "",
                nominalCurrent = "",
                errorMessage = null
            )
        }
        loadData(
            request = { repository.getResultPeds(diameter, power, casing, wall) },
            reduce = { values ->
                copy(results = values)
            }
        )
    }

    fun selectPed(ped: PedEntity) {
        _uiState.update { it.copy(selectedPed = ped, workingCurrent = "", nominalCurrent = "") }
    }

    fun updateWorkingCurrent(value: String) {
        _uiState.update { it.copy(workingCurrent = sanitizeDecimal(value)) }
    }

    fun updateNominalCurrent(value: String) {
        _uiState.update { it.copy(nominalCurrent = sanitizeDecimal(value)) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun reset() {
        val diameters = _uiState.value.diameters
        _uiState.value = PedUiState(diameters = diameters)
        if (diameters.isEmpty()) initialize()
    }

    private fun <T> loadData(
        request: suspend () -> List<T>,
        reduce: PedUiState.(List<T>) -> PedUiState
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { request() }
                .onSuccess { values ->
                    _uiState.update { state -> state.reduce(values).copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Не удалось загрузить данные"
                        )
                    }
                }
        }
    }

    private fun sanitizeDecimal(value: String): String = value.filterIndexed { index, char ->
        char.isDigit() || ((char == '.' || char == ',') && index > 0 &&
            value.take(index).none { it == '.' || it == ',' })
    }
}
