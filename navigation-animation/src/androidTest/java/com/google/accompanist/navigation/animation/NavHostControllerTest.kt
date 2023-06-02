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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.NoOpNavigator
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.get
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalAnimationApi::class)
@Suppress("DEPRECATION")
@LargeTest
@RunWith(AndroidJUnit4::class)
class NavHostControllerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRememberAnimatedNavController() {
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberAnimatedNavController()
            // get state to trigger recompose on navigate
            navController.currentBackStackEntryAsState().value
            AnimatedNavHost(navController, startDestination = first) {
                composable(first) { BasicText(first) }
                composable(second) { BasicText(second) }
            }
        }

        val navigator = composeTestRule.runOnIdle {
            navController.navigatorProvider[ComposeNavigator::class]
        }

        // trigger recompose
        composeTestRule.runOnIdle {
            navController.navigate(second)
        }

        composeTestRule.runOnIdle {
            assertThat(navController.navigatorProvider[ComposeNavigator::class])
                .isEqualTo(navigator)
        }
    }

    @Test
    fun testRememberAnimatedNavControllerAddsCustomNavigator() {
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            val customNavigator = remember { NoOpNavigator() }
            navController = rememberAnimatedNavController(customNavigator)
            // get state to trigger recompose on navigate
            navController.currentBackStackEntryAsState().value
            AnimatedNavHost(navController, startDestination = first) {
                composable(first) { BasicText(first) }
                composable(second) { BasicText(second) }
            }
        }

        val navigator = composeTestRule.runOnIdle {
            navController.navigatorProvider[NoOpNavigator::class]
        }

        // trigger recompose
        composeTestRule.runOnIdle {
            navController.navigate(second)
        }

        composeTestRule.runOnIdle {
            assertThat(navController.navigatorProvider[NoOpNavigator::class])
                .isEqualTo(navigator)
        }
    }
}

private const val first = "first"
private const val second = "second"
