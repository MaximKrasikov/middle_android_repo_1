package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

enum class SwipeDirection {
    VERTICAL,
    HORIZONTAL,
    UNDETERMINED
}

const val SWIPE_GESTURE_THRESHOLD = 100f

/**
 * abs(verticalMovement) > abs(horizontalMovement) = вертикальный свайп
 * abs(horizontalMovement) > abs(verticalMovement) = горизонтальный свайп
 * */
fun determineSwipeDirection(
    horizontalMovement: Float,
    verticalMovement: Float
): SwipeDirection {
    val absHorizontal = abs(horizontalMovement)
    val absVertical = abs(verticalMovement)

    return when {
        absVertical > absHorizontal -> SwipeDirection.VERTICAL
        absHorizontal > absVertical -> SwipeDirection.HORIZONTAL
        else -> SwipeDirection.UNDETERMINED
    }
}

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
