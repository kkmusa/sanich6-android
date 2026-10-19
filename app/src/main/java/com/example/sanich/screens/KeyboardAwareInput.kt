package com.example.sanich.screens

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Keeps a focused input field above the software keyboard.
 * The short delay lets Android finish resizing the window before scrolling.
 */
fun Modifier.keepVisibleAboveKeyboard(): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    this
        .bringIntoViewRequester(requester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                scope.launch {
                    delay(300)
                    requester.bringIntoView()
                }
            }
        }
}

/**
 * Common IME behavior for calculator fields: the keyboard stays open and
 * the Next key moves focus to the next focusable input. The existing
 * keepVisibleAboveKeyboard modifier then scrolls that field into view.
 */
@androidx.compose.runtime.Composable
fun nextFieldKeyboardActions(): androidx.compose.foundation.text.KeyboardActions {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    return androidx.compose.foundation.text.KeyboardActions(
        onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Next) }
    )
}


/** Last calculator field: closes the keyboard and clears focus. */
@androidx.compose.runtime.Composable
fun doneFieldKeyboardActions(): androidx.compose.foundation.text.KeyboardActions {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    return androidx.compose.foundation.text.KeyboardActions(
        onDone = {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    )
}
