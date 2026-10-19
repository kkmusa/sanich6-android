package com.example.sanich.screens

import android.os.SystemClock
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import kotlinx.coroutines.delay
import java.util.Locale

// State survives navigation: a timer uses monotonic elapsed time, not a composable coroutine.
object ManualDebitSession {
    var liters by mutableStateOf("")
    var seconds by mutableStateOf("")
    var timeInMinutes by mutableStateOf(false)
    var running by mutableStateOf(false)
    var startedAt by mutableLongStateOf(0L)
    var accumulatedMs by mutableLongStateOf(0L)
    var displayedMs by mutableLongStateOf(0L)
    var hasSession by mutableStateOf(false)
    private const val PREFS = "manual_debit_session_v2"
    fun elapsedMs(): Long = accumulatedMs + if(running) (SystemClock.elapsedRealtime()-startedAt).coerceAtLeast(0L) else 0L
    fun save(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("liters", liters).putString("seconds", seconds)
            .putBoolean("minutes", timeInMinutes).putBoolean("running", running)
            .putLong("accumulated", accumulatedMs)
            .putLong("savedElapsed", elapsedMs())
            .putLong("savedWall", System.currentTimeMillis())
            .putBoolean("hasSession", hasSession).apply()
    }
    fun restore(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.contains("savedWall")) return
        liters = p.getString("liters", "") ?: ""
        seconds = p.getString("seconds", "") ?: ""
        timeInMinutes = p.getBoolean("minutes", false)
        running = p.getBoolean("running", false)
        hasSession = p.getBoolean("hasSession", false)
        val previous = p.getLong("savedElapsed", 0L)
        val delta = (System.currentTimeMillis() - p.getLong("savedWall", System.currentTimeMillis())).coerceAtLeast(0L)
        accumulatedMs = previous + if (running) delta else 0L
        displayedMs = accumulatedMs
        startedAt = SystemClock.elapsedRealtime()
        if (hasSession) seconds = String.format(Locale.US, "%.2f", displayedMs / (if(timeInMinutes) 60000.0 else 1000.0))
    }
}

