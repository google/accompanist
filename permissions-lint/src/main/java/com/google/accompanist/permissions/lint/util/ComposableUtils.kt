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

@file:Suppress("FunctionName")

package com.google.accompanist.permissions.lint.util

import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.impl.compiled.ClsParameterImpl
import com.intellij.psi.impl.light.LightParameter
import kotlinx.metadata.jvm.annotations
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UAnonymousClass
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.getContainingDeclaration
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getParameterForArgument
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.withContainingElements

// FILE COPIED FROM:
// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/lint/common/src/main/java/androidx/compose/lint/ComposableUtils.kt

/**
 * Returns whether this [UCallExpression] is invoked within the body of a Composable function or
 * lambda.
 *
 * This searches parent declarations until we find a lambda expression or a function, and looks
 * to see if these are Composable.
 */
public fun UCallExpression.isInvokedWithinComposable(): Boolean {
    return ComposableBodyVisitor(this).isComposable()
}

// TODO: https://youtrack.jetbrains.com/issue/KT-45406
// KotlinUMethodWithFakeLightDelegate.hasAnnotation() (for reified functions for example)
// doesn't find annotations, so just look at the annotations directly.
/**
 * Returns whether this method is @Composable or not
 */
public val PsiMethod.isComposable: Boolean
    get() = annotations.any { it.qualifiedName == Composable.javaFqn }

/**
 * Returns whether this variable's type is @Composable or not
 */
public val UVariable.isComposable: Boolean
    get() {
        // Annotation on the lambda
        val annotationOnLambda = when (val initializer = uastInitializer) {
            is ULambdaExpression -> {
                val source = initializer.sourcePsi
                if (source is KtFunction) {
                    // Anonymous function, val foo = @Composable fun() {}
                    source.hasComposableAnnotation
                } else {
                    // Lambda, val foo = @Composable {}
                    initializer.findAnnotation(Composable.javaFqn) != null
                }
            }
            else -> false
        }
        // Annotation on the type, foo: @Composable () -> Unit = { }
        val annotationOnType = typeReference?.isComposable == true
        return annotationOnLambda || annotationOnType
    }

/**
 * Returns whether this parameter's type is @Composable or not
 */
private val PsiParameter.isComposable: Boolean
    get() = when {
        // The parameter is in a class file. Currently type annotations aren't currently added to
        // the underlying type (https://youtrack.jetbrains.com/issue/KT-45307), so instead we use
        // the metadata annotation.
        this is ClsParameterImpl
            // In some cases when a method is defined in bytecode and the call fails to resolve
            // to the ClsMethodImpl, we will instead get a LightParameter. Note that some Kotlin
            // declarations too will also appear as a LightParameter, so we can check to see if
            // the source language is Java, which means that this is a LightParameter for
            // bytecode, as opposed to for a Kotlin declaration.
            // https://youtrack.jetbrains.com/issue/KT-46883
            || (this is LightParameter && this.language is JavaLanguage) -> {
            // Find the containing method, so we can get metadata from the containing class
            val containingMethod = getParentOfType<PsiMethod>(true)
            val kmFunction = containingMethod!!.toKmFunction()

            val kmValueParameter = kmFunction?.valueParameters?.find {
                it.name == name
            }

            kmValueParameter?.type?.annotations?.find {
                it.className == Composable.kmClassName
            } != null
        }
        // The parameter is in a source declaration
        else -> (toUElement() as UParameter).typeReference!!.isComposable
    }

/**
 * Returns whether this lambda expression is @Composable or not
 */
public val ULambdaExpression.isComposable: Boolean
    get() = when (val lambdaParent = uastParent) {
        // Function call with a lambda parameter
        is UCallExpression -> {
            val parameter = lambdaParent.getParameterForArgument(this)
            parameter?.isComposable == true
        }
        // A local / non-local lambda variable
        is UVariable -> {
            lambdaParent.isComposable
        }
        // Either a new UAST type we haven't handled, or non-Kotlin declarations
        else -> false
    }

/**
 * Helper class that visits parent declarations above the provided [callExpression], until it
 * finds a lambda or method. This 'boundary' is used as the indicator for whether this
 * [callExpression] can be considered to be inside a Composable body or not.
 *
 * @see isComposable
 * @see parentUElements
 */
