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

package com.google.accompanist.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.DialogHost
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.LocalOwnersProvider
import androidx.navigation.createGraph
import androidx.navigation.get

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [NavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * The builder passed into this method is [remember]ed. This means that for this NavHost, the
 * contents of the builder cannot be changed.
 *
 * @param navController the navController for this host
 * @param startDestination the route for the start destination
 * @param modifier The modifier to be applied to the layout.
 * @param route the route for the graph
 * @param enterTransition callback to define enter transitions for destination in this host
 * @param exitTransition callback to define exit transitions for destination in this host
 * @param builder the builder used to construct the graph
 */
@Composable
@ExperimentalAnimationApi
public fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    route: String? = null,
    enterTransition: (initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition =
        { _, _ -> fadeIn(animationSpec = tween(2000)) },
    exitTransition: (initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition =
        { _, _ -> fadeOut(animationSpec = tween(2000)) },
    builder: NavGraphBuilder.() -> Unit
) {
    AnimatedNavHost(
        navController,
        remember(route, startDestination, builder) {
            navController.createGraph(startDestination, route, builder)
        },
        modifier,
        enterTransition,
        exitTransition
    )
}

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [NavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * @param navController the navController for this host
 * @param graph the graph for this host
 * @param modifier The modifier to be applied to the layout.
 * @param enterTransition callback to define enter transitions for destination in this host
 * @param exitTransition callback to define exit transitions for destination in this host
 */
@ExperimentalAnimationApi
@Composable
public fun AnimatedNavHost(
    navController: NavHostController,
    graph: NavGraph,
    modifier: Modifier = Modifier,
    enterTransition: (initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition =
        { _, _ -> fadeIn(animationSpec = tween(2000)) },
    exitTransition: (initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition =
        { _, _ -> fadeOut(animationSpec = tween(2000)) },
) {
    enterTransitions[graph.route] = enterTransition
    exitTransitions[graph.route] = exitTransition

    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "NavHost requires a ViewModelStoreOwner to be provided via LocalViewModelStoreOwner"
    }
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    val onBackPressedDispatcher = onBackPressedDispatcherOwner?.onBackPressedDispatcher

    // on successful recompose we setup the navController with proper inputs
    // after the first time, this will only happen again if one of the inputs changes
    navController.setLifecycleOwner(lifecycleOwner)
    navController.setViewModelStore(viewModelStoreOwner.viewModelStore)
    if (onBackPressedDispatcher != null) {
        navController.setOnBackPressedDispatcher(onBackPressedDispatcher)
    }

    navController.graph = graph

    val saveableStateHolder = rememberSaveableStateHolder()

    // Find the ComposeNavigator, returning early if it isn't found
    // (such as is the case when using TestNavHostController)
    val composeNavigator = navController.navigatorProvider.get<Navigator<out NavDestination>>(
        AnimatedComposeNavigator.NAME
    ) as? AnimatedComposeNavigator ?: return
    val backStack by composeNavigator.backStack.collectAsState()
    val transitionsInProgress by composeNavigator.transitionsInProgress.collectAsState()

    val backStackEntry = transitionsInProgress.keys.lastOrNull { entry ->
        entry.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    } ?: backStack.lastOrNull { entry ->
        entry.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    if (backStackEntry != null) {
        val destination = backStackEntry.destination as AnimatedComposeNavigator.Destination

        val leavingEntry = transitionsInProgress.keys.lastOrNull { entry ->
            !entry.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        // When there is no leaving entry, that means this is the start destination so this
        // transition never happens.
        val finalEnter = if (leavingEntry != null) {
            destination.enterTransition?.invoke(leavingEntry, backStackEntry)
                ?: enterTransitions[
                    (destination.hierarchy.first { enterTransitions.containsKey(it.route) }).route
                ]?.invoke(leavingEntry, backStackEntry) as EnterTransition
        } else {
            EnterTransition.None
        }

        val finalExit = if (leavingEntry != null) {
            (leavingEntry.destination as? AnimatedComposeNavigator.Destination)?.exitTransition?.invoke(
                leavingEntry, backStackEntry
            ) ?: exitTransitions[
                (
                    leavingEntry.destination.hierarchy.first {
                        exitTransitions.containsKey(it.route)
                    }
                    ).route
            ]?.invoke(leavingEntry, backStackEntry) as ExitTransition
        } else {
            ExitTransition.None
        }
        val transition = updateTransition(backStackEntry, label = "entry")
        transition.AnimatedContent(
            modifier, transitionSpec = { finalEnter with finalExit }
        ) { currentEntry ->
            // while in the scope of the composable, we provide the navBackStackEntry as the
            // ViewModelStoreOwner and LifecycleOwner
            currentEntry.LocalOwnersProvider(saveableStateHolder) {
                (currentEntry.destination as AnimatedComposeNavigator.Destination).content(currentEntry)
            }
        }
        if (transition.currentState == transition.targetState) {
            transitionsInProgress.forEach { entry ->
                entry.value.onTransitionComplete()
            }
        }
    }

    val dialogNavigator = navController.navigatorProvider.get<Navigator<out NavDestination>>(
        "dialog"
    ) as? DialogNavigator ?: return

    // Show any dialog destinations
    DialogHost(dialogNavigator)
}

@ExperimentalAnimationApi
internal val enterTransitions =
    mutableMapOf<String?,
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition>()

@ExperimentalAnimationApi
internal val exitTransitions =
    mutableMapOf<String?,
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition>()
