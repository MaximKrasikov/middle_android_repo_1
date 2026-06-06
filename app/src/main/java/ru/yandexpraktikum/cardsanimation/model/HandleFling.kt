package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs
const val VERTICAL_FLING_VELOCITY_THRESHOLD = 500f
const val HORIZONTAL_FLING_VELOCITY_THRESHOLD = 500f

fun handleFling(
    velocityX: Float,
    velocityY: Float,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
): Boolean {
    val direction = determineSwipeDirection(velocityX, velocityY)
    return when (direction) {
        SwipeDirection.VERTICAL -> handleVerticalFling(
            velocityY = velocityY,
            onExpand = onExpand,
            onCollapse = onCollapse
        )
        SwipeDirection.HORIZONTAL -> handleHorizontalFling(
            velocityX = velocityX,
        )
        SwipeDirection.UNDETERMINED -> false
    }
}
fun handleVerticalFling(
    velocityY: Float,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    threshold: Float = VERTICAL_FLING_VELOCITY_THRESHOLD
): Boolean {
    return when {
        velocityY < -threshold -> {
            onExpand()
            true
        }
        velocityY > threshold -> {
            onCollapse()
            true
        }
        else -> false
    }
}

fun handleHorizontalFling(
    velocityX: Float,
    threshold: Float = HORIZONTAL_FLING_VELOCITY_THRESHOLD
): Boolean {
    if (abs(velocityX) > threshold) {
        return true
    }
    return false
}