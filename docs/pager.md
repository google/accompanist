# Paging layouts

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-pager)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides paging layouts for Jetpack Compose. If you've used Android's [`ViewPager`](https://developer.android.com/reference/kotlin/androidx/viewpager/widget/ViewPager) before, it has similar properties.

!!! warning
    The pager layouts are currently experimental and the APIs could change at any time.
    All of the APIs are marked with the `@ExperimentalPagerApi` annotation.

## HorizontalPager

`HorizontalPager` is a layout which lays out items in a horizontal row, and allows the user to swipe between pages.

<figure>
    <video width="300" controls loop>
    <source src="horiz_demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>HorizontalPager</figcaption>
</figure>


The simplest usage looks like the following:

``` kotlin
// Display 10 items
val pagerState = rememberPagerState(pageCount = 10)

HorizontalPager(state = pagerState) { page ->
    // Our page content
    Text(
        text = "Page: $page",
        modifier = Modifier.fillMaxWidth()
    )
}
```

## VerticalPager

`VerticalPager` is similar to `HorizontalPager` but items are instead laid out vertically, and react to vertical scrolls from the user:

<figure>
    <video width="300" controls loop>
    <source src="vert_demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>VerticalPager</figcaption>
</figure>

``` kotlin
// Display 10 items
val pagerState = rememberPagerState(pageCount = 10)

VerticalPager(state = pagerState) { page ->
    // Our page content
    Text(
        text = "Page: $page",
        modifier = Modifier.fillMaxWidth()
    )
}
```

## Lazy creation

Pages in both `HorizontalPager` & `VerticalPager` are lazily created and laid-out as required by the layout. As the user scrolls through pages, any pages which are no longer required are removed from the content.

### Offscreen Limit

Both `HorizontalPager` & `VerticalPager` allow the setting of the `offscreenLimit`, which defines the number of pages that should be retained on either side of the current page. Pages beyond this limit will be removed, and then recreated when needed. This value defaults to `1`, but can be increased to enable pre-loading of more content:

```kotlin
HorizontalPager(
    state = pagerState,
    offscreenLimit = 2,
) { page ->
    // ...
}
```

---

## Usage

``` groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-pager:<version>"
}
```

### Library Snapshots

Snapshots of the current development version of this library are available, which track the latest commit. See [here](../using-snapshot-version) for more information on how to use them.

---

## Contributions

Please contribute! We will gladly review any pull requests.
Make sure to read the [Contributing](../contributing) page first though.

## License

```
Copyright 2021 The Android Open Source Project
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
