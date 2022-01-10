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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BackdropScaffold
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.backdrop
import com.google.accompanist.navigation.material.rememberBackdropNavigator
import com.google.accompanist.sample.AccompanistSampleTheme
import java.util.UUID

@ExperimentalMaterialApi
class BackdropNavSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                BackdropNavDemo()
            }
        }
    }
}

private object BackdropDestinations {
    const val Home = "HOME"
    const val Feed = "FEED"
    const val Sheet = "SHEET"
}

@ExperimentalMaterialApi
@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun BackdropNavDemo() {
    val bottomSheetNavigator = rememberBackdropNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    BackdropScaffold(
        bottomSheetNavigator,
        appBar = { }
    ) {
        NavHost(navController, BackdropDestinations.Home) {
            composable(BackdropDestinations.Home) {
                HomeScreen(
                    showSheet = {
                        navController.navigate(BackdropDestinations.Sheet + "?arg=From Home Screen")
                    },
                    showFeed = { navController.navigate(BackdropDestinations.Feed) }
                )
            }
            composable(BackdropDestinations.Feed) { Text("Feed!") }
            backdrop(BackdropDestinations.Sheet + "?arg={arg}") { backstackEntry ->
                val arg = backstackEntry.arguments?.getString("arg") ?: "Missing argument :("
                Backdrop(
                    showFeed = { navController.navigate(BackdropDestinations.Feed) },
                    showAnotherSheet = {
                        navController.navigate(BackdropDestinations.Sheet + "?arg=${UUID.randomUUID()}")
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
private fun Backdrop(showFeed: () -> Unit, showAnotherSheet: () -> Unit, arg: String) = Column {
    Text("Sheet with arg: $arg")
    Button(onClick = showFeed) {
        Text("Click me to navigate!")
    }
    Button(onClick = showAnotherSheet) {
        Text("Click me to show another sheet!")
    }
}
