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

@file:Suppress("DEPRECATION") // ListActivity

package dev.chrisbanes.accompanist.sample

import android.annotation.SuppressLint
import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import java.text.Collator
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap

/**
 * A [ListActivity] which automatically populates the list of sample activities in this app
 * with the category `dev.chrisbanes.accompanist.sample.SAMPLE_CODE`.
 */
class MainActivity : ListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listAdapter = SimpleAdapter(
            this,
            getData(intent.getStringExtra(EXTRA_PATH)),
            android.R.layout.simple_list_item_1,
            arrayOf("title"),
            intArrayOf(android.R.id.text1)
        )

        listView.isTextFilterEnabled = true
    }

    private fun getData(prefix: String?): List<Map<String, Any>> {
        val myData = ArrayList<Map<String, Any>>()

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory("dev.chrisbanes.accompanist.sample.SAMPLE_CODE")

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

        val entries = HashMap<String, Boolean>()

        list.forEach { info ->
            val labelSeq = info.loadLabel(packageManager)
            val label = labelSeq?.toString() ?: info.activityInfo.name

            if (prefixWithSlash.isNullOrEmpty() || label.startsWith(prefixWithSlash)) {
                val labelPath = label.split("/".toRegex()).toTypedArray()
                val nextLabel = if (prefixPath == null) labelPath[0] else labelPath[prefixPath.size]
                if (prefixPath?.size ?: 0 == labelPath.size - 1) {
                    addItem(
                        data = myData,
                        name = nextLabel,
                        intent = activityIntent(
                            info.activityInfo.applicationInfo.packageName,
                            info.activityInfo.name
                        )
                    )
                } else {
                    if (entries[nextLabel] == null) {
                        addItem(
                            data = myData,
                            name = nextLabel,
                            intent = browseIntent(
                                if (prefix == "") nextLabel else "$prefix/$nextLabel"
                            )
                        )
                        entries[nextLabel] = true
                    }
                }
            }
        }

        Collections.sort(myData, sDisplayNameComparator)

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

    private fun addItem(data: MutableList<Map<String, Any>>, name: String, intent: Intent) {
        val temp = mutableMapOf<String, Any>()
        temp["title"] = name
        temp["intent"] = intent
        data += temp
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val map = l.getItemAtPosition(position) as Map<*, *>
        val intent = map["intent"] as Intent?
        startActivity(intent)
    }

    companion object {
        private const val EXTRA_PATH = "com.example.android.apis.Path"

        private val sDisplayNameComparator = object : Comparator<Map<String, Any>> {
            private val collator = Collator.getInstance()

            override fun compare(map1: Map<String, Any>, map2: Map<String, Any>): Int {
                return collator.compare(map1["title"], map2["title"])
            }
        }
    }
}
