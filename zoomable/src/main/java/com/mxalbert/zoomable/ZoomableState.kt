package com.mxalbert.zoomable

import androidx.annotation.FloatRange
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Create a [ZoomableState] that is remembered across compositions.
 *
 * @param minScale The minimum [ZoomableState.scale] value.
 * @param maxScale The maximum [ZoomableState.scale] value.
 * @param doubleTapScale The [ZoomableState.scale] Value to animate to when a double tap happens.
 * @param overZoomConfig The [OverZoomConfig] to use or null to disable over-zoom effect.
 * @param initialScale The initial value for [ZoomableState.scale].
 * @param initialTranslationX The initial value for [ZoomableState.translationX].
 * @param initialTranslationY The initial value for [ZoomableState.translationY].
 */
@Composable
fun rememberZoomableState(
    @FloatRange(from = 0.0) minScale: Float = ZoomableDefaults.MinScale,
    @FloatRange(from = 0.0) maxScale: Float = ZoomableDefaults.MaxScale,
    @FloatRange(from = 0.0) doubleTapScale: Float = ZoomableDefaults.DoubleTapScale,
    overZoomConfig: OverZoomConfig? = null,
    @FloatRange(from = 0.0) initialScale: Float = minScale,
    @FloatRange(from = 0.0) initialTranslationX: Float = 0f,
    @FloatRange(from = 0.0) initialTranslationY: Float = 0f
): ZoomableState {
    return rememberSaveable(saver = ZoomableState.Saver) {
        ZoomableState(initialScale, initialTranslationX, initialTranslationY)
    }.apply {
        this.minScale = minScale
        this.maxScale = maxScale
        this.doubleTapScale = doubleTapScale
        this.overZoomConfig = overZoomConfig
    }
}

/**
 * A state object that can be hoisted to observe scale and translate for [Zoomable].
 *
 * @param initialScale The initial value for [scale].
 * @param initialTranslationX The initial value for [translationX].
 * @param initialTranslationY The initial value for [translationY].
 * @see rememberZoomableState
 */
