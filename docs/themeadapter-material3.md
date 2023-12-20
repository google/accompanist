# Material 3 Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-material3)](https://search.maven.org/search?q=g:com.google.accompanist)

!!! warning
    **This library is deprecated, and the API is no longer maintained. We recommend generating a theme with [Material Theme Builder](https://m3.material.io/theme-builder)** The original documentation is below.

## Migration
Recommendation: Use the [Material Theme Builder](https://m3.material.io/theme-builder) tool, or an alternative design tool, to generate a matching XML and Compose theme implementation for your app. See [Migrating XML themes to Compose](https://developer.android.com/jetpack/compose/designsystems/views-to-compose) to learn more.

You can checkout [Material Design 3 in Compose](https://developer.android.com/jetpack/compose/designsystems/material3#material-theming) to learn more about creating and adding theme to your app using Material Theme Builder.

## Original Documenation
A library that enables the reuse of [MDC-Android][mdc] Material 3 XML themes, for theming in [Jetpack Compose][compose].

![Material 3 Theme Adapter header](themeadapter/material3-header.png)

The basis of Material Design 3 theming in [Jetpack Compose][compose] is the [`MaterialTheme`][materialtheme] composable, where you provide [`ColorScheme`][colorscheme], [`Typography`][typography] and [`Shapes`][shapes] instances containing your styling parameters:

``` kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = typography,
    shapes = shapes
) {
    // M3 Surface, Scaffold, etc.
}
```

[Material Components for Android][mdc] themes allow for similar theming for views via XML theme attributes, like so:

``` xml
<style name="Theme.MyApp" parent="Theme.Material3.DayNight">
    <!-- Material 3 color attributes -->
    <item name="colorPrimary">@color/purple_500</item>
    <item name="colorSecondary">@color/purple_700</item>
    <item name="colorTertiary">@color/green_200</item>

    <!-- Material 3 type attributes-->
    <item name="textAppearanceBodyLarge">@style/TextAppearance.MyApp.BodyLarge</item>
    <item name="textAppearanceBodyMedium">@style/TextAppearance.MyApp.BodyMedium</item>
    
    <!-- Material 3 shape attributes-->
    <item name="shapeAppearanceCornerSmall">@style/ShapeAppearance.MyApp.CornerSmall</item>
</style>
```

This library attempts to bridge the gap between [Material Components for Android][mdc] M3 XML themes, and themes in [Jetpack Compose][compose], allowing your composable [`MaterialTheme`][materialtheme] to be based on the `Activity`'s XML theme:


``` kotlin
Mdc3Theme {
    // MaterialTheme.colorScheme, MaterialTheme.typography, MaterialTheme.shapes
    // will now contain copies of the Context's theme
}
```

This is especially handy when you're migrating an existing app, a `Fragment` (or other UI container) at a time.

### Customizing the M3 theme

The [`Mdc3Theme()`][mdc3theme] function will automatically read the host `Context`'s MDC theme and pass them to [`MaterialTheme`][materialtheme] on your behalf, but if you want to customize the generated values, you can do so via the [`createMdc3Theme()`][createmdc3theme] function:

``` kotlin
val context = LocalContext.current
var (colorScheme, typography, shapes) = createMdc3Theme(
    context = context
)

// Modify colorScheme, typography or shapes as required.
// Then pass them through to MaterialTheme...

MaterialTheme(
    colorScheme = colorScheme,
    typography = typography,
    shapes = shapes
) {
    // Rest of M3 layout
}
```

### Limitations

There are some known limitations with the implementation at the moment:

* This relies on your `Activity`/`Context` theme extending one of the `Theme.Material3` themes.
* Text colors are not read from the text appearances by default. You can enable it via the `setTextColors` function parameter.
* Variable fonts are not supported in Compose yet, meaning that the value of `android:fontVariationSettings` are currently ignored.
* MDC `ShapeAppearances` allow setting of different corner families (cut, rounded) on each corner, whereas Compose's [Shapes][shapes] allows only a single corner family for the entire shape. Therefore only the `app:cornerFamily` attribute is read, others (`app:cornerFamilyTopLeft`, etc) are ignored.
* You can modify the resulting `MaterialTheme` in Compose as required, but this _only_ works in Compose. Any changes you make will not be reflected in the `Activity` theme.

---

## Usage

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-material3)](https://search.maven.org/search?q=g:com.google.accompanist)

``` groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-themeadapter-material3:<version>"
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
Copyright 2022 The Android Open Source Project
 
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

[compose]: https://developer.android.com/jetpack/compose
[mdc]: https://github.com/material-components/material-components-android
[mdc3theme]: ../api/themeadapter-material3/com.google.accompanist.themeadapter.material3/-mdc-3-theme.html
[createmdc3theme]: ../api/themeadapter-material3/com.google.accompanist.themeadapter.material3/create-mdc-3-theme.html
[materialtheme]: https://developer.android.com/reference/kotlin/androidx/compose/material3/MaterialTheme
[colorscheme]: https://developer.android.com/reference/kotlin/androidx/compose/material3/ColorScheme
[typography]: https://developer.android.com/reference/kotlin/androidx/compose/material3/Typography
[shapes]: https://developer.android.com/reference/kotlin/androidx/compose/material3/Shapes
