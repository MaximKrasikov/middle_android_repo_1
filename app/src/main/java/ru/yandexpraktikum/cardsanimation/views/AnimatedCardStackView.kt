package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.SWIPE_GESTURE_THRESHOLD
import ru.yandexpraktikum.cardsanimation.model.determineSwipeDirection
import ru.yandexpraktikum.cardsanimation.model.handleDragEnd
import ru.yandexpraktikum.cardsanimation.model.handleFling
import ru.yandexpraktikum.cardsanimation.model.handleVerticalSwipe
import ru.yandexpraktikum.cardsanimation.model.logSwipeDirection
import kotlin.math.abs

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false

    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    private var verticalDragOffset = 0f
    private var horizontalDragOffset = 0f

    private var isAnimating = false
    private var animationStep = 0


    fun setCards(newCardDataList: List<CardData>) {
        cardDataList = newCardDataList
        setupCards()
    }

    private fun setupCards() {
        clearCards()
        cardDataList.forEachIndexed { index, cardData ->
            val cardView = AnimatedCardView(context).apply {
                setCardData(cardData)
                setStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        // Возврат в исходное положение
        isRotated = false
        updateCardPositions()
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    private fun updateCardPositions(animate: Boolean = false) {
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->
            // Расчёт расположения карт в исходной позиции
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1)
                22.5f - (index * angleStep)
            } else {
                0f
            }

            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val targetRotation = if (isRotated) {
                val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
                90f - (index * angleStep)
            } else {
                baseRotation
            }

            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            // TODO: [Задание 1] Замените на метод, который анимирует движение карты
            if (animate) {
                cardView.animateToRotation(targetRotation)
            } else {
                cardView.rotation = targetRotation
            }
        }
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                isRotated = !isRotated
                updateCardPositions(animate = true)
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                dragOffsetX -= distanceX
                dragOffsetY -= distanceY
                if (abs(distanceY) >= abs(distanceX)) {
                    verticalDragOffset -= distanceY
                } else {
                    horizontalDragOffset -= distanceX
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val direction = determineSwipeDirection(velocityX, velocityY)
                logSwipeDirection("fling", direction, velocityX, velocityY)
                handleFling(
                    velocityX = velocityX,
                    velocityY = velocityY,
                    onExpand = { setRotated(true) },
                    onCollapse = { setRotated(false) },
                )
                return true
            }
        })

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        if (isAnimating) return

        isAnimating = true
        animationStep = 1

        bottomCard.moveCardRight {
            // TODO: Добавить Шаг 2 в следующем задании
            isAnimating = false
            animationStep = 0
        }
    }

    private fun handleHorizontalSwipe() {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragOffsetX = 0f
                dragOffsetY = 0f
                verticalDragOffset = 0f
                horizontalDragOffset = 0f
            }
            MotionEvent.ACTION_UP -> {
                onDragGestureEnd()

                dragOffsetX = 0f
                dragOffsetY = 0f
                verticalDragOffset = 0f
                horizontalDragOffset = 0f
            }
        }

        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun onDragGestureEnd() {

        val direction = determineSwipeDirection(dragOffsetX, dragOffsetY)
        logSwipeDirection("drag end", direction, dragOffsetX, dragOffsetY)

        handleDragEnd(
            horizontalDragOffset = horizontalDragOffset,
            verticalDragOffset = verticalDragOffset,
            onCardsReorder = { handleHorizontalSwipe() },
            onFanStateChange = { newFanState -> setRotated(newFanState) },
        )
    }

    private fun setRotated(rotated: Boolean) {
        if (isRotated == rotated) return
        isRotated = rotated
        updateCardPositions(animate = true)
    }

    // Простая функция перестановки карт
    fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }

    // TODO: [Задание 4] Добавьте обработку горизонтальных свайпов (влево/вправо)
}