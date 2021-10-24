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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun rememberMapState(
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    zoom: Float = 2f
): MapState {
    return remember {
        MapState(
            latitude,
            longitude,
            zoom
        )
    }
}

@Stable
class MapState(
    latitude: Double,
    longitude: Double,
    zoom: Float
) {
    var latitude: Double by mutableStateOf(latitude)
    var longitude: Double by mutableStateOf(longitude)
    var zoom: Float by mutableStateOf(zoom)

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + zoom.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapState) return false

        return latitude == other.latitude &&
            longitude == other.longitude &&
            zoom == other.zoom
    }

    override fun toString(): String {
        return "Lat: $latitude Lng: $longitude Zoom: $zoom"
    }
}

@Composable
fun GoogleMaps(
    mapState: MapState,
    modifier: Modifier = Modifier,
    onMapReady: (GoogleMap) -> Unit = {},
    onMapUpdated: (GoogleMap) -> Unit = {},
) {
    val mapContainer = rememberMapContainer()
    val scope = rememberCoroutineScope()

    val cameraPosition = CameraPosition.fromLatLngZoom(LatLng(mapState.latitude, mapState.longitude), mapState.zoom)

    LaunchedEffect(key1 = mapContainer, block = {
        val mapFragment = (mapContainer.context as FragmentActivity)
            .supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        val map = mapFragment.awaitMap()
        onMapReady(map)

        map.setOnCameraIdleListener {
            val newPosition = map.cameraPosition
            mapState.latitude = newPosition.target.latitude
            mapState.longitude = newPosition.target.longitude
            mapState.zoom = newPosition.zoom
        }
    })

    AndroidView(factory = { mapContainer }, modifier = modifier) {
    val mapFragment = (it.context as FragmentActivity)
        .supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment

    scope.launch {
        val map = mapFragment.awaitMap()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

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
