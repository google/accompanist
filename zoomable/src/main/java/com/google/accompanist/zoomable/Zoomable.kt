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

@file:JvmName("Zoomable")

package com.google.accompanist.zoomable

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.launch

/**
 * A zoomable layout that can handle zoom in and out with drag support.
 *
 * @sample com.google.accompanist.sample.zoomable.ZoomableSample
 *
 * @param state the state object to be used to control or observe the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@Composable
fun Zoomable(
    state: ZoomableState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = modifier,
    ) {
        var childWidth by remember { mutableStateOf(0) }
        var childHeight by remember { mutableStateOf(0) }
        LaunchedEffect(
            childHeight,
            childWidth,
            state.scale,
        ) {
            val maxX = (childWidth * state.scale - constraints.maxWidth)
                .coerceAtLeast(0F) / 2F
            val maxY = (childHeight * state.scale - constraints.maxHeight)
                .coerceAtLeast(0F) / 2F
            state.updateBounds(maxX, maxY)
        }
        val transformableState = rememberTransformableState { zoomChange, _, _ ->
            state.onZoomChange(zoomChange)
        }
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if (state.zooming) {
                                change.consumePositionChange()
                                scope.launch {
                                    state.drag(dragAmount)
                                    state.addPosition(
                                        change.uptimeMillis,
                                        change.position
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            state.resetTracking()
                        },
                        onDragEnd = {
                            if (state.zooming) {
                                scope.launch {
                                    state.dragEnd()
                                }
                            }
                        },
                    )
                }
                .transformable(state = transformableState)
                .layout { measurable, constraints ->
                    val placeable =
                        measurable.measure(constraints = constraints)
                    childHeight = placeable.height
                    childWidth = placeable.width
                    layout(
                        width = constraints.maxWidth,
                        height = constraints.maxHeight
                    ) {
                        placeable.placeRelativeWithLayer(
                            (constraints.maxWidth - placeable.width) / 2,
                            (constraints.maxHeight - placeable.height) / 2
                        ) {
                            scaleX = state.scale
                            scaleY = state.scale
                            translationX = state.translateX
                            translationY = state.translateY
                        }
                    }
                }
        ) {
            content.invoke(this)
        }
    }
}