private class ComposableBodyVisitor(
    private val callExpression: UCallExpression
) {
    /**
     * @return whether the body can be considered Composable or not
     */
    fun isComposable(): Boolean = when (val element = parentUElements.last()) {
        is UMethod -> element.isComposable
        is ULambdaExpression -> element.isComposable
        else -> false
    }

    /**
     * Returns all parent [UElement]s until and including the boundary lambda / method.
     */
    fun parentUElements() = parentUElements

    /**
     * The outermost UElement that corresponds to the surrounding UDeclaration that contains
     * [callExpression], with the following special cases:
     *
     * - if the containing UDeclaration is a local property, we ignore it and search above as
     * it still could be created in the context of a Composable body
     * - if the containing UDeclaration is an anonymous class (object { }), we ignore it and
     * search above as it still could be created in the context of a Composable body
     */
    private val boundaryUElement by lazy {
        // The nearest property / function / etc declaration that contains this call expression
        var containingDeclaration = callExpression.getContainingDeclaration()

        fun UDeclaration.isLocalProperty() = (sourcePsi as? KtProperty)?.isLocal == true
        fun UDeclaration.isAnonymousClass() = this is UAnonymousClass
        fun UDeclaration.isPropertyInsideAnonymousClass() =
            getContainingUClass()?.isAnonymousClass() == true

        while (
            containingDeclaration != null &&
            (
                containingDeclaration.isLocalProperty() ||
                    containingDeclaration.isAnonymousClass() ||
                    containingDeclaration.isPropertyInsideAnonymousClass()
                )
        ) {
            containingDeclaration = containingDeclaration.getContainingDeclaration()
        }

        containingDeclaration
    }

    private val parentUElements by lazy {
        val elements = mutableListOf<UElement>()

        // Look through containing elements until we find a lambda or a method
        for (element in callExpression.withContainingElements) {
            elements += element
            when (element) {
                // TODO: consider handling the case of a lambda inside an inline function call,
                //  such as `apply` or `forEach`. These calls don't really change the
                //  'composability' here, but there may be other inline function calls that
                //  capture the lambda and invoke it elsewhere, so we might need to look for
                //  a callsInPlace contract in the metadata for the function, or the body of the
                //  source definition.
                is ULambdaExpression -> break
                is UMethod -> break
                // Stop when we reach the parent declaration to avoid escaping the scope.
                boundaryUElement -> break
            }
        }
        elements
    }
}

/**
 * Returns whether this type reference is @Composable or not
 */
private val UTypeReferenceExpression.isComposable: Boolean
    get() {
        if (type.hasAnnotation(Composable.javaFqn)) return true

        // Annotations on the types of local properties (val foo: @Composable () -> Unit = {})
        // are currently not present on the PsiType, we so need to manually check the underlying
        // type reference. (https://youtrack.jetbrains.com/issue/KTIJ-18821)
        return (sourcePsi as? KtTypeReference)?.hasComposableAnnotation == true
    }

/**
 * Returns whether this annotated declaration has a Composable annotation
 */
private val KtAnnotated.hasComposableAnnotation: Boolean
    get() = annotationEntries.any {
        (it.toUElement() as UAnnotation).qualifiedName == Composable.javaFqn
    }

private val RuntimePackageName = Package("androidx.compose.runtime")
private val Composable = Name(RuntimePackageName, "Composable")

/**
 * @return a [PackageName] with a Java-style (separated with `.`) [packageName].
 */
internal fun Package(packageName: String): PackageName =
    PackageName(packageName.split("."))

/**
 * @return a [Name] with the provided [pkg] and Java-style (separated with `.`) [shortName].
 */
internal fun Name(pkg: PackageName, shortName: String): Name =
    Name(pkg, shortName.split("."))

/**
 * Represents a qualified package
 *
 * @property segments the segments representing the package
 */
internal class PackageName internal constructor(internal val segments: List<String>) {
    /**
     * The Java-style package name for this [Name], separated with `.`
     */
    val javaPackageName: String
        get() = segments.joinToString(".")
}

/**
 * Represents the qualified name for an element
 *
 * @property pkg the package for this element
 * @property nameSegments the segments representing the element - there can be multiple in the
 * case of nested classes.
 */
internal class Name internal constructor(
    private val pkg: PackageName,
    private val nameSegments: List<String>
) {
    /**
     * The short name for this [Name]
     */
    val shortName: String
        get() = nameSegments.last()

    /**
     * The Java-style fully qualified name for this [Name], separated with `.`
     */
    val javaFqn: String
        get() = pkg.segments.joinToString(".", postfix = ".") +
            nameSegments.joinToString(".")

    /**
     * The [ClassName] for use with kotlinx.metadata. Note that in kotlinx.metadata the actual
     * type might be different from the underlying JVM type, for example:
     * kotlin/Int -> java/lang/Integer
     */
    val kmClassName: ClassName
        get() = pkg.segments.joinToString("/", postfix = "/") +
            nameSegments.joinToString(".")
}

private typealias ClassName = String
