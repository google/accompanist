/*
 * Copyright 2020 The Android Open Source Project
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

@file:Suppress("DEPRECATION")

package com.google.accompanist.sample.insets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ViewWindowInsetObserver
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class InsetsFragmentSample : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which means we need to through handling
        // insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val content = FrameLayout(this)
        content.id = View.generateViewId()
        setContentView(content)

        supportFragmentManager.commit {
            replace(content.id, InsetsFragment())
        }
    }
}

class InsetsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

        // Create an ViewWindowInsetObserver using this view
        val observer = ViewWindowInsetObserver(this)
        // Call start() to start listening now.
        // The WindowInsets instance is returned to us.
        val windowInsets = observer.start()

        setContent {
            // Update the system bars to be translucent
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }

            AccompanistSampleTheme {
                // Instead of calling ProvideWindowInsets, we use CompositionLocalProvider to provide
                // the WindowInsets instance from above to LocalWindowInsets
                CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                    InsetsBasics()
                }
            }
        }
    }
}
