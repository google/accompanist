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
    const val ktlint = "0.40.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.0.0-alpha03"

    const val gradleMavenPublishPlugin = "com.vanniktech:gradle-maven-publish-plugin:0.13.0"

    const val junit = "junit:junit:4.13"

    object Kotlin {
        private const val version = "1.4.21"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

        const val binaryCompatibility = "org.jetbrains.kotlinx:binary-compatibility-validator:0.2.3"
    }

    object Dokka {
        private const val version = "1.4.10.2"
        const val gradlePlugin = "org.jetbrains.dokka:dokka-gradle-plugin:$version"
    }

    object Coroutines {
        private const val version = "1.4.1"
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
            const val snapshot = ""
            const val version = "1.0.0-alpha09"

            @JvmStatic
            val snapshotUrl: String
                get() = when {
                    snapshot.isNotEmpty() -> {
                        "https://androidx.dev/snapshots/builds/$snapshot/artifacts/repository/"
                    }
                    else -> throw IllegalArgumentException("Snapshot version not set")
                }

            const val compiler = "androidx.compose.compiler:compiler:$version"

            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"

            const val ui = "androidx.compose.ui:ui:${version}"
            const val material = "androidx.compose.material:material:${version}"

            const val tooling = "androidx.ui:ui-tooling:${version}"
            const val test = "androidx.compose.ui:ui-test-junit4:${version}"
        }

        const val core = "androidx.core:core:1.2.0"
        const val coreKtx = "androidx.core:core-ktx:1.2.0"

        const val fragmentKtx = "androidx.fragment:fragment-ktx:1.3.0-beta01"

        const val lifecycleKtx = "androidx.lifecycle:lifecycle-runtime-ktx:2.3.0-beta01"

        const val coreAlpha = "androidx.core:core:1.5.0-alpha04"
    }

    object Coil {
        private const val version = "1.0.0"
        const val coil = "io.coil-kt:coil:$version"
        const val gif = "io.coil-kt:coil-gif:$version"
    }

    const val picasso = "com.squareup.picasso:picasso:2.8"

    const val glide = "com.github.bumptech.glide:glide:4.11.0"

    const val truth = "com.google.truth:truth:1.0.1"

    object OkHttp {
        const val okhttp = "com.squareup.okhttp3:okhttp:3.12.2"
        const val mockWebServer = "com.squareup.okhttp3:mockwebserver:3.12.2"
    }
}