@Composable
fun ManualDebitScreen(
    historyViewModel: CalculationHistoryViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
    LaunchedEffect(Unit) {
        while (true) { delay(1000); ManualDebitSession.save(context) }
    }
    var liters by ManualDebitSession::liters
    var seconds by ManualDebitSession::seconds
    var timeInMinutes by ManualDebitSession::timeInMinutes
    var running by ManualDebitSession::running
    var startedAt by ManualDebitSession::startedAt
    var accumulatedMs by ManualDebitSession::accumulatedMs
    var displayedMs by ManualDebitSession::displayedMs
    var hasSession by ManualDebitSession::hasSession
    var intervalMode by remember { mutableStateOf(false) }
    var startYesterday by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }
    val intervalSeconds = if (startTime != null && endTime != null) {
        val date = LocalDate.now()
        Duration.between(LocalDateTime.of(if (startYesterday) date.minusDays(1) else date, startTime), LocalDateTime.of(date, endTime)).seconds.toDouble()
    } else null

    LaunchedEffect(running) {
        while (running) {
            displayedMs = accumulatedMs + (SystemClock.elapsedRealtime() - startedAt)
            seconds = String.format(Locale.US, "%.2f", displayedMs / (if (timeInMinutes) 60000.0 else 1000.0))
            delay(100)
        }
    }

    val litersValue = liters.replace(',', '.').toDoubleOrNull()
    val enteredTimeValue = seconds.replace(',', '.').toDoubleOrNull()
    val secondsValue = if (intervalMode) intervalSeconds else enteredTimeValue?.let { if (timeInMinutes) it * 60.0 else it }
    val debit = if (litersValue != null && litersValue > 0.0 && secondsValue != null && secondsValue > 0.0) {
        litersValue * 86.4 / secondsValue
    } else null

    fun stopTimer() {
        if (running) {
            accumulatedMs += SystemClock.elapsedRealtime() - startedAt
            displayedMs = accumulatedMs
            running = false
            ManualDebitSession.save(context)
        }
    }

    fun saveCurrentCalculation() {
        val currentLiters = liters.replace(',', '.').toDoubleOrNull()
        val enteredCurrentTime = seconds.replace(',', '.').toDoubleOrNull()
        val currentSeconds = if (intervalMode) intervalSeconds else enteredCurrentTime?.let { if (timeInMinutes) it * 60.0 else it }
        val currentDebit = if (
            currentLiters != null && currentLiters > 0.0 &&
            currentSeconds != null && currentSeconds > 0.0
        ) currentLiters * 86.4 / currentSeconds else null

        currentDebit?.let {
            // An interval is a separate measurement, not an update of the timer entry.
            // Include all measured inputs in the key so another interval gets its own row.
            val recordKey = if (intervalMode) {
                val measuredDate = LocalDate.now()
                val fromDate = if (startYesterday) measuredDate.minusDays(1) else measuredDate
                "$historySessionKey|interval|$fromDate|$startTime|$measuredDate|$endTime|$currentLiters"
            } else historySessionKey
            historyViewModel.saveSession(recordKey, 
                HistoryEntry(
                    title = "Ручной замер дебита",
                    method = if (intervalMode) "Замер по временному интервалу" else "Турбинный объёмный расходомер",
                    result = "${it.prettyDebit()} м³/сут",
                    details = if (intervalMode) "Объём=$liters л; начало=${if (startYesterday) "вчера" else "сегодня"} $startTime; конец=сегодня $endTime; интервал=${currentSeconds?.toLong() ?: 0L} с" else "Объём=$liters л; время=$seconds ${if (timeInMinutes) "мин" else "с"}"
                )
            )
        }
    }

    PersistHistoryOnDispose(::saveCurrentCalculation, debounceKey = debit?.let { "$liters|$seconds|$it" })

    fun saveAndBack() {
        hasSession = hasSession || running || accumulatedMs > 0L || liters.isNotBlank()
        saveCurrentCalculation()
        ManualDebitSession.save(context)
        onBack()
    }
    BackHandler(onBack = ::saveAndBack)

    if (pickingStart != null) {
        IntervalTimeDialog(initialTime = if (pickingStart == true) startTime else endTime, onDismiss = { pickingStart = null }, onConfirm = { selected ->
            if (pickingStart == true) startTime = selected else endTime = selected
            pickingStart = null
        })
    }
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .padding(paddingValues)
        ) {
            CalculationTopBar("Ручной замер дебита", ::saveAndBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding().verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = liters,
                    onValueChange = { liters = it.filterDebitDecimal() },
                    label = { Text("Набежавший объём, л") },
                    modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Next), keyboardActions = nextFieldKeyboardActions()
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Расчёт по временному интервалу", Modifier.weight(1f))
                    Switch(checked = intervalMode, onCheckedChange = { intervalMode = it; if (it && running) stopTimer() })
                }
                if (intervalMode) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Начало")
                            OutlinedButton(onClick = { startYesterday = !startYesterday }) { Text(if (startYesterday) "Вчера" else "Сегодня") }
                            OutlinedButton(onClick = { pickingStart = true }) { Text(startTime?.toString() ?: "Выбрать время") }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Конец · Сегодня")
                            OutlinedButton(onClick = { pickingStart = false }) { Text(endTime?.toString() ?: "Выбрать время") }
                        }
                    }
                    if (endTime == null) Text("Укажите время окончания замера")
                    else if (intervalSeconds == null || intervalSeconds <= 0) Text("Время окончания должно быть позже начала")
                    else Text("Интервал: ${intervalSeconds.toLong() / 60} мин ${intervalSeconds.toLong() % 60} с")
                }
                if (!intervalMode) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { if (!intervalMode && !running) { timeInMinutes = false; seconds = String.format(Locale.US,"%.2f",accumulatedMs/1000.0) } }, modifier = Modifier.weight(1f)) { Text(if (!timeInMinutes) "✓ Секунды" else "Секунды") }
                    OutlinedButton(onClick = { if (!intervalMode && !running) { timeInMinutes = true; seconds = String.format(Locale.US,"%.2f",accumulatedMs/60000.0) } }, modifier = Modifier.weight(1f)) { Text(if (timeInMinutes) "✓ Минуты" else "Минуты") }
                }
                OutlinedTextField(
                    value = seconds,
                    onValueChange = {
                        if (!running) {
                            seconds = it.filterDebitDecimal()
                            accumulatedMs = ((seconds.replace(',', '.').toDoubleOrNull() ?: 0.0) * (if (timeInMinutes) 60000 else 1000)).toLong()
                            displayedMs = accumulatedMs
                        }
                    },
                    label = { Text("Время замера, ${if (timeInMinutes) "мин" else "с"}") },
                    modifier = Modifier.fillMaxWidth().keepVisibleAboveKeyboard(),
                    singleLine = true,
                    enabled = !running && !intervalMode,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Done), keyboardActions = doneFieldKeyboardActions()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (running) stopTimer() else {
                                hasSession = true
                                startedAt = SystemClock.elapsedRealtime()
                                running = true
                                ManualDebitSession.save(context)
                            }
                        },
                        modifier = Modifier.weight(1f), enabled = !intervalMode
                    ) { Text(if (running) "Стоп" else if (accumulatedMs > 0) "Продолжить" else "Старт") }
                    OutlinedButton(
                        onClick = {
                            running = false
                            startedAt = 0L
                            accumulatedMs = 0L
                            displayedMs = 0L
                            if (intervalMode) { startTime = null; endTime = null; startYesterday = false } else seconds = ""
                            hasSession = false
                            ManualDebitSession.save(context)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Сброс времени") }
                }

                } // End ordinary timer-only controls

                ResultCardSimple(
                    title = "Дебит скважины",
                    result = debit?.let { "${it.prettyDebit()} м³/сут" },
                    error = if (intervalMode && endTime == null) "Укажите время окончания замера" else if (intervalMode && startTime == null) "Укажите время начала замера" else if (intervalMode && (intervalSeconds ?: 0.0) <= 0.0) "Некорректный временной интервал" else if (liters.isBlank() || seconds.isBlank() && !intervalMode) "Введите объём и время" else "Значения должны быть больше нуля"
                )


                OutlinedButton(
                    onClick = {
                        running = false
                        liters = ""
                        seconds = ""
                        startTime = null; endTime = null; startYesterday = false
                        startedAt = 0L
                        accumulatedMs = 0L
                        displayedMs = 0L
                        hasSession = false
                        ManualDebitSession.save(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Очистить") }
            }
        }
    }
}

