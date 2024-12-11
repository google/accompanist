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

package com.google.accompanist.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * A list which automatically populates the list of sample activities in this app
 * with the category `com.google.accompanist.sample.SAMPLE_CODE`.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val data = getData(intent.getStringExtra(EXTRA_PATH))

        setContent {
            AccompanistSampleTheme {
                MainScreen(
                    listData = data,
                    onItemClick = { startActivity(it) }
                )
            }
        }
    }

    private fun getData(prefix: String?): List<AccompanistSample> {
        val myData = mutableListOf<AccompanistSample>()

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory("com.google.accompanist.sample.SAMPLE_CODE")

        @SuppressLint("QueryPermissionsNeeded") // Only querying our own Activities
        val list = packageManager.queryIntentActivities(mainIntent, 0)

        val prefixPath: Array<String>?
        var prefixWithSlash = prefix

        if (prefix.isNullOrEmpty()) {
            prefixPath = null
        } else {
            prefixPath = prefix.split("/".toRegex()).toTypedArray()
            prefixWithSlash = "$prefix/"
        }

        val entries = mutableMapOf<String, Boolean>()

        list.forEach { info ->
            val labelSeq = info.loadLabel(packageManager)
            val label = labelSeq?.toString() ?: info.activityInfo.name

            if (prefixWithSlash.isNullOrEmpty() || label.startsWith(prefixWithSlash)) {
                val labelPath = label.split("/".toRegex()).toTypedArray()
                val nextLabel = if (prefixPath == null) labelPath[0] else labelPath[prefixPath.size]
                if ((prefixPath?.size ?: 0) == labelPath.size - 1) {
                    myData.add(
                        AccompanistSample(
                            title = nextLabel,
                            intent = activityIntent(
                                info.activityInfo.applicationInfo.packageName,
                                info.activityInfo.name
                            )
                        )
                    )
                } else {
                    if (entries[nextLabel] == null) {
                        myData.add(
                            AccompanistSample(
                                title = nextLabel,
                                intent = browseIntent(
                                    if (prefix == "") nextLabel else "$prefix/$nextLabel"
                                )
                            )
                        )
                        entries[nextLabel] = true
                    }
                }
            }
        }

        myData.sortBy { it.title }

        return myData
    }

    private fun activityIntent(pkg: String, componentName: String): Intent {
        val result = Intent()
        result.setClassName(pkg, componentName)
        return result
    }

    private fun browseIntent(path: String): Intent {
        val result = Intent()
        result.setClass(this, MainActivity::class.java)
        result.putExtra(EXTRA_PATH, path)
        return result
    }
}

private const val EXTRA_PATH = "com.example.android.apis.Path"
