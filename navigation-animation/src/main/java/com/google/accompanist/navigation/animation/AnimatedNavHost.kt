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

package com.google.accompanist.navigation.animation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
 * @param popEnterTransition callback to define popEnter transitions for destination in this host
 * @param popExitTransition callback to define popExit transitions for destination in this host
 * @param builder the builder used to construct the graph
 */
@Composable
@ExperimentalAnimationApi
public fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    route: String? = null,
    enterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? =
        { _, _ -> fadeIn(animationSpec = tween(700)) },
    exitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? =
        { _, _ -> fadeOut(animationSpec = tween(700)) },
    popEnterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? = enterTransition,
    popExitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? = exitTransition,
    builder: NavGraphBuilder.() -> Unit
) {
    AnimatedNavHost(
        navController,
        remember(route, startDestination, builder) {
            navController.createGraph(startDestination, route, builder)
        },
        modifier,
        contentAlignment,
        enterTransition,
        exitTransition,
        popEnterTransition,
        popExitTransition
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
 * @param popEnterTransition callback to define popEnter transitions for destination in this host
 * @param popExitTransition callback to define popExit transitions for destination in this host
 */
@ExperimentalAnimationApi
@Composable
public fun AnimatedNavHost(
    navController: NavHostController,
    graph: NavGraph,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    enterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? =
        { _, _ -> fadeIn(animationSpec = tween(700)) },
    exitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? =
        { _, _ -> fadeOut(animationSpec = tween(700)) },
    popEnterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? = enterTransition,
    popExitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? = exitTransition,
) {

    enterTransitions[graph.route] = enterTransition
    exitTransitions[graph.route] = exitTransition
    popEnterTransitions[graph.route] = popEnterTransition
    popExitTransitions[graph.route] = popExitTransition

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
    val visibleTransitionsInProgress = rememberVisibleList(transitionsInProgress)
    val visibleBackStack = rememberVisibleList(backStack)
    visibleTransitionsInProgress.PopulateVisibleList(transitionsInProgress)
    visibleBackStack.PopulateVisibleList(backStack)

    val backStackEntry = visibleTransitionsInProgress.lastOrNull() ?: visibleBackStack.lastOrNull()

    if (backStackEntry != null) {
        val finalEnter: AnimatedContentScope<String>.() -> EnterTransition = {
            val initialEntry = transitionsInProgress.lastOrNull { entry ->
                initialState == entry.id
            } ?: backStack.last { entry ->
                initialState == entry.id
            }
            val targetEntry = transitionsInProgress.lastOrNull { entry ->
                targetState == entry.id
            } ?: backStack.last { entry ->
                targetState == entry.id
            }
            val targetDestination = targetEntry.destination as AnimatedComposeNavigator.Destination

            if (composeNavigator.isPop.value) {
                targetDestination.popEnterTransition?.invoke(this, initialEntry, targetEntry)
                    ?: popEnterTransitions[
                        (
                            targetDestination.hierarchy.first {
                                popEnterTransitions.containsKey(it.route)
                            }
                            ).route
                    ]?.invoke(this, initialEntry, targetEntry) as EnterTransition
            } else {
                targetDestination.enterTransition?.invoke(this, initialEntry, targetEntry)
                    ?: enterTransitions[
                        (
                            targetDestination.hierarchy.first { enterTransitions.containsKey(it.route) }
                            ).route
                    ]?.invoke(this, initialEntry, targetEntry) as EnterTransition
            }
        }

        val finalExit: AnimatedContentScope<String>.() -> ExitTransition = {
            val initialEntry = transitionsInProgress.lastOrNull { entry ->
                initialState == entry.id
            } ?: backStack.last { entry ->
                initialState == entry.id
            }
            val initialDestination = initialEntry.destination as AnimatedComposeNavigator.Destination
            val targetEntry = transitionsInProgress.lastOrNull { entry ->
                targetState == entry.id
            } ?: backStack.last { entry ->
                targetState == entry.id
            }

            if (composeNavigator.isPop.value) {
                initialDestination.popExitTransition?.invoke(
                    this, initialEntry, targetEntry
                ) ?: popExitTransitions[
                    (
                        initialDestination.hierarchy.first {
                            popExitTransitions.containsKey(it.route)
                        }
                        ).route
                ]?.invoke(this, initialEntry, targetEntry) as ExitTransition
            } else {
                initialDestination.exitTransition?.invoke(
                    this, initialEntry, targetEntry
                ) ?: exitTransitions[
                    (
                        initialDestination.hierarchy.first {
                            exitTransitions.containsKey(it.route)
                        }
                        ).route
                ]?.invoke(this, initialEntry, targetEntry) as ExitTransition
            }
        }

        val transition = updateTransition(backStackEntry.id, label = "entry")
        transition.AnimatedContent(
            modifier,
            transitionSpec = { finalEnter(this) with finalExit(this) },
            contentAlignment
        ) {
            val currentEntry = transitionsInProgress.lastOrNull { entry ->
                it == entry.id
            } ?: backStack.lastOrNull { entry ->
                it == entry.id
            }
            // while in the scope of the composable, we provide the navBackStackEntry as the
            // ViewModelStoreOwner and LifecycleOwner
            currentEntry?.LocalOwnersProvider(saveableStateHolder) {
                (currentEntry.destination as AnimatedComposeNavigator.Destination)
                    .content(this, currentEntry)
            }
        }
        if (transition.currentState == transition.targetState) {
            transitionsInProgress.forEach { entry ->
                composeNavigator.markTransitionComplete(entry)
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
        (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)?>()

@ExperimentalAnimationApi
internal val exitTransitions =
    mutableMapOf<String?,
        (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)?>()

@ExperimentalAnimationApi
internal val popEnterTransitions =
    mutableMapOf<String?,
        (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)?>()

@ExperimentalAnimationApi
internal val popExitTransitions =
    mutableMapOf<String?,
        (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)?>()

@Composable
private fun MutableList<NavBackStackEntry>.PopulateVisibleList(
    transitionsInProgress: Collection<NavBackStackEntry>
) {
    transitionsInProgress.forEach { entry ->
        DisposableEffect(entry.lifecycle) {
            val observer = LifecycleEventObserver { _, event ->
                // ON_START -> add to visibleBackStack, ON_STOP -> remove from visibleBackStack
                if (event == Lifecycle.Event.ON_START) {
                    add(entry)
                }
                if (event == Lifecycle.Event.ON_STOP) {
                    remove(entry)
                }
            }
            entry.lifecycle.addObserver(observer)
            onDispose {
                entry.lifecycle.removeObserver(observer)
            }
        }
    }
}

@Composable
private fun rememberVisibleList(transitionsInProgress: Collection<NavBackStackEntry>) =
    remember(transitionsInProgress) {
        mutableStateListOf<NavBackStackEntry>().also {
            it.addAll(
                transitionsInProgress.filter { entry ->
                    entry.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                }
            )
        }
    }
