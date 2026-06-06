package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

/**
 * Направление свайпа, определённое по смещению или скорости жеста.
 */
enum class SwipeDirection {
    /** Вертикальный свайп (вверх или вниз) */
    VERTICAL,

    /** Горизонтальный свайп (влево или вправо) */
    HORIZONTAL,

    /** Недостаточное движение для определения направления */
    UNDETERMINED
}

/**
 * Определяет направление свайпа, сравнивая абсолютные значения
 * горизонтального и вертикального смещения.
 *
 * @param horizontalMovement накопленное горизонтальное смещение или velocityX
 * @param verticalMovement накопленное вертикальное смещение или velocityY
 */
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
