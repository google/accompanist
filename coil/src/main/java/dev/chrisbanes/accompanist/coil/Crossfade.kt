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

package dev.chrisbanes.accompanist.coil

import android.graphics.ColorMatrixColorFilter
import androidx.compose.animation.asDisposableClock
import androidx.compose.animation.core.AnimationClockObservable
import androidx.compose.animation.core.FloatPropKey
import androidx.compose.animation.core.createAnimation
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.core.util.Pools
import coil.Coil
import coil.decode.DataSource
import coil.request.ImageRequest

private const val DefaultTransitionDuration = 1000

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [androidx.compose.foundation.Image], using a crossfade
 * when first loaded.
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param data The data to load. See [ImageRequest.Builder.data] for the types allowed.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param getFailurePainter Optional builder for the [Painter] to be used to draw the failure
 * loading result. Passing in `null` will result in falling back to the default [Painter].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImageWithCrossfade(
    data: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = DefaultTransitionDuration,
    getFailurePainter: @Composable ((ErrorResult) -> Painter?)? = null,
    loading: @Composable (() -> Unit)? = null,
    shouldRefetchOnSizeChange: (currentResult: RequestResult, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda
) {
    CoilImage(
        data = data,
        alignment = alignment,
        contentScale = contentScale,
        getSuccessPainter = { crossfadePainter(it, durationMs = crossfadeDuration) },
        getFailurePainter = getFailurePainter,
        loading = loading,
        modifier = modifier,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted
    )
}

/**
 * Creates a composable that will attempt to load the given [request] using [Coil], and then
 * display the result in an [androidx.compose.foundation.Image], using a crossfade
 * animation when first loaded.
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param request The request to execute. If the request does not have a [ImageRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param getFailurePainter Optional builder for the [Painter] to be used to draw the failure
 * loading result. Passing in `null` will result in falling back to the default [Painter].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImageWithCrossfade(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = DefaultTransitionDuration,
    getFailurePainter: @Composable ((ErrorResult) -> Painter?)? = null,
    loading: @Composable (() -> Unit)? = null,
    shouldRefetchOnSizeChange: (currentResult: RequestResult, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda
) {
    CoilImage(
        request = request,
        alignment = alignment,
        contentScale = contentScale,
        getSuccessPainter = { crossfadePainter(it, durationMs = crossfadeDuration) },
        getFailurePainter = getFailurePainter,
        loading = loading,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        modifier = modifier,
        onRequestCompleted = onRequestCompleted
    )
}

/**
 * A composable function which runs an fade animation on the given [result], returning the
 * [Painter] which should be used to paint the [ImageAsset].
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param result The result of a image fetch.
 * @param skipFadeWhenLoadedFromMemory Whether the fade animation should be skipped when the result
 * has been loaded from memory.
 * @param durationMs The duration of the crossfade animation in milliseconds.
 * @param clock The animation clock.
 */
@Composable
private fun crossfadePainter(
    result: SuccessResult,
    skipFadeWhenLoadedFromMemory: Boolean = true,
    durationMs: Int = DefaultTransitionDuration,
    clock: AnimationClockObservable = AnimationClockAmbient.current.asDisposableClock()
): Painter {
    return if (skipFadeWhenLoadedFromMemory && result.isFromMemory()) {
        // If can skip the fade when loaded from memory, we do not need to run an animation on it
        defaultSuccessPainterGetter(result)
    } else {
        val observablePainter = remember {
            ObservableCrossfadeImagePainter(result.image, durationMs, clock).also { it.start() }
        }
        when {
            // If the animation is running, return it as the painter
            !observablePainter.isFinished -> observablePainter
            // If the animation has finished, revert back to the default painter
            else -> defaultSuccessPainterGetter(result)
        }
    }
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
        paint.asFrameworkPaint().colorFilter = ColorMatrixColorFilter(matrix)

        drawCanvas { canvas, _ ->
            canvas.drawImageRect(image, srcOffset, srcSize, IntOffset.Zero, srcSize, paint)
        }

        // Reset the Paint instance and release it back to the pool
        paint.asFrameworkPaint().reset()
        paintPool.release(paint)
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
