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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.awaitFirstDown
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
import kotlin.math.floor
import kotlin.math.roundToInt

private const val ScrollThreshold = 0.4f

/**
 * TODO
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
            currentPage = currentPage.coerceIn(_minPage, _maxPage)
        }

    private var _maxPage by mutableStateOf(maxPage)
    var maxPage: Int
        get() = _maxPage
        set(value) {
            _maxPage = value.coerceAtLeast(_minPage)
            currentPage = currentPage.coerceIn(_minPage, maxPage)
        }

    private var _currentPage = mutableStateOf(currentPage.coerceIn(minPage, maxPage))
    var currentPage: Int
        get() = _currentPage.value
        private set(value) {
            _currentPage.value = value.coerceIn(_minPage, _maxPage)
        }

    private val _currentPageOffset = mutableStateOf(0f)

    /**
     * TODO kdoc
     */
    var currentPageOffset: Float
        get() = _currentPageOffset.value
        private set(value) {
            _currentPageOffset.value = value.coerceIn(
                minimumValue = if (currentPage == maxPage) 0f else -1f,
                maximumValue = if (currentPage == minPage) 0f else 1f,
            )
        }

    /**
     * TODO kdoc
     */
    enum class SelectionState { Selected, Undecided }

    /**
     * TODO kdoc
     */
    var selectionState by mutableStateOf(SelectionState.Selected)
        private set

    private val mutatorMutex = MutatorMutex()

    /**
     * TODO
     */
    suspend fun animateScrollToPage(
        page: Int,
        animationSpec: AnimationSpec<Float> = spring(),
        initialVelocity: Float = 0f,
    ) {
        if (page == currentPage) return

        mutatorMutex.mutate {
            selectionState = SelectionState.Undecided
            animate(
                initialValue = currentPage + currentPageOffset,
                targetValue = page.coerceIn(minPage, maxPage).toFloat(),
                initialVelocity = initialVelocity,
                animationSpec = animationSpec
            ) { value, _ ->
                currentPage = floor(value).toInt()
                currentPageOffset = currentPage - value
            }
            selectionState = SelectionState.Selected
        }
    }

    /**
     * TODO
     */
    suspend fun scrollToPage(page: Int) {
        mutatorMutex.mutate {
            currentPage = page
            currentPageOffset = 0f
            selectionState = SelectionState.Selected
        }
    }

    private fun snapPage() {
        currentPage -= currentPageOffset.roundToInt()
        currentPageOffset = 0f
        selectionState = SelectionState.Selected
    }

    private fun determineSpringBackOffset(): Float {
        val offset = when {
            currentPageOffset >= ScrollThreshold -> 1f
            currentPageOffset <= -ScrollThreshold -> -1f
            else -> 0f
        }
        return offset.coerceIn(
            minimumValue = if (currentPage == maxPage) 0f else -1f,
            maximumValue = if (currentPage == minPage) 0f else 1f
        )
    }

    /**
     * TODO make this public?
     *
     * TODO: need to enforce velocity in percentage rather than pixels
     */
    private suspend fun fling(
        velocity: Float,
        animationSpec: DecayAnimationSpec<Float>,
    ) {
        val target = animationSpec.calculateTargetValue(currentPageOffset, velocity)

        // If we're at our page bounds, and we're flinging in the bounded direction, skip...
        if (velocity < 0 && currentPage == maxPage) return
        if (velocity > 0 && currentPage == minPage) return

        // If the animation can naturally end outside of current page bounds, we will
        // animate with decay.
        if (target < -1f || target > 1f) {
            // Animate with the decay animation spec using the fling velocity
            AnimationState(currentPageOffset, velocity).animateDecay(animationSpec) {
                // The property will coerce the value to the corrent range
                currentPageOffset = value

                if (value <= -1f || value >= 1f) {
                    // If we reach the bounds of the allowed offset, throw a CancellationException
                    // to cancel the animation
                    cancelAnimation()
                }
            }
            snapPage()
        } else {
            // Otherwise we animate to the next item, or spring-back depending on the offset
            animate(
                initialValue = currentPageOffset,
                targetValue = determineSpringBackOffset(),
                initialVelocity = velocity,
                animationSpec = spring()
            ) { value, _ ->
                currentPageOffset = value
            }
            snapPage()
        }
    }

    internal suspend fun PointerInputScope.detectPageTouch(pageSize: Int) {
        val velocityTracker = VelocityTracker()
        val decay = splineBasedDecay<Float>(this)

        while (true) {
            mutatorMutex.mutate {
                awaitPointerEventScope {
                    val down = awaitFirstDown()

                    // Reset the velocity tracker and add our initial down event
                    velocityTracker.resetTracking()
                    velocityTracker.addPosition(down)

                    selectionState = SelectionState.Undecided

                    horizontalDrag(down.id) { change ->
                        // Snap the value by the amount of finger movement
                        currentPageOffset += (change.positionChange().x / pageSize)
                        // Add the movement to the velocity tracker
                        velocityTracker.addPosition(change)
                    }
                }
            }

            mutatorMutex.mutate {
                // The drag has finished, now calculate the velocity and fling
                fling(
                    // Velocity is in pixels per second, but we deal in percentage offsets, so we
                    // need to scale the velocity to match
                    velocity = velocityTracker.calculateVelocity().y / pageSize,
                    animationSpec = decay
                )
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
