# Insets for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-insets)](https://search.maven.org/search?q=g:com.google.accompanist)

Insets for Jetpack Compose takes a lot of the ideas which drove [Insetter][insetter-view] for views, and applies them for use in composables.

## Usage
To setup Insets in your composables, you need to call the `ProvideWindowInsets` function and
wrap your content. This would typically be done near the top level of your composable hierarchy:

``` kotlin
setContent {
  MaterialTheme {
    ProvideWindowInsets {
      // your content
    }
  }
}
```

!!! note
    **This library does not disable window decor fitting.** For your view hierarchy to able to receive insets, you need to make sure to call: [`WindowCompat.setDecorFitsSystemWindows(window, false)`](https://developer.android.com/reference/androidx/core/view/WindowCompat#setDecorFitsSystemWindows(android.view.Window,%20boolean)) from your Activity. You also need to set the system bar backgrounds to be transparent, which can be done with our [System UI Controller](../systemuicontroller) library.

`ProvideWindowInsets` allows the library to set an [`OnApplyWindowInsetsListener`][insetslistener] on your content's host view. That listener is used to update the value of a composition local bundled in this library: `LocalWindowInsets`.

`LocalWindowInsets` holds an instance of `WindowInsets` which contains the value of various [WindowInsets][insets] [types][insettypes]. You can use the values manually like so:

``` kotlin
@Composable
fun ImeAvoidingBox() {
    val insets = LocalWindowInsets.current
    
    val imeBottom = with(LocalDensity.current) { insets.ime.bottom.toDp() }
    Box(Modifier.padding(bottom = imeBottom))
}
```

...but we also provide some easy-to-use [Modifier][modifier]s.

### Modifiers

We provide two types of modifiers for easy handling of insets: padding and size.

#### Padding modifiers
The padding modifiers allow you to apply padding to a composable which matches a specific type of inset. Currently we provide:

- [`Modifier.statusBarsPadding()`](../api/insets/insets/com.google.accompanist.insets/status-bars-padding.html)
- [`Modifier.navigationBarsPadding()`](../api/insets/insets/com.google.accompanist.insets/navigation-bars-padding.html)
- [`Modifier.systemBarsPadding()`](../api/insets/insets/com.google.accompanist.insets/system-bars-padding.html)
- [`Modifier.imePadding()`](../api/insets/insets/com.google.accompanist.insets/ime-padding.html)
- [`Modifier.navigationBarsWithImePadding()`](../api/insets/insets/com.google.accompanist.insets/navigation-bars-with-ime-padding.html)

These are commonly used to move composables out from under the system bars. The common example would be a [`FloatingActionButton`][fab]:

``` kotlin
FloatingActionButton(
    onClick = { /* TODO */ },
    modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp) // normal 16dp of padding for FABs
        .navigationBarsPadding() // Move it out from under the nav bar
) {
    Icon(imageVector = Icons.Default.Add, contentDescription = null)
}
```

#### Size modifiers
The size modifiers allow you to match the size of a composable to a specific type of inset. Currently we provide:

- [`Modifier.statusBarsHeight()`](../api/insets/insets/com.google.accompanist.insets/status-bars-height.html)
- [`Modifier.navigationBarsHeight()`](../api/insets/insets/com.google.accompanist.insets/navigation-bars-height.html)
- [`Modifier.navigationBarsWidth()`](../api/insets/insets/com.google.accompanist.insets/navigation-bars-width.html)

These are commonly used to allow composables behind the system bars, to provide background protection, or similar:

``` kotlin
Spacer(
    Modifier
        .background(Color.Black.copy(alpha = 0.7f))
        .statusBarsHeight() // Match the height of the status bar
        .fillMaxWidth()
)
```

### PaddingValues
Compose also provides the concept of [`PaddingValues`][paddingvalues], a data class which contains the padding values to be applied on all dimensions (similar to a rect). This is commonly used with container composables, such as [`LazyColumn`][lazycolumn], to set the content padding.

You may want to use inset values for content padding, so this library provides the [`rememberWindowInsetsTypePaddingValues()`](..//api/insets/insets/com.google.accompanist.insets/remember-window-insets-type-padding-values.html) extension function to convert between [`WindowInsets.Type`][api-type] and [`PaddingValues`][paddingvalues]. Here's an example of using the system bars insets:

``` kotlin
LazyColumn(
    contentPadding = rememberWindowInsetsTypePaddingValues(
        type = LocalWindowInsets.current.systemBars,
        applyTop = true,
        applyBottom = true,
    )
) {
    // content
}
```

For a more complex example, see the [`EdgeToEdgeLazyColumn`](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/insets/EdgeToEdgeLazyColumn.kt) example:

<a href="images/edge-to-edge-list.jpg">
<img src="images/edge-to-edge-list.jpg" width=300>
</a>

## Inset-aware layouts (`insets-ui`)

Unfortunately, most of Compose Material's layouts do not support the use of content padding, which means that the following code probably doesn't produce the effect you want:

``` kotlin
// 😥 This likely doesn't do what you want
TopAppBar(
    // content
    modifier = Modifier.statusBarsPadding()
)
```

To workaround this, we provide the `insets-ui` companion library which contains versions of commonly used layouts, with the addition of a `contentPadding` parameter. The example below is using our [`TopAppBar`](../api/insets-ui/insets-ui/com.google.accompanist.insets.ui/-top-app-bar.html) layout, providing the status bar insets to use as content padding:

``` kotlin
import com.google.accompanist.insets.ui.TopAppBar

TopAppBar(
    contentPadding = rememberWindowInsetsTypePaddingValues(
        LocalWindowInsets.current.statusBars,
        applyStart = true,
        applyTop = true,
        applyEnd = true,
    )
) {
    // content
}
```

See the [API docs](../api/insets-ui/insets-ui/com.google.accompanist.insets.ui/) for a list of the other layouts provided in the library.

## 🚧 Experimental

The features below are experimental, and require developers to [opt-in](https://kotlinlang.org/docs/reference/opt-in-requirements.html).

### Animated Insets support

=== "Info"

    ![](images/ime-insets.gif){: align=right loading=lazy }

    The library now has experimental support for [`WindowInsetsAnimations`](https://developer.android.com/reference/android/view/WindowInsetsAnimation), allowing your content is react to inset animations, such as the on screen-keyboard (IME) being animated on/off screen. The `imePadding()` and `navigationBarsWithImePadding()` modifiers are available especially for this use-case. 

    This functionality works wherever [WindowInsetsAnimationCompat](https://developer.android.com/reference/androidx/core/view/WindowInsetsAnimationCompat) works, which at the time or writing is on devices running API 21+.

    To enable animated insets support, you need need to new `ProvideWindowInsets` overload, and set `windowInsetsAnimationsEnabled = true`.

=== "Usage"

    ``` kotlin
    ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
        // content
    }
    ```

    You can then use the new `navigationBarsWithImePadding()` modifier like so:

    ``` kotlin
    OutlinedTextField(
        // other params,
        modifier = Modifier.navigationBarsWithImePadding()
    )
    ```

    See the [ImeAnimationSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/insets/ImeAnimationSample.kt) for a working example.

### IME animations
If you're using the animation insets support for IME/keyboard animations, you also need to ensure that the activity's `windowSoftInputMode` is set to `adjustResize`:

``` xml
<activity
      android:name=".MyActivity"
      android:windowSoftInputMode="adjustResize">
</activity>
```

The default value of `windowSoftInputMode` _should_ work, but Compose does not currently set the flags necessary (see [here](https://issuetracker.google.com/154101484)).

### Controlling the IME (on-screen keyboard)

=== "Info"

    ![](images/ime-scroll.gif){: loading=lazy align=right }

    This library also has support for controlling the IME from scroll gestures, allowing your scrollable components to pull/push the IME on/off screen. This is acheived through the built-in [`NestedScrollConnection`](https://developer.android.com/reference/kotlin/androidx/compose/ui/gesture/nestedscroll/NestedScrollConnection) implementation returned by [`rememberImeNestedScrollConnection()`](../api/insets/insets/com.google.accompanist.insets/remember-ime-nested-scroll-connection.html).

    This functionality only works when running on devices with API 30+.

=== "Usage"

    ``` kotlin
    // Here we're using ScrollableColumn, but it also works with LazyColumn, etc.
    ScrollableColumn(
        // We use the nestedScroll modifier, passing in the 
        // the connection from rememberImeNestedScrollConnection()
        modifier = Modifier.nestedScroll(
            connection = rememberImeNestedScrollConnection()
        )
    ) {
        // list content
    }
    ```

    See the [ImeAnimationSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/insets/ImeAnimationSample.kt) for a working example.


## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-insets)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-insets:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

## Something not working?

If you find that something isn't working correctly, here's a checklist to try:

- Check that you've called [`WindowCompat.setDecorFitsSystemWindows(window, false)`](https://developer.android.com/reference/androidx/core/view/WindowCompat#setDecorFitsSystemWindows(android.view.Window,%20boolean)) in your Activity. Unless you do that, the window decor will consume the insets, and they will not be dispatched to your content.
- If it's something related to the keyboard, check that the Activity's `windowSoftInputMode` is set to [`adjustResize`](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#SOFT_INPUT_ADJUST_RESIZE). Without that, IME visibility changes will not be sent as inset changes.
- Similarly, if you're setting [`android:windowFullscreen`](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_FULLSCREEN) to `true` (or using a `.Fullscreen` theme), be aware that `adjustResize` will not work. Please see the [documentation](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_FULLSCREEN) for an alternative.
- If you're using `ProvideWindowInsets` (or `ViewWindowInsetObserver`) in multiple layers of your view hierarchy (i.e. in the activity, _and_ in a fragment), you need to turn off consuming of insets. By default `ProvideWindowInsets` and `ViewWindowInsetObserver` will completely consume any insets passed to it. In the previous example, this means that the activity content will get the insets, but the fragment won't. To disable consuming, pass `consumeWindowInsets = false` to `ProvideWindowInsets` or `ViewWindowInsetObserver.start()`.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-insets/
[insetter-view]: https://github.com/chrisbanes/insetter
[insets]: https://developer.android.com/reference/kotlin/androidx/core/view/WindowInsetsCompat
[insettypes]: https://developer.android.com/reference/kotlin/androidx/core/view/WindowInsetsCompat.Type
[insetslistener]: https://developer.android.com/reference/kotlin/androidx/core/view/OnApplyWindowInsetsListener
[modifier]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier
[paddingvalues]: https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/PaddingValues
[lazycolumn]: https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/package-summary#lazycolumn
[fab]: https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#floatingactionbutton
[api-type]: ../api/insets/insets/com.google.accompanist.insets/-window-insets/-type/
