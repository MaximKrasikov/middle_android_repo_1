package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

/** Порог смещения для распознавания свайпа в многошаговой анимации (Задание 5) */
const val SWIPE_GESTURE_THRESHOLD = 100f

/** Минимальное вертикальное смещение (в пикселях) для распознавания медленного свайпа */
const val VERTICAL_SWIPE_THRESHOLD = 50f

/** Минимальное горизонтальное смещение (в пикселях) для распознавания медленного свайпа */
const val HORIZONTAL_SWIPE_THRESHOLD = 50f

/**
 * Обрабатывает вертикальный свайп по накопленному смещению (медленный жест).
 * Свайп вверх раскрывает колоду, свайп вниз — закрывает.
 *
 * @return true, если жест превысил порог и был обработан
 */
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

/**
 * Обрабатывает горизонтальный свайп по накопленному смещению (медленный жест).
 * Любой горизонтальный свайп (влево или вправо) запускает перестановку карт.
 *
 * @return true, если жест превысил порог и был обработан
 */
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

/**
 * Обрабатывает вертикальный свайп: раскрывает или закрывает колоду.
 *
 * @param verticalDragDistance накопленное вертикальное смещение
 * @param onFanStateChange колбэк с новым состоянием веера (true — раскрыта)
 */
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

/**
 * Обрабатывает горизонтальный свайп: запускает перестановку карт.
 *
 * @param horizontalDragDistance накопленное горизонтальное смещение
 * @param onCardsReorder колбэк перестановки карт
 */
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
