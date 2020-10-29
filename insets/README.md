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

TODO


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
