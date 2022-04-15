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

package com.google.accompanist.navigation.material

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeProgress
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorState
import com.google.accompanist.navigation.material.BottomSheetNavigator.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The state of a [ModalBottomSheetLayout] that the [BottomSheetNavigator] drives
 *
 * @param sheetState The sheet state that is driven by the [BottomSheetNavigator]
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
public class BottomSheetNavigatorSheetState(private val sheetState: ModalBottomSheetState) {
    /**
     * @see ModalBottomSheetState.isVisible
     */
    public val isVisible: Boolean
        get() = sheetState.isVisible

    /**
     * @see ModalBottomSheetState.currentValue
     */
    public val currentValue: ModalBottomSheetValue
        get() = sheetState.currentValue

    /**
     * @see ModalBottomSheetState.targetValue
     */
    public val targetValue: ModalBottomSheetValue
        get() = sheetState.targetValue

    /**
     * @see ModalBottomSheetState.offset
     */
    public val offset: State<Float>
        get() = sheetState.offset

    /**
     * @see ModalBottomSheetState.overflow
     */
    public val overflow: State<Float>
        get() = sheetState.overflow

    /**
     * @see ModalBottomSheetState.direction
     */
    public val direction: Float
        get() = sheetState.direction

    /**
     * @see ModalBottomSheetState.progress
     */
    public val progress: SwipeProgress<ModalBottomSheetValue>
        get() = sheetState.progress
}

/**
 * Create and remember a [BottomSheetNavigator]
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun rememberBottomSheetNavigator(
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    skipHalfExpanded: Boolean = false
): BottomSheetNavigator {
    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        animationSpec,
        skipHalfExpanded
    )
    return remember(sheetState) {
        BottomSheetNavigator(sheetState = sheetState)
    }
}

/**
 * Navigator that drives a [ModalBottomSheetState] for use of [ModalBottomSheetLayout]s
 * with the navigation library. Every destination using this Navigator must set a valid
 * [Composable] by setting it directly on an instantiated [Destination] or calling
 * [androidx.navigation.compose.material.bottomSheet].
 *
 * <b>The [sheetContent] [Composable] will always host the latest entry of the back stack. When
 * navigating from a [BottomSheetNavigator.Destination] to another
 * [BottomSheetNavigator.Destination], the content of the sheet will be replaced instead of a
 * new bottom sheet being shown.</b>
 *
 * When the sheet is dismissed by the user, the [state]'s [NavigatorState.backStack] will be popped.
 *
 * @param sheetState The [ModalBottomSheetState] that the [BottomSheetNavigator] will use to
 * drive the sheet state
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Navigator.Name("BottomSheetNavigator")
public class BottomSheetNavigator(
    internal val sheetState: ModalBottomSheetState
) : Navigator<BottomSheetNavigator.Destination>() {

    private var attached by mutableStateOf(false)

    /**
     * Get the back stack from the [state]. In some cases, the [sheetContent] might be composed
     * before the Navigator is attached, so we specifically return an empty flow if we aren't
     * attached yet.
     */
    private val backStack: StateFlow<List<NavBackStackEntry>>
        get() = if (attached) {
            state.backStack
        } else {
            MutableStateFlow(emptyList())
        }

    /**
     * Access properties of the [ModalBottomSheetLayout]'s [ModalBottomSheetState]
     */
    public val navigatorSheetState = BottomSheetNavigatorSheetState(sheetState)

    /**
     * A [Composable] function that hosts the current sheet content. This should be set as
     * sheetContent of your [ModalBottomSheetLayout].
     */
    public val sheetContent: @Composable ColumnScope.() -> Unit = @Composable {
        val columnScope = this
        val saveableStateHolder = rememberSaveableStateHolder()
        val backStackEntries by backStack.collectAsState()

        // We always replace the sheet's content instead of overlaying and nesting floating
        // window destinations. That means that only *one* concurrent destination is supported by
        // this navigator.
        val latestEntry = backStackEntries.lastOrNull { entry ->
            // We might have entries in the back stack that aren't started currently, so filter
            // these
            entry.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        // Mark all of the entries' transitions as complete, except for the entry we are
        // currently displaying because it will have its transition completed when the sheet's
        // animation has completed
        DisposableEffect(backStackEntries) {
            backStackEntries.forEach {
                if (it != latestEntry) state.markTransitionComplete(it)
            }
            onDispose { }
        }

        SheetContentHost(
            columnHost = columnScope,
            backStackEntry = latestEntry,
            sheetState = sheetState,
            saveableStateHolder = saveableStateHolder,
            onSheetShown = { backStackEntry ->
                state.markTransitionComplete(backStackEntry)
            },
            onSheetDismissed = { backStackEntry ->
                // Sheet dismissal can be started through popBackStack in which case we have a
                // transition that we'll want to complete
                if (state.transitionsInProgress.value.contains(backStackEntry)) {
                    state.markTransitionComplete(backStackEntry)
                } else {
                    state.pop(popUpTo = backStackEntry, saveState = false)
                }
            }
        )
    }

    override fun onAttach(state: NavigatorState) {
        super.onAttach(state)
        attached = true
    }

    override fun createDestination(): Destination = Destination(navigator = this, content = {})

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ) {
        entries.forEach { entry ->
            state.pushWithTransition(entry)
        }
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
    }

    /**
     * [NavDestination] specific to [BottomSheetNavigator]
     */
    @NavDestination.ClassType(Composable::class)
    public class Destination(
        navigator: BottomSheetNavigator,
        internal val content: @Composable ColumnScope.(NavBackStackEntry) -> Unit
    ) : NavDestination(navigator), FloatingWindow
}
