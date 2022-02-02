package com.google.accompanist.videoplayer
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
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.videoplayer.icons.Forward10
import com.google.accompanist.videoplayer.icons.Fullscreen
import com.google.accompanist.videoplayer.icons.FullscreenExit
import com.google.accompanist.videoplayer.icons.MoreVert
import com.google.accompanist.videoplayer.icons.Pause
import com.google.accompanist.videoplayer.icons.Replay10
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoPlayerControl(
    state: VideoPlayerState,
    title: String,
    subtitle: String? = null,
    background: Color = Color.Black.copy(0.30f),
    contentColor: Color = Color.White,
    progressLineColor: Color = MaterialTheme.colors.primaryVariant,
    onOptionsClick: (() -> Unit)? = null,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ControlHeader(
                modifier = Modifier.fillMaxWidth(),
                title = title,
                subtitle = subtitle,
                onOptionsClick = onOptionsClick
            )
            PlaybackControl(
                isPlaying = state.isPlaying.value,
                control = state.control
            )
            TimelineControl(
                modifier = Modifier.fillMaxWidth(),
                progressLineColor = progressLineColor,
                isFullScreen = state.isFullscreen.value,
                videoDurationMs = state.videoDurationMs.value,
                videoPositionMs = state.videoPositionMs.value
            ) {
                state.control.setFullscreen(!state.isFullscreen.value)
            }
        }
    }
}

@Composable
private fun ControlHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    onOptionsClick: (() -> Unit)?,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = title,
                color = LocalContentColor.current,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = LocalContentColor.current.copy(0.80f),
                    style = MaterialTheme.typography.body1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onOptionsClick != null) {
            AdaptiveIconButton(
                modifier = Modifier.size(SmallIconButtonSize),
                onClick = onOptionsClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun PlaybackControl(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    control: VideoPlayerControl
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(
            modifier = Modifier
                .size(BigIconButtonSize)
                .padding(10.dp),
            onClick = control::rewind
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Rounded.Replay10,
                contentDescription = null
            )
        }
        IconButton(
            modifier = Modifier.size(BigIconButtonSize),
            onClick = { if (isPlaying) control.pause() else control.play() }
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null
            )
        }
        IconButton(
            modifier = Modifier
                .size(BigIconButtonSize)
                .padding(10.dp),
            onClick = control::forward
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Rounded.Forward10,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun TimelineControl(
    modifier: Modifier,
    progressLineColor: Color,
    isFullScreen: Boolean,
    videoDurationMs: Long,
    videoPositionMs: Long,
    onFullScreenToggle: () -> Unit,
) {
    val timestamp = remember(videoDurationMs, videoPositionMs.milliseconds.inWholeSeconds) {
        prettyVideoTimestamp(videoDurationMs.milliseconds, videoPositionMs.milliseconds)
    }
    val progress = remember(videoPositionMs) {
        1.0f - ((videoDurationMs - videoPositionMs) / videoDurationMs.toFloat())
    }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = timestamp)
            Spacer(modifier = Modifier.weight(1.0f))
            AdaptiveIconButton(
                modifier = Modifier.size(SmallIconButtonSize),
                onClick = onFullScreenToggle
            ) {
                Icon(
                    imageVector = if (isFullScreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            progress = progress,
            color = progressLineColor,
            backgroundColor = Color.LightGray
        )
    }
}


/**
 * Allow the button to be any size instead of constraining it to 48dp
 * **/
@Composable
private fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
    }
}

private val BigIconButtonSize = 52.dp
private val SmallIconButtonSize = 32.dp


