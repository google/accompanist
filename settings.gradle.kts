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

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = uri("https://nexus.mimikko.cn/repository/maven-public/")) {
            credentials {
                username = "dev"
                password = "shouer2019"
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = uri("https://nexus.mimikko.cn/repository/maven-public/")) {
            credentials {
                username = "dev"
                password = "shouer2019"
            }
        }
    }
}

//plugins {
//    id("com.gradle.enterprise").version("3.10.3")
//}

//gradleEnterprise {
//    buildScan {
//        termsOfServiceUrl = "https://gradle.com/terms-of-service"
//        termsOfServiceAgree = "yes"
//    }
//}

include(":adaptive")
include(":internal-testutils")
include(":insets-ui")
include(":appcompat-theme")
include(":drawablepainter")
include(":navigation-animation")
include(":navigation-material")
include(":pager")
include(":pager-indicators")
include(":permissions")
include(":permissions-lint")
include(":placeholder")
include(":placeholder-material")
include(":placeholder-material3")
include(":flowlayout")
include(":systemuicontroller")
include(":swiperefresh")
include(":sample")
include(":testharness")
include(":themeadapter-core")
include(":themeadapter-appcompat")
include(":themeadapter-material")
include(":themeadapter-material3")
include(":web")

rootProject.name = "accompanist"
