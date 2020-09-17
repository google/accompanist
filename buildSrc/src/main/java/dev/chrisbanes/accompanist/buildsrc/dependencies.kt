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

package dev.chrisbanes.accompanist.buildsrc

object Versions {
    const val ktlint = "0.38.1"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:4.2.0-alpha10"

    const val gradleMavenPublishPlugin = "com.vanniktech:gradle-maven-publish-plugin:0.12.0"

    const val junit = "junit:junit:4.13"

    object Kotlin {
        const val version = "1.4.0"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

        const val binaryCompatibility = "org.jetbrains.kotlinx:binary-compatibility-validator:0.2.3"
    }

    object Dokka {
        const val gradlePlugin = "org.jetbrains.dokka:dokka-gradle-plugin:0.10.1"
    }

    object Coroutines {
        private const val version = "1.3.9"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        object Test {
            private const val version = "1.2.0"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            const val ext = "androidx.test.ext:junit:1.1.1"

            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
        }

        object Compose {
            const val snapshot = "6834837"
            const val version = "1.0.0-SNAPSHOT"

            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"

            const val ui = "androidx.compose.ui:ui:${version}"
            const val material = "androidx.compose.material:material:${version}"

            const val tooling = "androidx.ui:ui-tooling:${version}"
            const val test = "androidx.ui:ui-test:${version}"
        }

        const val core = "androidx.core:core:1.2.0"
        const val coreKtx = "androidx.core:core-ktx:1.2.0"

        const val appcompat = "androidx.appcompat:appcompat:1.3.0-alpha02"
    }

    const val coil = "io.coil-kt:coil:1.0.0-rc1"

    const val truth = "com.google.truth:truth:1.0.1"
    const val mockk = "io.mockk:mockk-android:1.10.0"
}