@Stable
class ZoomableState(
    @FloatRange(from = 0.0) initialScale: Float = ZoomableDefaults.MinScale,
    @FloatRange(from = 0.0) initialTranslationX: Float = 0f,
    @FloatRange(from = 0.0) initialTranslationY: Float = 0f
) {
    /**
     * The minimum [scale] value.
     */
    @FloatRange(from = 0.0)
    var minScale: Float = ZoomableDefaults.MinScale
        set(value) {
            if (field != value) {
                field = value
                scale = scale  // Make sure scale is in range
            }
        }

    /**
     * The maximum [scale] value.
     */
    @FloatRange(from = 0.0)
    var maxScale: Float = ZoomableDefaults.MaxScale
        set(value) {
            if (field != value) {
                field = value
                scale = scale  // Make sure scale is in range
            }
        }

    var overZoomConfig: OverZoomConfig? by mutableStateOf(null)

    /**
     * The [scale] value to animate to when a double tap happens.
     */
    @FloatRange(from = 0.0)
    var doubleTapScale: Float = ZoomableDefaults.DoubleTapScale

    /**
     * Current progress of the dismiss drag ranging from 0.0 to 1.0.
     * Useful e.g. if you want to animate the alpha of the content.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    val dismissDragProgress: Float
        get() = if (size.height == 0) 0f else
            abs(dismissDragAbsoluteOffsetY) / (size.height * DismissDragThreshold)

    private val velocityTracker = VelocityTracker()
    private var _scale by mutableStateOf(initialScale)
    private var _translationX = Animatable(initialTranslationX)
    private var _translationY = Animatable(initialTranslationY)
    private var _childSize by mutableStateOf(Size.Zero)

    internal var boundOffset by mutableStateOf(IntOffset.Zero)
        private set

    internal var dismissDragAbsoluteOffsetY by mutableStateOf(0f)
        private set

    internal val dismissDragOffsetY: Float
        get() {
            val maxOffset = childSize.height
            return if (maxOffset == 0f) 0f else {
                val progress = (dismissDragAbsoluteOffsetY / maxOffset).coerceIn(-1f, 1f)
                childSize.height / DismissDragResistanceFactor * sin(progress * PI.toFloat() / 2)
            }
        }

    internal val shouldDismiss: Boolean
        get() = abs(dismissDragAbsoluteOffsetY) > size.height * DismissDragThreshold

    internal var size = IntSize.Zero
        set(value) {
            if (field != value) {
                field = value
                updateBounds()
            }
        }

    internal var childSize: Size
        get() = _childSize
        set(value) {
            if (_childSize != value) {
                _childSize = value
                updateBounds()
            }
        }

    /**
     * Current scale of [Zoomable].
     */
    @get:FloatRange(from = 0.0)
    var scale: Float
        get() = _scale
        private set(value) {
            _scale = value.coerceIn(minimumValue = minScale, maximumValue = maxScale)
            updateBounds()
        }

    /**
     * Current translationX of [Zoomable].
     */
    @get:FloatRange(from = 0.0)
    val translationX: Float
        get() = _translationX.value

    /**
     * Current translationY of [Zoomable].
     */
    @get:FloatRange(from = 0.0)
    val translationY: Float
        get() = _translationY.value

    internal val minSnapScale: Float
        get() = max(minScale, overZoomConfig?.minSnapScale ?: 0f)

    val isZooming: Boolean
        get() = scale > minSnapScale && scale <= maxScale

    private var flingJob: Job? = null
    internal var isGestureInProgress: Boolean by mutableStateOf(false)
        private set

    private fun updateBounds() {
        val offsetX = childSize.width * scale - size.width
        val offsetY = childSize.height * scale - size.height
        boundOffset = IntOffset((offsetX / 2f).roundToInt(), (offsetY / 2f).roundToInt())
        val maxX = offsetX.coerceAtLeast(0f) / 2f
        val maxY = offsetY.coerceAtLeast(0f) / 2f
        _translationX.updateBounds(-maxX, maxX)
        _translationY.updateBounds(-maxY, maxY)
    }

    internal fun calculateTargetTranslation(centroid: Offset): Offset =
        (size.toSize().center + Offset(translationX, translationY) - centroid) / scale

    /**
     * Animate [scale] to [targetScale].
     *
     * @param targetScale The [scale] value to animate to.
     * @param targetTranslation The [translationX] and [translationY] value to animate to. Use the
     * default value to maintain current center point. Use [Offset.Unspecified] to leave
     * translation unchanged.
     * @param animationSpec [AnimationSpec] to be used for this scaling.
     */
    suspend fun animateScaleTo(
        targetScale: Float,
        targetTranslation: Offset = Offset(translationX, translationY) / scale * targetScale,
        animationSpec: AnimationSpec<Float> = spring()
    ) = coroutineScope {
        val initialTranslation = Offset(translationX, translationY)
        val initialScale = scale
        val range = targetScale - initialScale
        animate(
            initialValue = initialScale,
            targetValue = targetScale,
            animationSpec = animationSpec
        ) { value, _ ->
            launch {
                // Update scale here to ensure scale and translation values are updated
                // in the same snapshot
                scale = value
                if (targetTranslation != Offset.Unspecified) {
                    val fraction = if (range == 0f) 1f else (value - initialScale) / range
                    val translation = lerp(initialTranslation, targetTranslation, fraction)
                    _translationX.snapTo(translation.x)
                    _translationY.snapTo(translation.y)
                }
            }
        }
    }

    /**
     * Animate [translationX] and [translationY] to [targetTranslation].
     *
     * @param targetTranslation The [translationX] and [translationY] value to animate to.
     * @param animationSpec [AnimationSpec] to be used for this scaling.
     */
    suspend fun animateTranslateTo(
        targetTranslation: Offset,
        animationSpec: AnimationSpec<Offset> = spring()
    ) = coroutineScope {
        animate(
            typeConverter = Offset.VectorConverter,
            initialValue = Offset(translationX, translationY),
            targetValue = targetTranslation,
            animationSpec = animationSpec
        ) { value, _ ->
            launch {
                _translationX.snapTo(value.x)
                _translationY.snapTo(value.y)
            }
        }
    }

    private suspend fun fling(velocity: Velocity) {
        coroutineScope {
            flingJob = coroutineContext[Job]
            val spec = exponentialDecay<Float>()
            launch {
                _translationX.animateDecay(initialVelocity = velocity.x, animationSpec = spec)
            }
            launch {
                _translationY.animateDecay(initialVelocity = velocity.y, animationSpec = spec)
            }
        }

        isGestureInProgress = false
        flingJob = null
    }

    internal fun onGestureStart() {
        flingJob?.cancel()
        isGestureInProgress = true
    }

    internal suspend fun onTransform(centroid: Offset, pan: Offset, zoom: Float) {
        var targetTranslation = calculateTargetTranslation(centroid - pan)
        scale *= zoom
        targetTranslation = targetTranslation * scale - size.toSize().center + centroid
        _translationX.snapTo(targetTranslation.x)
        _translationY.snapTo(targetTranslation.y)
    }

    internal fun onTransformEnd() {
        isGestureInProgress = false
    }

    internal suspend fun onDrag(dragAmount: Offset) {
        _translationX.snapTo(_translationX.value + dragAmount.x)
        _translationY.snapTo(_translationY.value + dragAmount.y)
    }

    internal suspend fun onDragEnd() {
        val velocity = velocityTracker.calculateVelocity()
        velocityTracker.resetTracking()
        fling(velocity)
    }

    internal fun onZoomChange(zoomChange: Float) {
        scale *= zoomChange
    }

    internal fun addPosition(timeMillis: Long, position: Offset) {
        velocityTracker.addPosition(timeMillis = timeMillis, position = position)
    }

    internal fun resetTracking() {
        velocityTracker.resetTracking()
    }

    internal fun onDismissDrag(dragAmountY: Float) {
        dismissDragAbsoluteOffsetY += dragAmountY
    }

    internal suspend fun onDismissDragEnd() {
        animate(
            initialValue = dismissDragAbsoluteOffsetY,
            targetValue = 0f
        ) { value, _ ->
            dismissDragAbsoluteOffsetY = value
        }
    }

    override fun toString(): String =
        "ZoomableState(translateX=%.1f,translateY=%.1f,scale=%.2f)".format(
            translationX, translationY, scale
        )

    companion object {
        /**
         * The default [Saver] implementation for [ZoomableState].
         */
        val Saver: Saver<ZoomableState, *> = listSaver(
            save = {
                listOf(
                    it.translationX,
                    it.translationY,
                    it.scale
                )
            },
            restore = {
                ZoomableState(
                    initialTranslationX = it[0],
                    initialTranslationY = it[1],
                    initialScale = it[2]
                )
            }
        )
    }
}

