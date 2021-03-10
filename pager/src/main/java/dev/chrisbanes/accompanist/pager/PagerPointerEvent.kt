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

package dev.chrisbanes.accompanist.pager

import android.util.Log
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal sealed class PagerPointerEvent {
    data class Down(val change: PointerInputChange) : PagerPointerEvent()

    data class Drag(
        val dx: Float,
        val dy: Float,
        val change: PointerInputChange,
    ) : PagerPointerEvent()

    data class Up(
        val velocity: Velocity
    ) : PagerPointerEvent()

    object Cancel : PagerPointerEvent()
}

internal suspend fun PointerInputScope.detectPagerPointerEvents(
    channel: SendChannel<PagerPointerEvent>
) = coroutineScope {
    val velocityTracker = VelocityTracker()
    forEachGesture {
        try {
            awaitPointerEventScope {
                val down = awaitFirstDown()

                if (DebugLog) {
                    Log.d("PagerPointerEvent", "detectPagerPointerEvents: DOWN")
                }

                // Reset the velocity tracker and add our initial down event
                velocityTracker.resetTracking()
                velocityTracker.addPosition(down)

                // Send the down event
                launch {
                    channel.send(PagerPointerEvent.Down(down))
                }

                // TODO: need to wait for touch slop?

                horizontalDrag(down.id) { change ->
                    // Add the movement to the velocity tracker
                    velocityTracker.addPosition(change)

                    val delta = change.positionChange()
                    val event = PagerPointerEvent.Drag(
                        dx = delta.x,
                        dy = delta.y,
                        change = change,
                    )
                    launch {
                        channel.send(event)
                    }
                }

                if (DebugLog) {
                    Log.d("PagerPointerEvent", "detectPagerPointerEvents: UP")
                }

                launch {
                    channel.send(
                        PagerPointerEvent.Up(velocityTracker.calculateVelocity())
                    )
                }
            }
        } catch (e: CancellationException) {
            channel.send(PagerPointerEvent.Cancel)
        }
    }
}

private fun VelocityTracker.addPosition(change: PointerInputChange) {
    addPosition(change.uptimeMillis, change.position)
}
