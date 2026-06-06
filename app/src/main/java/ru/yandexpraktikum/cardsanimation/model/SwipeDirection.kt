package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

enum class SwipeDirection {
    VERTICAL,
    HORIZONTAL,
    UNDETERMINED
}

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