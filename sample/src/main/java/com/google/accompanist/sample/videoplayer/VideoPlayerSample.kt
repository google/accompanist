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

package com.google.accompanist.sample.videoplayer

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.videoplayer.VideoPlayer
import com.google.accompanist.videoplayer.VideoPlayerControl
import com.google.accompanist.videoplayer.rememberVideoPlayerState
import com.google.android.exoplayer2.MediaItem

class VideoPlayerSample : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                val playerState = rememberVideoPlayerState()

                Column {
                    VideoPlayer(
                        modifier = Modifier
                            .background(Color.Black)
                            .adaptiveSize(playerState.isFullscreen.value, LocalView.current),
                        playerState = playerState,
                    ) {
                        /**
                         * Use default control or implement your own
                         * */
                        VideoPlayerControl(
                            state = playerState,
                            title = "Elephant Dream",
                            subtitle = "By Blender Foundation",
                        )
                    }
                    if (!playerState.isFullscreen.value) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(500) {
                                Text(text = "More content")
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    playerState.player.setMediaItem(MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"))
                    playerState.player.prepare()
                    playerState.player.playWhenReady = true
                }
            }
        }
    }

}

private fun Modifier.adaptiveSize(fullscreen: Boolean, view: View): Modifier {
    return if (fullscreen) {
        hideSystemBars(view)
        fillMaxSize()
    } else {
        showSystemBars(view)
        fillMaxWidth().height(250.dp)
    }
}


private fun hideSystemBars(view: View) {
    val windowInsetsController = ViewCompat.getWindowInsetsController(view) ?: return
    // Configure the behavior of the hidden system bars
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    // Hide both the status bar and the navigation bar
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

private fun showSystemBars(view: View) {
    val windowInsetsController = ViewCompat.getWindowInsetsController(view) ?: return
    // Show both the status bar and the navigation bar
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
}