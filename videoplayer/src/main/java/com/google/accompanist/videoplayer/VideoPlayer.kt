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
import android.os.Build
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @param playerState state to attach to this composable.
 * @param lifecycleOwner required to manage the ExoPlayer instance.
 * @param controller you can use [VideoPlayerControl] or alternatively implement your own
 * */
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    playerState: VideoPlayerState,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    controller: @Composable () -> Unit,
) {
    VideoPlayer(
        modifier = modifier,
        playerState = playerState,
        controller = controller
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> playerState.showControlUi()
                    Lifecycle.Event.ON_STOP -> playerState.player.pause()
                    else -> Unit
                }
            } else {
                when (event) {
                    Lifecycle.Event.ON_START -> playerState.showControlUi()
                    Lifecycle.Event.ON_STOP -> playerState.player.pause()
                    else -> Unit
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerState.player.release()
        }
    }
}

@Composable
private fun VideoPlayer(
    modifier: Modifier,
    playerState: VideoPlayerState,
    controller: @Composable () -> Unit
) {
    Box(modifier = modifier.defaultPlayerTapGestures(playerState)) {
        AndroidView(
            modifier = Modifier.adaptiveLayout(
                aspectRatio = playerState.videoSize.value.aspectRatio(),
                resizeMode = playerState.videoResizeMode.value
            ),
            factory = { context ->
                SurfaceView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }.also {
                    playerState.player.setVideoSurfaceView(it)
                }
            }
        )

        AnimatedVisibility(
            visible = playerState.isControlUiVisible.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            controller()
        }
    }
}

@JvmInline
value class ResizeMode private constructor(val value: Int) {
    companion object {
        val Fit = ResizeMode(0)
        val FixedWidth = ResizeMode(1)
        val FixedHeight = ResizeMode(2)
        val Fill = ResizeMode(3)
        val Zoom = ResizeMode(4)
    }
}

private fun Modifier.adaptiveLayout(
    aspectRatio: Float,
    resizeMode: ResizeMode = ResizeMode.Fit
) = layout { measurable, constraints ->
    val resizedConstraint = constraints.resizeForVideo(resizeMode, aspectRatio)
    val placeable = measurable.measure(resizedConstraint)
    layout(constraints.maxWidth, constraints.maxHeight) {
        // Center x and y axis relative to the layout
        placeable.placeRelative(
            x = (constraints.maxWidth - resizedConstraint.maxWidth) / 2,
            y = (constraints.maxHeight - resizedConstraint.maxHeight) / 2
        )
    }
}

private fun Modifier.defaultPlayerTapGestures(playerState: VideoPlayerState) = pointerInput(Unit) {
    detectTapGestures(
        onDoubleTap = {
            if (playerState.videoResizeMode.value == ResizeMode.Zoom) {
                playerState.control.setVideoResize(ResizeMode.Fit)
            } else {
                playerState.control.setVideoResize(ResizeMode.Zoom)
            }
        },
        onTap = {
            if (playerState.isControlUiVisible.value) {
                playerState.hideControlUi()
            } else {
                playerState.showControlUi()
            }
        }
    )
}