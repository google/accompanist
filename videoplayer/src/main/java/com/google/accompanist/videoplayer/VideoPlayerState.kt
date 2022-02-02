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
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Build and remember default implementation of [VideoPlayerState]
 *
 * @param hideControllerAfterMs time after which [VideoPlayerState.isControlUiVisible] will be set to false.
 * interactions with [VideoPlayerState.control] will reset the internal counter.
 * if null is provided the controller wont be hidden until [VideoPlayerState.hideControlUi] is called again
 * @param videoPositionPollInterval interval on which the [VideoPlayerState.videoPositionMs] will be updated,
 * you can set a lower number to update the ui faster though it will consume more cpu resources.
 * Take in consideration that this value is updated only when [VideoPlayerState.isControlUiVisible] is set to true,
 * if you need to get the last value use [ExoPlayer.getCurrentPosition].
 * @param coroutineScope this scope will be used to poll for [VideoPlayerState.videoPositionMs] updates
 * @param context used to build an [ExoPlayer] instance
 * @param config you can use this to configure [ExoPlayer]
 * */
@Composable
fun rememberVideoPlayerState(
    hideControllerAfterMs: Long? = 3000,
    videoPositionPollInterval: Long = 500,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    config: ExoPlayer.Builder.() -> Unit = {
        setSeekForwardIncrementMs(10 * 1000)
        setSeekForwardIncrementMs(10 * 1000)
    }
): VideoPlayerState = remember {
    VideoPlayerStateImpl(
        player = ExoPlayer.Builder(context).apply(config).build(),
        coroutineScope = coroutineScope,
        hideControllerAfterMs = hideControllerAfterMs,
        videoPositionPollInterval = videoPositionPollInterval
    ).also {
        it.player.addListener(it)
    }
}

class VideoPlayerStateImpl(
    override val player: ExoPlayer,
    private val coroutineScope: CoroutineScope,
    private val hideControllerAfterMs: Long?,
    private val videoPositionPollInterval: Long,
) : VideoPlayerState, Player.Listener {
    override val videoSize = mutableStateOf(player.videoSize)
    override val videoResizeMode = mutableStateOf(ResizeMode.Fit)
    override val videoPositionMs = mutableStateOf(0L)
    override val videoDurationMs = mutableStateOf(0L)

    override val isFullscreen = mutableStateOf(false)
    override val isPlaying = mutableStateOf(player.isPlaying)
    override val playerState = mutableStateOf(player.playbackState)

    override val isOptionsUiVisible = mutableStateOf(false)
    override val isControlUiVisible = mutableStateOf(false)
    override val control = object : VideoPlayerControl {
        override fun play() {
            controlUiLastInteractionMs = 0
            player.play()
        }

        override fun pause() {
            controlUiLastInteractionMs = 0
            player.pause()
        }

        override fun forward() {
            controlUiLastInteractionMs = 0
            player.seekForward()
        }

        override fun rewind() {
            controlUiLastInteractionMs = 0
            player.seekBack()
        }

        override fun setVideoResize(mode: ResizeMode) {
            controlUiLastInteractionMs = 0
            videoResizeMode.value = mode
        }

        override fun setFullscreen(value: Boolean) {
            controlUiLastInteractionMs = 0
            isFullscreen.value = value
        }
    }

    override fun hideOptionsUi() {
        isOptionsUiVisible.value = true
    }

    override fun showOptionsUi() {
        isOptionsUiVisible.value = false
    }

    private var pollVideoPositionJob: Job? = null
    private var controlUiLastInteractionMs = 0L

    override fun hideControlUi() {
        controlUiLastInteractionMs = 0
        isControlUiVisible.value = false
        pollVideoPositionJob?.cancel()
        pollVideoPositionJob = null
    }

    override fun showControlUi() {
        isControlUiVisible.value = true
        pollVideoPositionJob?.cancel()
        pollVideoPositionJob = coroutineScope.launch {
            if (hideControllerAfterMs != null) {
                while (true) {
                    videoPositionMs.value = player.currentPosition
                    delay(videoPositionPollInterval)
                    controlUiLastInteractionMs += videoPositionPollInterval
                    if (controlUiLastInteractionMs >= hideControllerAfterMs) {
                        hideControlUi()
                        break
                    }
                }
            } else {
                while (true) {
                    videoPositionMs.value = player.currentPosition
                    delay(videoPositionPollInterval)
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        this.isPlaying.value = isPlaying
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) videoDurationMs.value = player.duration
        this.playerState.value = playbackState
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        this.videoSize.value = videoSize
    }
}


interface VideoPlayerState {
    val player: ExoPlayer

    val videoSize: State<VideoSize>
    val videoResizeMode: State<ResizeMode>
    val videoPositionMs: State<Long>
    val videoDurationMs: State<Long>

    val isFullscreen: State<Boolean>
    val isPlaying: State<Boolean>
    val playerState: State<Int>

    val isOptionsUiVisible: State<Boolean>
    val isControlUiVisible: State<Boolean>
    val control: VideoPlayerControl

    fun hideOptionsUi()
    fun showOptionsUi()

    fun hideControlUi()
    fun showControlUi()
}

interface VideoPlayerControl {
    fun play()
    fun pause()

    fun forward()
    fun rewind()

    fun setFullscreen(value: Boolean)
    fun setVideoResize(mode: ResizeMode)
}