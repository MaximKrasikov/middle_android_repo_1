package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

const val SWIPE_GESTURE_THRESHOLD = 100f

fun handleVerticalSwipe(
    verticalDragDistance: Float,
    onFanStateChange: (Boolean) -> Unit,
    threshold: Float = SWIPE_GESTURE_THRESHOLD
): Boolean {
    return when {
        verticalDragDistance < -threshold -> {
            onFanStateChange(true)
            true
        }
        verticalDragDistance > threshold -> {
            onFanStateChange(false)
            true
        }
        else -> false
    }
}

fun handleHorizontalSwipe(
    horizontalDragDistance: Float,
    onCardsReorder: () -> Unit,
    threshold: Float = SWIPE_GESTURE_THRESHOLD
): Boolean {
    if (abs(horizontalDragDistance) > threshold) {
        onCardsReorder()
        return true
    }
    return false
}
