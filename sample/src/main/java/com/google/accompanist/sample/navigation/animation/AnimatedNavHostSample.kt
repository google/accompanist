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

package com.google.accompanist.sample.navigation.animation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.sample.AccompanistSampleTheme

@ExperimentalAnimationApi
class AnimatedNavHostSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                ExperimentalAnimationNav()
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ExperimentalAnimationNav() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController, startDestination = "Blue") {
        composable(
            "Blue",
            enterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Red" ->
                        slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = { _, target ->
                when (target.destination.route) {
                    "Red" ->
                        slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700))
                    else -> null
                }
            },
            popEnterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Red" ->
                        slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700))
                    else -> null
                }
            },
            popExitTransition = { _, target ->
                when (target.destination.route) {
                    "Red" ->
                        slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700))
                    else -> null
                }
            }
        ) { BlueScreen(navController) }
        composable(
            "Red",
            enterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Blue" ->
                        slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700))
                    "Green" ->
                        slideInVertically(initialOffsetY = { 1800 }, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = { _, target ->
                when (target.destination.route) {
                    "Blue" ->
                        slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700))
                    "Green" ->
                        slideOutVertically(targetOffsetY = { -1800 }, animationSpec = tween(700))
                    else -> null
                }
            },
            popEnterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Blue" ->
                        slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700))
                    "Green" ->
                        slideInVertically(initialOffsetY = { -1800 }, animationSpec = tween(700))
                    else -> null
                }
            },
            popExitTransition = { _, target ->
                when (target.destination.route) {
                    "Blue" ->
                        slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700))
                    "Green" ->
                        slideOutVertically(targetOffsetY = { 1800 }, animationSpec = tween(700))
                    else -> null
                }
            }
        ) { RedScreen(navController) }
        navigation(
            startDestination = "Green",
            route = "Inner",
            enterTransition = { _, _ -> expandIn(animationSpec = tween(700)) },
            exitTransition = { _, _ -> shrinkOut(animationSpec = tween(700)) }
        ) {
            composable(
                "Green",
                enterTransition = { initial, _ ->
                    when (initial.destination.route) {
                        "Red" ->
                            slideInVertically(
                                initialOffsetY = { 1800 }, animationSpec = tween(700)
                            )
                        else -> null
                    }
                },
                exitTransition = { _, target ->
                    when (target.destination.route) {
                        "Red" ->
                            slideOutVertically(
                                targetOffsetY = { -1800 }, animationSpec = tween(700)
                            )
                        else -> null
                    }
                },
                popEnterTransition = { initial, _ ->
                    when (initial.destination.route) {
                        "Red" ->
                            slideInVertically(
                                initialOffsetY = { -1800 }, animationSpec = tween(700)
                            )
                        else -> null
                    }
                },
                popExitTransition = { _, target ->
                    when (target.destination.route) {
                        "Red" ->
                            slideOutVertically(
                                targetOffsetY = { 1800 }, animationSpec = tween(700)
                            )
                        else -> null
                    }
                }
            ) { GreenScreen(navController) }
        }
    }
}

@Composable
fun BlueScreen(navController: NavHostController) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Blue)
    ) {
        Spacer(Modifier.height(Dp(25f)))
        NavigateButton(
            "Navigate Horizontal",
            Modifier
                .wrapContentWidth()
                .then(Modifier.align(Alignment.CenterHorizontally))
        ) { navController.navigate("Red") }
        Spacer(Modifier.height(Dp(25f)))
        NavigateButton(
            "Navigate Expand",
            Modifier
                .wrapContentWidth()
                .then(Modifier.align(Alignment.CenterHorizontally))
        ) { navController.navigate("Inner") }
        Spacer(Modifier.weight(1f))
        NavigateBackButton(navController)
    }
}

@Composable
fun RedScreen(navController: NavHostController) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        Spacer(Modifier.height(Dp(25f)))
        NavigateButton(
            "Navigate Horizontal",
            Modifier
                .wrapContentWidth()
                .then(Modifier.align(Alignment.CenterHorizontally))
        ) { navController.navigate("Blue") }
        Spacer(Modifier.height(Dp(25f)))
        NavigateButton(
            "Navigate Vertical",
            Modifier
                .wrapContentWidth()
                .then(Modifier.align(Alignment.CenterHorizontally))
        ) { navController.navigate("Green") }
        Spacer(Modifier.weight(1f))
        NavigateBackButton(navController)
    }
}

@Composable
fun GreenScreen(navController: NavHostController) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Green)
    ) {
        Spacer(Modifier.height(Dp(25f)))
        NavigateButton(
            "Navigate to Red",
            Modifier
                .wrapContentWidth()
                .then(Modifier.align(Alignment.CenterHorizontally))
        ) { navController.navigate("Red") }
        Spacer(Modifier.weight(1f))
        NavigateBackButton(navController)
    }
}

@Composable
fun NavigateButton(
    text: String,
    modifier: Modifier = Modifier,
    listener: () -> Unit = { }
) {
    Button(
        onClick = listener,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
        modifier = modifier
    ) {
        Text(text = text)
    }
}

@Composable
fun NavigateBackButton(navController: NavController) {
    // Use LocalLifecycleOwner.current as a proxy for the NavBackStackEntry
    // associated with this Composable
    if (navController.currentBackStackEntry == LocalLifecycleOwner.current &&
        navController.previousBackStackEntry != null
    ) {
        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go to Previous screen")
        }
    }
}
