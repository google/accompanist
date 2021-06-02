# Swipe Refresh for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-swiperefresh)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides a layout which provides the swipe-to-refresh UX pattern, similar to Android's [`SwipeRefreshLayout`](https://developer.android.com/training/swipe/add-swipe-interface).

<figure>
    <video width="400" controls loop>
    <source src="demo.mp4" type="video/mp4">
        Your browser does not support the video tag.
    </video>
    <figcaption>SwipeRefresh demo</figcaption>
</figure>

## Usage 

To implement this UX pattern there are two key APIs which are needed: [`SwipeRefresh`][api_swiperefresh], which is provides the layout, and [`rememberSwipeRefreshState()`][api_rememberstate] which provides some remembered state.

The basic usage of a [`SwipeRefresh`][api_swiperefresh] using a ViewModel looks like so:

``` kotlin
val viewModel: MyViewModel = viewModel()
val isRefreshing by viewModel.isRefreshing.collectAsState()

SwipeRefresh(
    state = rememberSwipeRefreshState(isRefreshing),
    onRefresh = { viewModel.refresh() },
) {
    LazyColumn {
        items(30) { index ->
            // TODO: list items
        }
    }
}
```

The full example, including the view model implementation can be found [here](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/swiperefresh/DocsSamples.kt).

The content needs to be 'scrollable' for `SwipeRefresh()` to be able to react to swipe gestures. Layouts such as [`LazyColumn`][lazycolumn] are automatically scrollable, but others such as [`Column`][column] are not. In those instances, you can provide a [`Modifier.verticalScroll`][verticalscroll] modifier to that content like so:

``` kotlin
SwipeRefresh(
    // ...
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        // content
    }
}
```


### Indicating a refresh without swiping

As this library is built with a seperate state object, it's easy to display a refreshing indicator without a swipe to triggering it.

The unrealistic example below displays a forever refreshing indicator:

``` kotlin
val swipeRefreshState = rememberSwipeRefreshState(true)

SwipeRefresh(
    state = swipeRefreshState,
    onRefresh = { /* todo */ },
) {
    LazyColumn {
        items(30) { index ->
            // TODO: list items
        }
    }
}
```

## Indicator

The library provides a default indicator: [`SwipeRefreshIndicator()`][api_swiperefreshindicator], which `SwipeRefresh` uses automatically. You can customize the default indicator, and even provide your own indicator content using the `indicator` slot.

### Customizing default indicator

To customize the default indicator, we can provide our own `indicator` content block, to call [`SwipeRefreshIndicator()`][api_swiperefreshindicator] with customized parameters:

=== "Sample"

    ``` kotlin
    SwipeRefresh(
        state = /* ... */,
        onRefresh = /* ... */,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                // Pass the SwipeRefreshState + trigger through
                state = state,
                refreshTriggerDistance = trigger,
                // Enable the scale animation
                scale = true,
                // Change the color and shape
                backgroundColor = MaterialTheme.colors.primary,
                shape = MaterialTheme.shapes.small,
            )
        }
    )
    ```

=== "Demo video"

    <figure>
        <video width="480" controls loop>
        <source src="tweaked.mp4" type="video/mp4">
            Your browser does not support the video tag.
        </video>
        <figcaption>Tweaked indicator demo</figcaption>
    </figure>

### Custom indicator

As mentioned, you can also provide your own custom indicator content. A [`SwipeRefreshState`][api_swiperefreshstate] is provided to `indicator` content slot, which contains the information necessary to react to a swipe refresh gesture.

An example of a custom indicator is provided [here][sample_customindictor].

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-swiperefresh)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-swiperefresh:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

  [compose]: https://developer.android.com/jetpack/compose
  [snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-swiperefresh/
  [api_swiperefreshstate]: ../api/swiperefresh/swiperefresh/com.google.accompanist.swiperefresh/-swipe-refresh-state/
  [api_swiperefreshindicator]: ../api/swiperefresh/swiperefresh/com.google.accompanist.swiperefresh/-swipe-refresh-indicator.html
  [api_swiperefresh]: ../api/swiperefresh/swiperefresh/com.google.accompanist.swiperefresh/-swipe-refresh.html
  [api_rememberstate]: ../api/swiperefresh/swiperefresh/com.google.accompanist.swiperefresh/remember-swipe-refresh-state.html
  [sample_customindictor]: https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/swiperefresh/SwipeRefreshCustomIndicatorSample.kt
  [lazycolumn]: https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/package-summary#LazyColumn(androidx.compose.ui.Modifier,androidx.compose.foundation.lazy.LazyListState,androidx.compose.foundation.layout.PaddingValues,kotlin.Boolean,androidx.compose.foundation.layout.Arrangement.Vertical,androidx.compose.ui.Alignment.Horizontal,androidx.compose.foundation.gestures.FlingBehavior,kotlin.Function1)
  [column]: https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#Column(androidx.compose.ui.Modifier,androidx.compose.foundation.layout.Arrangement.Vertical,androidx.compose.ui.Alignment.Horizontal,kotlin.Function1)
  [verticalscroll]: https://developer.android.com/jetpack/compose/gestures#scroll-modifiers