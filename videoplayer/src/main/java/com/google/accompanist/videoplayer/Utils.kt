@file:Suppress("NOTHING_TO_INLINE")
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
package com.google.accompanist.videoplayer

import android.widget.FrameLayout
import androidx.compose.ui.unit.Constraints
import com.google.android.exoplayer2.video.VideoSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal inline fun VideoSize.aspectRatio(): Float =
    if (height == 0 || width == 0) 0f else (width * pixelWidthHeightRatio) / height

/**
 * The [FrameLayout] will not resize itself if the fractional difference between its natural
 * aspect ratio and the requested aspect ratio falls below this threshold.
 *
 *
 * This tolerance allows the view to occupy the whole of the screen when the requested aspect
 * ratio is very close, but not exactly equal to, the aspect ratio of the screen. This may reduce
 * the number of view layers that need to be composited by the underlying system, which can help
 * to reduce power consumption.
 */
private const val MAX_ASPECT_RATIO_DIFFERENCE_FRACTION = 0.01f

internal inline fun Constraints.resizeForVideo(
    mode: ResizeMode,
    aspectRatio: Float
): Constraints {
    if (aspectRatio <= 0f) return this

    var width = maxWidth
    var height = maxHeight
    val constraintAspectRatio: Float = (width / height).toFloat()
    val difference = aspectRatio / constraintAspectRatio - 1

    if (kotlin.math.abs(difference) <= MAX_ASPECT_RATIO_DIFFERENCE_FRACTION) {
        return this
    }

    when (mode) {
        ResizeMode.Fit -> {
            if (difference > 0) {
                height = (width / aspectRatio).toInt()
            } else {
                width = (height * aspectRatio).toInt()
            }
        }
        ResizeMode.Zoom -> {
            if (difference > 0) {
                width = (height * aspectRatio).toInt()
            } else {
                height = (width / aspectRatio).toInt()
            }
        }
        ResizeMode.FixedWidth -> {
            height = (width / aspectRatio).toInt()
        }
        ResizeMode.FixedHeight -> {
            width = (height * aspectRatio).toInt()
        }
        ResizeMode.Fill -> Unit
    }

    return this.copy(maxWidth = width, maxHeight = height)
}


/**
 * Will return a timestamp denoting the current video [position] and the [duration] in the following
 * format "mm:ss / mm:ss"
 * **/
internal inline fun prettyVideoTimestamp(
    duration: Duration,
    position: Duration
): String = buildString {
    appendMinutesAndSeconds(duration)
    append(" / ")
    appendMinutesAndSeconds(position)
}

/**
 * Will split [duration] in minutes and seconds and append it to [this] in the following format "mm:ss"
 * */
private fun StringBuilder.appendMinutesAndSeconds(duration: Duration) {
    val minutes = duration.inWholeMinutes
    val seconds = (duration - minutes.minutes).inWholeSeconds
    appendDoubleDigit(minutes)
    append(':')
    appendDoubleDigit(seconds)
}

/**
 * Will append [value] as double digit to [this].
 * If a single digit value is passed, ex: 4 then a 0 will be added as prefix resulting in 04
 * */
private fun StringBuilder.appendDoubleDigit(value: Long) {
    if (value < 10) {
        append(0)
        append(value)
    } else {
        append(value)
    }
}