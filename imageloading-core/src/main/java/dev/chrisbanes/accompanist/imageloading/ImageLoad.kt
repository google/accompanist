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

package dev.chrisbanes.accompanist.imageloading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

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
fun <R : Any, TR : Any> ImageLoad(
    request: R,
    executeRequest: suspend (TR) -> ImageLoadState,
    modifier: Modifier = Modifier,
    requestKey: Any = request,
    transformRequestForSize: (R, IntSize) -> TR?,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    val imageMgr = remember(requestKey) {
        ImageManager(
            request = request,
            executeRequest = executeRequest,
            transformRequestForSize = transformRequestForSize,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
            onRequestCompleted = onRequestCompleted,
        )
    }

    // NOTE: All of the things that we want to be able to change without recreating the whole object
    // we want to do inside of here.
    SideEffect {
        imageMgr.transformRequestForSize = transformRequestForSize
        imageMgr.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
        imageMgr.onRequestCompleted = onRequestCompleted
    }

    // NOTE: It's important that this is Box and not BoxWithConstraints. This is dramatically cheaper,
    // and also has not children. You could use Box(modifier, content) here if you want, and add a
    // content lambda, but that would be for content inside / on top of the image, and not for the
    // image itself like the current implementation.
    Box(modifier.then(imageMgr.modifier)) {
        if (imageMgr.loadState !is ImageLoadState.Success) {
            content(imageMgr.loadState)
        }
    }
}

// This class holds all of the state for the image and manages the request.
private class ImageManager<R : Any, TR : Any>(
    val request: R,
    val executeRequest: suspend (TR) -> ImageLoadState,
    var transformRequestForSize: (R, IntSize) -> TR?,
    var shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    var onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
) : RememberObserver {

    // the size of the image, as informed by the layout system and the request.
    //
    // This value will be read during:
    //   COMPOSITION: NO
    //   LAYOUT:      YES
    //   DRAW:        NO
    private var requestSize by mutableStateOf<IntSize?>(null)

    // The actual image state, populated by the image request.
    //
    // This value will be read during:
    //   COMPOSITION: NO
    //   LAYOUT:      NO
    //   DRAW:        YES
    internal var loadState by mutableStateOf<ImageLoadState>(ImageLoadState.Loading)

    private var scope = CoroutineScope(Job())

    private val layout = object : LayoutModifier {
        override fun MeasureScope.measure(
            measurable: Measurable,
            constraints: Constraints
        ): MeasureResult {
            // NOTE: this is where the interesting logic is, but there shouldn't be anything here that
            // you can't do that you're doing using BoxWithConstraints currently.
            val size = IntSize(
                width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
                height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
            )
            if (requestSize == null ||
                (requestSize != size && shouldRefetchOnSizeChange(loadState, size))
            ) {
                requestSize = size
            }

            val placeable = measurable.measure(constraints)
            return layout(
                size.width,
                size.height
            ) {
                placeable.place(0, 0)
            }
        }
    }

    // NOTE: We build a modifier once, for each ImageManager, which handles everything. We ensure that
    // no state objects are used in its construction, so that all state observations are limited to
    // the layout and drawing phases.
    val modifier: Modifier = Modifier.composed {
        // NOTE: i'm not quite sure if it's smarter to put the layout modifier at the top of the
        // chain (before paint) or the bottom of it (after paint).
        layout
            // NOTE: since we aren't using `Image` anymore, it is important that we handle semantics properly
//        .semantics {
//            contentDescription = contentDescription
//            role = Role.Image
//        }
            // NOTE: not sure how important this is, but `Image` has it
            .clipToBounds()
            .paint(
                painter = loadState.getPainterOrNull() ?: ColorPainter(Color.Transparent),
                // NOTE: You should probably pipe some of these values through
//            alignment = alignment,
//            contentScale = contentScale,
            )
    }

    // NOTE: both onAbandoned and onForgotten are where we should cancel any image requests and dispose
    // of things
    override fun onAbandoned() {
        scope.cancel()
    }

    override fun onForgotten() {
        scope.cancel()
    }

    override fun onRemembered() {
        // you can use a coroutine scope that is scoped to the composable, or something more custom like
        // the imageLoader or whatever. The main point is, here is where you would start your loading
        scope.launch {
            loadState = requestSize
                ?.let { transformRequestForSize(request, it) }
                ?.let { transformedRequest ->
                    try {
                        executeRequest(transformedRequest)
                    } catch (ce: CancellationException) {
                        // We specifically don't do anything for the request coroutine being
                        // cancelled: https://github.com/chrisbanes/accompanist/issues/217
                        throw ce
                    } catch (throwable: Throwable) {
                        ImageLoadState.Error(painter = null, throwable = throwable)
                    }.also(onRequestCompleted)
                } ?: ImageLoadState.Loading
        }
    }
}

/**
 * Empty lamdba for use in the `onRequestCompleted` parameter.
 */
val EmptyRequestCompleteLambda: (ImageLoadState) -> Unit = {}

/**
 * Default lamdba for use in the `shouldRefetchOnSizeChange` parameter.
 */
val DefaultRefetchOnSizeChangeLambda: (ImageLoadState, IntSize) -> Boolean = { _, _ -> false }
