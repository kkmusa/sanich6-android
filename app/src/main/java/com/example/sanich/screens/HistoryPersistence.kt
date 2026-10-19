package com.example.sanich.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

/**
 * Saves the latest valid calculation after a quiet 2-second period.
 * It also saves immediately when the app goes to background or the screen leaves composition.
 * The screen itself handles system/gesture Back and calls the same save function immediately.
 */
@Composable
fun PersistHistoryOnDispose(
    saveCurrentCalculation: () -> Unit,
    debounceKey: Any? = null,
    debounceMillis: Long = 2_000L
) {
    val latestSave = rememberUpdatedState(saveCurrentCalculation)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(debounceKey) {
        if (debounceKey != null) {
            delay(debounceMillis)
            latestSave.value()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                latestSave.value()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            latestSave.value()
        }
    }
}
