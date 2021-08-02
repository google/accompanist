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

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.plusAssign
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalAnimationApi::class)
@LargeTest
@RunWith(AndroidJUnit4::class)
class AnimatedNavHostTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAnimatedNavHost() {
        lateinit var navController: NavHostController

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            navController = rememberNavController()
            navController.navigatorProvider += AnimatedComposeNavigator()
            AnimatedNavHost(navController, startDestination = first) {
                composable(first) { BasicText(first) }
                composable(second) { BasicText(second) }
            }
        }

        val firstEntry = navController.currentBackStackEntry

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.runOnIdle {
            assertThat(firstEntry?.lifecycle?.currentState)
                .isEqualTo(Lifecycle.State.RESUMED)
        }

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.runOnIdle {
            navController.navigate(second)
        }

        assertThat(firstEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.CREATED)
        assertThat(navController.currentBackStackEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.STARTED)

        // advance half way between the crossfade
        composeTestRule.mainClock.advanceTimeBy(AnimationConstants.DefaultDurationMillis.toLong() / 2)

        assertThat(firstEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.CREATED)
        assertThat(navController.currentBackStackEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.STARTED)

        composeTestRule.onNodeWithText(first).assertExists()
        composeTestRule.onNodeWithText(second).assertExists()

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.runOnIdle {
            assertThat(firstEntry?.lifecycle?.currentState)
                .isEqualTo(Lifecycle.State.CREATED)
            assertThat(navController.currentBackStackEntry?.lifecycle?.currentState)
                .isEqualTo(Lifecycle.State.RESUMED)
        }

        composeTestRule.mainClock.autoAdvance = false

        val secondEntry = navController.currentBackStackEntry

        composeTestRule.runOnIdle {
            navController.popBackStack()
        }

        assertThat(navController.currentBackStackEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.STARTED)
        assertThat(secondEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.CREATED)

        // advance half way between the crossfade
        composeTestRule.mainClock.advanceTimeBy(AnimationConstants.DefaultDurationMillis.toLong() / 2)

        assertThat(navController.currentBackStackEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.STARTED)
        assertThat(secondEntry?.lifecycle?.currentState)
            .isEqualTo(Lifecycle.State.CREATED)

        composeTestRule.onNodeWithText(first).assertExists()
        composeTestRule.onNodeWithText(second).assertExists()

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.runOnIdle {
            assertThat(navController.currentBackStackEntry?.lifecycle?.currentState)
                .isEqualTo(Lifecycle.State.RESUMED)
            assertThat(secondEntry?.lifecycle?.currentState)
                .isEqualTo(Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun testAnimatedNavHostDeeplink() {
        lateinit var navController: NavHostController

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            // Add the flags to make NavController think this is a deep link
            val activity = LocalContext.current as? Activity
            activity?.intent?.run {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            navController = rememberNavController()
            navController.navigatorProvider += AnimatedComposeNavigator()
            AnimatedNavHost(navController, startDestination = first) {
                composable(first) { BasicText(first) }
                composable(
                    second,
                    deepLinks = listOf(navDeepLink { action = Intent.ACTION_MAIN })
                ) {
                    BasicText(second)
                }
            }
        }

        composeTestRule.waitForIdle()

        val firstEntry = navController.getBackStackEntry(first)
        val secondEntry = navController.getBackStackEntry(second)

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.runOnIdle {
            assertThat(firstEntry.lifecycle.currentState)
                .isEqualTo(Lifecycle.State.CREATED)
            assertThat(secondEntry.lifecycle.currentState)
                .isEqualTo(Lifecycle.State.RESUMED)
        }
    }
}

private const val first = "first"
private const val second = "second"