private fun String.filterDebitDecimal(): String = filterIndexed { index, char ->
    char.isDigit() || ((char == '.' || char == ',') && index > 0 && take(index).none { it == '.' || it == ',' })
}
private fun Double.prettyDebit(): String = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')

/** Rectangular swipe wheels. Each wheel snaps to the nearest full hour/minute. */
@Composable
private fun IntervalTimeDialog(
    initialTime: LocalTime?,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    // Keep existing time on reopen; otherwise start at current local time.
    val initial = remember { initialTime ?: LocalTime.now() }
    var hour by remember { mutableStateOf(initial.hour) }
    var minute by remember { mutableStateOf(initial.minute) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время") },
        text = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ManualTimeWheel("Часы", hour, 24, Modifier.weight(1f)) { hour = it }
                ManualTimeWheel("Минуты", minute, 60, Modifier.weight(1f)) { minute = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(hour, minute)) }) { Text("Готово") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun ManualTimeWheel(
    title: String,
    selected: Int,
    count: Int,
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val halfItemPx = with(LocalDensity.current) { 24.dp.toPx() }

    // Update the value only after the user stops swiping; the wheel then settles
    // precisely onto a number instead of leaving two half-visible numbers.
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }.distinctUntilChanged().collect { scrolling ->
            if (!scrolling) {
                val candidate = (state.firstVisibleItemIndex +
                    if (state.firstVisibleItemScrollOffset >= halfItemPx) 1 else 0)
                    .coerceIn(0, count - 1)
                if (state.firstVisibleItemIndex != candidate || state.firstVisibleItemScrollOffset != 0) {
                    state.animateScrollToItem(candidate)
                }
                onSelected(candidate)
            }
        }
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Box(
            Modifier.padding(top = 8.dp).fillMaxWidth().height(144.dp)
                .border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(10.dp))
                .background(Color(0xFFF9F9F9), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.fillMaxWidth().height(48.dp)
                    .background(Color(0xFFFFE5AE), RoundedCornerShape(6.dp))
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(count) { value ->
                    Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            value.toString().padStart(2, '0'),
                            fontSize = if (value == selected) 27.sp else 21.sp,
                            fontWeight = if (value == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (value == selected) Color(0xFF242424) else Color(0xFF858585),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
