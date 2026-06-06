package ru.yandexpraktikum.cardsanimation.model

import android.util.Log

private const val CARD_STACK_GESTURE_TAG = "AnimatedSwipeCardLog"

fun logSwipeDirection(
    source: String,
    direction: SwipeDirection,
    offsetX: Float,
    offsetY: Float
) {
    if (direction == SwipeDirection.UNDETERMINED) return
    Log.d(
        CARD_STACK_GESTURE_TAG,
        "$source: $direction (offsetX=$offsetX, offsetY=$offsetY)"
    )
}
