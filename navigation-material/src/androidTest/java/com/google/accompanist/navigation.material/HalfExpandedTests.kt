/*
 * Copyright 2022 The Android Open Source Project
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)
class HalfExpandedTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSkipHalfExpandedWhenOverHalfScreenHeight(): Unit = runBlocking {
        trySkipHalfExpanded(true, 0.6f, ModalBottomSheetValue.Expanded)
    }

    @Test
    fun testNoSkipHalfExpandedWhenOverHalfScreenHeight(): Unit = runBlocking {
        trySkipHalfExpanded(false, 0.6f, ModalBottomSheetValue.HalfExpanded)
    }

    @Test
    fun testNoSkipHalfExpandedWhenUnderHalfScreenHeight(): Unit = runBlocking {
        trySkipHalfExpanded(false, 0.4f, ModalBottomSheetValue.Expanded)
    }

    @Test
    fun testSkipHalfExpandedWhenUnderHalfScreenHeight(): Unit = runBlocking {
        trySkipHalfExpanded(true, 0.4f, ModalBottomSheetValue.Expanded)
    }

    private suspend fun trySkipHalfExpanded(skipHalfExpanded: Boolean, screenPercent: Float, expectedSheetState: ModalBottomSheetValue) {
        coroutineScope {
            lateinit var navController: NavHostController
            lateinit var bottomSheetNavigator: BottomSheetNavigator
            composeTestRule.setContent {
                bottomSheetNavigator = rememberBottomSheetNavigator(skipHalfExpanded = skipHalfExpanded)
                navController = rememberNavController(bottomSheetNavigator)

                ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
                    NavHost(
                        navController = navController,
                        startDestination = "start"
                    ) {
                        composable("start") {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                        bottomSheet("bottomSheet") {
                            Box(modifier = Modifier.fillMaxSize(screenPercent))
                        }
                    }
                }
            }
            launch(Dispatchers.Main) {
                navController.navigate("bottomSheet")
            }
            composeTestRule.awaitIdle()
            Truth.assertThat(bottomSheetNavigator.navigatorSheetState.currentValue)
                .isEqualTo(expectedSheetState)
        }
    }
}
