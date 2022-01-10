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
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SwipeProgress
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.rememberBackdropScaffoldState
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The state of a [BackdropScaffold] that the [BackdropNavigator] drives
 *
 * @param backdropState The sheet state that is driven by the [BackdropNavigator]
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
public class BackdropNavigatorState(private val backdropState: BackdropScaffoldState) {
    /**
     * @see BackdropScaffoldState.isRevealed
     */
    public val isRevealed: Boolean
        get() = backdropState.isRevealed
    /**
     * @see BackdropScaffoldState.isConcealed
     */
    public val isConcealed: Boolean
        get() = backdropState.isConcealed
    /**
     * @see BackdropScaffoldState.snackbarHostState
     */
    public val snackbarHostState: SnackbarHostState
        get() = backdropState.snackbarHostState

    /**
     * @see BackdropScaffoldState.currentValue
     */
    public val currentValue: BackdropValue
        get() = backdropState.currentValue

    /**
     * @see BackdropScaffoldState.targetValue
     */
    public val targetValue: BackdropValue
        get() = backdropState.targetValue

    /**
     * @see BackdropScaffoldState.offset
     */
    public val offset: State<Float>
        get() = backdropState.offset

    /**
     * @see BackdropScaffoldState.overflow
     */
    public val overflow: State<Float>
        get() = backdropState.overflow

    /**
     * @see BackdropScaffoldState.direction
     */
    public val direction: Float
        get() = backdropState.direction

    /**
     * @see BackdropScaffoldState.progress
     */
    public val progress: SwipeProgress<BackdropValue>
        get() = backdropState.progress

    /**
     * @see BackdropScaffoldState.isAnimationRunning
     */
    public val isAnimationRunning: Boolean
        get() = backdropState.isAnimationRunning
}

/**
 * Create and remember a [BackdropNavigator]
 *
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 * @param snackbarHostState The [SnackbarHostState] used to show snackbars inside the scaffold.
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun rememberBackdropNavigator(
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (BackdropValue) -> Boolean = { true },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): BackdropNavigator {
    val sheetState = rememberBackdropScaffoldState(
        BackdropValue.Concealed,
        animationSpec,
        confirmStateChange,
        snackbarHostState
    )
    return remember(sheetState) {
        BackdropNavigator(backdropScaffoldState = sheetState)
    }
}

/**
 * Navigator that drives a [BackdropScaffoldState] for use of [BackdropScaffold]s with the navigation
 * library. Every destination using this Navigator must set a valid [Composable] by setting it
 * directly on an instantiated [Destination] or calling [backdrop].
 *
 * <b>The [backLayerContent] [Composable] will always host the latest entry of the back stack. When
 * navigating from a [BackdropNavigator.Destination] to another [BackdropNavigator.Destination],
 * the content of the back layer will be replaced.</b>
 *
 * When the back layer is dismissed by the user, the [state]'s [NavigatorState.backStack] will be popped.
 *
 * @param backdropScaffoldState The [BackdropScaffoldState] that the [BackdropNavigator] will use to
 * drive the sheet state
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Navigator.Name("BackdropNavigator")
public class BackdropNavigator(
    internal val backdropScaffoldState: BackdropScaffoldState
) : Navigator<BackdropNavigator.Destination>() {

    private var attached by mutableStateOf(false)

    /**
     * Get the back stack from the [state]. In some cases, the [backLayerContent] might be composed
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
     * Access properties of the [BackdropScaffold]'s [BackdropScaffoldState]
     */
    public val navigatorState = BackdropNavigatorState(backdropScaffoldState)

    /**
     * A [Composable] function that hosts the current back layer content. This should be set as
     * backLayerContent of your [BackdropScaffold].
     */
    public val backLayerContent: @Composable () -> Unit = @Composable {
        val saveableStateHolder = rememberSaveableStateHolder()
        val backStackEntries by backStack.collectAsState()

        // We always replace the back layer's content instead of overlaying and nesting floating
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

        BackdropContentHost(
            backStackEntry = latestEntry,
            backdropScaffoldState = backdropScaffoldState,
            saveableStateHolder = saveableStateHolder,
            onBackLayerShown = { backStackEntry ->
                state.markTransitionComplete(backStackEntry)
            },
            onBackLayerDismissed = { backStackEntry ->
                // Back layer dismissal can be started through popBackStack in which case we have a
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
     * [NavDestination] specific to [BackdropNavigator]
     */
    @NavDestination.ClassType(Composable::class)
    public class Destination(
        navigator: BackdropNavigator,
        internal val content: @Composable (NavBackStackEntry) -> Unit
    ) : NavDestination(navigator), FloatingWindow
}
