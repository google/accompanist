# Pager layouts

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-pager)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides paging layouts for Jetpack Compose. If you've used Android's [`ViewPager`](https://developer.android.com/reference/kotlin/androidx/viewpager/widget/ViewPager) before, it has similar properties.

!!! warning
    The pager layouts are currently experimental and the APIs could change at any time.
    All of the APIs are marked with the `@ExperimentalPagerApi` annotation.

## HorizontalPager

[`HorizontalPager`][api-horizpager] is a layout which lays out items in a horizontal row, and allows the user to horizontally swipe between pages.

<figure>
    <video width="300" controls loop>
    <source src="horiz_demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>HorizontalPager demo</figcaption>
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

[`VerticalPager`][api-vertpager] is very similar to [`HorizontalPager`][api-horizpager] but items are laid out vertically, and react to vertical swipes:

<figure>
    <video width="300" controls loop>
    <source src="vert_demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>VerticalPager demo</figcaption>
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

Pages in both [`HorizontalPager`][api-horizpager] and [`VerticalPager`][api-vertpager] are lazily composed and laid-out as required by the layout. As the user scrolls through pages, any pages which are no longer required are removed from the content.

### Offscreen Limit

The [PagerState][pagerstate-api] API allows the setting of the `offscreenLimit`, which defines the number of pages that should be retained on either side of the current page. Pages beyond this limit will be removed, and then recreated as needed. This value defaults to `1`, but can be increased to enable pre-loading of more content:

```kotlin
val pagerState = rememberPagerState(
    pageCount = 10,
    offscreenLimit = 2,
)

HorizontalPager(state = pagerState) { page ->
    // ...
}
```

## Item scroll effects

A common use-case is to apply effects to your pager items, using the scroll position to drive those effects.

The [HorizontalPagerTransitionSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/pager/HorizontalPagerTransitionSample.kt) demonstrates how this can be done:

<figure>
    <video width="300" controls loop>
    <source src="transition_demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>Item effects demo</figcaption>
</figure>


The scope provided to your pager content allows apps to easily reference the [`currentPage`][currentpage-api] and [`currentPageOffset`][currentpageoffset-api]. The effects can then be calculated using those values. We provide the [`calculateCurrentOffsetForPage()`][calcoffsetpage] extension functions to support calculation of the 'offset' for a given page:

``` kotlin
import com.google.accompanist.pager.calculateCurrentOffsetForPage

HorizontalPager(state = pagerState) { page ->
    Card(
        Modifier
            .graphicsLayer {
                // Calculate the absolute offset for the current page from the
                // scroll position. We use the absolute value which allows us to mirror
                // any effects for both directions
                val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue

                // We animate the scaleX + scaleY, between 85% and 100%
                lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                ).also { scale ->
                    scaleX = scale
                    scaleY = scale
                }

                // We animate the alpha, between 50% and 100%
                alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            }
    ) {
        // Card content
    }
}
```

## Reacting to page changes

The [`PagerState.currentPage`][currentpage-api] property is updated whenever the selected page changes. You can use the `snapshowFlow` function to observe changes in a flow:

``` kotlin
LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect { page ->
        // Selected page has changed...
    }
}
```

## Indicators

We also publish a sibling library called `pager-indicators` which provides some simple indicator composables for use with [`HorizontalPager`][api-horizpager] and [`VerticalPager`][api-vertpager].

<figure>
    <video width="300" controls loop>
    <source src="indicators_demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>Pager indicators demo</figcaption>
</figure>

The [HorizontalPagerWithIndicatorSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/pager/HorizontalPagerWithIndicatorSample.kt) and [VerticalPagerWithIndicatorSample](https://github.com/google/accompanist/blob/snapshot/sample/src/main/java/com/google/accompanist/sample/pager/VerticalPagerWithIndicatorSample.kt) show you how to use these.


### Integration with Tabs

A common use-case for [`HorizontalPager`][api-horizpager] is to be used in conjunction with a [`TabRow`](https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#tabrow) or [`ScrollableTabRow`](https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#scrollabletabrow).

<figure>
    <video width="300" controls loop>
    <source src="tabs_demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>HorizontalPager + TabRow</figcaption>
</figure>


Provided in the `pager-indicators` library is a modifier which can be used on a tab indicator like so:

``` kotlin
val pagerState = rememberPagerState(pageCount = pages.size)

TabRow(
    // Our selected tab is our current page
    selectedTabIndex = pagerState.currentPage,
    // Override the indicator, using the provided pagerTabIndicatorOffset modifier
    indicator = { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
        )
    }
) {
    // Add tabs for all of our pages
    pages.forEachIndexed { index, title ->
        Tab(
            text = { Text(title) },
            selected = pagerState.currentPage == index,
            onClick = { /* TODO */ },
        )
    }
}

HorizontalPager(state = pagerState) { page ->
    // TODO: page content
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

    // If using indicators, also depend on 
    implementation "com.google.accompanist:accompanist-pager-indicators:<version>"
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

  [api-vertpager]: ../api/pager/pager/com.google.accompanist.pager/-vertical-pager.html
  [api-horizpager]: ../api/pager/pager/com.google.accompanist.pager/-horizontal-pager.html
  [currentpage-api]: ../api/pager/pager/com.google.accompanist.pager/-pager-state/current-page.html
  [currentpageoffset-api]: ../api/pager/pager/com.google.accompanist.pager/-pager-state/current-page-offset.html
  [calcoffsetpage]: ../api/pager/pager/com.google.accompanist.pager/calculate-current-offset-for-page.html
  [pagerstate-api]: ../api/pager/pager/com.google.accompanist.pager/remember-pager-state.html