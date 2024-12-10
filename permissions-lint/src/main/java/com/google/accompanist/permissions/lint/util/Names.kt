/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.accompanist.permissions.lint.util

/**
 * File copied from
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/lint/common/src/main/java/androidx/compose/lint/Names.kt
 */

import kotlin.metadata.ClassName

/** Contains common names used for lint checks. */
object Names {
    object Animation {
        val PackageName = Package("androidx.compose.animation")

        object Core {
            val PackageName = Package("androidx.compose.animation.core")
            val Animatable = Name(PackageName, "Animatable")
        }
    }

    object AnimationCore {
        val PackageName = Package("androidx.compose.animation.core")
    }

    object Runtime {
        val PackageName = Package("androidx.compose.runtime")

        val Composable = Name(PackageName, "Composable")
        val CompositionLocal = Name(PackageName, "CompositionLocal")
        val DerivedStateOf = Name(PackageName, "derivedStateOf")
        val State = Name(PackageName, "State")
        val MutableState = Name(PackageName, "MutableState")
        val MutableStateOf = Name(PackageName, "mutableStateOf")
        val MutableIntStateOf = Name(PackageName, "mutableIntStateOf")
        val MutableLongStateOf = Name(PackageName, "mutableLongStateOf")
        val MutableFloatStateOf = Name(PackageName, "mutableFloatStateOf")
        val MutableDoubleStateOf = Name(PackageName, "mutableDoubleStateOf")
        val MutableStateListOf = Name(PackageName, "mutableStateListOf")
        val MutableStateMapOf = Name(PackageName, "mutableStateMapOf")
        val ProduceState = Name(PackageName, "produceState")
        val Remember = Name(PackageName, "remember")
        val DisposableEffect = Name(PackageName, "DisposableEffect")
        val RememberSaveable = Name(PackageName, "rememberSaveable")
        val LaunchedEffect = Name(PackageName, "LaunchedEffect")
        val ReusableContent = Name(PackageName, "ReusableContent")
        val Key = Name(PackageName, "key")
        val StructuralEqualityPolicy = Name(PackageName, "structuralEqualityPolicy")
    }

    object Ui {
        val PackageName = Package("androidx.compose.ui")
        val Composed = Name(PackageName, "composed")
        val Modifier = Name(PackageName, "Modifier")

        object Layout {
            val PackageName = Package("androidx.compose.ui.layout")
            val ParentDataModifier = Name(PackageName, "ParentDataModifier")
        }

        object Pointer {
            val PackageName = Package(Ui.PackageName, "input.pointer")
            val PointerInputScope = Name(PackageName, "PointerInputScope")
            val PointerInputScopeModifier = Name(PackageName, "pointerInput")
            val AwaitPointerEventScope = Name(PackageName, "awaitPointerEventScope")
        }

        object Unit {
            val PackageName = Package("androidx.compose.ui.unit")
            val Dp = Name(PackageName, "Dp")
        }

        object Node {
            val PackageName = Package(Ui.PackageName, "node")
            val CurrentValueOf = Name(PackageName, "currentValueOf")
        }
    }

    object UiGraphics {
        val PackageName = Package("androidx.compose.ui.graphics")
        val Color = Name(PackageName, "Color")
    }
}

/**
 * Represents a qualified package
 *
 * @property segments the segments representing the package
 */
class PackageName internal constructor(internal val segments: List<String>) {
    /** The Java-style package name for this [Name], separated with `.` */
    val javaPackageName: String
        get() = segments.joinToString(".")
}

/**
 * Represents the qualified name for an element
 *
 * @property pkg the package for this element
 * @property nameSegments the segments representing the element - there can be multiple in the case
 *   of nested classes.
 */
class Name
internal constructor(private val pkg: PackageName, private val nameSegments: List<String>) {
    /** The short name for this [Name] */
    val shortName: String
        get() = nameSegments.last()

    /** The Java-style fully qualified name for this [Name], separated with `.` */
    val javaFqn: String
        get() = pkg.segments.joinToString(".", postfix = ".") + nameSegments.joinToString(".")

    /**
     * The [ClassName] for use with kotlin.metadata. Note that in kotlin.metadata the actual type
     * might be different from the underlying JVM type, for example: kotlin/Int -> java/lang/Integer
     */
    val kmClassName: ClassName
        get() = pkg.segments.joinToString("/", postfix = "/") + nameSegments.joinToString(".")

    /** The [PackageName] of this element. */
    val packageName: PackageName
        get() = pkg
}

/** @return a [PackageName] with a Java-style (separated with `.`) [packageName]. */
fun Package(packageName: String): PackageName = PackageName(packageName.split("."))

/** @return a [PackageName] with a Java-style (separated with `.`) [packageName]. */
fun Package(packageName: PackageName, shortName: String): PackageName =
    PackageName(packageName.segments + shortName.split("."))

/** @return a [Name] with the provided [pkg] and Java-style (separated with `.`) [shortName]. */
fun Name(pkg: PackageName, shortName: String): Name = Name(pkg, shortName.split("."))
