package com.example.sanich.screens

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import androidx.compose.material.icons.filled.Close
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

/** Stores only document bookmarks; available to the bundled local viewer. */
private class VnrBookmarkStorage(context: Context) {
    private val prefs = context.getSharedPreferences("vnr_bookmarks", Context.MODE_PRIVATE)
    @JavascriptInterface fun read(): String = prefs.getString("pages", "[]") ?: "[]"
    @JavascriptInterface fun write(value: String): Boolean = try {
        val rows = org.json.JSONArray(value)
        require(value.length <= 100_000 && rows.length() <= 207)
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            require(row.getInt("page") in 1..207 && row.getString("name").length <= 120)
        }
        prefs.edit().putString("pages", value).commit()
    } catch (_: Exception) { false }
}

/** Local document viewer with an always-accessible contents shortcut and scroll progress. */
private class VnrWebView(context: Context) : WebView(context) {
    var onPositionChanged: ((Float) -> Unit)? = null

    private fun maxScroll(): Int = (computeVerticalScrollRange() - height).coerceAtLeast(0)

    fun goToFraction(fraction: Float) {
        scrollTo(0, (maxScroll() * fraction.coerceIn(0f, 1f)).roundToInt())
    }

    fun refreshPosition() {
        val max = maxScroll()
        onPositionChanged?.invoke(if (max == 0) 0f else (scrollY.toFloat() / max).coerceIn(0f, 1f))
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        refreshPosition()
    }
}

@Composable
fun VnrReglamentScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var browser by remember { mutableStateOf<VnrWebView?>(null) }
    var found by remember { mutableIntStateOf(0) }
    var active by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    fun invokeSearch(script: String) {
        browser?.evaluateJavascript(script) { result ->
            try {
                val obj = JSONObject(result)
                active = obj.optInt("active", 0)
                found = obj.optInt("total", 0)
            } catch (_: Exception) { active = 0; found = 0 }
        }
    }
    val gold = Color(0xFFFFC400)
    val brown = Color(0xFF5C3800)

    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize()) {
        CalculationTopBar("Регламент ВНР", onBack)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; invokeSearch("runSearch(" + JSONObject.quote(it) + ")") },
                label = { Text("Поиск по регламенту") },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = {
                        query = ""
                        invokeSearch("runSearch('')")
                    }) { Icon(Icons.Filled.Close, "Очистить поиск") }
                },
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(
                onClick = { invokeSearch("nextHit(false)") }, enabled = found > 0,
                modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = gold, contentColor = brown,
                    disabledContainerColor = Color(0xFFFFE8A3), disabledContentColor = Color(0xFF987C45)
                )
            ) { Icon(Icons.Filled.KeyboardArrowLeft, "Предыдущее совпадение", Modifier.size(34.dp)) }
            FilledIconButton(
                onClick = { invokeSearch("nextHit(true)") }, enabled = found > 0,
                modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = gold, contentColor = brown,
                    disabledContainerColor = Color(0xFFFFE8A3), disabledContentColor = Color(0xFF987C45)
                )
            ) { Icon(Icons.Filled.KeyboardArrowRight, "Следующее совпадение", Modifier.size(34.dp)) }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (query.isBlank()) "Регламент ВНР" else "Совпадение $active из $found",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = { browser?.scrollTo(0, 0); progress = 0f },
                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = brown),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) { Text("↑ Оглавление") }
        }
        AndroidView(
            factory = { context ->
                VnrWebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webChromeClient = android.webkit.WebChromeClient()
                    addJavascriptInterface(VnrBookmarkStorage(context), "SanichBookmarks")
                    settings.allowFileAccess = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    // WebView's native kinetic scrolling responds to swipe velocity and decelerates.
                    isVerticalScrollBarEnabled = false // use the themed seek bar below instead
                    isHorizontalScrollBarEnabled = false
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            post { refreshPosition() }
                        }
                    }
                    onPositionChanged = { if (!dragging) progress = it }
                    loadUrl("file:///android_asset/vnr/reglament.html")
                    browser = this
                }
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            update = { view ->
                view.onPositionChanged = { if (!dragging) progress = it }
            }
        )
        Row(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Положение", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = progress,
                onValueChange = {
                    dragging = true
                    progress = it
                    browser?.goToFraction(it)
                },
                onValueChangeFinished = {
                    dragging = false
                    browser?.refreshPosition()
                },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = gold, activeTrackColor = gold,
                    inactiveTrackColor = Color(0xFFFFE8A3)
                )
            )
            Text("${(progress * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}
