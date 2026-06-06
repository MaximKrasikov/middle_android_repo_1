package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs
const val VERTICAL_SWIPE_THRESHOLD = 50f
const val HORIZONTAL_SWIPE_THRESHOLD = 50f

fun handleDragEnd(
    horizontalDragOffset: Float,
    verticalDragOffset: Float,
    onFanStateChange: (Boolean) -> Unit,
    threshold: Float = SWIPE_GESTURE_THRESHOLD
): Boolean {
    val isVerticalDominant = abs(verticalDragOffset) > abs(horizontalDragOffset)
    val isHorizontalDominant = abs(horizontalDragOffset) > abs(verticalDragOffset)

    return when {
        isVerticalDominant && abs(verticalDragOffset) > threshold -> {
            handleVerticalSwipe(
                verticalDragDistance = verticalDragOffset,
                onFanStateChange = onFanStateChange,
                threshold = threshold
            )
        }
        isHorizontalDominant && abs(horizontalDragOffset) > threshold -> {
            handleHorizontalSwipe(
                horizontalDragDistance = horizontalDragOffset,
                threshold = threshold
            )
        }
        else -> false
    }
}

fun handleVerticalDragOffset(
    verticalDragOffset: Float,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    threshold: Float = VERTICAL_SWIPE_THRESHOLD
): Boolean {
    return when {
        verticalDragOffset < -threshold -> {
            onExpand()
            true
        }
        verticalDragOffset > threshold -> {
            onCollapse()
            true
        }
        else -> false
    }
}

fun handleHorizontalDragOffset(
    horizontalDragOffset: Float,
    onSwapCards: () -> Unit,
    threshold: Float = HORIZONTAL_SWIPE_THRESHOLD
): Boolean {
    if (abs(horizontalDragOffset) > threshold) {
        onSwapCards()
        return true
    }
    return false
}