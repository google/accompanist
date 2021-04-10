/*
 * Copyright 2021 The Android Open Source Project
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

package com.google.accompanist.imageloading

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A generic image loading painter, which provides hooks for image loading libraries to use.
 * Apps shouldn't generally use this function, instead preferring one of the extension libraries
 * which build upon this, such as the Coil and Glide libraries.
 *
 * @param state The request to execute.
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded.
 * Default: `false`.
 * @param fadeInDurationMs Duration for the fade animation in milliseconds when [fadeIn] is enabled.
 * @param previewPlaceholder Drawable resource ID which will be displayed when this function is
 * ran in preview mode.
 */
@Composable
fun <R : Any> rememberLoadPainter(
    state: ImageState<R>,
    fadeIn: Boolean = false,
    fadeInDurationMs: Int = DefaultTransitionDuration,
    @DrawableRes previewPlaceholder: Int = 0,
): Painter {
    if (LocalInspectionMode.current && previewPlaceholder != 0) {
        // If we're in inspection mode (preview) and we have a preview placeholder, just draw
        // that using an Image and return
        return painterResource(previewPlaceholder)
    }

    val coroutineScope = rememberCoroutineScope()

    val imageLoader = remember(state, coroutineScope) {
        ImageLoader(state, coroutineScope)
    }

    // This runs our fade in animation
    val fadeInColorFilter = fadeInAsState(
        imageState = state,
        enabled = { result ->
            // We run the fade in animation if the result is loaded from disk/network. This allows
            // us to approximate only running the animation on 'first load'
            fadeIn && result is ImageLoadState.Success && result.source != DataSource.MEMORY
        },
        durationMs = fadeInDurationMs
    )

    // Our result painter, created from the ImageState with some composition lifecycle
    // callbacks
    val resultPainter = state.painterAsState()

    // Our painter. We use a DelegatingPainter which delegates the actual drawn/laid-out painter
    // to our result painter state. That block will be called during layout and drawing,
    // which means that any changes to `state.loadState` will automatically re-trigger layout/draw.
    return remember(state) {
        LoadPainter(
            painter = { resultPainter.value },
            transitionColorFilter = { fadeInColorFilter.value },
            imageLoader = imageLoader,
        )
    }
}

private class LoadPainter<R : Any>(
    private val painter: () -> Painter,
    private val transitionColorFilter: () -> ColorFilter?,
    private val imageLoader: ImageLoader<R>,
) : Painter() {
    private val paint by lazy(LazyThreadSafetyMode.NONE) { Paint() }

    private var alpha: Float = 1f
    private var colorFilter: ColorFilter? = null

    override val intrinsicSize: Size
        get() = painter().intrinsicSize

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        // Update the request size, based on the provided canvas size
        imageLoader.requestSize = IntSize(
            width = if (size.width >= 0.5f) size.width.roundToInt() else -1,
            height = if (size.height >= 0.5f) size.width.roundToInt() else -1,
        )

        val transitionColorFilter = transitionColorFilter()

        if (colorFilter != null && transitionColorFilter != null) {
            // If we have a transition color filter, and a specified color filter we need to
            // draw the content in a layer for both to apply.
            // See https://github.com/google/accompanist/issues/262
            drawIntoCanvas { canvas ->
                paint.colorFilter = transitionColorFilter
                canvas.saveLayer(bounds = size.toRect(), paint = paint)
                with(painter()) {
                    draw(size, alpha, colorFilter)
                }
                canvas.restore()
            }
        } else {
            // Otherwise we just draw the content directly, using the filter
            with(painter()) {
                draw(size, alpha, colorFilter ?: transitionColorFilter)
            }
        }
    }
}

/**
 * Allows us observe the current result [Painter] as state. This function allows us to
 * minimize the amount of composition needed, such that only this function needs to be restarted
 * when the `loadState` changes.
 */
@Composable
private fun ImageState<*>.painterAsState(): State<Painter> {
    val painter = loadState.drawable
        ?.let { rememberDrawablePainter(it) }
        ?: EmptyPainter
    return rememberUpdatedState(painter)
}

