/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.accompanist.internal.test

import androidx.test.filters.FlakyTest
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rule used to retry tests annotated with `FlakyTest`.
 */
class RetryTestRule(val retryCount: Int = 3) : MethodRule {

    private val retriesLeft = AtomicInteger(retryCount)

    override fun apply(base: Statement, method: FrameworkMethod, target: Any?): Statement? {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                if (method.getAnnotation(FlakyTest::class.java) == null) {
                    base.evaluate()
                    return
                }
                while (retriesLeft.getAndDecrement() > 0) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        if (retriesLeft.get() > 0) {
                            System.err.println(
                                "${method.name} failed, retrying. " +
                                    "($retriesLeft retries left)"
                            )
                            t.printStackTrace(System.err)
                        } else {
                            System.err.println(
                                "${method.name} failed after retrying $retryCount times."
                            )
                            throw t
                        }
                    }
                }
            }
        }
    }
}
