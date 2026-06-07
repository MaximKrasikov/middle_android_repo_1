package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

/**
 * Обрабатывает завершение медленного перетаскивания: определяет доминирующее
 * направление жеста и вызывает только один обработчик, предотвращая конфликты.
 *
 * @param horizontalDragOffset накопленное горизонтальное смещение
 * @param verticalDragOffset накопленное вертикальное смещение
 * @param onFanStateChange колбэк раскрытия/закрытия колоды (true — раскрыта)
 * @param onCardsReorder колбэк запуска перестановки карт
 * @return true, если жест превысил порог и был обработан
 */
fun handleDragEnd(
    horizontalDragOffset: Float,
    verticalDragOffset: Float,
    onFanStateChange: (Boolean) -> Unit,
    onCardsReorder: () -> Unit,
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
                onCardsReorder = onCardsReorder,
                threshold = threshold
            )
        }
        else -> false
    }
}
