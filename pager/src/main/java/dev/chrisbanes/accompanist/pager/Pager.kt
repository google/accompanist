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

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")

package dev.chrisbanes.accompanist.pager

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * This is a modified version of:
 * https://gist.github.com/adamp/07d468f4bcfe632670f305ce3734f511
 */

class PagerState(
    currentPage: Int = 0,
    minPage: Int = 0,
    maxPage: Int = 0
) {
    private var _minPage by mutableStateOf(minPage)
    var minPage: Int
        get() = _minPage
        set(value) {
            _minPage = value.coerceAtMost(_maxPage)
            _currentPage = _currentPage.coerceIn(_minPage, _maxPage)
        }

    private var _maxPage by mutableStateOf(maxPage, structuralEqualityPolicy())
    var maxPage: Int
        get() = _maxPage
        set(value) {
            _maxPage = value.coerceAtLeast(_minPage)
            _currentPage = _currentPage.coerceIn(_minPage, maxPage)
        }

    private var _currentPage by mutableStateOf(currentPage.coerceIn(minPage, maxPage))
    var currentPage: Int
        get() = _currentPage
        set(value) {
            _currentPage = value.coerceIn(minPage, maxPage)
        }

    /**
     * TODO kdoc
     */
    enum class SelectionState { Selected, Undecided }

    private var _selectionState by mutableStateOf(SelectionState.Selected)

    /**
     * TODO kdoc
     */
    val selectionState
        get() = _selectionState

    suspend fun <R> selectPage(block: PagerState.() -> R): R = try {
        _selectionState = SelectionState.Undecided
        block()
    } finally {
        selectPage()
    }

    suspend fun selectPage() {
        currentPage -= currentPageOffset.roundToInt()
        snapToOffset(0f)
        _selectionState = SelectionState.Selected
    }

    internal val _currentPageOffset = Animatable(0f).apply {
        updateBounds(-1f, 1f)
    }

    /**
     * TODO kdoc
     */
    val currentPageOffset: Float
        get() = _currentPageOffset.value

    /**
     * TODO make this public?
     */
    private suspend fun snapToOffset(offset: Float) {
        val max = if (currentPage == minPage) 0f else 1f
        val min = if (currentPage == maxPage) 0f else -1f
        _currentPageOffset.snapTo(offset.coerceIn(min, max))

        Log.d("PagerState", "snapToOffset: $offset. $this")
    }

    private suspend fun deltaY(delta: Float) = snapToOffset(_currentPageOffset.value + delta)

    private fun determineSpringBackOffset(): Float {
        val max = if (currentPage == minPage) 0f else 1f
        val min = if (currentPage == maxPage) 0f else -1f
        val offset = when {
            currentPageOffset >= 0.4f -> 1f
            currentPageOffset <= -0.4f -> -1f
            else -> 0f
        }
        return offset.coerceIn(min, max)
    }

    /**
     * TODO make this public?
     */
    private suspend fun fling(velocity: Float, animationSpec: DecayAnimationSpec<Float>) {
        val target = animationSpec.calculateTargetValue(currentPageOffset, velocity)

        // If the animation can naturally end outside of visual bounds, we will
        // animate with decay.
        if (target < -1f || target > 1f) {
            // Animate with the decay animation spec using the fling velocity
            if (velocity < 0 && currentPage == maxPage) return
            if (velocity > 0 && currentPage == minPage) return
            // TODO: need to enforce velocity in percentage rather than pixels
            _currentPageOffset.animateDecay(velocity, animationSpec)
            selectPage()
        } else {
            // Not enough velocity to be dismissed, spring back to 0f
            _currentPageOffset.animateTo(
                targetValue = determineSpringBackOffset(),
                initialVelocity = velocity
            )
            selectPage()
        }
    }

    internal suspend fun PointerInputScope.detectPageTouch(pageSize: Int) {
        val velocityTracker = VelocityTracker()

        coroutineScope {
            while (true) {
                val down = awaitPointerEventScope { awaitFirstDown() }

                // Reset the velocity tracker and add our initial down event
                velocityTracker.resetTracking()
                velocityTracker.addPosition(down)

                awaitPointerEventScope {
                    awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ ->
                        _selectionState = SelectionState.Undecided

                        launch {
                            deltaY(change.positionChange().x / pageSize)
                        }
                        // Add the movement to the velocity tracker
                        velocityTracker.addPosition(change)
                    }
                }

                awaitPointerEventScope {
                    horizontalDrag(down.id) { change ->
                        // Snaps the value by the amount of finger movement
                        launch {
                            deltaY(change.positionChange().x / pageSize)
                        }
                        // Add the movement to the velocity tracker
                        velocityTracker.addPosition(change)
                    }
                }

                // The drag has finished, now calculate the velocity and fling
                val velocity = velocityTracker.calculateVelocity().y

                launch {
                    val decay = splineBasedDecay<Float>(this@detectPageTouch)
                    // Velocity is in pixels per second, but we deal in percentage offsets, so we
                    // need to scale the velocity to match
                    val pageVelocity = velocity / pageSize
                    fling(pageVelocity, decay)
                }
            }
        }
    }

    override fun toString(): String = "PagerState(" +
        "minPage=$minPage, " +
        "maxPage=$maxPage, " +
        "currentPage=$currentPage, " +
        "selectionState=$selectionState, " +
        "currentPageOffset=$currentPageOffset" +
        ")"
}

