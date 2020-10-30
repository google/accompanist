# Insets for Jetpack Compose

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.chrisbanes.accompanist/accompanist-insets/badge.svg)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

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

> Note: Whether `ProvideWindowInsets` is called outside or within `MaterialTheme` doesn't particularly matter.

`ProvideWindowInsets` allows the library to set an [`OnApplyWindowInsetsListener`][insetslistener] on your content's host view. That listener is used to update the value of an ambient bundled in this library: `AmbientWindowInsets`.

`AmbientWindowInsets` holds an instance of `WindowInsets` which contains the value of various [WindowInsets][insets] [types][insettypes]. You can use the values manually like so:

``` kotlin
@Composable
fun ImeAvoidingBox() {
  val insets = AmbientWindowInsets.current

  Box(Modifier.padding(bottom = insets.ime.bottom))
}
```

...but we also provide some easy-to-use [Modifier][modifier]s.

### Modifiers

We provide two types of modifiers for easy handling of insets: padding and size.

#### Padding modifiers
The padding modifiers allow you to apply padding to a composable which matches a specific type of inset. Currently we provide:

- `Modifier.statusBarsPadding()`
- `Modifier.navigationBarsPadding()`
- `Modifier.systemBarsPadding()`

These are commonly used to move composables out from under the system bars. The common example would be a [`FloatingActionButton`][fab]:

``` kotlin
FloatingActionButton(
    icon = { Icon(...) },
    modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp) // normal 16dp of padding for FABs
        .navigationBarsPadding() // Move it out from under the nav bar
)
```

#### Size modifiers
The size modifiers allow you to match the size of a composable to a specific type of inset. Currently we provide:

- `Modifier.statusBarsHeight()`
- `Modifier.navigationBarsHeight()`
- `Modifier.navigationBarsWidth()`

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

You may want to use inset values for content padding, so this library provides the `Insets.toPaddingValues()` extension function to convert between `Insets` and `PaddingValues`. Here's an example of using the system bars insets:

``` kotlin
LazyColumn(
  contentPadding = AmbientWindowInsets.current.systemBars.toPaddingValues()
)
```

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-insets:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/chrisbanes/accompanist/accompanist-glide/
[insetter-view]: https://github.com/chrisbanes/insetter
[insets]: https://developer.android.com/reference/kotlin/androidx/core/view/WindowInsetsCompat
[insettypes]: https://developer.android.com/reference/kotlin/androidx/core/view/WindowInsetsCompat.Type
[insetslistener]: https://developer.android.com/reference/kotlin/androidx/core/view/OnApplyWindowInsetsListener
[modifier]: https://developer.android.com/reference/kotlin/androidx/ui/core/Modifier
[paddingvalues]: https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/PaddingValues
[lazycolumn]: https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/package-summary#lazycolumn
[fab]: https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#floatingactionbutton