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

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.google.accompanist.permissions.lint.util.Name
import com.google.accompanist.permissions.lint.util.Package
import com.google.accompanist.permissions.lint.util.PackageName
import com.google.accompanist.permissions.lint.util.isInvokedWithinComposable
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import java.util.EnumSet

/**
 * [Detector] that checks `async` and `launch` calls to make sure they don't happen inside the
 * body of a composable function / lambda.
 */
public class PermissionsLaunchDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(
        LaunchPermissionRequest.shortName, LaunchMultiplePermissionsRequest.shortName
    )

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!method.isInPackageName(PermissionsPackageName)) return

        if (node.isInvokedWithinComposable()) {
            context.report(
                PermissionLaunchedDuringComposition,
                node,
                context.getNameLocation(node),
                "Calls to ${method.name} should happen inside a regular lambda or " +
                    " a side-effect, but never in the Composition."
            )
        }
    }

    public companion object {
        public val PermissionLaunchedDuringComposition: Issue = Issue.create(
            "PermissionLaunchedDuringComposition",
            "Calls to `launchPermissionRequest` or `launchMultiplePermissionRequest` " +
                "should happen inside a regular lambda or a side-effect but never in the " +
                "Composition.",
            "Calls to `launchPermissionRequest` or `launchMultiplePermissionRequest` " +
                "in the Composition throw a runtime exception. Please call them inside a regular " +
                "lambda or in a side-effect.",
            Category.CORRECTNESS, 3, Severity.ERROR,
            Implementation(
                PermissionsLaunchDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES)
            )
        )
    }
}

/**
 * Returns whether [this] has [packageName] as its package name.
 */
private fun PsiMethod.isInPackageName(packageName: PackageName): Boolean {
    val actual = (containingFile as? PsiJavaFile)?.packageName
    return packageName.javaPackageName == actual
}

private val PermissionsPackageName = Package("com.google.accompanist.permissions")
private val LaunchPermissionRequest =
    Name(PermissionsPackageName, "launchPermissionRequest")
private val LaunchMultiplePermissionsRequest =
    Name(PermissionsPackageName, "launchMultiplePermissionRequest")
