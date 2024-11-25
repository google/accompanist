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
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.android.kotlin.get().pluginId)
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.google.accompanist.sample"
        minSdk = 21
        targetSdk = 33

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    namespace = "com.google.accompanist.sample"
}

dependencies {
    implementation(project(":adaptive"))
    implementation(project(":drawablepainter"))
    implementation(project(":insets-ui"))
    implementation(project(":navigation-animation"))
    implementation(project(":navigation-material"))
    implementation(project(":permissions"))
    implementation(project(":systemuicontroller"))
    implementation(project(":testharness")) // Don't use in production! Use the configurations below
    testImplementation(project(":testharness"))
    androidTestImplementation(project(":testharness"))
    implementation(project(":web"))

    implementation(libs.androidx.appcompat)
    implementation(libs.mdc)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    implementation(libs.compose.material.material)
    implementation(libs.compose.material.iconsext)
    implementation(libs.compose.material3.material3)
    implementation(libs.compose.foundation.layout)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.util)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.runtime)

    implementation(libs.kotlin.stdlib)

    lintChecks(project(":permissions-lint"))
}
