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

package com.google.accompanist.systemuicontroller

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.SdkSuppress
import com.google.accompanist.internal.test.IgnoreOnRobolectric
import com.google.accompanist.internal.test.waitUntil
import com.google.accompanist.internal.test.withActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category

abstract class BaseSystemUiControllerTest {
    @get:Rule
    val rule = ActivityScenarioRule(ComponentActivity::class.java)

    @Before
    fun setup() {
        if (Build.VERSION.SDK_INT >= 29) {
            // On API 29+, the system can modify the bar colors to maintain contrast.
            // We disable that here to make it simple to assert expected values
            rule.scenario.onActivity {
                it.window.apply {
                    isNavigationBarContrastEnforced = false
                    isStatusBarContrastEnforced = false
                }
            }
        }
    }

    @Test
    fun statusBarColor() {
        val window = rule.window

        rule.scenario.onActivity {
            // Now create an AndroidSystemUiController() and set the status bar color
            val controller = AndroidSystemUiController(it.contentView)
            controller.setStatusBarColor(Color.Blue, darkIcons = false)
        }

        // Assert that the color was set
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Blue)
    }

    @Test
    fun navigationBarColor() {
        val window = rule.window

        rule.scenario.onActivity {
            // Now create an AndroidSystemUiController() and set the status bar color
            val controller = AndroidSystemUiController(it.contentView)
            controller.setNavigationBarColor(Color.Green, darkIcons = false)
        }

        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Green)
    }

    @Test
    fun systemBarColor() {
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the system bar colors
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(it.contentView)
            controller.setSystemBarsColor(Color.Red, darkIcons = false)
        }

        // Assert that the colors were set
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Red)
        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Red)
    }

    @Test
    @Category(IgnoreOnRobolectric::class) // Robolectric implements the new behavior from 23+
    @SdkSuppress(maxSdkVersion = 22)
    open fun statusBarIcons_scrim() {
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(it.contentView)
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
            val windowInsetsController = ViewCompat.getWindowInsetsController(it.contentView)!!
            assertThat(windowInsetsController.isAppearanceLightStatusBars).isFalse()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 23)
    fun statusBarIcons_native() {
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the status bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(it.contentView)
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
            val windowInsetsController = ViewCompat.getWindowInsetsController(it.contentView)!!
            assertThat(windowInsetsController.isAppearanceLightStatusBars).isTrue()
        }
    }

    @Test
    @Category(IgnoreOnRobolectric::class) // Robolectric implements the new behavior from 25+
    @SdkSuppress(maxSdkVersion = 25)
    fun navigationBarIcons_scrim() {
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(rule.contentView)
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
            val windowInsetsController = ViewCompat.getWindowInsetsController(it.contentView)!!
            assertThat(windowInsetsController.isAppearanceLightNavigationBars).isFalse()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 26)
    fun navigationBar_native() {
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        rule.scenario.onActivity {
            val controller = AndroidSystemUiController(rule.contentView)
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
            val windowInsetsController = ViewCompat.getWindowInsetsController(it.contentView)!!
            assertThat(windowInsetsController.isAppearanceLightNavigationBars).isTrue()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun navigationBar_contrastEnforced() {
        rule.scenario.onActivity {
            val view = rule.contentView
            val window = rule.window

            // Now create an AndroidSystemUiController()
            val controller = AndroidSystemUiController(view)

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
    @SdkSuppress(minSdkVersion = 23) // rootWindowInsets which work
    @Category(IgnoreOnRobolectric::class)
    fun statusBarsVisibility() {
        // Now create an AndroidSystemUiController() and set the system bar colors
        val controller = rule.scenario.withActivity { AndroidSystemUiController(it.contentView) }

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
    @SdkSuppress(minSdkVersion = 23) // rootWindowInsets which work
    @Category(IgnoreOnRobolectric::class)
    fun navigationBarsVisibility() {
        // Now create an AndroidSystemUiController() and set the system bar colors
        val controller = rule.scenario.withActivity { AndroidSystemUiController(it.contentView) }

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
    @SdkSuppress(minSdkVersion = 23) // rootWindowInsets which work
    fun systemBarsVisibility() {
        // Now create an AndroidSystemUiController() and set the system bar colors
        val controller = rule.scenario.withActivity { AndroidSystemUiController(it.contentView) }

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
            ViewCompat.getRootWindowInsets(rule.contentView)!!.isVisible(type)
        }
    }
}

private val ActivityScenarioRule<*>.contentView: View
    get() = scenario.withActivity { it.contentView }

private val ActivityScenarioRule<*>.window: Window
    get() = scenario.withActivity { it.window }

private val Activity.contentView: View
    get() = findViewById(android.R.id.content)!!
