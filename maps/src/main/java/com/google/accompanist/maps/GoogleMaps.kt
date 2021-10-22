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

package com.google.accompanist.maps

import android.view.LayoutInflater
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch

@Composable
fun GoogleMaps(
    latitude: Double,
    longitude: Double,
    zoom: Float,
    onCameraMoved: (Double, Double, Float) -> Unit,
    modifier: Modifier = Modifier,
    onMapReady: (GoogleMap) -> Unit = {},
    onMapUpdated: (GoogleMap) -> Unit = {},
) {
    val mapContainer = rememberMapContainer()
    val scope = rememberCoroutineScope()

    val cameraPosition = remember(latitude, longitude) {
        LatLng(latitude, longitude)
    }

    LaunchedEffect(key1 = mapContainer, block = {
        val mapFragment = (mapContainer.context as FragmentActivity)
            .supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        val map = mapFragment.awaitMap()
        onMapReady(map)

        map.setOnCameraIdleListener {
            val newPosition = map.cameraPosition
            onCameraMoved(
                newPosition.target.latitude,
                newPosition.target.longitude,
                newPosition.zoom
            )
        }
    })

    AndroidView(factory = { mapContainer }, modifier = modifier) {
    val mapFragment = (it.context as FragmentActivity)
        .supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment

    val position = cameraPosition
    val z = zoom
    scope.launch {
        val map = mapFragment.awaitMap()

        val newCameraPosition = CameraPosition.fromLatLngZoom(position, z)
        if (map.cameraPosition != newCameraPosition) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
        }

        onMapUpdated(map)
    }
}
}

@Composable
private fun rememberMapContainer(): View {
    val context = LocalContext.current
    val mapContainer = remember {
        LayoutInflater.from(context)
            .inflate(R.layout.layout_map, null, false)
    }

    return mapContainer
}
