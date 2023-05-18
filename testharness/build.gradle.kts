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
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.android.kotlin.get().pluginId)
    id(libs.plugins.jetbrains.dokka.get().pluginId)
    id(libs.plugins.gradle.metalava.get().pluginId)
    id(libs.plugins.vanniktech.maven.publish.get().pluginId)
    jacoco
}

kotlin {
    explicitApi()
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        // Required for JaCoCo + Robolectric
        // https://github.com/robolectric/robolectric/issues/2230
        isIncludeNoLocationClasses = true

        // Required for JDK 11 with the above
        // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
        excludes?.add("jdk.internal.*")
    }
}

android {
    namespace = "com.google.accompanist.testharness"

    compileSdk = 33

    defaultConfig {
        minSdk = 21
        // targetSdkVersion has no effect for libraries. This is only used for the test APK
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }

    buildFeatures {
        buildConfig = false
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    lint {
        textReport = true
        textOutput = File("stdout")
        // We run a full lint analysis as build part in CI, so skip vital checks for assemble tasks
        checkReleaseBuilds = false
    }

    packaging {
        // Some of the META-INF files conflict with coroutines-test. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        resources {
            excludes += listOf("/META-INF/AL2.0", "/META-INF/LGPL2.1")
        }
    }

    testCoverage {
        jacocoVersion = "0.8.8"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.useJUnit {
                excludeCategories("com.google.accompanist.internal.test.IgnoreOnRobolectric")
            }
        }
        animationsDisabled = true
    }

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

metalava {
    sourcePaths.setFrom("src/main")
    filename.set("api/current.api")
    reportLintsAsErrors.set(true)
}

dependencies {
    implementation(libs.compose.foundation.foundation)
    implementation(libs.androidx.core)
    testImplementation(libs.androidx.core)
    implementation(libs.napier)
    implementation(libs.kotlin.coroutines.android)

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

    testImplementation(libs.robolectric)
}
