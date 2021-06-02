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
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemUiControllerTest {
    @get:Rule
    val rule = ActivityScenarioRule(ComponentActivity::class.java)

    @Before
    fun setup() {
        if (Build.VERSION.SDK_INT >= 29) {
            // On API 29+, the system can modify the bar colors to maintain contrast.
            // We disable that here to make it simple to assert expected values
            val window = rule.scenario.withActivity { it.window }
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
    }

    @Test
    @UiThreadTest
    fun statusBarColor() {
        val view = rule.contentView
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the status bar color
        val controller = AndroidSystemUiController(view)
        controller.setStatusBarColor(Color.Blue, darkIcons = false)

        // Assert that the color was set
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Blue)
    }

    @Test
    @UiThreadTest
    fun navigationBarColor() {
        val view = rule.contentView
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the status bar color
        val controller = AndroidSystemUiController(view)
        controller.setNavigationBarColor(Color.Green, darkIcons = false)

        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Green)
    }

    @Test
    @UiThreadTest
    fun systemBarColor() {
        val view = rule.contentView
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the system bar colors
        val controller = AndroidSystemUiController(view)
        controller.setSystemBarsColor(Color.Red, darkIcons = false)

        // Assert that the colors were set
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Red)
        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Red)
    }

    @Test
    @UiThreadTest
    @SdkSuppress(maxSdkVersion = 22)
    fun statusBarIcons_scrim() {
        val view = rule.contentView
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        val controller = AndroidSystemUiController(view)
        controller.setStatusBarColor(Color.White, darkIcons = true) {
            // Here we can provide custom logic to 'darken' the color to maintain contrast.
            // We return red just to assert below.
            Color.Red
        }

        // Assert that the colors were set to our 'darkened' color
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.Red)

        // Assert that the system couldn't apply the native light icons
        val windowInsetsController = ViewCompat.getWindowInsetsController(view)!!
        assertThat(windowInsetsController.isAppearanceLightStatusBars).isFalse()
    }

    @Test
    @UiThreadTest
    @SdkSuppress(minSdkVersion = 23)
    fun statusBarIcons_native() {
        val view = rule.contentView
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the status bar with dark icons
        val controller = AndroidSystemUiController(view)
        controller.setStatusBarColor(Color.White, darkIcons = true) {
            // Here we can provide custom logic to 'darken' the color to maintain contrast.
            // We return red just to assert below.
            Color.Red
        }

        // Assert that the colors were darkened color is not used
        assertThat(Color(window.statusBarColor)).isEqualTo(Color.White)

        // Assert that the system applied the native light icons
        val windowInsetsController = ViewCompat.getWindowInsetsController(view)!!
        assertThat(windowInsetsController.isAppearanceLightStatusBars).isTrue()
    }

    @Test
    @UiThreadTest
    @SdkSuppress(maxSdkVersion = 25)
    fun navigationBarIcons_scrim() {
        val view = rule.contentView
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        val controller = AndroidSystemUiController(view)
        controller.setNavigationBarColor(Color.White, darkIcons = true) {
            // Here we can provide custom logic to 'darken' the color to maintain contrast.
            // We return red just to assert below.
            Color.Red
        }

        // Assert that the colors were set to our 'darkened' color
        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.Red)

        // Assert that the system couldn't apply the native light icons
        val windowInsetsController = ViewCompat.getWindowInsetsController(view)!!
        assertThat(windowInsetsController.isAppearanceLightNavigationBars).isFalse()
    }

    @Test
    @UiThreadTest
    @SdkSuppress(minSdkVersion = 26)
    fun navigationBar_native() {
        val view = rule.contentView
        val window = rule.window

        // Now create an AndroidSystemUiController() and set the navigation bar with dark icons
        val controller = AndroidSystemUiController(view)
        controller.setNavigationBarColor(Color.White, darkIcons = true) {
            // Here we can provide custom logic to 'darken' the color to maintain contrast.
            // We return red just to assert below.
            Color.Red
        }

        // Assert that the colors were darkened color is not used
        assertThat(Color(window.navigationBarColor)).isEqualTo(Color.White)

        // Assert that the system applied the native light icons
        val windowInsetsController = ViewCompat.getWindowInsetsController(view)!!
        assertThat(windowInsetsController.isAppearanceLightNavigationBars).isTrue()
    }

    @Test
    @UiThreadTest
    @SdkSuppress(minSdkVersion = 29)
    fun navigationBar_contrastEnforced() {
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

    @Test
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
            ViewCompat.getRootWindowInsets(rule.contentView)?.isVisible(type) == true
        }
    }
}

private val ActivityScenarioRule<*>.contentView: View
    get() = scenario.withActivity { it.contentView }

private val ActivityScenarioRule<*>.window: Window
    get() = scenario.withActivity { it.window }

private val Activity.contentView: View
    get() = findViewById(android.R.id.content)!!
