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

package com.google.accompanist.swiperefresh

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A class to encapsulate details of different indicator sizes.
 *
 * @param size The overall size of the indicator.
 * @param arcRadius The radius of the arc.
 * @param strokeWidth The width of the arc stroke.
 * @param arrowWidth The width of the arrow.
 * @param arrowHeight The height of the arrow.
 */
data class IndicatorSize(
    val size: Dp,
    val arcRadius: Dp,
    val strokeWidth: Dp,
    val arrowWidth: Dp,
    val arrowHeight: Dp,
    val refreshTrigger: Dp
) {
    companion object {
        val DEFAULT = IndicatorSize(
            size = SIZE_DEFAULT,
            arcRadius = CENTER_RADIUS_DEFAULT,
            strokeWidth = STROKE_WIDTH_DEFAULT,
            arrowWidth = ARROW_WIDTH_DEFAULT,
            arrowHeight = ARROW_HEIGHT_DEFAULT,
            refreshTrigger = REFRESH_TRIGGER_DEFAULT
        )
        val LARGE = IndicatorSize(
            size = SIZE_LARGE,
            arcRadius = CENTER_RADIUS_LARGE,
            strokeWidth = STROKE_WIDTH_LARGE,
            arrowWidth = ARROW_WIDTH_LARGE,
            arrowHeight = ARROW_HEIGHT_LARGE,
            refreshTrigger = REFRESH_TRIGGER_LARGE
        )
    }
}

private val SIZE_DEFAULT = 40.dp
private val CENTER_RADIUS_DEFAULT = 7.5.dp
private val STROKE_WIDTH_DEFAULT = 2.5.dp
private val ARROW_WIDTH_DEFAULT = 10.dp
private val ARROW_HEIGHT_DEFAULT = 5.dp
private val REFRESH_TRIGGER_DEFAULT = 64.dp

private val SIZE_LARGE = 56.dp
private val CENTER_RADIUS_LARGE = 11.dp
private val STROKE_WIDTH_LARGE = 3.dp
private val ARROW_WIDTH_LARGE = 12.dp
private val ARROW_HEIGHT_LARGE = 6.dp
private val REFRESH_TRIGGER_LARGE = 80.dp
