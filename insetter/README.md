# Insetter for Jetpack Compose

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.chrisbanes.accompanist/accompanist-insetter/badge.svg)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

Insetter for Jetpack Compose takes a lot of the ideas which drove [Insetter][insetter-view] for views, and applies them for use in composables.

## Usage
To setup Insetter in your composables, you need to call the `ProvideDisplayInsets` function and
wrap your content. This would typically be done near the top level of your composable hierarchy:

``` kotlin
setContent {
  MaterialTheme {
    ProvideDisplayInsets {
      // your content
    }
  }
}
```

> Note: Whether `ProvideDisplayInsets` is called outside or within `MaterialTheme` doesn't particularly matter.

`ProvideDisplayInsets` allows Insetter to set an [`OnApplyWindowInsetsListener`][insetslistener] on your content's host view. That listener is used to update the value of an ambient bundled in this library: `AmbientInsetter`.

`AmbientInsetter` holds an instance of `DisplayInsets` which contains the value of various [WindowInsets][insets] [types][insettypes]. You can use the values manually like so:

``` kotlin
@Composable
fun ImeAvoidingBox() {
  val insets = AmbientInsetter.current

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
    implementation "dev.chrisbanes.accompanist:accompanist-insetter:<version>"
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
