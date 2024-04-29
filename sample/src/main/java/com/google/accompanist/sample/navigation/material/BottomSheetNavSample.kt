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

@file:Suppress("DEPRECATION")

package com.google.accompanist.sample.navigation.material

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.dismiss.DismissHandler
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

private object Destinations {
    const val Home = "HOME"
    const val Feed = "FEED"
    const val Sheet = "SHEET"
    const val Sheet2 = "SHEET2"
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun BottomSheetNavDemo() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    ModalBottomSheetLayout(bottomSheetNavigator) {
        NavHost(navController, Destinations.Home) {
            composable(Destinations.Home) {
                HomeScreen(
                    showSheet = {
                        navController.navigate(Destinations.Sheet + "?arg=From Home Screen")
                    },
                    showFeed = { navController.navigate(Destinations.Feed) }
                )
            }
            composable(Destinations.Feed) { Text("Feed!") }
            bottomSheet(Destinations.Sheet + "?arg={arg}") { backstackEntry ->
                val arg = backstackEntry.arguments?.getString("arg") ?: "Missing argument :("
                BottomSheet(
                    showFeed = { navController.navigate(Destinations.Feed) },
                    showAnotherSheet = {
                        navController.navigate(Destinations.Sheet2 + "?arg=Confirmation Dialog to dismiss the sheet")
                    },
                    arg = arg
                )
            }

            bottomSheet(Destinations.Sheet2 + "?arg={arg}") { backstackEntry ->
                val arg = backstackEntry.arguments?.getString("arg") ?: "Missing argument :("
                BottomSheet2(
                    showFeed = { navController.navigate(Destinations.Feed) },
                    showAnotherSheet = {
                        navController.navigate(Destinations.Sheet2 + "?arg=${UUID.randomUUID()}")
                    },
                    navigateUp = {
                        navController.popBackStack()
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
private fun BottomSheet(
    showFeed: () -> Unit,
    showAnotherSheet: () -> Unit,
    arg: String
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Sheet with arg: $arg")
        Button(onClick = showFeed) {
            Text("Click me to navigate!")
        }
        Button(onClick = showAnotherSheet) {
            Text("Click me to show another sheet!")
        }
    }
}

@Composable
private fun BottomSheet2(
    showFeed: () -> Unit,
    showAnotherSheet: () -> Unit,
    navigateUp: () -> Unit,
    arg: String
) {
    var showConfirmationDialog by remember {
        mutableStateOf(false)
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            onCancel = { showConfirmationDialog = false },
            onApprove = { navigateUp() }
        )
    }
    DismissHandler {
        showConfirmationDialog = true
    }

    BottomSheet(
        showFeed = showFeed,
        showAnotherSheet = showAnotherSheet,
        arg = arg
    )
}

@Composable
private fun ConfirmationDialog(
    onApprove: () -> Unit,
    onCancel: () -> Unit
) = AlertDialog(
    title = { Text(text = "Alert!") },
    text = { Text("Do you really want do leave that screen?") },
    onDismissRequest = onCancel,
    buttons = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onCancel() }
            ) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { onApprove() }
            ) {
                Text(text = "Ok")
            }
        }
    }
)
