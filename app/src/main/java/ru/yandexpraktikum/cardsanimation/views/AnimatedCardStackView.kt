package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.determineSwipeDirection
import ru.yandexpraktikum.cardsanimation.model.handleDragEnd
import ru.yandexpraktikum.cardsanimation.model.handleFling
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

    /** true — анимация перетасовки выполняется, жесты заблокированы */
    private var isAnimating = false

    /** Текущий шаг многошаговой анимации перетасовки (1, 2, 3) */
    private var animationStep = 0

    /** Накопленное смещение пальца за время жеста (onScroll) */
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    /** Накопленное вертикальное смещение для обработки свайпов вверх/вниз */
    private var verticalDragOffset = 0f

    /** Накопленное горизонтальное смещение для обработки свайпов влево/вправо */
    private var horizontalDragOffset = 0f

    /** true, если направление уже определено через onFling */
    private var flingHandled = false

    init {
        isClickable = true
    }

    /**
     * Детектор жестов: onScroll накапливает смещение,
     * onFling обрабатывает быстрые свайпы.
     */
    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (isAnimating) return false
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
                if (isAnimating) return false
                flingHandled = true
                val direction = determineSwipeDirection(velocityX, velocityY)
                logSwipeDirection("fling", direction, velocityX, velocityY)
                handleFling(
                    velocityX = velocityX,
                    velocityY = velocityY,
                    onExpand = { setRotated(true) },
                    onCollapse = { setRotated(false) },
                    onSwapCards = { handleHorizontalSwipe() }
                )
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (isAnimating) return false
                isRotated = !isRotated
                updateCardPositions(animate = true)
                return true
            }
        }
    )

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
        isRotated = false
        updateCardPositions()
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    /**
     * Обновляет позицию и поворот каждой карты.
     * @param animate true — плавная анимация поворота, false — мгновенная установка
     */
    private fun updateCardPositions(animate: Boolean = false) {
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1)
                22.5f - (index * angleStep)
            } else {
                0f
            }

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

            if (animate) {
                cardView.animateToRotation(targetRotation)
            } else {
                cardView.rotation = targetRotation
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed && !isAnimating) {
            updateCardPositions()
        }
    }

    /**
     * Запускает полную 3-шаговую анимацию перетасовки карт.
     */
    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        if (isAnimating) return

        isAnimating = true
        animationStep = 1

        bottomCard.moveCardRight {
            animationStep = 2
            bringCardToFront(bottomCard)
            bottomCard.moveCardToTop {
                animationStep = 3
                reorderCardsData()
                animateAllCardsToFinalPositions()
            }
        }
    }

    /** Обновляет данные и порядок view без пересоздания карточек */
    private fun reorderCardsData() {
        val reorderedCards = cardDataList.drop(1) + cardDataList.first()
        cardDataList = reorderedCards

        val bottomCardView = cards.removeAt(0)
        cards.add(bottomCardView)

        cards.forEachIndexed { index, cardView ->
            cardView.setCardData(cardDataList[index])
        }
    }

    /** Синхронно поворачивает все карты в финальные позиции */
    private fun animateAllCardsToFinalPositions() {
        var completedAnimations = 0
        val totalAnimations = cards.size

        cards.forEachIndexed { index, cardView ->
            val finalRotation = calculateFinalRotation(index)

            cardView.adjustToFinalPosition(finalRotation, index) {
                completedAnimations++
                if (completedAnimations == totalAnimations) {
                    finalizeCardPositions()
                }
            }
        }
    }

    /** Вычисляет финальный угол поворота карты с учётом состояния веера */
    private fun calculateFinalRotation(cardIndex: Int): Float {
        val cardCount = cards.size
        return if (isRotated) {
            val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
            90f - (cardIndex * angleStep)
        } else {
            val angleStep = if (cardCount > 1) 45f / (cardCount - 1) else 0f
            22.5f - (cardIndex * angleStep)
        }
    }

    /** Сбрасывает позиции и завершает анимацию перетасовки */
    private fun finalizeCardPositions() {
        val cardWidth = 100f * resources.displayMetrics.density
        val cardHeight = 160f * resources.displayMetrics.density
        val sharedX = width / 2f - cardWidth / 2f
        val sharedY = height / 2f - cardHeight / 2f

        cards.forEachIndexed { index, card ->
            card.setStackPosition(index)
            card.x = sharedX
            card.y = sharedY
            card.rotation = calculateFinalRotation(index)
        }

        isAnimating = false
        animationStep = 0
    }

    /** Поднимает карту над остальными через z-порядок и повышенный elevation */
    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val maxElevation = (4 + cards.size + 20).toFloat() * resources.displayMetrics.density
        card.cardView.cardElevation = maxElevation
    }

    /** Запускает перестановку карт по горизонтальному свайпу */
    private fun handleHorizontalSwipe() {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard)
    }

    /**
     * Обрабатывает завершение медленного жеста с учётом блокировки во время анимации.
     */
    private fun onDragGestureEnd() {
        if (isAnimating) return

        val direction = determineSwipeDirection(dragOffsetX, dragOffsetY)
        logSwipeDirection("drag end", direction, dragOffsetX, dragOffsetY)

        handleDragEnd(
            horizontalDragOffset = horizontalDragOffset,
            verticalDragOffset = verticalDragOffset,
            onFanStateChange = { newFanState -> setRotated(newFanState) },
            onCardsReorder = { handleHorizontalSwipe() }
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAnimating && event.action != MotionEvent.ACTION_DOWN) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isAnimating) return true
                dragOffsetX = 0f
                dragOffsetY = 0f
                verticalDragOffset = 0f
                horizontalDragOffset = 0f
                flingHandled = false
            }
            MotionEvent.ACTION_UP -> {
                if (!flingHandled) {
                    onDragGestureEnd()
                }
                dragOffsetX = 0f
                dragOffsetY = 0f
                verticalDragOffset = 0f
                horizontalDragOffset = 0f
                flingHandled = false
            }
        }
        gestureDetector.onTouchEvent(event)
        return true
    }

    /** Устанавливает состояние раскрытия колоды и запускает анимацию поворота карт */
    private fun setRotated(rotated: Boolean) {
        if (isRotated == rotated) return
        isRotated = rotated
        updateCardPositions(animate = true)
    }
}
