package ru.yandexpraktikum.cardsanimation.model

import androidx.compose.runtime.Immutable

enum class CardSwapAnimationStep {
    NONE,
    MOVE_AWAY,
    RETURN_BACK,
    SETTLE_ROTATION
}

@Immutable
data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: CardSwapAnimationStep = CardSwapAnimationStep.NONE
)
