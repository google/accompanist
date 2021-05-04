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

package com.google.accompanist.sample.insets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ViewWindowInsetObserver
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberWindowInsetsTypePaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
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
            AccompanistSampleTheme {
                // Instead of calling ProvideWindowInsets, we use CompositionLocalProvider to provide
                // the WindowInsets instance from above to LocalWindowInsets
                CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                    Sample()
                }
            }
        }
    }
}

@Composable
private fun Sample() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }

    Surface {
        Box(Modifier.fillMaxSize()) {
            /**
             * We use [TopAppBar] from accompanist-insets-ui which allows us to provide
             * content padding matching the system bars insets.
             */
            TopAppBar(
                title = { Text(stringResource(R.string.insets_title_fragment)) },
                backgroundColor = MaterialTheme.colors.surface,
                contentPadding = rememberWindowInsetsTypePaddingValues(
                    LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            FloatingActionButton(
                onClick = { /* */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Face icon"
                )
            }
        }
    }
}
