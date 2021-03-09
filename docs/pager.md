# Pager composable

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-pager)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

A library which provides a horizontally paging composable. If you've used Android's [`ViewPager`](https://developer.android.com/reference/kotlin/androidx/viewpager/widget/ViewPager) before, it has similar properties.

<video width="300" controls loop>
  <source src="demo.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>

The simplest usage looks like the following:

``` kotlin
val pagerState = remember {
    // Display 10 items
    PagerState(pageCount = 10)
}

Pager(state = pagerState) { page ->
    // Our page content
    Text(
        text = "Page: $page",
        modifier = Modifier.fillMaxWidth()
    )
}
```

## Lazy creation

Pages in a `Pager` are lazily created and laid-out as required by the layout. As the user scrolls through pages, any pages which are no longer required are removed from the content.

### Offscreen Limit

Pager allows the setting of the `offscreenLimit`, which defines the number of pages that should be retained on either side of the current page. Pages beyond this limit will be removed, and then recreated when needed. This value defaults to `1`, but can be increased to enable pre-loading of more content:

```kotlin
Pager(
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
    implementation "dev.chrisbanes.accompanist:accompanist-pager:<version>"
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
