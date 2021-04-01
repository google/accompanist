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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
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

    internal val internalRequestFlow: Flow<R?> get() = snapshotFlow { request }
    internal val internalRequest get() = request
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
 * @param colorFilter Optional [ColorFilter] to apply for the [Painter] when it is rendered onscreen.
 * If you provide a color filter and enable [fadeIn], the content will be drawn in a off-screen
 * layer whilst the animation is running, which is known is cause performance issues.
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
            initialShouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )
    }.apply {
        this.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
    }

    // This runs our fade in animation
    val fadeInColorFilter by fadeInAsState(
        imageState = state,
        enabled = fadeIn,
        durationMs = fadeInDurationMs
    )

    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else Modifier

    // We only use the saveLayer path IF a ColorFilter has been provided to AsyncImage.
    // A layer is needed for both the specified filter, and our fade in filter to be applied,
    // but this is slow.
    // Otherwise we return null, and rely on setting the color matrix on the paint below.
    val fadeInLayerDrawModifier = ColorFilterLayerDrawModifier {
        if (colorFilter != null) fadeInColorFilter else null
    }

    // Our paint modifier. We need to use a sub-class of PainterModifier to optimize our
    // animating color filter, so that our instance performs state reads rather than needing to be
    // recreated
    val paintModifier = object : PainterModifier() {
        override val painter: Painter get() = imageLoader.painter
        override val colorFilter: ColorFilter? get() = colorFilter ?: fadeInColorFilter
        override val alignment: Alignment get() = alignment
        override val contentScale: ContentScale get() = contentScale
    }

    // We build a modifier once, for each ImageLoader, which handles everything. We
    // ensure that no state objects are used in its construction, so that all state
    // observations are limited to the layout and drawing phases.
    // We do read some values from the function parameters for the paint() modifier, but they are
    // limited to less-commonly modified values (alignment + contentScale).
    Box(
        modifier
            .then(semantics)
            .then(fadeInLayerDrawModifier)
            .clipToBounds()
            .then(paintModifier)
            .then(imageLoader.layoutModifier)
    )
}

@Composable
private fun fadeInAsState(
    imageState: AsyncImageState<*>,
    enabled: Boolean,
    durationMs: Int,
): State<ColorFilter?> {
    val colorFilter = remember(imageState.internalRequest) { mutableStateOf<ColorFilter?>(null) }

    if (enabled && imageState.loadState is ImageLoadState.Success) {
        val colorMatrix = remember { ColorMatrix() }
        val fadeInTransition = updateFadeInTransition(imageState, durationMs = durationMs)

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
    }

    return colorFilter
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
            initialShouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )
    }.apply {
        this.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
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
    initialShouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean,
) : RememberObserver {
    // Our size to use when performing the image load request
    var requestSize by mutableStateOf<IntSize?>(null)

    // Our updated shouldRefetchOnSizeChange
    var shouldRefetchOnSizeChange by mutableStateOf(initialShouldRefetchOnSizeChange)

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
        // Update our request size. The observing flow below checks shouldRefetchOnSizeChange
        requestSize = IntSize(
            width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
        )

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
                state.internalRequestFlow,
                snapshotFlow { requestSize }
                    .filterNotNull()
                    // We use filterSubsequent() so that the first emitted size skips the predicate
                    .filterSubsequent { shouldRefetchOnSizeChange(state.loadState, it) },
                transform = { request, size -> request to size }
            ).collectLatest { (request, size) ->
                state.execute(request, size)
            }
        }
    }
}

private object EmptyPainter : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified
    override fun DrawScope.onDraw() {}
}

/**
 * A variant of [kotlinx.coroutines.flow.Flow.filter] which always emits the first value,
 * then uses [predicate] as expected for subsequent emissions.
 */
private fun <T> Flow<T>.filterSubsequent(
    predicate: suspend (T) -> Boolean
): Flow<T> = flow {
    var hasEmitted = false
    collect { value ->
        // Emit the value if this is the first value, or predicate returns true
        if (!hasEmitted || predicate(value)) {
            hasEmitted = true
            return@collect emit(value)
        }
    }
}
