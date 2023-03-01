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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * An interface that defines the contract for managing the in-app review process.
 */
@Stable
interface InAppReviewManager {
    /**
     * Launches the native Google Play Store dialog for requesting reviews from the user.
     *
     * @param activity The activity that launches the review flow dialog.
     * @param onReviewRequestSuccess A lambda that will be called if the review request was successful.
     * The flow has finished. The API does not indicate whether the user reviewed or not, or even whether
     * the review dialog was shown. Thus, no matter the result, we continue our app flow.
     * @param onReviewRequestFail A lambda that will be called if the review request fails. The exception
     * parameter will contain the error that caused the failure, if any.
     */
    fun launchReviewFlow(
        activity: Activity,
        onReviewRequestSuccess: () -> Unit = {},
        onReviewRequestFail: (exception: Exception?) -> Unit = {},
    )
}

/**
 * Returns an instance of [InAppReviewManager] that can be used to launch the native Google Play Store
 * dialog for requesting a review from the user.
 *
 * @return An instance of [InAppReviewManager].
 */
@Composable
fun rememberInAppReviewManager(): InAppReviewManager {
    // Get the application context using the LocalContext.current
    val applicationContext = LocalContext.current.applicationContext

    // Use the remember{} function to cache the InAppReviewManager instance and prevent
    // unnecessary recreation on recomposition
    return remember {
        // Create a new DefaultInAppReviewManager instance using the ReviewManagerFactory.create() API
        // with the retrieved application context
        DefaultInAppReviewManager(
            reviewManager = ReviewManagerFactory.create(applicationContext)
        )
    }
}

