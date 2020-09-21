/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("CoilImage")
@file:JvmMultifileClass

package dev.chrisbanes.accompanist.coil

import android.graphics.ColorMatrixColorFilter
import androidx.compose.animation.asDisposableClock
import androidx.compose.animation.core.AnimationClockObservable
import androidx.compose.animation.core.FloatPropKey
import androidx.compose.animation.core.createAnimation
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawCanvas
import androidx.compose.ui.graphics.painter.ImagePainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.core.util.Pools
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.ImageRequest

private const val DefaultTransitionDuration = 1000

/**
 * TODO
 */
@Deprecated(
    "Use new CrossfadeImage or crossfade = true",
    ReplaceWith(
        """CoilImage(
    data = data,
    modifier = modifier,
    imageLoader = imageLoader,
    shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
    onRequestCompleted = onRequestCompleted,
    error = error,
    loading = loading
) { result ->
    CrossfadeImage(
        result = result,
        alignment = alignment,
        contentScale = contentScale,
        crossfadeDurationMs = crossfadeDuration
    )
}"""
    )
)
@Composable
fun CoilImageWithCrossfade(
    data: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = DefaultTransitionDuration,
    imageLoader: ImageLoader = Coil.imageLoader(ContextAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: RequestResult, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda,
    error: @Composable ((ErrorResult) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null
) {
    CoilImage(
        data = data,
        modifier = modifier,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        error = error,
        loading = loading
    ) { result ->
        CrossfadeImage(
            result = result,
            alignment = alignment,
            contentScale = contentScale,
            crossfadeDurationMs = crossfadeDuration
        )
    }
}

/**
 * TODO
 */
@Deprecated(
    "Use new CrossfadeImage or crossfade = true",
    ReplaceWith(
        """CoilImage(
    request = request,
    modifier = modifier,
    imageLoader = imageLoader,
    shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
    onRequestCompleted = onRequestCompleted,
    error = error,
    loading = loading
) { result ->
    CrossfadeImage(
        result = result,
        alignment = alignment,
        contentScale = contentScale,
        crossfadeDurationMs = crossfadeDuration
    )
}"""
    )
)
@Composable
fun CoilImageWithCrossfade(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = DefaultTransitionDuration,
    imageLoader: ImageLoader = Coil.imageLoader(ContextAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: RequestResult, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda,
    error: @Composable ((ErrorResult) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null
) {
    CoilImage(
        request = request,
        loading = loading,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        modifier = modifier,
        onRequestCompleted = onRequestCompleted,
        error = error,
    ) { result ->
        CrossfadeImage(
            result = result,
            alignment = alignment,
            contentScale = contentScale,
            crossfadeDurationMs = crossfadeDuration
        )
    }
}

/**
 * TODO
 *
 * @param skipFadeWhenLoadedFromMemory Whether the fade animation should be skipped when the result
 * has been loaded from memory.
 * @param crossfadeDurationMs The duration of the crossfade animation in milliseconds.
 */
@Composable
fun CrossfadeImage(
    asset: ImageAsset,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    clock: AnimationClockObservable = AnimationClockAmbient.current.asDisposableClock(),
    crossfadeEnabled: Boolean = true,
    crossfadeDurationMs: Int = DefaultTransitionDuration
) {
    // Default painter for the image
    val imagePainter = remember(asset) { ImagePainter(asset) }

    val painter = if (crossfadeEnabled) {
        val observablePainter = remember(asset) {
            ObservableCrossfadeImagePainter(asset, crossfadeDurationMs, clock).also { it.start() }
        }
        when {
            // If the animation is running, return it as the painter
            !observablePainter.isFinished -> observablePainter
            // If the animation has finished, revert back to the default painter
            else -> imagePainter
        }
    } else {
        // If the fade is disabled, just use the standard ImagePainter
        imagePainter
    }

    Image(
        painter = painter,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        modifier = modifier,
    )
}

/**
 * TODO
 *
 * @param skipFadeWhenLoadedFromMemory Whether the fade animation should be skipped when the result
 * has been loaded from memory.
 * @param crossfadeDurationMs The duration of the crossfade animation in milliseconds.
 */
@Composable
fun CrossfadeImage(
    result: SuccessResult,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    clock: AnimationClockObservable = AnimationClockAmbient.current.asDisposableClock(),
    skipFadeWhenLoadedFromMemory: Boolean = true,
    crossfadeEnabled: Boolean = true,
    crossfadeDurationMs: Int = DefaultTransitionDuration
) {
    CrossfadeImage(
        asset = result.image,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        modifier = modifier,
        crossfadeEnabled = crossfadeEnabled && !(skipFadeWhenLoadedFromMemory && result.isFromMemory()),
        crossfadeDurationMs = crossfadeDurationMs,
        clock = clock,
    )
}

private class ObservableCrossfadeImagePainter(
    private val image: ImageAsset,
    duration: Int,
    clock: AnimationClockObservable,
    private val srcOffset: IntOffset = IntOffset.Zero,
    private val srcSize: IntSize = IntSize(image.width, image.height)
) : Painter() {
    var isFinished by mutableStateOf(false)
        private set

    // Initial matrix is completely transparent. We use the NeverEqual equivalence check since this
    // is a mutable entity.
    private var matrix by mutableStateOf(
        value = ImageLoadingColorMatrix(0f, 0f, 0f),
        policy = neverEqualPolicy()
    )

    private val animation = CrossfadeTransition.definition(duration).createAnimation(clock)

    init {
        animation.onUpdate = {
            // Update the matrix state value with the new animated properties. This works since
            // we're using the NeverEqual equivalence check
            matrix = matrix.apply {
                saturationFraction = animation[CrossfadeTransition.Saturation]
                alphaFraction = animation[CrossfadeTransition.Alpha]
                brightnessFraction = animation[CrossfadeTransition.Brightness]
            }
        }

        animation.onStateChangeFinished = { state ->
            if (state == CrossfadeTransition.State.Loaded) {
                isFinished = true
            }
        }
    }

    override fun DrawScope.onDraw() {
        val paint = paintPool.acquire() ?: Paint()

        try {
            paint.asFrameworkPaint().colorFilter = ColorMatrixColorFilter(matrix)

            drawCanvas { canvas, _ ->
                canvas.drawImageRect(image, srcOffset, srcSize, IntOffset.Zero, srcSize, paint)
            }
        } finally {
            // Reset the Paint instance and release it back to the pool
            paint.asFrameworkPaint().reset()
            paintPool.release(paint)
        }
    }

    /**
     * Return the dimension of the underlying [ImageAsset] as its intrinsic width and height
     */
    override val intrinsicSize: Size get() = srcSize.toSize()

    fun start() {
        // Start the animation by transitioning to the Loaded state
        animation.toState(CrossfadeTransition.State.Loaded)
    }
}

/**
 * A pool which allows us to cache and re-use [Paint] instances, which are relatively expensive
 * to create.
 */
private val paintPool = Pools.SimplePool<Paint>(2)

private object CrossfadeTransition {
    enum class State {
        Loaded, Empty
    }

    val Alpha = FloatPropKey()
    val Brightness = FloatPropKey()
    val Saturation = FloatPropKey()

    fun definition(durationMs: Int) = transitionDefinition<State> {
        state(State.Empty) {
            this[Alpha] = 0f
            this[Brightness] = 0.8f
            this[Saturation] = 0f
        }
        state(State.Loaded) {
            this[Alpha] = 1f
            this[Brightness] = 1f
            this[Saturation] = 1f
        }

        transition {
            // Alpha animates over the first 50%
            Alpha using tween(durationMillis = durationMs / 2)
            // Brightness animates over the first 75%
            Brightness using tween(durationMillis = durationMs * 3 / 4)
            // Saturation animates over whole duration
            Saturation using tween(durationMillis = durationMs)
        }
    }
}

private fun SuccessResult.isFromMemory(): Boolean {
    return source == DataSource.MEMORY || source == DataSource.MEMORY_CACHE
}
