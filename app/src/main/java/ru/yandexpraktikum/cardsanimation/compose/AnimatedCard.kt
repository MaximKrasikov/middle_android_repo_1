package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationStep
import kotlin.math.cos
import kotlin.math.sin

private const val ROTATION_DEFAULT_DURATION_MS = 800
private const val ROTATION_SETTLE_DURATION_MS = 300
private const val TRANSLATION_DURATION_MS = 300

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    animationState: CardSwapAnimationState = CardSwapAnimationState(),
    onAnimationStepComplete: (CardSwapAnimationStep) -> Unit = {}
) {
    val density = LocalDensity.current
    val isAnimating = animationState.isAnimating
    val animationStep = animationState.animationStep
    val isBottomCard = cardIndex == 0
    val shouldBringToFront =
        isAnimating && animationStep == CardSwapAnimationStep.RETURN_BACK && isBottomCard

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(
            durationMillis = if (animationStep == CardSwapAnimationStep.SETTLE_ROTATION) {
                ROTATION_SETTLE_DURATION_MS
            } else {
                ROTATION_DEFAULT_DURATION_MS
            }
        ),
        finishedListener = {
            if (animationStep == CardSwapAnimationStep.SETTLE_ROTATION && isAnimating && cardIndex == 0) {
                onAnimationStepComplete(CardSwapAnimationStep.SETTLE_ROTATION)
            }
        },
        label = "rotation"
    )

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == CardSwapAnimationStep.MOVE_AWAY && isBottomCard -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * cos(rotationRad).toFloat()
            }
            isAnimating && animationStep == CardSwapAnimationStep.RETURN_BACK && isBottomCard -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = TRANSLATION_DURATION_MS),
        finishedListener = {
            if (isAnimating && isBottomCard) {
                when (animationStep) {
                    CardSwapAnimationStep.MOVE_AWAY -> onAnimationStepComplete(CardSwapAnimationStep.MOVE_AWAY)
                    CardSwapAnimationStep.RETURN_BACK -> onAnimationStepComplete(CardSwapAnimationStep.RETURN_BACK)
                    CardSwapAnimationStep.SETTLE_ROTATION,
                    CardSwapAnimationStep.NONE -> Unit
                }
            }
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == CardSwapAnimationStep.MOVE_AWAY && isBottomCard -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * sin(rotationRad).toFloat()
            }
            isAnimating && animationStep == CardSwapAnimationStep.RETURN_BACK && isBottomCard -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = TRANSLATION_DURATION_MS),
        label = "translationY"
    )

    val cardModifier = Modifier
        .size(width = 100.dp, height = 160.dp)
        .graphicsLayer {
            translationX = if (isAnimating && animationStep != CardSwapAnimationStep.SETTLE_ROTATION) {
                animatedTranslationX
            } else {
                0f
            }
            translationY = if (isAnimating && animationStep != CardSwapAnimationStep.SETTLE_ROTATION) {
                animatedTranslationY
            } else {
                0f
            }
            rotationZ = animatedRotation
            transformOrigin = TransformOrigin(0.5f, 1.0f)
        }
        .then(if (shouldBringToFront) Modifier.zIndex(1000f) else Modifier)

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}
