# Insets for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-insets)](https://search.maven.org/search?q=g:com.google.accompanist)

!!! warning
    **This library is deprecated, with official insets support in androidx.compose.foundation.** The migration guide and original documentation is below.

## Migration

The official `androidx.compose.foundation` insets support is very similar to accompanist/insets, with a few changes.

`androidx.compose.foundation` also does not disable window decor fitting, so you still need to call [`WindowCompat.setDecorFitsSystemWindows(window, false)`](https://developer.android.com/reference/androidx/core/view/WindowCompat#setDecorFitsSystemWindows(android.view.Window,%20boolean)) from your Activity.
You also still need to set the system bar backgrounds to be transparent, which can be done with our [System UI Controller](../systemuicontroller) library.

If you are using insets for IME support, you also still need to ensure that the activity's `windowSoftInputMode` is set to `adjustResize`:

```xml
<activity
      android:name=".MyActivity"
      android:windowSoftInputMode="adjustResize">
</activity>
```

## Migration steps:

1. Remove `ProvideWindowInsets` (there is no equivalent in `androidx.compose.foundation`)
1. Remove `ViewWindowInsetObserver` (there is no equivalent in `androidx.compose.foundation`)
1. Replace padding modifiers with `androidx.compose.foundation` equivalents. If using `additionalPadding` or only applying the insets to certain sides, use the corresponding [`WindowInsets.add`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets).add(androidx.compose.foundation.layout.WindowInsets)) and [`WindowInsets.only`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets).only(androidx.compose.foundation.layout.WindowInsetsSides)) extensions.
1. Replace `rememberInsetsPaddingValues` with the equivalent `WindowInsets.asPaddingValues`.
1. Replace direct calculations from `LocalWindowInsets.current` with calculations on `WindowInsets`.
1. Continue using the non-deprecated [`insets-ui`](#inset-aware-layouts-insets-ui) and experimental [`rememberImeNestedScrollConnection()`](#controlling-the-ime-on-screen-keyboard) for now.

For reference, consult the [Migration table](#migration-table) below.

## Inset consumption

The biggest behavioral change between `accompanist/insets` and `androidx.compose.foundation` is in the consumption behavior of padding modifiers.

In `accompanist/insets`, the padding modifiers always padded the full size of the specified inset types, which led to some unintuitive duplicate padding when nesting modifiers.

For example, let’s look at what happens when we have nested boxes, where the outer one has Modifier.systemBarsPadding() applied, and the inner has Modifier.imePadding():

```kotlin
Box(Modifier.systemBarsPadding()) {
    Box(Modifier.imePadding()) {
        // content
    }
}
```

Let’s assume that the bottom system bar padding is `30dp`, to account for the navigation bar padding, and let’s assume that when the IME is visible, the height of the IME is `150dp`.

When the IME is closed, the outer box will apply the bottom `30dp` as padding, and the inner box will apply zero additional padding, since the IME isn’t visible.

When the IME opens, the outer box will continue to apply the bottom `30dp` as the system bar padding, and the inner box will now apply `150dp` bottom padding, since that is the full height of the IME.

This results in a total padding of `180dp` applied to the content, which double pads the bottom navigation bar padding.
The solutions to this issue were using `derivedWindowInsetsTypeOf`, built-in derived types like `Modifier.navigationBarsWithImePadding()`, or performing calculations manually to apply the remaining padding.

In `androidx.compose.foundation`, when the IME is open, the outer box still apply the bottom `30dp`, but the inner box will only apply the remaining `120dp` needed to have the content be padded a total of `150dp` to match the height of the IME.

This behavior can be influenced further in `androidx.compose.foundation` with [`Modifier.consumedWindowInsets()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).consumedWindowInsets(androidx.compose.foundation.layout.WindowInsets))

As a result, the equivalent of `Modifier.navigationBarsWithImePadding()` is simply `Modifier.navigationBarsPadding().imePadding()`.

## Migration table:

| accompanist/insets                                                                                                                | androidx.compose.foundation                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|-----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ProvideWindowInsets`                                                                                                             | (remove)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `Modifier.systemBarsPadding()`                                                                                                    | [`Modifier.systemBarsPadding()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).systemBarsPadding())                                                                                                                                                                                                                                                                                                                                        |
| `Modifier.systemBarsPadding(bottom = false)`                                                                                      | [`Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.ui.Modifier).windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets))                                                                                                                                                                             |
| `Modifier.statusBarsPadding()`                                                                                                    | [`Modifier.statusBarsPadding()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).statusBarsPadding())                                                                                                                                                                                                                                                                                                                                        |
| `Modifier.navigationBarsPadding()`                                                                                                | [`Modifier.navigationBarsPadding()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).navigationBarsPadding())                                                                                                                                                                                                                                                                                                                                |
| `Modifier.imePadding()`                                                                                                           | [`Modifier.imePadding()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).imePadding())                                                                                                                                                                                                                                                                                                                                                      |
| `Modifier.cutoutPadding()`                                                                                                        | [`Modifier.displayCutoutPadding()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).displayCutoutPadding())                                                                                                                                                                                                                                                                                                                                  |
| `Modifier.navigationBarsWithImePadding()`                                                                                         | [`Modifier.navigationBarsPadding().imePadding()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).imePadding())                                                                                                                                                                                                                                                                                                                              |
| `Modifier.statusBarsHeight()`                                                                                                     | [`Modifier.windowInsetsTopHeight(WindowInsets.statusBars)`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).windowInsetsTopHeight(androidx.compose.foundation.layout.WindowInsets))                                                                                                                                                                                                                                                          |
| `Modifier.navigationBarsHeight()`                                                                                                 | [`Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).windowInsetsBottomHeight(androidx.compose.foundation.layout.WindowInsets))                                                                                                                                                                                                                                                |
| `Modifier.navigationBarsWidth()`                                                                                                  | [`Modifier.windowInsetsStartWidth(WindowInsets.navigationBars)`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).windowInsetsStartWidth(androidx.compose.foundation.layout.WindowInsets)) / [`Modifier.windowInsetsEndWidth(WindowInsets.navigationBars)`](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).windowInsetsEndWidth(androidx.compose.foundation.layout.WindowInsets)) |
| `rememberInsetsPaddingValues(insets = LocalWindowInsets.current.statusBars, applyStart = true, applyTop = true, applyEnd = true)` | [`WindowInsets.statusBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top).asPaddingValues()`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets).asPaddingValues())                                                                                                                                                                                                                         |
| `derivedWindowInsetsTypeOf`                                                                                                       | [`WindowInsets.union(windowInsets: WindowInsets)`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets).union(androidx.compose.foundation.layout.WindowInsets))                                                                                                                                                                                                                                          |
| `LocalWindowInsets.current.navigationBars`                                                                                        | [`WindowInsets.navigationBars`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets.Companion).navigationBars())                                                                                                                                                                                                                                                                                         |
| `LocalWindowInsets.current.statusBars`                                                                                            | [`WindowInsets.statusBars`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets.Companion).statusBars())                                                                                                                                                                                                                                                                                                 |
| `LocalWindowInsets.current.ime`                                                                                                   | [`WindowInsets.ime`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets.Companion).ime())                                                                                                                                                                                                                                                                                                               |
| `LocalWindowInsets.current.systemGestures`                                                                                        | [`WindowInsets.systemGestures`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets.Companion).systemGestures())                                                                                                                                                                                                                                                                                         |
| `LocalWindowInsets.current.systemBars`                                                                                            | [`WindowInsets.systemBars`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets.Companion).systemBars())                                                                                                                                                                                                                                                                                                 |
| `LocalWindowInsets.current.displayCutout`                                                                                         | [`WindowInsets.displayCutout`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#(androidx.compose.foundation.layout.WindowInsets.Companion).displayCutout())                                                                                                                                                                                                                                                                                           |
| `LocalWindowInsets.current.ime.bottom`                                                                                            | [`WindowInsets.ime.getBottom(LocalDensity.current)`](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/WindowInsets#getBottom(androidx.compose.ui.unit.Density))                                                                                                                                                                                                                                                                                                        |
| `WindowInsets.Type.isVisible`                                                                                                     | https://issuetracker.google.com/issues/217770337                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `WindowInsets.Type.animationInProgress`                                                                                           | https://issuetracker.google.com/issues/217770337                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `WindowInsets.Type.animationFraction`                                                                                             | https://issuetracker.google.com/issues/217770337                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `WindowInsets.Type.layoutInsets`                                                                                                  | https://issuetracker.google.com/issues/217770337                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `WindowInsets.Type.animatedInsets`                                                                                                | https://issuetracker.google.com/issues/217770337                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `rememberImeNestedScrollConnection()`                                                                                             | https://issuetracker.google.com/issues/217770710                                                                                                                                                                                                                                                                                                                                                                                                                                                        |

## Original docs

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

- [`Modifier.statusBarsPadding()`](../api/insets/com.google.accompanist.insets/status-bars-padding.html)
- [`Modifier.navigationBarsPadding()`](../api/insets/com.google.accompanist.insets/navigation-bars-padding.html)
- [`Modifier.systemBarsPadding()`](../api/insets/com.google.accompanist.insets/system-bars-padding.html)
- [`Modifier.imePadding()`](../api/insets/com.google.accompanist.insets/ime-padding.html)
- [`Modifier.navigationBarsWithImePadding()`](../api/insets/com.google.accompanist.insets/navigation-bars-with-ime-padding.html)
- [`Modifier.cutoutPadding()`](../api/insets/com.google.accompanist.insets/cutout-padding.html)

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

- [`Modifier.statusBarsHeight()`](../api/insets/com.google.accompanist.insets/status-bars-height.html)
- [`Modifier.navigationBarsHeight()`](../api/insets/com.google.accompanist.insets/navigation-bars-height.html)
- [`Modifier.navigationBarsWidth()`](../api/insets/com.google.accompanist.insets/navigation-bars-width.html)

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

You may want to use inset values for content padding, so this library provides the [`rememberInsetsPaddingValues()`](..//api/insets/com.google.accompanist.insets/remember-insets-padding-values.html) extension function to convert between `Insets` and [`PaddingValues`][paddingvalues]. Here's an example of using the system bars insets:

``` kotlin
LazyColumn(
    contentPadding = rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
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

To workaround this, we provide the `insets-ui` companion library which contains versions of commonly used layouts, with the addition of a `contentPadding` parameter. The example below is using our [`TopAppBar`](../api/insets-ui/com.google.accompanist.insets.ui/-top-app-bar.html) layout, providing the status bar insets to use as content padding:

``` kotlin
import com.google.accompanist.insets.ui.TopAppBar

TopAppBar(
    contentPadding = rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.statusBars,
        applyStart = true,
        applyTop = true,
        applyEnd = true,
    )
) {
    // content
}
```

The library also provides a modified copy of Compose Material's [`Scaffold`](https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#Scaffold(androidx.compose.ui.Modifier,androidx.compose.material.ScaffoldState,kotlin.Function0,kotlin.Function0,kotlin.Function1,kotlin.Function0,androidx.compose.material.FabPosition,kotlin.Boolean,kotlin.Function1,kotlin.Boolean,androidx.compose.ui.graphics.Shape,androidx.compose.ui.unit.Dp,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1)) which better supports edge-to-edge layouts, by drawing the top and bottom bars over the content.

``` kotlin
Scaffold(
    topBar = {
        // We use TopAppBar from accompanist-insets-ui which allows us to provide
        // content padding matching the system bars insets.
        TopAppBar(
            title = { Text(stringResource(R.string.insets_title_list)) },
            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
            contentPadding = rememberInsetsPaddingValues(
                LocalWindowInsets.current.statusBars,
                applyBottom = false,
            ),
        )
    },
    bottomBar = {
        // We add a spacer as a bottom bar, which is the same height as
        // the navigation bar
        Spacer(Modifier.navigationBarsHeight().fillMaxWidth())
    },
) { contentPadding ->
    // We apply the contentPadding passed to us from the Scaffold
    Box(Modifier.padding(contentPadding)) {
        // content
    }
}
```

See the [API docs](../api/insets-ui/com.google.accompanist.insets.ui/) for a list of the other layouts provided in the library.

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

## 🚧 Experimental

The features below are experimental, and require developers to [opt-in](https://kotlinlang.org/docs/reference/opt-in-requirements.html).

### Controlling the IME (on-screen keyboard)

=== "Info"

    ![](images/ime-scroll.gif){: loading=lazy align=right }

    This library also has support for controlling the IME from scroll gestures, allowing your scrollable components to pull/push the IME on/off screen. This is achieved through the built-in [`NestedScrollConnection`](https://developer.android.com/reference/kotlin/androidx/compose/ui/gesture/nestedscroll/NestedScrollConnection) implementation returned by [`rememberImeNestedScrollConnection()`](../api/insets/com.google.accompanist.insets/remember-ime-nested-scroll-connection.html).

    This functionality only works when running on devices with API 30+.

=== "Usage"

    ``` kotlin
    // Here we're using a scrollable Column, but it also works with LazyColumn, etc.
    Column(
        // We use the nestedScroll modifier, passing in the 
        // the connection from rememberImeNestedScrollConnection()
        modifier = Modifier
            .nestedScroll(connection = rememberImeNestedScrollConnection())
            .verticalScroll(state = rememberScrollState())
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
    // If using insets-ui
    implementation "com.google.accompanist:accompanist-insets-ui:<version>"
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
[api-type]: ../api/insets/com.google.accompanist.insets/-window-insets/-type/