@Composable
private fun fadeInAsState(
    imageState: ImageState<*>,
    enabled: (ImageLoadState) -> Boolean,
    durationMs: Int,
): State<ColorFilter?> {
    val colorFilter = remember(imageState.internalRequest) { mutableStateOf<ColorFilter?>(null) }

    val loadState = imageState.loadState
    if (enabled(loadState)) {
        val colorMatrix = remember { ColorMatrix() }
        val fadeInTransition = updateFadeInTransition(loadState, durationMs)

        colorFilter.value = if (!fadeInTransition.isFinished) {
            colorMatrix.apply {
                updateAlpha(fadeInTransition.alpha)
                updateBrightness(fadeInTransition.brightness)
                updateSaturation(fadeInTransition.saturation)
            }.let { ColorFilter.colorMatrix(it) }
        } else {
            // If the fade-in isn't running, reset the color matrix
            null
        }
    } else {
        // If the fade in is not enabled, we don't use a fade in transition
        colorFilter.value = null
    }

    return colorFilter
}

@Stable
private class ImageLoader<R : Any>(
    val state: ImageState<R>,
    val coroutineScope: CoroutineScope,
) : RememberObserver {
    // Our size to use when performing the image load request
    var requestSize by mutableStateOf<IntSize?>(null)

    // Current request job
    private var job: Job? = null

    // Our layout modifier, which allows us to receive the incoming constraints to update
    // requestSize. Using a modifier allows us to avoid using BoxWithConstraints and the cost of
    // subcomposition. For most usages subcomposition is fine, but Image's tend to be used
    // in large quantities which multiplies the cost.

    override fun onAbandoned() {
        // We've been abandoned from composition, so cancel our request handling coroutine
        job?.cancel()
        job = null
    }

    override fun onForgotten() {
        // We've been forgotten from composition, so cancel our request handling coroutine
        job?.cancel()
        job = null
    }

    override fun onRemembered() {
        // Cancel any on-going job (this shouldn't really happen anyway)
        job?.cancel()

        // We've been remembered, so launch a coroutine to observe the current request object,
        // and the request size. Whenever either of these values change, the collectLatest block
        // will run and execute the image load (with any on-going request cancelled).
        job = coroutineScope.launch {
            combine(
                state.internalRequestFlow,
                snapshotFlow { requestSize }.filterNotNull(),
                transform = { request, size -> request to size }
            ).collectLatest { (request, size) ->
                state.execute(request, size)
            }
        }
    }
}

/**
 * @hide
 */
@Deprecated("Only used to help migration. DO NOT USE.")
@Composable
fun <R : Any> ImageSuchDeprecated(
    state: ImageState<R>,
    modifier: Modifier,
    @DrawableRes previewPlaceholder: Int = 0,
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    if (LocalInspectionMode.current && previewPlaceholder != 0) {
        // If we're in inspection mode (preview) and we have a preview placeholder, just draw
        // that using an Image and return
        Image(
            painter = painterResource(previewPlaceholder),
            contentDescription = null,
            modifier = modifier,
        )
        return
    }

    val coroutineScope = rememberCoroutineScope()

    val imageLoader = remember(state, coroutineScope) {
        ImageLoader(state, coroutineScope)
    }

    Box(
        propagateMinConstraints = true,
        modifier = modifier
            // Layout modifier to receive the incoming constraints, such that we can use them
            // to update our request size
            .layout { measurable, constraints ->
                // Update our request size. The observing flow below checks shouldRefetchOnSizeChange
                imageLoader.requestSize = IntSize(
                    width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
                    height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
                )

                // No-op measure + layout
                val placeable = measurable.measure(constraints)
                layout(width = placeable.width, height = placeable.height) {
                    placeable.place(0, 0)
                }
            }
    ) {
        content(state.loadState)
    }
}

private object EmptyPainter : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified
    override fun DrawScope.onDraw() {}
}
