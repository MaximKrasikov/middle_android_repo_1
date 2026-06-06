package ru.yandexpraktikum.cardsanimation.model

import android.util.Log

private const val TAG = "AnimatedSwipeCardLog"

fun logSwipeDirection(
    source: String,
    direction: SwipeDirection,
    offsetX: Float,
    offsetY: Float
) {
    if (direction == SwipeDirection.UNDETERMINED) return
    Log.d(
        TAG,
        "$source: $direction (offsetX=$offsetX, offsetY=$offsetY)"
    )
}
