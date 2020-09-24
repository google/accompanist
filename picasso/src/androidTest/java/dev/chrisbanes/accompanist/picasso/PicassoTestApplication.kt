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

package dev.chrisbanes.accompanist.picasso

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.StrictMode

class PicassoTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(
            object : DefaultActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedState: Bundle?) {
                    // [CoilTest] uses MockWebServer.url() which internally does a network check,
                    // and triggers StrictMode. To workaround that in the tests, we allow network
                    // on main thread.
                    val threadPolicy = StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .permitNetwork()
                        .build()
                    StrictMode.setThreadPolicy(threadPolicy)
                }
            }
        )
    }
}

/**
 * [Application.ActivityLifecycleCallbacks] which adds default empty method implementations.
 */
private interface DefaultActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, savedState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
