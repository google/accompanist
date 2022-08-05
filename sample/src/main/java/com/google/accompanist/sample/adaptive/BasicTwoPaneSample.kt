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

package com.google.accompanist.sample.adaptive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.adaptive.DelegateTwoPaneStrategy
import com.google.accompanist.adaptive.FractionHorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.FractionVerticalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.TwoPaneStrategy
import com.google.accompanist.adaptive.calculateWindowGeometry
import com.google.accompanist.sample.AccompanistSampleTheme

class BasicTwoPaneSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                val windowGeometry = calculateWindowGeometry(this)

                TwoPane(
                    first = {
                        Card(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("First")
                            }
                        }
                    },
                    second = {
                        Card(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("Second")
                            }
                        }
                    },
                    strategy = TwoPaneStrategy(
                        fallbackStrategy = DelegateTwoPaneStrategy(
                            firstStrategy = FractionVerticalTwoPaneStrategy(
                                splitFraction = 0.75f,
                            ),
                            secondStrategy = FractionHorizontalTwoPaneStrategy(
                                splitFraction = 0.75f,
                            ),
                            useFirstStrategy = { _, _, layoutCoordinates ->
                                // Split vertically if the height is larger than the width
                                layoutCoordinates.size.height >= layoutCoordinates.size.width
                            }
                        ),
                        windowGeometry = windowGeometry
                    ),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
