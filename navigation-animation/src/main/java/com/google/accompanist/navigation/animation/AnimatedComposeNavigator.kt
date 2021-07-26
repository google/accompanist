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
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

/**
 * Navigator that navigates through [Composable]s. Every destination using this Navigator must
 * set a valid [Composable] by setting it directly on an instantiated [Destination] or calling
 * [composable].
 */
@ExperimentalAnimationApi
@Navigator.Name("animatedComposable")
public class AnimatedComposeNavigator : Navigator<AnimatedComposeNavigator.Destination>() {
    internal val transitionsInProgress get() = state.transitionsInProgress

    internal val backStack get() = state.backStack

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ) {
        entries.forEach { entry ->
            state.pushWithTransition(entry)
        }
    }

    override fun createDestination(): Destination {
        return Destination(this, content = { })
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
    }

    /**
     * NavDestination specific to [AnimatedComposeNavigator]
     */
    @ExperimentalAnimationApi
    @NavDestination.ClassType(Composable::class)
    public class Destination(
        navigator: AnimatedComposeNavigator,
        internal val content: @Composable (NavBackStackEntry) -> Unit,
        internal var enterTransition:
            ((initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition?)? = null,
        internal var exitTransition:
            ((initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition?)? = null
    ) : NavDestination(navigator)

    internal companion object {
        internal const val NAME = "animatedComposable"
    }
}
