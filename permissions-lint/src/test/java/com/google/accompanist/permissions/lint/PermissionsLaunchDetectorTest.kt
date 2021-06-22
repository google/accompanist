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

@file:Suppress("UnstableApiUsage")

package com.google.accompanist.permissions.lint

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintResult
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/* ktlint-disable max-line-length */

/**
 * Test for [PermissionsLaunchDetector].
 */
@RunWith(JUnit4::class)
internal class PermissionsLaunchDetectorTest {

    private fun check(fileToAdd: String): TestLintResult {
        return TestLintTask.lint()
            .files(
                LaunchPermissionsStub,
                ComposableStub,
                TestFiles.kt(fileToAdd)
            )
            .issues(PermissionsLaunchDetector.PermissionLaunchedDuringComposition)
            .run()
    }

    @Test
    fun errors() {
        check(
            """
            import androidx.compose.runtime.Composable
            import com.google.accompanist.permissions.*

            @Composable
            fun Test() {
                PermissionState().launchPermissionRequest()
                MultiplePermissionsState().launchMultiplePermissionRequest()
            }

            val lambda = @Composable {
                PermissionState().launchPermissionRequest()
                MultiplePermissionsState().launchMultiplePermissionRequest()
            }

            val lambda2: @Composable () -> Unit = {
                PermissionState().launchPermissionRequest()
                MultiplePermissionsState().launchMultiplePermissionRequest()
            }

            @Composable
            fun LambdaParameter(content: @Composable () -> Unit) {}

            @Composable
            fun Test2() {
                LambdaParameter(content = {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                })
                LambdaParameter {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                }
            }

            fun test3() {
                val localLambda1 = @Composable {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                }

                val localLambda2: @Composable () -> Unit = {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                }
            }
        """
        )
            .expect(
                """
                    src/test.kt:7: Error: Calls to launchPermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                    PermissionState().launchPermissionRequest()
                                                      ~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:8: Error: Calls to launchMultiplePermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                    MultiplePermissionsState().launchMultiplePermissionRequest()
                                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:12: Error: Calls to launchPermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                    PermissionState().launchPermissionRequest()
                                                      ~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:13: Error: Calls to launchMultiplePermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                    MultiplePermissionsState().launchMultiplePermissionRequest()
                                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:17: Error: Calls to launchPermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                    PermissionState().launchPermissionRequest()
                                                      ~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:18: Error: Calls to launchMultiplePermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                    MultiplePermissionsState().launchMultiplePermissionRequest()
                                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:27: Error: Calls to launchPermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        PermissionState().launchPermissionRequest()
                                                          ~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:28: Error: Calls to launchMultiplePermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        MultiplePermissionsState().launchMultiplePermissionRequest()
                                                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:31: Error: Calls to launchPermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        PermissionState().launchPermissionRequest()
                                                          ~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:32: Error: Calls to launchMultiplePermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        MultiplePermissionsState().launchMultiplePermissionRequest()
                                                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:38: Error: Calls to launchPermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        PermissionState().launchPermissionRequest()
                                                          ~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:39: Error: Calls to launchMultiplePermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        MultiplePermissionsState().launchMultiplePermissionRequest()
                                                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:43: Error: Calls to launchPermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        PermissionState().launchPermissionRequest()
                                                          ~~~~~~~~~~~~~~~~~~~~~~~
                    src/test.kt:44: Error: Calls to launchMultiplePermissionRequest should happen inside a regular lambda or  a side-effect, but never in the Composition. [PermissionLaunchedDuringComposition]
                                        MultiplePermissionsState().launchMultiplePermissionRequest()
                                                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    14 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun noErrors() {
        check(
            """
            import com.google.accompanist.permissions.*
            
            fun test() {
                PermissionState().launchPermissionRequest()
                MultiplePermissionsState().launchMultiplePermissionRequest()
            }

            val lambda = {
                PermissionState().launchPermissionRequest()
                MultiplePermissionsState().launchMultiplePermissionRequest()
            }

            val lambda2: () -> Unit = {
                PermissionState().launchPermissionRequest()
                MultiplePermissionsState().launchMultiplePermissionRequest()
            }

            fun lambdaParameter(action: () -> Unit) {}

            fun test2() {
                lambdaParameter(action = {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                })
                lambdaParameter {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                }
            }

            fun test3() {
                val localLambda1 = {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                }

                val localLambda2: () -> Unit = {
                    PermissionState().launchPermissionRequest()
                    MultiplePermissionsState().launchMultiplePermissionRequest()
                }
            }
        """
        )
            .expectClean()
    }
}

private val LaunchPermissionsStub = TestFiles.kt(
    "com/google/accompanist/permissions/LaunchPermissions.kt",
    """
        package com.google.accompanist.permissions

        class PermissionState {
            fun launchPermissionRequest()
        }

        class MultiplePermissionsState {
            fun launchMultiplePermissionRequest()
        }
    """
).indented().within("src")

private val ComposableStub = TestFiles.kt(
    "androidx/compose/runtime/Composable.kt",
    """
        package androidx.compose.runtime

        annotation class Composable
    """
).indented().within("src")
/* ktlint-enable max-line-length */
