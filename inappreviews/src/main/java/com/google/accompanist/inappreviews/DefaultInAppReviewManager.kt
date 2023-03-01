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

package com.google.accompanist.inappreviews

import android.app.Activity
import com.google.android.play.core.review.ReviewManager

internal class DefaultInAppReviewManager constructor(
    private val reviewManager: ReviewManager
) : InAppReviewManager {

    override fun launchReviewFlow(
        activity: Activity,
        onReviewRequestSuccess: () -> Unit,
        onReviewRequestFail: (exception: Exception?) -> Unit
    ) {
        val requestTask = reviewManager.requestReviewFlow()

        requestTask.addOnCompleteListener { requestTaskResult ->
            if (requestTaskResult.isSuccessful) {
                val reviewInfo = requestTaskResult.result
                val launchTask = reviewManager.launchReviewFlow(activity, reviewInfo)

                launchTask.addOnCompleteListener { launchTaskResult ->
                    if (launchTaskResult.isSuccessful) {
                        onReviewRequestSuccess()
                    } else {
                        onReviewRequestFail(launchTaskResult.exception)
                    }
                }
            } else {
                onReviewRequestFail(requestTaskResult.exception)
            }
        }
    }
}