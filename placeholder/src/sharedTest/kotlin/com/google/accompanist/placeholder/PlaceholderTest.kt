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
package com.google.accompanist.placeholder

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.google.accompanist.internal.test.IgnoreOnRobolectric
import com.google.accompanist.internal.test.assertPixels
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    @Category(IgnoreOnRobolectric::class) // captureToImage doesn't work on Robolectric
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
    @Category(IgnoreOnRobolectric::class) // captureToImage doesn't work on Robolectric
    fun placeholder_switchVisible2() {
        var visible by mutableStateOf(true)

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(128.dp)
                    .background(color = Color.Black)
                    .placeholder(
                        visible = visible,
                        color = Color.Gray,
                        highlight = Solid(Color.Red)
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
    @Category(IgnoreOnRobolectric::class) // captureToImage doesn't work on Robolectric
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
    @Category(IgnoreOnRobolectric::class) // captureToImage doesn't work on Robolectric
    fun placeholder_switchAnimatedBrush() {
        var animatedBrush by mutableStateOf(Solid(Color.Red))

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(128.dp)
                    .background(color = Color.Black)
                    .placeholder(
                        visible = true,
                        color = Color.Gray,
                        highlight = animatedBrush
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

        animatedBrush = Solid(Color.Blue)

        composeTestRule.onNodeWithTag(contentTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .captureToImage()
            .assertPixels(Color.Blue)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    @Category(IgnoreOnRobolectric::class) // captureToImage doesn't work on Robolectric
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
            // There is no stable API to assert the shape.
            // So check the color of the vertices simply.
            .assertPixelsOfVertices(Color.Black)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    @Category(IgnoreOnRobolectric::class) // captureToImage doesn't work on Robolectric
    fun placeholder_switchShape2() {
        var shape by mutableStateOf(RectangleShape)

        composeTestRule.setContent {
            Box(
                Modifier
                    .size(20.dp)
                    .background(color = Color.Black)
                    .placeholder(
                        visible = true,
                        color = Color.Gray,
                        highlight = Solid(Color.Red),
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
            // There is no stable API to assert the shape.
            // So check the color of the vertices simply.
            .assertPixelsOfVertices(Color.Black)
    }

    @Test
    fun placeholder_inspectableParameter1() {
        val highlight = PlaceholderHighlight.shimmer(Color.Red)
        val modifier = Modifier.placeholder(
            visible = true,
            color = Color.Blue,
            highlight = highlight,
        ) as InspectableValue

        assertThat(modifier.nameFallback).isEqualTo("placeholder")
        assertThat(modifier.valueOverride).isEqualTo(true)
        assertThat(modifier.inspectableElements.asIterable()).containsExactly(
            ValueElement("visible", true),
            ValueElement("color", Color.Blue),
            ValueElement("highlight", highlight),
            ValueElement("shape", RectangleShape)
        )
    }

    @Test
    fun placeholder_inspectableParameter2() {
        val highlight = PlaceholderHighlight.fade(Color.Red)
        val modifier = Modifier.placeholder(
            visible = true,
            color = Color.Blue,
            highlight = highlight,
        ) as InspectableValue

        assertThat(modifier.nameFallback).isEqualTo("placeholder")
        assertThat(modifier.valueOverride).isEqualTo(true)
        assertThat(modifier.inspectableElements.asIterable()).containsExactly(
            ValueElement("visible", true),
            ValueElement("color", Color.Blue),
            ValueElement("highlight", highlight),
            ValueElement("shape", RectangleShape)
        )
    }
}

internal class Solid(
    private val color: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(delayMillis = 0, durationMillis = 500),
        repeatMode = RepeatMode.Restart
    )
) : PlaceholderHighlight {
    override fun alpha(progress: Float): Float = 1f

    override fun brush(progress: Float, size: Size): Brush {
        return SolidColor(color)
    }
}
