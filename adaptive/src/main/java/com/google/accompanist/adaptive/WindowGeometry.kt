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

package com.google.accompanist.adaptive

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.window.layout.DisplayFeature
import androidx.window.layout.WindowInfoTracker

/**
 * A description of the current window geometry.
 */
public interface WindowGeometry {

    /**
     * The current [WindowSizeClass].
     */
    val windowSizeClass: WindowSizeClass

    /**
     * The current list of known [DisplayFeature]s.
     */
    val displayFeatures: List<DisplayFeature>
}

/**
 * Calculates the [WindowGeometry] for the given [activity].
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
public fun calculateWindowGeometry(activity: Activity): WindowGeometry {
    val windowSizeClass = calculateWindowSizeClass(activity)

    val windowInfoTracker = remember(activity) { WindowInfoTracker.getOrCreate(activity) }
    val windowLayoutInfo = remember(windowInfoTracker, activity) {
        windowInfoTracker.windowLayoutInfo(activity)
    }

    val displayFeatures by produceState(initialValue = emptyList<DisplayFeature>()) {
        windowLayoutInfo.collect { info ->
            value = info.displayFeatures
        }
    }

    return object : WindowGeometry {
        override val windowSizeClass: WindowSizeClass
            get() = windowSizeClass

        override val displayFeatures: List<DisplayFeature>
            get() = displayFeatures
    }
}
