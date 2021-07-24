/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.accompanist.navigation.material

import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navArgument
import androidx.navigation.contains
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.plusAssign
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@LargeTest
@RunWith(AndroidJUnit4::class)
internal class NavGraphBuilderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCurrentBackStackEntryNavigate() {
        lateinit var navController: TestNavHostController
        val key = "key"
        val arg = "myarg"
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider += createBottomSheetNavigator()

            NavHost(navController, startDestination = firstRoute) {
                bottomSheet(firstRoute) { }
                bottomSheet("$secondRoute/{$key}") { }
            }
        }

        composeTestRule.runOnUiThread {
            navController.navigate("$secondRoute/$arg")
            assertThat(navController.currentBackStackEntry!!.arguments!!.getString(key))
                .isEqualTo(arg)
        }
    }

    @Test
    fun testDefaultArguments() {
        lateinit var navController: TestNavHostController
        val key = "key"
        val defaultArg = "default"
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider += createBottomSheetNavigator()

            NavHost(navController, startDestination = firstRoute) {
                bottomSheet(firstRoute) { }
                bottomSheet(
                    secondRoute,
                    arguments = listOf(navArgument(key) { defaultValue = defaultArg })
                ) { }
            }
        }

        composeTestRule.runOnUiThread {
            navController.navigate(secondRoute)
            assertThat(navController.currentBackStackEntry!!.arguments!!.getString(key))
                .isEqualTo(defaultArg)
        }
    }

    @Test
    fun testDeepLink() {
        lateinit var navController: TestNavHostController
        val uriString = "https://www.example.com"
        val deeplink = NavDeepLinkRequest.Builder.fromUri(Uri.parse(uriString)).build()
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider += createBottomSheetNavigator()

            NavHost(navController, startDestination = firstRoute) {
                bottomSheet(firstRoute) { }
                bottomSheet(
                    secondRoute,
                    deepLinks = listOf(navDeepLink { uriPattern = uriString })
                ) { }
            }
        }

        composeTestRule.runOnUiThread {
            navController.navigate(uriString.toUri())
            assertThat(navController.currentBackStackEntry!!.destination.hasDeepLink(deeplink))
                .isTrue()
        }
    }

    @Test
    fun testNavigationNestedStart() {
        lateinit var navController: TestNavHostController
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider += createBottomSheetNavigator()

            NavHost(navController, startDestination = firstRoute) {
                navigation(startDestination = secondRoute, route = firstRoute) {
                    bottomSheet(secondRoute) { }
                }
            }
        }

        composeTestRule.runOnUiThread {
            assertWithMessage("Sheet destination should be added to the graph")
                .that(secondRoute in navController.graph)
                .isTrue()
        }
    }

    private fun createBottomSheetNavigator() =
        BottomSheetNavigator(sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden))
}

private const val firstRoute = "first"
private const val secondRoute = "second"
private const val thirdRoute = "third"
