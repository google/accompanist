# Jetpack Compose + Material Design Components theme

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.chrisbanes.accompanist/accompanist-mdc-theme/badge.svg)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

The basis of theming in Compose comes through the [`MaterialTheme`][materialtheme] function, where you provide [`ColorPalette`](https://developer.android.com/reference/kotlin/androidx/ui/material/ColorPalette), [`Shapes`](https://developer.android.com/reference/kotlin/androidx/ui/material/Shapes) and [`Typography`](https://developer.android.com/reference/kotlin/androidx/ui/material/Typography) instances containing your styling parameters:

``` kotlin
MaterialTheme(
    typography = type,
    colors = colors,
    shapes = shapes
) {
    // Surface, Scaffold, etc
}
```

If you're migrating an existing app though, you will likely already have your brand/design styling parameters declared in the [Android Themes and Styling](https://medium.com/androiddevelopers/android-styling-themes-vs-styles-ebe05f917578) system. Wouldn't it be nice if we could re-use them in our compose layouts? 

Enter the `accompanist-mdc-theme` library, which contains a number of functions to help inter-operate between the two.

For times when you want to copy over your context's [Material Design Components][mdc] theme to your compose layouts, you can use it like so:


``` kotlin
MaterialThemeFromMdcTheme {
    // MaterialTheme.colors, MaterialTheme.shapes, MaterialTheme.typography
    // will now contain copies of the context theme
}
```

This is especially handy when you're migrating an existing app, a fragment (or other UI container) a piece at a time.

### Customizing the theme

The `MaterialThemeFromMdcTheme()` function will automatically read host context's MDC theme and pass them to [`MaterialTheme`][materialtheme] on your behalf, but if you want to customize the generated values, you can do so via the `generateMaterialThemeFromMdcTheme()` function:

``` kotlin
var (colors, type, shapes) = generateMaterialThemeFromMdcTheme()

// Modify colors, type or shapes are required. Then pass them
// through to MaterialTheme...

MaterialTheme(
    typography = type,
    colors = colors,
    shapes = shapes
) {
    // rest of layout
}
```

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-mdc-theme:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/
[mdc]: https://material.io/develop/android/

## Limitations

There are some known limitations with the implementation at the moment:

* This relies on your Activity/Context theme extending one of the `Theme.MaterialComponents` themes.
* Text colors are not read from any text appearances by default. You can enable it via the `useTextColors` function parameter.
* `android:fontVariationSettings` is currently not used, due to variable fonts not being implemented in Compose yet.
* MDC `ShapeAppearances` allow setting of corner families (cut, rounded) per corner, whereas Compose's [Shapes][shapes] allows one type for the entire shape. This means that only the `app:cornerFamily` attribute is read, with the others (`app:cornerFamilyTopLeft`, etc) being ignored.

 [materialtheme]: https://developer.android.com/reference/kotlin/androidx/ui/material/MaterialTheme
 [shapes]: https://developer.android.com/reference/kotlin/androidx/ui/material/Shapes