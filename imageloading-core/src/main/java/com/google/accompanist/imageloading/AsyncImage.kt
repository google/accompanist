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

@file:JvmName("AsyncImage")

package com.google.accompanist.imageloading

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * A state base class that can be hoisted to control image loads for [AsyncImage].
 */
@Stable
abstract class AsyncImageState<R : Any> {
    /**
     * The current request object.
     *
     * Extending classes should return the current request object, which should be backed by
     * a [androidx.compose.runtime.State] instance.
     */
    protected abstract val request: R?

    /**
     * The current [ImageLoadState].
     */
    var loadState by mutableStateOf<ImageLoadState>(ImageLoadState.Empty)
        private set

    /**
     * The function which executes the requests, and update [loadState] as appropriate with the
     * result.
     */
    internal suspend fun execute(request: R?, size: IntSize) {
        if (request == null) {
            // If we don't have a request, set our state to Empty and return
            loadState = ImageLoadState.Empty
            return
        }

        // Otherwise we're about to start a request, so set us to 'Loading'
        loadState = ImageLoadState.Loading

        loadState = try {
            executeRequest(request, size)
        } catch (ce: CancellationException) {
            // We specifically don't do anything for the request coroutine being
            // cancelled: https://github.com/google/accompanist/issues/217
            throw ce
        } catch (e: Error) {
            // Re-throw all Errors
            throw e
        } catch (e: IllegalStateException) {
            // Re-throw all IllegalStateExceptions
            throw e
        } catch (e: IllegalArgumentException) {
            // Re-throw all IllegalArgumentExceptions
            throw e
        } catch (t: Throwable) {
            // Anything else, we wrap in a Error state instance
            ImageLoadState.Error(painter = null, throwable = t)
        }
    }

    /**
     * Extending classes should implement this function to execute the [request] with the given
     * [size] constraints.
     */
    protected abstract suspend fun executeRequest(request: R, size: IntSize): ImageLoadState

    internal val requestFlow: Flow<R?>
        get() = snapshotFlow { request }
}

/**
 * A generic image loading composable, which provides hooks for image loading libraries to use.
 * Apps shouldn't generally use this function, instead preferring one of the extension libraries
 * which build upon this, such as the Coil and Glide libraries.
 *
 * @param state The request to execute.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageBitmap] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageBitmap].
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen.
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded.
 * Default: `false`.
 * @param fadeInDurationMs Duration for the fade animation in milliseconds when [fadeIn] is enabled.
 * @param previewPlaceholder Drawable resource ID which will be displayed when this function is
 * ran in preview mode.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 */
@Composable
fun <R : Any> AsyncImage(
    state: AsyncImageState<R>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    fadeInDurationMs: Int = DefaultTransitionDuration,
    @DrawableRes previewPlaceholder: Int = 0,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = { _, _ -> false },
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
        ImageLoader(
            state = state,
            coroutineScope = coroutineScope,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )
    }

    val cf = if (fadeIn && state.loadState is ImageLoadState.Success) {
        val fadeInTransition = updateFadeInTransition(
            key = imageLoader.state,
            durationMs = fadeInDurationMs
        )
        remember { ColorMatrix() }
            .apply {
                updateAlpha(fadeInTransition.alpha)
                updateBrightness(fadeInTransition.brightness)
                updateSaturation(fadeInTransition.saturation)
            }
            .let { matrix ->
                ColorFilter.colorMatrix(matrix)
            }
    } else {
        // If fade in isn't enabled, just use the provided `colorFilter`
        colorFilter
    }

    // Update our ImageLoader with any parameter values changes
    SideEffect {
        imageLoader.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
    }

    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else Modifier

    // We build a modifier once, for each ImageLoader, which handles everything. We
    // ensure that no state objects are used in its construction, so that all state
    // observations are limited to the layout and drawing phases.
    // We do read some values from the function parameters for the paint() modifier, but they are
    // limited to less-commonly modified values (alignment + contentScale).
    Box(
        modifier
            .then(semantics)
            .clipToBounds()
            .paint(
                painter = imageLoader.painter,
                alignment = alignment,
                contentScale = contentScale,
                colorFilter = cf,
            )
            .then(imageLoader.layoutModifier)
    )
}

/**
 * @hide
 */
@Deprecated("Only used to help migration. DO NOT USE.")
@Composable
fun <R : Any> AsyncImageSuchDeprecated(
    request: AsyncImageState<R>,
    modifier: Modifier,
    @DrawableRes previewPlaceholder: Int = 0,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean,
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

    val imageLoader = remember(request, coroutineScope) {
        ImageLoader(
            state = request,
            coroutineScope = coroutineScope,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )
    }

    // NOTE: All of the things that we want to be able to change without recreating the whole object
    // we want to do inside of here.
    SideEffect {
        imageLoader.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
    }

    Box(
        propagateMinConstraints = true,
        modifier = modifier.then(imageLoader.layoutModifier)
    ) {
        content(request.loadState)
    }
}

@Stable
private class ImageLoader<R : Any>(
    val state: AsyncImageState<R>,
    val coroutineScope: CoroutineScope,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean,
) : RememberObserver {
    // Our size to use when performing the image load request
    var requestSize by mutableStateOf<IntSize?>(null)

    // Our updated shouldRefetchOnSizeChange
    var shouldRefetchOnSizeChange by mutableStateOf(shouldRefetchOnSizeChange)

    // Current request job
    private var job: Job? = null

    // Our painter. We use a DelegatingPainter which delegates the actual drawn/laid-out painter
    // to a lambda block. That block will be called during layout and drawing, which means
    // that any changes to `state.loadState` will automatically re-trigger layout/draw.
    // This allows us to use a single Painter instance, and usually a single Modifier.paint().
    val painter: Painter = DelegatingPainter {
        state.loadState.let { state ->
            when (state) {
                is ImageLoadState.Success -> state.painter
                is ImageLoadState.Error -> state.painter ?: EmptyPainter
                else -> EmptyPainter
            }
        }
    }

    // Our layout modifier, which allows us to receive the incoming constraints to update
    // requestSize. Using a modifier allows us to avoid using BoxWithConstraints and the cost of
    // subcomposition. For most usages subcomposition is fine, but AsyncImage's tend to be used
    // in large quantities which multiplies the cost.
    val layoutModifier = Modifier.layout { measurable, constraints ->
        val newSize = IntSize(
            width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
        )

        if (requestSize == null ||
            (
                requestSize != newSize &&
                    this@ImageLoader.shouldRefetchOnSizeChange(state.loadState, newSize)
                )
        ) {
            requestSize = newSize
        }

        // No-op measure + layout
        val placeable = measurable.measure(constraints)
        layout(width = placeable.width, height = placeable.height) {
            placeable.place(0, 0)
        }
    }

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
                state.requestFlow,
                snapshotFlow { requestSize }.filterNotNull(),
                transform = { request, size -> request to size }
            ).collectLatest { (request, size) ->
                state.execute(request, size)
            }
        }
    }

    companion object {
        private val EmptyPainter = ColorPainter(Color.Transparent)
    }
}
