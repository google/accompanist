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

package com.google.accompanist.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.InspectableValue
import androidx.compose.ui.platform.ValueElement
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import com.google.accompanist.imageloading.test.assertPixels
import com.google.accompanist.placeholder.PlaceholderAnimatedBrush.Companion.fade
import com.google.accompanist.placeholder.PlaceholderAnimatedBrush.Companion.shimmer
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@LargeTest
@RunWith(JUnit4::class)
class PlaceholderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val contentTag = "Content"

    @Before
    fun before() {
        isDebugInspectorInfoEnabled = true
    }

    @After
    fun after() {
        isDebugInspectorInfoEnabled = false
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun placeholder_switchVisible1() {
        var visible by mutableStateOf(true)

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(128.dp)
                    .background(color = Color.Black)
                    .placeholder(visible = visible, color = Color.Red)
                    .testTag(contentTag)
            )
        }

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Red)

        visible = false

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Black)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun placeholder_switchVisible2() {
        var visible by mutableStateOf(true)

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(128.dp)
                    .background(color = Color.Black)
                    .placeholder(
                        visible = visible,
                        animatedBrush = shimmer(Color.Red, Color.Red)
                    )
                    .testTag(contentTag)
            )
        }

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Red)

        visible = false

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Black)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun placeholder_switchColor() {
        var color by mutableStateOf(Color.Red)

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(128.dp)
                    .background(color = Color.Black)
                    .placeholder(visible = true, color = color)
                    .testTag(contentTag)
            )
        }

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Red)

        color = Color.Blue

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Blue)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun placeholder_switchAnimatedBrush() {
        var animatedBrush by mutableStateOf(shimmer(Color.Red, Color.Red))

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(128.dp)
                    .background(color = Color.Black)
                    .placeholder(
                        visible = true,
                        animatedBrush = animatedBrush
                    )
                    .testTag(contentTag)
            )
        }

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Red)

        animatedBrush = fade(Color.Blue, Color.Blue)

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Blue)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun placeholder_switchShape1() {
        var shape by mutableStateOf(RectangleShape)

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(20.dp)
                    .background(color = Color.Black)
                    .placeholder(
                        visible = true,
                        color = Color.Red,
                        shape = shape
                    )
                    .testTag(contentTag)
            )
        }

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(20.dp)
            .assertHeightIsEqualTo(20.dp)
            .captureToImage()
            .assertPixels(Color.Red)

        shape = CircleShape

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(20.dp)
            .assertHeightIsEqualTo(20.dp)
            .captureToImage()
            .assertShape(
                density = composeTestRule.density,
                shape = CircleShape,
                shapeColor = Color.Red,
                backgroundColor = Color.Black
            )
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun placeholder_switchShape2() {
        var shape by mutableStateOf(RectangleShape)

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(20.dp)
                    .background(color = Color.Black)
                    .placeholder(
                        visible = true,
                        animatedBrush = shimmer(Color.Red, Color.Red),
                        shape = shape
                    )
                    .testTag(contentTag)
            )
        }

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(20.dp)
            .assertHeightIsEqualTo(20.dp)
            .captureToImage()
            .assertPixels(Color.Red)

        shape = CircleShape

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(20.dp)
            .assertHeightIsEqualTo(20.dp)
            .captureToImage()
            .assertShape(
                density = composeTestRule.density,
                shape = CircleShape,
                shapeColor = Color.Red,
                backgroundColor = Color.Black
            )
    }

    @Test
    fun placeholder_inspectableParameter1() {
        val modifier = Modifier.placeholder(true, Color.Magenta) as InspectableValue
        assertThat(modifier.nameFallback).isEqualTo("placeholder")
        assertThat(modifier.valueOverride).isEqualTo(true)
        assertThat(modifier.inspectableElements.asIterable()).containsExactly(
            ValueElement("visible", true),
            ValueElement("color", Color.Magenta),
            ValueElement("shape", RectangleShape)
        )
    }

    @Test
    fun placeholder_inspectableParameter2() {
        val modifier = Modifier.placeholder(true, shimmer()) as InspectableValue
        assertThat(modifier.nameFallback).isEqualTo("placeholder")
        assertThat(modifier.valueOverride).isEqualTo(true)
        assertThat(modifier.inspectableElements.asIterable()).containsExactly(
            ValueElement("visible", true),
            ValueElement("animatedBrush", shimmer()),
            ValueElement("shape", RectangleShape)
        )
    }
}
