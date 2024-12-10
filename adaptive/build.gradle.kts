/*
 * Copyright 2023 The Android Open Source Project
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
@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.accompanist.android.library)
    alias(libs.plugins.accompanist.android.library.compose)
    alias(libs.plugins.accompanist.android.library.published)
}

android {
    namespace = "com.google.accompanist.adaptive"

    sourceSets {
        named("test") {
            java.srcDirs("src/sharedTest/kotlin")
            res.srcDirs("src/sharedTest/res")
        }
        named("androidTest") {
            java.srcDirs("src/sharedTest/kotlin")
            res.srcDirs("src/sharedTest/res")
        }
    }
}

dependencies {
    api(libs.compose.foundation.foundation)
    api(libs.compose.ui.ui)
    api(libs.androidx.window)
    
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.compose.ui.util)

    // ======================
    // Test dependencies
    // ======================

    androidTestImplementation(project(":internal-testutils"))
    testImplementation(project(":internal-testutils"))

    androidTestImplementation(libs.junit)
    testImplementation(libs.junit)

    androidTestImplementation(libs.truth)
    testImplementation(libs.truth)

    androidTestImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.compose.ui.test.junit4)

    androidTestImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.runner)

    androidTestImplementation(libs.androidx.window.testing)
    testImplementation(libs.androidx.window.testing)

    testImplementation(libs.robolectric)
}
