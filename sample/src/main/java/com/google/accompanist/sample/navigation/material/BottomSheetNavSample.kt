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

package com.google.accompanist.sample.navigation.material

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.compose.type_safe_args.annotation.ComposeDestination
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.accompanist.sample.AccompanistSampleTheme
import java.util.UUID

class BottomSheetNavSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                BottomSheetNavDemo()
            }
        }
    }
}

object Destinations {
    @ComposeDestination
    interface Home {
        companion object
    }

    @ComposeDestination
    interface Feed {
        companion object
    }

    @ComposeDestination
    interface Sheet {
        val arg: String
        companion object
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun BottomSheetNavDemo() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    ModalBottomSheetLayout(bottomSheetNavigator) {
        NavHost(navController, Destinations.Home.route) {
            composable(Destinations.Home.route) {
                HomeScreen(
                    showSheet = {
                        navController.navigate(Destinations.Sheet.getDestination(arg = "From Home Screen"))
                    },
                    showFeed = { navController.navigate(Destinations.Feed.getDestination()) }
                )
            }
            composable(Destinations.Feed.route) { Text("Feed!") }
            bottomSheet(Destinations.Sheet.route) { backstackEntry ->
                val (arg) = Destinations.Sheet.parseArguments(backstackEntry)
                BottomSheet(
                    showFeed = { navController.navigate(Destinations.Feed.getDestination()) },
                    showAnotherSheet = {
                        navController.navigate(Destinations.Sheet.getDestination(arg = "${UUID.randomUUID()}"))
                    },
                    arg = arg
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(showSheet: () -> Unit, showFeed: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Body")
        Button(onClick = showSheet) {
            Text("Show sheet!")
        }
        Button(onClick = showFeed) {
            Text("Navigate to Feed")
        }
    }
}

@Composable
private fun BottomSheet(showFeed: () -> Unit, showAnotherSheet: () -> Unit, arg: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Sheet with arg: $arg")
        Button(onClick = showFeed) {
            Text("Click me to navigate!")
        }
        Button(onClick = showAnotherSheet) {
            Text("Click me to show another sheet!")
        }
    }
}
