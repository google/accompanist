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

package com.google.accompanist.sample.maps

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.google.accompanist.maps.GoogleMaps

class BasicMapSampleActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var lat by rememberSaveable { mutableStateOf(-32.0) }
            var lng by rememberSaveable { mutableStateOf(151.0) }
            var zoom by rememberSaveable { mutableStateOf(1f) }

            GoogleMaps(
                lat,
                lng,
                zoom,
                onCameraMoved = { latitude, longitude, z ->
                    lat = latitude
                    lng = longitude
                    zoom = z

                    Log.d("Maps", "Camera moved")
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
