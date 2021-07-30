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

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.get
import androidx.navigation.navigation

/**
 * Add the [Composable] to the [NavGraphBuilder]
 *
 * @param route route for the destination
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param enterTransition callback to determine the destination's enter transition
 * @param exitTransition callback to determine the destination's exit transition
 * @param popEnterTransition callback to determine the destination's popEnter transition
 * @param popExitTransition callback to determine the destination's popExit transition
 * @param content composable for the destination
 */
@ExperimentalAnimationApi
public fun NavGraphBuilder.composable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition?
    )? = null,
    exitTransition: (
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition?
    )? = null,
    popEnterTransition: (
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition?
    )? = enterTransition,
    popExitTransition: (
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition?
    )? = exitTransition,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    addDestination(
        AnimatedComposeNavigator.Destination(
            provider[AnimatedComposeNavigator::class],
            content,
            enterTransition,
            exitTransition,
            popEnterTransition,
            popExitTransition
        ).apply {
            this.route = route
            arguments.forEach { (argumentName, argument) ->
                addArgument(argumentName, argument)
            }
            deepLinks.forEach { deepLink ->
                addDeepLink(deepLink)
            }
        }
    )
}

/**
 * Construct a nested [NavGraph]
 *
 * @param startDestination the starting destination's route for this NavGraph
 * @param route the destination's unique route
 * @param enterTransition callback to define enter transitions for destination in this NavGraph
 * @param exitTransition callback to define exit transitions for destination in this NavGraph
 * @param popEnterTransition callback to define pop enter transitions for destination in this
 * NavGraph
 * @param popExitTransition callback to define pop exit transitions for destination in this NavGraph
 * @param builder the builder used to construct the graph
 *
 * @return the newly constructed nested NavGraph
 */
@ExperimentalAnimationApi
public fun NavGraphBuilder.navigation(
    startDestination: String,
    route: String,
    enterTransition: ((initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? =
        { _, _ -> fadeIn(animationSpec = tween(700)) },
    exitTransition: ((initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? =
        { _, _ -> fadeOut(animationSpec = tween(700)) },
    popEnterTransition: (
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition
    )? = enterTransition,
    popExitTransition: (
        (initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition
    )? = exitTransition,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(startDestination, route, builder).apply {
        enterTransition?.let { enterTransitions[route] = enterTransition }
        exitTransition?.let { exitTransitions[route] = exitTransition }
        popEnterTransition?.let { popEnterTransitions[route] = popEnterTransition }
        popExitTransition?.let { popExitTransitions[route] = popExitTransition }
    }
}
