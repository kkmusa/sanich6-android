package com.example.sanich.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val IndicatorOrange = Color(0xFFF28C00)

@Composable
fun VerticalScrollIndicator(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    ScrollThumb(
        progress = if (scrollState.maxValue > 0) {
            scrollState.value.toFloat() / scrollState.maxValue.toFloat()
        } else 0f,
        visible = scrollState.maxValue > 0,
        modifier = modifier
    )
}

@Composable
fun VerticalScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = listState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo
    val canScroll = listState.canScrollBackward || listState.canScrollForward

    // У истории карточки разной высоты. Деление только на (totalItems - 1)
    // даёт заметный скачок в самом низу, потому что последние несколько карточек
    // уже одновременно находятся во viewport. Оцениваем реальный видимый диапазон
    // по средней высоте текущих карточек и размеру viewport. Так позиция бегунка
    // заранее приближается к 100% и не прыгает в конец на последнем участке.
    val rawProgress = when {
        !canScroll || totalItems <= 0 -> 0f
        !listState.canScrollBackward -> 0f
        else -> {
            val firstVisible = visibleItems.firstOrNull()
            val averageItemSize = visibleItems
                .map { it.size.toFloat() }
                .average()
                .toFloat()
                .coerceAtLeast(1f)
            val viewportSize =
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset)
                    .toFloat()
                    .coerceAtLeast(1f)
            val visibleCapacity = (viewportSize / averageItemSize).coerceAtLeast(1f)
            val maxFirstItemPosition = (totalItems.toFloat() - visibleCapacity).coerceAtLeast(1f)
            val fractionInsideItem = if (firstVisible != null) {
                listState.firstVisibleItemScrollOffset.toFloat() / firstVisible.size.coerceAtLeast(1).toFloat()
            } else {
                0f
            }
            val itemPosition = listState.firstVisibleItemIndex.toFloat() + fractionInsideItem

            when {
                !listState.canScrollForward -> 1f
                else -> (itemPosition / maxFirstItemPosition).coerceIn(0f, 0.999f)
            }
        }
    }
    val progress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 90),
        label = "historyScrollIndicator"
    )

    ScrollThumb(progress = progress, visible = canScroll, modifier = modifier)
}

@Composable
private fun ScrollThumb(
    progress: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(11.dp)
            .background(Color.Black.copy(alpha = 0.10f), RoundedCornerShape(50))
    ) {
        val density = LocalDensity.current
        val thumbHeight = 64.dp
        val availableOffsetPx = with(density) {
            (maxHeight - thumbHeight).toPx().coerceAtLeast(0f)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, (availableOffsetPx * progress.coerceIn(0f, 1f)).roundToInt()) }
                .width(8.dp)
                .height(thumbHeight)
                .background(IndicatorOrange, RoundedCornerShape(50))
        )
    }
}
