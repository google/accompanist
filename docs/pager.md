# Pager layouts

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-pager)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides paging layouts for Jetpack Compose. If you've used Android's [`ViewPager`](https://developer.android.com/reference/kotlin/androidx/viewpager/widget/ViewPager) before, it has similar properties.

!!! warning
**This library is deprecated, with official pager support in `androidx.compose.foundation.pager` ** 
The original documentation is below the migration guide.

## Migration

1. Make sure you are using Compose 1.4.0+ before attempting to migrate to `androidx.compose.foundation.pager`. 
2. Change `com.google.accompanist.pager.HorizontalPager` to `androidx.compose.foundation.pager.HorizontalPager`, and the same for `com.google.accompanist.pager.VerticalPager` to change to `androidx.compose.foundation.pager.VerticalPager`
3. Change `count` variable to `pageCount`. 
4. Change `itemSpacing` parameter to `pageSpacing`.
5. Change any usages of `rememberPagerState()` to `androidx.compose.foundation.pager.rememberPagerState()`
6. For more mappings - see the migration table below. 
7. Run your changes on device and check to see if there are any differences. 

One thing to note is that there is a new parameter on `androidx.compose.foundation.Pager`, for `pageSize`, by default this 
uses a `PageSize.Fill`, but can also be changed to use a fixed size, like `PageSize.Fixed(200.dp)` for a fixed size paging.


## Migration Table

The following is a mapping of the pager classes from accompanist to androidx.compose:

| accompanist/pager                    | androidx.compose.foundation                                                                                                                         |
|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `HorizontalPager`                    | `androidx.compose.foundation.pager.HorizontalPager`                                                                                                 |
| `VerticalPager`                      | `androidx.compose.foundation.pager.VerticalPager`                                                                                                   |
| `rememberPagerState`                 | `androidx.compose.foundation.pager.rememberPagerState`                                                                                              |
| `PagerState#pageCount`               | Use `canScrollForward` or `canScrollBackward`                                                                                                       |
| `calculateCurrentOffsetForPage`      | Use `(pagerState.currentPage - page) + pagerState.currentPageOffsetFraction`                                                                        |
| `PagerState#currentPageOffset`       | `PagerState#currentPageOffsetFraction`                                                                                                              |
| `Modifier.pagerTabIndicatorOffset()` | Implement it yourself, or still include and use `accompanist-pager-indicators`, it now supports `androidx.compose.foundation.pager.PagerState`      |
| `HorizontalPagerIndicator`           | Implement it yourself, or still include and use `accompanist-pager-indicators`, it now supports `androidx.compose.foundation.pager.HorizontalPager` |
| `VerticalPagerIndicator`             | Implement it yourself, or still include and use `accompanist-pager-indicators`, it now supports `androidx.compose.foundation.pager.HorizontalPager` |
| `PagerDefaults.flingBehavior()`      | `androidx.compose.foundation.pager.PagerDefaults.flingBehavior()`                                                                                   |

The biggest change is that `HorizontalPager` and `VerticalPager`'s number of pages is now called `pageCount` instead of `count`.

# Deprecated Guidance for Accompanist Pager
The following is the deprecated guide for using Pager in Accompanist. Please see above migration section for how to use the `androidx.compose` Pager.

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
HorizontalPager(count = 10) { page ->
    // Our page content
    Text(
        text = "Page: $page",
        modifier = Modifier.fillMaxWidth()
    )
}
```

If you want to jump to a specific page, you either call call `pagerState.scrollToPage(index)` or  `pagerState.animateScrollToPage(index)` method in a `CoroutineScope`.

``` kotlin
val pagerState = rememberPagerState()

HorizontalPager(count = 10, state = pagerState) { page ->
    // ...page content
}

// Later, scroll to page 2
scope.launch {
    pagerState.scrollToPage(2)
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
VerticalPager(count = 10) { page ->
    // Our page content
    Text(
        text = "Page: $page",
        modifier = Modifier.fillMaxWidth()
    )
}
```

## Lazy creation

Pages in both [`HorizontalPager`][api-horizpager] and [`VerticalPager`][api-vertpager] are lazily composed and laid-out as required by the layout. As the user scrolls through pages, any pages which are no longer required are removed from the content.

Under the covers, `HorizontalPager` use [`LazyRow`](https://developer.android.com/jetpack/compose/lists#lazy), and `VerticalPager` uses [`LazyColumn`](https://developer.android.com/jetpack/compose/lists#lazy).


## Content Padding

`HorizontalPager` and `VerticalPager` both support the setting of content padding, which allows you to influence the maximum size and alignment of pages.

You can see how different content padding values affect a `HorizontalPager` below:

=== "start = 64.dp"

    Setting the start padding has the effect of aligning the pages towards the end.

    ![](contentpadding-start.png){: loading=lazy width=70% align=center }

    ``` kotlin
    HorizontalPager(
        count = 4,
        contentPadding = PaddingValues(start = 64.dp),
    ) { page ->
        // page content
    }
    ```

=== "horizontal = 32.dp"

    Setting both the start and end padding to the same value has the effect of centering the item horizontally.

    ![](contentpadding-horizontal.png){: loading=lazy width=70% align=center }

    ``` kotlin
    HorizontalPager(
        count = 4,
        contentPadding = PaddingValues(horizontal = 32.dp),
    ) { page ->
        // page content
    }
    ```

=== "end = 64.dp"

    Setting the end padding has the effect of aligning the pages towards the start.

    ![](contentpadding-end.png){: loading=lazy width=70% align=center }

    ``` kotlin
    HorizontalPager(
        count = 4,
        contentPadding = PaddingValues(end = 64.dp),
    ) { page ->
        // page content
    }
    ```

Similar effects for `VerticalPager` can be achieved by setting the `top` and `bottom` values. The value `32.dp` is only used
here as an example, you can set each of the padding dimensions to whatever value you wish.

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

HorizontalPager(count = 4) { page ->
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

The [`PagerState.currentPage`][currentpage-api] property is updated whenever the selected page changes. You can use the `snapshotFlow` function to observe changes in a flow:

``` kotlin
val pagerState = rememberPagerState()

LaunchedEffect(pagerState) {
    // Collect from the pager state a snapshotFlow reading the currentPage
    snapshotFlow { pagerState.currentPage }.collect { page ->
        AnalyticsService.sendPageSelectedEvent(page)
    }
}

VerticalPager(
    count = 10,
    state = pagerState,
) { page ->
    Text(text = "Page: $page")
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
val pagerState = rememberPagerState()

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

HorizontalPager(
    count = pages.size,
    state = pagerState,
) { page ->
    // TODO: page content
}
```

## Changes in v0.19.0

In v0.19.0 both `HorizontalPager` and `VerticalPager` were re-written to be based on `LazyRow` and `LazyColumn` respectively. As part of this change, a number of feature and API changes were made:

### PagerState

- The `pageCount` parameter on `rememberPagerState()` has been removed, replaced with the `count` parameter on `HorizontalPager()` and `VerticalPager()`.
- The `animationSpec`, `initialVelocity` and `skipPages` parameters on `animateScrollToPage()` have been removed. The lazy components handle this automatically.

### HorizontalPager & VerticalPager

- Ability to set `contentPadding` (see [above](#content-padding)).
- Ability to specify a `key` for each page.
- The `horizontalAlignment` parameter on `HorizontalPager`, and the `verticalAlignment` parameter on `VerticalPager` have been removed. A similar effect can be implemented with an appropriate content padding (see [above](#content-padding)).
- The `infiniteLooping` parameter and feature have been removed. A sample demonstrating how to achieve this effect can be found [here][looping-sample].
- The `offscreenLimit` parameter has been removed. We no longer have control of what items are laid out 'off screen'.
- The `dragEnabled` parameter has removed.
- `PagerScope` (the page item scope) no longer implements `BoxScope`. 

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
  [looping-sample]: https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/pager/HorizontalPagerLoopingSample.kt
