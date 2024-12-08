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
    namespace = "com.google.accompanist.permissions"

    defaultConfig {
        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation.foundation)
    implementation(libs.kotlin.coroutines.android)

    lintChecks(project(":permissions-lint"))
    lintPublish(project(":permissions-lint"))

    // ======================
    // Test dependencies
    // ======================

    androidTestUtil(libs.androidx.test.orchestrator)

    androidTestImplementation(project(":internal-testutils"))
    androidTestImplementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.compose.material.material)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.truth)

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.foundation.foundation)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.uiAutomator)
}
