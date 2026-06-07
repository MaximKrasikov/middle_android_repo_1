package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationStep
import ru.yandexpraktikum.cardsanimation.model.determineSwipeDirection
import ru.yandexpraktikum.cardsanimation.model.handleDragEnd
import ru.yandexpraktikum.cardsanimation.model.logSwipeDirection
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
private fun calculateCardRotation(
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
    var cardList by remember(cards) { mutableStateOf(cards) }
    val cardCount = cardList.size
    var isRotated by remember { mutableStateOf(false) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    val interactionSource = remember { MutableInteractionSource() }

    val animationStateRef = rememberUpdatedState(animationState)
    val startCardSwapAnimation by rememberUpdatedState(newValue = {
        if (animationState.isAnimating) return@rememberUpdatedState
        animationState = CardSwapAnimationState(
            isAnimating = true,
            animationStep = CardSwapAnimationStep.MOVE_AWAY
        )
    })

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                var dragOffsetX = 0f
                var dragOffsetY = 0f
                var horizontalDragOffset = 0f
                var verticalDragOffset = 0f

                detectDragGestures(
                    onDragStart = {
                        if (animationStateRef.value.isAnimating) return@detectDragGestures
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        horizontalDragOffset = 0f
                        verticalDragOffset = 0f
                    },
                    onDragEnd = {
                        if (!animationStateRef.value.isAnimating) {
                            val direction = determineSwipeDirection(dragOffsetX, dragOffsetY)
                            logSwipeDirection("drag end", direction, dragOffsetX, dragOffsetY)

                            handleDragEnd(
                                horizontalDragOffset = horizontalDragOffset,
                                verticalDragOffset = verticalDragOffset,
                                onFanStateChange = { newFanState -> isRotated = newFanState },
                                onCardsReorder = { startCardSwapAnimation() }
                            )
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
                    if (animationStateRef.value.isAnimating) return@detectDragGestures
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
                interactionSource = interactionSource,
                indication = null
            ) {
                if (!animationState.isAnimating) {
                    isRotated = !isRotated
                }
            },
        contentAlignment = Alignment.Center
    ) {
        cardList.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    animationState = animationState,
                    onAnimationStepComplete = { step ->
                        handleAnimationStepComplete(
                            step = step,
                            cardIndex = i,
                            onStepChange = { newStep ->
                                animationState =
                                    CardSwapAnimationState(isAnimating = true, animationStep = newStep)
                            },
                            onAnimationComplete = {
                                animationState = CardSwapAnimationState()
                            },
                            onReorderCards = { cardList = reorderCards(cardList) }
                        )
                    }
                )
            }
        }
    }
}

// Простая функция перестановки карт
private fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

/**
 * Управляет переходами между шагами анимации перетасовки.
 * Только нижняя карта (index 0) инициирует смену шагов.
 */
private fun handleAnimationStepComplete(
    step: CardSwapAnimationStep,
    cardIndex: Int,
    onStepChange: (CardSwapAnimationStep) -> Unit,
    onAnimationComplete: () -> Unit,
    onReorderCards: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            CardSwapAnimationStep.MOVE_AWAY -> onStepChange(CardSwapAnimationStep.RETURN_BACK)
            CardSwapAnimationStep.RETURN_BACK -> {
                onReorderCards()
                onStepChange(CardSwapAnimationStep.SETTLE_ROTATION)
            }
            CardSwapAnimationStep.SETTLE_ROTATION -> onAnimationComplete()
            CardSwapAnimationStep.NONE -> Unit
        }
    }
}
