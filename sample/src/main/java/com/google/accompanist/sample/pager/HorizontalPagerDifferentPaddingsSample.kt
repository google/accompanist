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

@file:Suppress("DEPRECATION")
package com.google.accompanist.sample.pager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.rememberRandomSampleImageUrl
import kotlin.math.roundToInt

class HorizontalPagerDifferentPaddingsSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                Surface {
                    Sample()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter") // Sample deals with paddings itself
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.horiz_pager_title_different_paddings)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPagerDifferentPaddings()
    }
}

/**
 * This demo demonstrates how to achieve a behavior where for the user it feels like
 * there is no start padding before the first item and no end padding after the last item,
 * but all other items are centered with half of the padding applied for the first/last item.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerDifferentPaddings() {
    val count = 4
    val padding = 16.dp

    HorizontalPager(
        count = count,
        contentPadding = PaddingValues(horizontal = padding),
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Card(
            Modifier
                .offset {
                    // Calculate the offset do neutralize paddings on the sides on
                    // the first and the last page.
                    val pageOffset = calculateCurrentOffsetForPage(page)
                    val offsetToFillStartPadding = minOf(page + pageOffset - 1, 0f)
                    val offsetToFillEndPadding = maxOf(page + pageOffset - count + 2, 0f)
                    val xOffset = padding.toPx() * (offsetToFillStartPadding + offsetToFillEndPadding)
                    IntOffset(x = xOffset.roundToInt(), y = 0)
                }
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(18.dp)
        ) {
            Image(
                painter = rememberImagePainter(
                    data = rememberRandomSampleImageUrl(width = 600),
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
