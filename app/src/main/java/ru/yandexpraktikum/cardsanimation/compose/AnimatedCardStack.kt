package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.SWIPE_GESTURE_THRESHOLD
import ru.yandexpraktikum.cardsanimation.model.determineSwipeDirection
import ru.yandexpraktikum.cardsanimation.model.handleVerticalSwipe
import ru.yandexpraktikum.cardsanimation.model.logSwipeDirection
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                var dragOffsetX = 0f
                var dragOffsetY = 0f
                var verticalDragOffset = 0f
                var horizontalDragOffset = 0f
                detectDragGestures(
                    onDragStart = {
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    },
                    onDragEnd = {
                        val direction = determineSwipeDirection(dragOffsetX, dragOffsetY)
                        logSwipeDirection("drag end", direction, dragOffsetX, dragOffsetY)

                        val threshold = SWIPE_GESTURE_THRESHOLD
                        val isVerticalDominant =
                            abs(verticalDragOffset) > abs(horizontalDragOffset)

                        when {
                            isVerticalDominant && abs(verticalDragOffset) > threshold -> {
                                handleVerticalSwipe(
                                    verticalDragDistance = verticalDragOffset,
                                    onFanStateChange = { newFanState -> isRotated = newFanState }
                                )
                            }
                        }

                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        horizontalDragOffset = 0f
                        verticalDragOffset = 0f
                    }

                ) { _, dragAmount ->
                    // Определяем направление свайпа
                    dragOffsetX += dragAmount.x
                    dragOffsetY += dragAmount.y
                    if (abs(dragAmount.y) >= abs(dragAmount.x)) {
                        verticalDragOffset += dragAmount.y
                    } else {
                        horizontalDragOffset += dragAmount.x
                    }
                }
            }
            .clickable(
                onClick = { isRotated = !isRotated }
            ),
        contentAlignment = Alignment.Center
    ) {
        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}