internal const val DismissDragResistanceFactor = 2f
internal const val DismissDragThreshold = 0.25f

object ZoomableDefaults {
    /**
     * The default value for [ZoomableState.minScale].
     */
    const val MinScale = 1f

    /**
     * The default value for [ZoomableState.maxScale].
     */
    const val MaxScale = 4f

    /**
     * The default value for [ZoomableState.doubleTapScale].
     */
    const val DoubleTapScale = 2f
}

/**
 * Configuration for over-zoom effect.
 *
 * @property minSnapScale The minimum [ZoomableState.scale] value to snap to after a zoom gesture
 * finishes.
 * @property maxSnapScale The maximum [ZoomableState.scale] value to snap to after a zoom gesture
 * finishes.
 */
@Immutable
class OverZoomConfig(
    @FloatRange(from = 0.0) val minSnapScale: Float,
    @FloatRange(from = 0.0) val maxSnapScale: Float
) {
    operator fun contains(scale: Float): Boolean = scale in minSnapScale..maxSnapScale

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OverZoomConfig

        if (minSnapScale != other.minSnapScale) return false
        if (maxSnapScale != other.maxSnapScale) return false

        return true
    }

    override fun hashCode(): Int {
        var result = minSnapScale.hashCode()
        result = 31 * result + maxSnapScale.hashCode()
        return result
    }
}

internal val OverZoomConfig.range: ClosedFloatingPointRange<Float>
    get() = minSnapScale..maxSnapScale
