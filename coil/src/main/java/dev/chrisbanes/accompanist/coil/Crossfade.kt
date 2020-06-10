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
import androidx.animation.AnimationClockObservable
import androidx.animation.FloatPropKey
import androidx.animation.createAnimation
import androidx.animation.transitionDefinition
import androidx.compose.Composable
import androidx.compose.NeverEqual
import androidx.compose.getValue
import androidx.compose.mutableStateOf
import androidx.compose.remember
import androidx.compose.setValue
import androidx.core.util.Pools
import androidx.ui.animation.asDisposableClock
import androidx.ui.core.Alignment
import androidx.ui.core.AnimationClockAmbient
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.geometry.Offset
import androidx.ui.geometry.Size
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.Paint
import androidx.ui.graphics.drawscope.DrawScope
import androidx.ui.graphics.drawscope.drawCanvas
import androidx.ui.graphics.painter.Painter
import coil.Coil
import coil.decode.DataSource
import coil.request.GetRequest
import coil.request.GetRequestBuilder
import kotlin.math.roundToInt

private const val DefaultTransitionDuration = 1000

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image], using a crossfade when first loaded.
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param data The data to load. See [GetRequestBuilder.data] for the types allowed.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param getFailurePainter Optional builder for the [Painter] to be used to draw the failure
 * loading result. Passing in `null` will result in falling back to the default [Painter].
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
        onRequestCompleted = onRequestCompleted
    )
}

/**
 * Creates a composable that will attempt to load the given [request] using [Coil], and then
 * display the result in an [Image], using a crossfade animation when first loaded.
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param request The request to execute. If the request does not have a [GetRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param getFailurePainter Optional builder for the [Painter] to be used to draw the failure
 * loading result. Passing in `null` will result in falling back to the default [Painter].
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImageWithCrossfade(
    request: GetRequest,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = DefaultTransitionDuration,
    getFailurePainter: @Composable ((ErrorResult) -> Painter?)? = null,
    loading: @Composable (() -> Unit)? = null,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda
) {
    CoilImage(
        request = request,
        alignment = alignment,
        contentScale = contentScale,
        getSuccessPainter = { crossfadePainter(it, durationMs = crossfadeDuration) },
        getFailurePainter = getFailurePainter,
        loading = loading,
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
    private val srcOffset: Offset = Offset.Zero,
    private val srcSize: Size = Size(image.width.toFloat(), image.height.toFloat())
) : Painter() {
    var isFinished by mutableStateOf(false)
        private set

    // Initial matrix is completely transparent. We use the NeverEqual equivalence check since this
    // is a mutable entity.
    private var matrix by mutableStateOf(ImageLoadingColorMatrix(0f, 0f, 0f), NeverEqual)

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
            canvas.drawImageRect(image, srcOffset, srcSize, Offset.Zero, size, paint)
        }

        // Reset the Paint instance and release it back to the pool
        paint.asFrameworkPaint().reset()
        paintPool.release(paint)
    }

    /**
     * Return the dimension of the underlying [Image] as its intrinsic width and height
     */
    override val intrinsicSize: Size get() = srcSize

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

    fun definition(durationMs: Int) = transitionDefinition {
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
            Alpha using tween<Float> {
                // Alpha animation runs over the first 50%
                duration = durationMs / 2
            }
            Brightness using tween<Float> {
                // Brightness animation runs over the first 75%
                duration = (durationMs * 0.75f).roundToInt()
            }
            Saturation using tween<Float> {
                duration = durationMs
            }
        }
    }
}

private fun SuccessResult.isFromMemory(): Boolean {
    return source == DataSource.MEMORY || source == DataSource.MEMORY_CACHE
}
