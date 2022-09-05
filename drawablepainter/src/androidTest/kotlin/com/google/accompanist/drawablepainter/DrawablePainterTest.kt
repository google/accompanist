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

package com.google.accompanist.drawablepainter

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawablePainterTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun drawableWithIntrinsicSize() {
        lateinit var drawable: Drawable
        rule.setContent {
            with(LocalDensity.current) {
                drawable = IntrinsicSizeDrawable(
                    width = 128.dp.roundToPx(),
                    height = 32.dp.roundToPx()
                )
            }
            Image(
                modifier = Modifier.width(256.dp),
                painter = rememberDrawablePainter(drawable = drawable),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }

        rule.waitForIdle()
        assertThat(drawable.bounds.width()).isEqualTo(drawable.intrinsicWidth)
        assertThat(drawable.bounds.height()).isEqualTo(drawable.intrinsicHeight)
    }

    @Test
    fun drawableWithoutIntrinsicSize() {
        val drawable = NoIntrinsicSizeDrawable()
        rule.setContent {
            Image(
                modifier = Modifier
                    .width(128.dp)
                    .height(64.dp)
                    .testTag(TestTag),
                painter = rememberDrawablePainter(drawable = drawable),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }

        rule.waitForIdle()
        val imageSize = rule.onNodeWithTag(TestTag).fetchSemanticsNode().size
        assertThat(drawable.bounds.width()).isEqualTo(imageSize.width)
        assertThat(drawable.bounds.height()).isEqualTo(imageSize.height)
    }

    private companion object {
        const val TestTag = "ImageItem"
    }
}
