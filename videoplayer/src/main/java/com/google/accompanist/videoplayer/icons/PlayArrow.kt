package com.google.accompanist.videoplayer.icons
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

internal val Icons.Rounded.PlayArrow: ImageVector
    get() {
        if (_playArrow != null) {
            return _playArrow!!
        }
        _playArrow = materialIcon(name = "Rounded.PlayArrow") {
            materialPath {
                moveTo(8.0f, 6.82f)
                verticalLineToRelative(10.36f)
                curveToRelative(0.0f, 0.79f, 0.87f, 1.27f, 1.54f, 0.84f)
                lineToRelative(8.14f, -5.18f)
                curveToRelative(0.62f, -0.39f, 0.62f, -1.29f, 0.0f, -1.69f)
                lineTo(9.54f, 5.98f)
                curveTo(8.87f, 5.55f, 8.0f, 6.03f, 8.0f, 6.82f)
                close()
            }
        }
        return _playArrow!!
    }

private var _playArrow: ImageVector? = null