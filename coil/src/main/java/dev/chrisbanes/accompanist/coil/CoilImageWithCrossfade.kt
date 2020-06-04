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

import androidx.animation.AnimationClockObservable
import androidx.animation.FloatPropKey
import androidx.animation.TransitionAnimation
import androidx.animation.createAnimation
import androidx.animation.transitionDefinition
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.mutableStateOf
import androidx.compose.remember
import androidx.compose.setValue
import androidx.ui.animation.asDisposableClock
import androidx.ui.core.Alignment
import androidx.ui.core.AnimationClockAmbient
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.painter.Painter
import coil.Coil
import coil.decode.DataSource
import coil.request.GetRequest
import coil.request.GetRequestBuilder
import kotlin.math.roundToInt

private const val defaultTransitionDuration = 1000

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image], using a crossfade when first loaded.
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param data The data to load. See [GetRequestBuilder.data] for the types allowed.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 */
@Composable
fun CoilImageWithCrossfade(
    data: Any,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = defaultTransitionDuration,
    clock: AnimationClockObservable = AnimationClockAmbient.current.asDisposableClock(),
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda,
    getFailurePainter: @Composable (ErrorResult) -> Painter? = { defaultFailurePainterGetter(it) },
    modifier: Modifier = Modifier
) {
    CoilImageWithCrossfade(
        request = GetRequest.Builder(ContextAmbient.current).data(data).build(),
        alignment = alignment,
        contentScale = contentScale,
        crossfadeDuration = crossfadeDuration,
        clock = clock,
        getFailurePainter = getFailurePainter,
        onRequestCompleted = onRequestCompleted,
        modifier = modifier
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
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 */
@Composable
fun CoilImageWithCrossfade(
    request: GetRequest,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = defaultTransitionDuration,
    clock: AnimationClockObservable = AnimationClockAmbient.current.asDisposableClock(),
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda,
    getFailurePainter: @Composable (ErrorResult) -> Painter? = { defaultFailurePainterGetter(it) },
    modifier: Modifier = Modifier
) {
    CoilImage(
        request = request,
        alignment = alignment,
        contentScale = contentScale,
        getSuccessPainter = { result ->
            when (result.source) {
                DataSource.MEMORY, DataSource.MEMORY_CACHE -> {
                    // If the image was loaded from memory, or the memory cache, we do not
                    // need to run another animation on it
                    defaultSuccessPainterGetter(result)
                }
                else -> {
                    val observablePainter = remember {
                        ObservableCrossfade(result, crossfadeDuration, clock).also { it.start() }
                    }
                    observablePainter.painter
                }
            }
        },
        getFailurePainter = getFailurePainter,
        onRequestCompleted = onRequestCompleted,
        modifier = modifier
    )
}

@Suppress("JoinDeclarationAndAssignment")
private class ObservableCrossfade(
    result: SuccessResult,
    crossfadeDuration: Int,
    clock: AnimationClockObservable
) {
    var painter: Painter by mutableStateOf(
        createCrossfadePainter(
            image = result.image,
            saturation = 0f,
            alpha = 0f,
            brightness = 0f
        )
    )

    private val animation: TransitionAnimation<CrossfadeTransition.State>

    init {
        animation = CrossfadeTransition.definition(crossfadeDuration).createAnimation(clock)

        animation.onUpdate = {
            // Animation tick, so update the painter using the current transition property values
            painter = createCrossfadePainter(
                image = result.image,
                saturation = animation[CrossfadeTransition.Saturation],
                alpha = animation[CrossfadeTransition.Alpha],
                brightness = animation[CrossfadeTransition.Brightness]
            )
        }

        animation.onStateChangeFinished = {
            if (it == CrossfadeTransition.State.Loaded) {
                // Once the transition has finished, we revert back to the default painter
                painter = defaultSuccessPainterGetter(result)
            }
        }
    }

    fun start() {
        // Start the animation by transitioning to the Loaded state
        animation.toState(CrossfadeTransition.State.Loaded)
    }
}

private object CrossfadeTransition {
    internal enum class State {
        Loaded, Empty
    }

    internal val Alpha = FloatPropKey()
    internal val Brightness = FloatPropKey()
    internal val Saturation = FloatPropKey()

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
                duration = durationMs / 2
            }
            Brightness using tween<Float> {
                duration = (durationMs * 0.75f).roundToInt()
            }
            Saturation using tween<Float> {
                duration = durationMs
            }
        }
    }
}

private fun createCrossfadePainter(
    image: ImageAsset,
    saturation: Float = 1f,
    alpha: Float = 1f,
    brightness: Float = 1f
): Painter {
    // Create and update the ImageLoadingColorMatrix from the transition state
    val matrix = ImageLoadingColorMatrix()
    matrix.saturationFraction = saturation
    matrix.alphaFraction = alpha
    matrix.brightnessFraction = brightness
    return ColorMatrixImagePainter(image, colorMatrix = matrix)
}
