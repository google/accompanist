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

@file:JvmName("ImageLoad")
@file:JvmMultifileClass

package com.google.accompanist.imageloading

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * TODO
 */
@Stable
abstract class ImageLoadRequest<R : Any> {
    /**
     * This should be [androidx.compose.runtime.State] backed.
     */
    abstract var request: R?

    /**
     * The current [ImageLoadState].
     */
    var loadState by mutableStateOf<ImageLoadState>(ImageLoadState.Loading)
        private set

    /**
     * The function which executes the requests, and update [loadState] as appropriate with the
     * result.
     */
    internal suspend fun execute(request: R, size: IntSize) {
        loadState = ImageLoadState.Loading

        loadState = try {
            executeRequest(request, size)
        } catch (ce: CancellationException) {
            // We specifically don't do anything for the request coroutine being
            // cancelled: https://github.com/chrisbanes/accompanist/issues/217
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
}

/**
 * A generic image loading composable, which provides hooks for image loading libraries to use.
 * Apps shouldn't generally use this function, instead preferring one of the extension libraries
 * which build upon this, such as the Coil library.
 *
 * The [executeRequest] parameters allows providing of a lambda to execute the 'image load'.
 * The [R] type and [request] parameter should be whatever primitive the library uses to
 * model a request. The [TR] type would normally be the same as [R], but allows transforming of
 * the request for execution (say to wrap with extra information).
 *
 * @param request The request to execute.
 * @param executeRequest Suspending lambda to execute an image loading request.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param requestKey The object to key this request on. If the request type supports equality then
 * the default value will work. Otherwise pass in the `data` value.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param transformRequestForSize Optionally transform [request] for the given [IntSize].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun <R : Any> ImageLoad(
    request: ImageLoadRequest<R>,
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

    val imageLoader = remember(request, coroutineScope) {
        ImageLoader(
            requestState = request,
            coroutineScope = coroutineScope,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )
    }

    val cf = if (fadeIn && request.loadState is ImageLoadState.Success) {
        val fadeInTransition = updateFadeInTransition(
            key = imageLoader.requestState,
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

    // NOTE: All of the things that we want to be able to change without recreating the whole object
    // we want to do inside of here.
    SideEffect {
        imageLoader.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
        imageLoader.contentScale = contentScale
        imageLoader.colorFilter = cf
        imageLoader.alignment = alignment
    }

    // NOTE: It's important that this is Box and not BoxWithConstraints. This is dramatically cheaper,
    // and also has not children. You could use Box(modifier, content) here if you want, and add a
    // content lambda, but that would be for content inside / on top of the image, and not for the
    // image itself like the current implementation.

    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else Modifier

    // NOTE: We build a modifier once, for each ImageManager, which handles everything. We
    // ensure that no state objects are used in its construction, so that all state
    // observations are limited to the layout and drawing phases.
    Box(
        modifier
            .then(semantics)
            .clipToBounds()
            .then(imageLoader.paintModifier)
            .then(imageLoader.layoutModifier)
    )
}

/**
 * @hide
 */
@Deprecated("Only used to help migration. DO NOT USE.")
@Composable
fun <R : Any> ImageLoadSuchDeprecated(
    request: ImageLoadRequest<R>,
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
            requestState = request,
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
    val requestState: ImageLoadRequest<R>,
    val coroutineScope: CoroutineScope,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean,
) : RememberObserver {

    // the size of the image, as informed by the layout system and the request.
    //
    // This value will be read during layout
    private var requestSize by mutableStateOf<IntSize?>(null)

    var colorFilter by mutableStateOf<ColorFilter?>(null)
    var contentScale by mutableStateOf(ContentScale.Fit)
    var alignment by mutableStateOf(Alignment.Center)
    var shouldRefetchOnSizeChange by mutableStateOf(shouldRefetchOnSizeChange)

    // Current request job
    private var job: Job? = null

    val layoutModifier = Modifier.layout { measurable, constraints ->
        // NOTE: this is where the interesting logic is, but there shouldn't be anything here that
        // you can't do that you're doing using BoxWithConstraints currently.
        val newSize = IntSize(
            width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
        )

        if (requestSize == null ||
            (requestSize != newSize && shouldRefetchOnSizeChange(requestState.loadState, newSize))
        ) {
            requestSize = newSize
        }

        val placeable = measurable.measure(constraints)
        layout(width = placeable.width, height = placeable.height) {
            placeable.place(0, 0)
        }
    }

    val paintModifier = object : PainterModifier() {
        override val painter: Painter by derivedStateOf {
            requestState.loadState.let { state ->
                if (state is ImageLoadState.Success) {
                    state.painter
                } else EmptyPainter
            }
        }

        override val alignment: Alignment
            get() = this@ImageLoader.alignment

        override val contentScale: ContentScale
            get() = this@ImageLoader.contentScale

        override val colorFilter: ColorFilter?
            get() = this@ImageLoader.colorFilter

        override val alpha: Float
            get() = 1f

        override val sizeToIntrinsics: Boolean
            get() = true
    }

    override fun onAbandoned() {
        job?.cancel()
        job = null
    }

    override fun onForgotten() {
        job?.cancel()
        job = null
    }

    override fun onRemembered() {
        job?.cancel()
        job = coroutineScope.launch {
            combine(
                snapshotFlow { requestState.request }.filterNotNull(),
                snapshotFlow { requestSize }.filterNotNull(),
                transform = { request, size -> request to size }
            ).collectLatest { (request, size) ->
                requestState.execute(request, size)
            }
        }
    }

    companion object {
        private val EmptyPainter = ColorPainter(Color.Transparent)
    }
}
