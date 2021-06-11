package moe.tlaster.zoomable

import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Create a [ZoomableState] that is remembered across compositions.
 *
 * Changes to the provided values for [minScale] and [maxScale] will **not** result
 * in the state being recreated or changed in any way if it has already been created.
 *
 * @param minScale the minimum scale value for [ZoomableState.minScale]
 * @param maxScale the maximum scale value for [ZoomableState.maxScale]
 */
@Composable
fun rememberZoomableState(
    @FloatRange(from = 0.0) minScale: Float = 1f,
    @FloatRange(from = 0.0) maxScale: Float = Float.MAX_VALUE,
): ZoomableState = rememberSaveable(
    saver = ZoomableState.Saver
) {
    ZoomableState(
        minScale = minScale,
        maxScale = maxScale,
    )
}

/**
 * A state object that can be hoisted to observe scale and translate for [Zoomable].
 *
 * In most cases, this will be created via [rememberZoomableState].
 *
 * @param minScale the minimum scale value for [ZoomableState.minScale]
 * @param maxScale the maximum scale value for [ZoomableState.maxScale]
 * @param initialTranslateX the initial translateX value for [ZoomableState.translateX]
 * @param initialTranslateY the initial translateY value for [ZoomableState.translateY]
 * @param initialScale the initial scale value for [ZoomableState.scale]
 */
@Stable
class ZoomableState(
    @FloatRange(from = 0.0) val minScale: Float = 1f,
    @FloatRange(from = 0.0) val maxScale: Float = Float.MAX_VALUE,
    @FloatRange(from = 0.0) initialTranslateX: Float = 0f,
    @FloatRange(from = 0.0) initialTranslateY: Float = 0f,
    @FloatRange(from = 0.0) initialScale: Float = minScale,
) {
    private val velocityTracker = VelocityTracker()
    private val _translateY = Animatable(initialTranslateY)
    private val _translateX = Animatable(initialTranslateX)
    private val _scale = Animatable(initialScale)

    init {
        require(minScale < maxScale) { "minScale must be < maxScale" }
    }

    /**
     * The current scale value for [Zoomable]
     */
    @get:FloatRange(from = 0.0)
    val scale: Float
        get() = _scale.value

    /**
     * The current translateY value for [Zoomable]
     */
    @get:FloatRange(from = 0.0)
    val translateY: Float
        get() = _translateY.value

    /**
     * The current translateX value for [Zoomable]
     */
    @get:FloatRange(from = 0.0)
    val translateX: Float
        get() = _translateX.value

    internal val zooming: Boolean
        get() = scale > minScale && scale < maxScale

    /**
     * Instantly sets scale of [Zoomable] to given [scale]
     */
    suspend fun snapScaleTo(scale: Float) = coroutineScope {
        _scale.snapTo(scale.coerceIn(minimumValue = minScale, maximumValue = maxScale))
    }

    /**
     * Animates scale of [Zoomable] to given [scale]
     */
    suspend fun animateScaleTo(
        scale: Float,
        animationSpec: AnimationSpec<Float> = spring(),
        initialVelocity: Float = 0f,
    ) = coroutineScope {
        _scale.animateTo(
            targetValue = scale.coerceIn(minimumValue = minScale, maximumValue = maxScale),
            animationSpec = animationSpec,
            initialVelocity = initialVelocity,
        )
    }

    private suspend fun fling(velocity: Offset) = coroutineScope {
        launch {
            _translateY.animateDecay(
                velocity.y / 2f,
                exponentialDecay()
            )
        }
        launch {
            _translateX.animateDecay(
                velocity.x / 2f,
                exponentialDecay()
            )
        }
    }

    internal suspend fun drag(dragDistance: Offset) = coroutineScope {
        launch {
            _translateY.snapTo((_translateY.value + dragDistance.y))
        }
        launch {
            _translateX.snapTo((_translateX.value + dragDistance.x))
        }
    }

    internal suspend fun dragEnd() {
        val velocity = velocityTracker.calculateVelocity()
        fling(Offset(velocity.x, velocity.y))
    }

    internal suspend fun updateBounds(maxX: Float, maxY: Float) = coroutineScope {
        _translateY.updateBounds(-maxY, maxY)
        _translateX.updateBounds(-maxX, maxX)
    }

    internal suspend fun onZoomChange(zoomChange: Float) = snapScaleTo(scale * zoomChange)

    internal fun addPosition(timeMillis: Long, position: Offset) {
        velocityTracker.addPosition(timeMillis = timeMillis, position = position)
    }

    internal fun resetTracking() {
        velocityTracker.resetTracking()
    }

    override fun toString(): String = "ZoomableState(" +
        "minScale=$minScale, " +
        "maxScale=$maxScale, " +
        "translateY=$translateY" +
        "translateX=$translateX" +
        "scale=$scale" +
        ")"

    companion object {
        /**
         * The default [Saver] implementation for [ZoomableState].
         */
        val Saver: Saver<ZoomableState, *> = listSaver(
            save = {
                listOf(
                    it.translateX,
                    it.translateY,
                    it.scale,
                    it.minScale,
                    it.maxScale,
                )
            },
            restore = {
                ZoomableState(
                    initialTranslateX = it[0],
                    initialTranslateY = it[1],
                    initialScale = it[2],
                    minScale = it[3],
                    maxScale = it[4],
                )
            }
        )
    }
}
