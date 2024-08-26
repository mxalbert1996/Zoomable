package com.mxalbert.zoomable

import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZoomableStateTest {

    private val decayAnimationSpec =
        SplineBasedFloatDecayAnimationSpec(Density(1f)).generateDecayAnimationSpec<Float>()

    @Test
    fun `ZoomableState default params`() {
        val state = ZoomableState(decayAnimationSpec)
        assertThat(state.minScale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.minScale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.maxScale).isEqualTo(ZoomableDefaults.MaxScale)
        assertThat(state.doubleTapScale).isEqualTo(ZoomableDefaults.DoubleTapScale)
        assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
    }

    @Test
    fun `ZoomableState constructor params`() {
        val state = ZoomableState(
            decayAnimationSpec,
            initialScale = 2f,
            initialTranslationX = 100f,
            initialTranslationY = 100f
        )
        assertThat(state.scale).isEqualTo(2f)
        assertThat(state.translationX).isEqualTo(100f)
        assertThat(state.translationY).isEqualTo(100f)
    }

    @Test
    fun `Translation not reset after stored`() {
        val state = ZoomableState(
            decayAnimationSpec,
            initialScale = 2f,
            initialTranslationX = 100f,
            initialTranslationY = 100f
        )
        state.size = IntSize(200, 200)
        state.childSize = Size(0f, 0f)
        assertThat(state.translationX).isEqualTo(100f)
        assertThat(state.translationY).isEqualTo(100f)
    }
}
