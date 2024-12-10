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

@file:Suppress("NOTHING_TO_INLINE")

package com.google.accompanist.internal.test

public inline fun parameterizedParams(): List<Array<Any>> = emptyList()

public inline fun <reified T> List<Array<T>>.combineWithParameters(
    vararg values: T
): List<Array<T>> {
    if (isEmpty()) {
        return values.map { arrayOf(it) }
    }

    return fold(emptyList()) { acc, args ->
        val result = acc.toMutableList()
        values.forEach { v ->
            result += ArrayList<T>().apply {
                addAll(args)
                add(v)
            }.toTypedArray()
        }
        result.toList()
    }
}