@Immutable
private data class PageData(val page: Int) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@PageData
}

private val Measurable.page: Int
    get() = (parentData as? PageData)?.page ?: error("No PageData for measurable $this")

@Composable
fun Pager(
    state: PagerState,
    modifier: Modifier = Modifier,
    offscreenLimit: Int = 2,
    pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
    var pageSize by remember { mutableStateOf(0) }
    Layout(
        content = {
            val minPage = (state.currentPage - offscreenLimit).coerceAtLeast(state.minPage)
            val maxPage = (state.currentPage + offscreenLimit).coerceAtMost(state.maxPage)

            for (page in minPage..maxPage) {
                val pageData = PageData(page)
                key(pageData) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = pageData
                    ) {
                        val scope = remember(this, state) {
                            PagerScopeImpl(this, state)
                        }
                        scope.pageContent(page)
                    }
                }
            }
        },
        modifier = modifier.pointerInput(Unit) {
            with(state) {
                detectPageTouch(pageSize = pageSize)
            }
        },
    ) { measurables, constraints ->
        layout(constraints.maxWidth, constraints.maxHeight) {
            val currentPage = state.currentPage
            val offset = state.currentPageOffset
            val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            measurables
                .map { it.measure(childConstraints) to it.page }
                .forEach { (placeable, page) ->
                    // TODO: current this centers each page. We should investigate reading
                    //  gravity modifiers on the child, or maybe as a param to Pager.
                    val xCenterOffset = (constraints.maxWidth - placeable.width) / 2
                    val yCenterOffset = (constraints.maxHeight - placeable.height) / 2

                    if (currentPage == page) {
                        pageSize = placeable.width
                    }

                    val xItemOffset = ((page + offset - currentPage) * placeable.width).roundToInt()

                    placeable.place(
                        x = xCenterOffset + xItemOffset,
                        y = yCenterOffset
                    )
                }
        }
    }
}

/**
 * Scope for [Pager] content.
 */
interface PagerScope : BoxScope {
    /**
     * Returns the current selected page
     */
    val currentPage: Int

    /**
     * Returns the current selected page offset
     */
    val currentPageOffset: Float

    /**
     * Returns the current selection state
     */
    val selectionState: PagerState.SelectionState
}

private class PagerScopeImpl(
    private val boxScope: BoxScope,
    private val state: PagerState,
) : PagerScope, BoxScope by boxScope {
    override val currentPage: Int
        get() = state.currentPage

    override val currentPageOffset: Float
        get() = state.currentPageOffset

    override val selectionState: PagerState.SelectionState
        get() = state.selectionState
}

private fun VelocityTracker.addPosition(change: PointerInputChange) {
    addPosition(change.uptimeMillis, change.position)
}
