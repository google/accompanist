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

package com.google.accompanist.systemuicontroller

import android.os.Build
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest
import androidx.test.filters.SdkSuppress
import com.google.accompanist.internal.test.IgnoreOnRobolectric
import com.google.accompanist.internal.test.waitUntil
import com.google.accompanist.internal.test.withActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivitySystemUiControllerTest {
    @get:Rule
    val rule = ActivityScenarioRule(ComponentActivity::class.java)

    private lateinit var window: Window
    private lateinit var contentView: View

    @Before
    fun setup() {
        window = rule.scenario.withActivity { it.window }
        contentView = rule.scenario.withActivity { it.findViewById(android.R.id.content)!! }

        if (Build.VERSION.SDK_INT >= 29) {
            // On API 29+, the system can modify the bar colors to maintain contrast.
            // We disable that here to make it simple to assert expected values
            rule.scenario.onActivity {
                window.apply {
                    isNavigationBarContrastEnforced = false
                    isStatusBarContrastEnforced = false
                }
            }
        }
    }

    @Test
    fun statusBarColor() {
        rule.scenario.onActivity {
            // Now create an AndroidSystemUiController() and set the status bar color
            val controller = AndroidSystemUiController(contentView, window)
            controller.setStatusBarColor(Color.Blue, darkIcons = false)
        }

        // Assert that the color was set
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Blue)
    }

    @Test
    fun getStatusBarColor() {
        lateinit var controller: AndroidSystemUiController

        rule.scenario.onActivity {
            controller = AndroidSystemUiController(contentView, window)
            controller.setStatusBarColor(Color.Blue, darkIcons = false)
        }

        assertThat(controller.statusBarColor).isEqualTo(Color.Blue)
    }

    @Test
    fun navigationBarColor() {
        rule.scenario.onActivity {
            // Now create an AndroidSystemUiController() and set the status bar color
            val controller = AndroidSystemUiController(contentView, window)
            controller.setNavigationBarColor(Color.Green, darkIcons = false)
        }

        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Green)
    }

    @Test
    fun getNavigationBarColor() {
        lateinit var controller: AndroidSystemUiController

        rule.scenario.onActivity {
            controller = AndroidSystemUiController(contentView, window)
            controller.setNavigationBarColor(Color.Green, darkIcons = false)
        }

        assertThat(controller.navigationBarColor).isEqualTo(Color.Green)
    }

    @Test
    fun systemBarColor() {
        // Now create an AndroidSystemUiController() and set the system bar colors
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(contentView, window)
            controller.setSystemBarsColor(Color.Red, darkIcons = false)
        }

        // Assert that the colors were set
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Red)
        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Red)
    }

    @Test
    @Category(IgnoreOnRobolectric::class) // Robolectric implements the new behavior from 23+
    @SdkSuppress(maxSdkVersion = 22)
    fun statusBarIcons_scrim() {
        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(contentView, window)
            controller.setStatusBarColor(Color.White, darkIcons = true) {
                // Here we can provide custom logic to 'darken' the color to maintain contrast.
                // We return red just to assert below.
                Color.Red
            }
        }

        // Assert that the colors were set to our 'darkened' color
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Red)

        // Assert that the system couldn't apply the native light icons
        rule.scenario.onActivity {
            val windowInsetsController = WindowCompat.getInsetsController(window, contentView)
            assertThat(windowInsetsController.isAppearanceLightStatusBars).isFalse()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 23)
    fun statusBarIcons_native() {
        // Now create an AndroidSystemUiController() and set the status bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(contentView, window)
            controller.setStatusBarColor(Color.White, darkIcons = true) {
                // Here we can provide custom logic to 'darken' the color to maintain contrast.
                // We return red just to assert below.
                Color.Red
            }
        }

        // Assert that the colors were darkened color is not used
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.White)

        // Assert that the system applied the native light icons
        rule.scenario.onActivity {
            val windowInsetsController = WindowCompat.getInsetsController(window, contentView)
            assertThat(windowInsetsController.isAppearanceLightStatusBars).isTrue()
        }
    }

    @Test
    @Category(IgnoreOnRobolectric::class) // Robolectric implements the new behavior from 25+
    @SdkSuppress(maxSdkVersion = 25)
    fun navigationBarIcons_scrim() {
        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(contentView, window)
            controller.setNavigationBarColor(Color.White, darkIcons = true) {
                // Here we can provide custom logic to 'darken' the color to maintain contrast.
                // We return red just to assert below.
                Color.Red
            }
        }

        // Assert that the colors were set to our 'darkened' color
        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Red)

        // Assert that the system couldn't apply the native light icons
        rule.scenario.onActivity {
            val windowInsetsController = WindowCompat.getInsetsController(window, contentView)
            assertThat(windowInsetsController.isAppearanceLightNavigationBars).isFalse()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 26)
    fun navigationBar_native() {
        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(contentView, window)
            controller.setNavigationBarColor(Color.White, darkIcons = true) {
                // Here we can provide custom logic to 'darken' the color to maintain contrast.
                // We return red just to assert below.
                Color.Red
            }
        }

        // Assert that the colors were darkened color is not used
        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.White)

        // Assert that the system applied the native light icons
        rule.scenario.onActivity {
            val windowInsetsController = WindowCompat.getInsetsController(window, contentView)
            assertThat(windowInsetsController.isAppearanceLightNavigationBars).isTrue()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun navigationBar_contrastEnforced() {
        rule.scenario.onActivity {
            // Now create an AndroidSystemUiController()
            val controller = AndroidSystemUiController(contentView, window)

            // Assert that the contrast is not enforced initially
            assertThat(controller.isNavigationBarContrastEnforced).isFalse()

            // and set the navigation bar with dark icons and enforce contrast
            controller.setNavigationBarColor(
                Color.Transparent,
                darkIcons = true,
                navigationBarContrastEnforced = true
            ) {
                // Here we can provide custom logic to 'darken' the color to maintain contrast.
                // We return red just to assert below.
                Color.Red
            }

            // Assert that the colors were darkened color is not used
            assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Transparent)

            // Assert that the system applied the contrast enforced property
            assertThat(window.isNavigationBarContrastEnforced).isTrue()

            // Assert that the controller reflects that the contrast is enforced
            assertThat(controller.isNavigationBarContrastEnforced).isTrue()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 30) // TODO: https://issuetracker.google.com/issues/189366125
    fun systemBarsBehavior_showBarsByTouch() {
        val controller = rule.scenario.withActivity {
            AndroidSystemUiController(contentView, window)
        }

        rule.scenario.onActivity {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
        }

        assertThat(WindowCompat.getInsetsController(window, contentView).systemBarsBehavior)
            .isEqualTo(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH)
    }

    @Test
    @SdkSuppress(minSdkVersion = 30) // TODO: https://issuetracker.google.com/issues/189366125
    fun systemBarsBehavior_showBarsBySwipe() {
        val controller = rule.scenario.withActivity {
            AndroidSystemUiController(contentView, window)
        }

        rule.scenario.onActivity {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        }

        assertThat(WindowCompat.getInsetsController(window, contentView).systemBarsBehavior)
            .isEqualTo(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE)
    }

    @Test
    @SdkSuppress(minSdkVersion = 30) // TODO: https://issuetracker.google.com/issues/189366125
    fun systemBarsBehavior_showTransientBarsBySwipe() {
        val controller = rule.scenario.withActivity {
            AndroidSystemUiController(contentView, window)
        }

        rule.scenario.onActivity {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        assertThat(WindowCompat.getInsetsController(window, contentView).systemBarsBehavior)
            .isEqualTo(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
    }

    @Test
    @FlakyTest(detail = "https://github.com/google/accompanist/issues/491")
    @SdkSuppress(minSdkVersion = 23) // rootWindowInsets which work
    @Category(IgnoreOnRobolectric::class)
    fun statusBarsVisibility() {
        // Now create an AndroidSystemUiController() and set the system bar colors
        val controller = rule.scenario.withActivity {
            AndroidSystemUiController(contentView, window)
        }

        // First show the bars
        rule.scenario.onActivity {
            controller.isStatusBarVisible = true
        }
        waitUntil { isRootWindowTypeVisible(WindowInsetsCompat.Type.statusBars()) }

        // Now hide the bars
        rule.scenario.onActivity {
            controller.isStatusBarVisible = false
        }
        waitUntil { !isRootWindowTypeVisible(WindowInsetsCompat.Type.statusBars()) }
    }

    @Test
    @FlakyTest(detail = "https://github.com/google/accompanist/issues/491")
    @SdkSuppress(minSdkVersion = 23) // rootWindowInsets which work
    @Category(IgnoreOnRobolectric::class)
    fun navigationBarsVisibility() {
        // Now create an AndroidSystemUiController() and set the system bar colors
        val controller = rule.scenario.withActivity {
            AndroidSystemUiController(contentView, window)
        }

        // First show the bars
        rule.scenario.onActivity {
            controller.isNavigationBarVisible = true
        }
        waitUntil { isRootWindowTypeVisible(WindowInsetsCompat.Type.navigationBars()) }

        // Now hide the bars
        rule.scenario.onActivity {
            controller.isNavigationBarVisible = false
        }
        waitUntil { !isRootWindowTypeVisible(WindowInsetsCompat.Type.navigationBars()) }
    }

    @Test
    @Category(IgnoreOnRobolectric::class)
    @FlakyTest(detail = "https://github.com/google/accompanist/issues/491")
    @SdkSuppress(minSdkVersion = 23) // rootWindowInsets which work
    fun systemBarsVisibility() {
        // Now create an AndroidSystemUiController() and set the system bar colors
        val controller = rule.scenario.withActivity {
            AndroidSystemUiController(contentView, window)
        }

        // First show the bars
        rule.scenario.onActivity {
            controller.isSystemBarsVisible = true
        }
        waitUntil { isRootWindowTypeVisible(WindowInsetsCompat.Type.navigationBars()) }
        waitUntil { isRootWindowTypeVisible(WindowInsetsCompat.Type.statusBars()) }

        // Now hide the bars
        rule.scenario.onActivity {
            controller.isSystemBarsVisible = false
        }
        waitUntil { !isRootWindowTypeVisible(WindowInsetsCompat.Type.navigationBars()) }
        waitUntil { !isRootWindowTypeVisible(WindowInsetsCompat.Type.statusBars()) }
    }

    private fun isRootWindowTypeVisible(type: Int): Boolean {
        return rule.scenario.withActivity {
            ViewCompat.getRootWindowInsets(contentView)!!.isVisible(type)
        }
    }
}
