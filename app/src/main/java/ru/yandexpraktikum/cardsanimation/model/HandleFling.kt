package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

/** Минимальная вертикальная скорость для распознавания быстрого fling-свайпа */
const val VERTICAL_FLING_VELOCITY_THRESHOLD = 500f

/** Минимальная горизонтальная скорость для распознавания быстрого fling-свайпа */
const val HORIZONTAL_FLING_VELOCITY_THRESHOLD = 500f

/**
 * Обрабатывает вертикальный fling по скорости движения (быстрый жест).
 * Свайп вверх раскрывает колоду, свайп вниз — закрывает.
 *
 * @return true, если жест превысил порог и был обработан
 */
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

/**
 * Обрабатывает горизонтальный fling по скорости движения (быстрый жест).
 * Любой горизонтальный fling запускает перестановку карт.
 *
 * @return true, если жест превысил порог и был обработан
 */
fun handleHorizontalFling(
    velocityX: Float,
    onSwapCards: () -> Unit,
    threshold: Float = HORIZONTAL_FLING_VELOCITY_THRESHOLD
): Boolean {
    if (abs(velocityX) > threshold) {
        onSwapCards()
        return true
    }
    return false
}

/**
 * Обрабатывает fling-жест: определяет доминирующее направление
 * и вызывает только один обработчик, предотвращая конфликт жестов.
 *
 * @return true, если жест был обработан
 */
fun handleFling(
    velocityX: Float,
    velocityY: Float,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onSwapCards: () -> Unit
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
            onSwapCards = onSwapCards
        )
        SwipeDirection.UNDETERMINED -> false
    }
}
