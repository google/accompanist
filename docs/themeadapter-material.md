# Material Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-material)](https://search.maven.org/search?q=g:com.google.accompanist)

!!! warning
    **This library is deprecated, and the API is no longer maintained.** The original documentation is below.

## Migration
Recommendation: Use the [Material Theme Builder](https://m3.material.io/theme-builder) tool, or an alternative design tool, to generate a matching XML and Compose theme implementation for your app. See [Migrating XML themes to Compose](https://developer.android.com/jetpack/compose/designsystems/views-to-compose) to learn more.

You can checkout [Material Design 3 in Compose](https://developer.android.com/jetpack/compose/designsystems/material3#material-theming) to learn more about creating and adding theme to your app using Material Theme Builder.

## Original Documentation

A library that enables the reuse of [MDC-Android][mdc] Material 2 XML themes, for theming in [Jetpack Compose][compose].

![Material Theme Adapter header](themeadapter/material-header.png)

The basis of Material Design 2 theming in [Jetpack Compose][compose] is the [`MaterialTheme`][materialtheme] composable, where you provide [`Colors`][colors], [`Typography`][typography] and [`Shapes`][shapes] instances containing your styling parameters:

``` kotlin
MaterialTheme(
    colors = colors,
    typography = type,
    shapes = shapes
) {
    // M2 Surface, Scaffold, etc.
}
```

[Material Components for Android][mdc] themes allow for similar theming for views via XML theme attributes, like so:

``` xml
<style name="Theme.MyApp" parent="Theme.MaterialComponents.DayNight">
    <!-- Material 2 color attributes -->
    <item name="colorPrimary">@color/purple_500</item>
    <item name="colorSecondary">@color/green_200</item>

    <!-- Material 2 type attributes-->
    <item name="textAppearanceBody1">@style/TextAppearance.MyApp.Body1</item>
    <item name="textAppearanceBody2">@style/TextAppearance.MyApp.Body2</item>

    <!-- Material 2 shape attributes-->
    <item name="shapeAppearanceSmallComponent">@style/ShapeAppearance.MyApp.SmallComponent</item>
</style>
```

This library attempts to bridge the gap between [Material Components for Android][mdc] M2 XML themes, and themes in [Jetpack Compose][compose], allowing your composable [`MaterialTheme`][materialtheme] to be based on the `Activity`'s XML theme:


``` kotlin
MdcTheme {
    // MaterialTheme.colors, MaterialTheme.typography, MaterialTheme.shapes
    // will now contain copies of the Context's theme
}
```

This is especially handy when you're migrating an existing app, a `Fragment` (or other UI container) at a time.

!!! caution
    If you are using an AppCompat (i.e. non-MDC) theme in your app, you should use
    [AppCompat Theme Adapter](https://github.com/google/accompanist/tree/main/themeadapter-appcompat)
    instead, as it attempts to bridge the gap between [AppCompat][appcompat] XML themes, and M2 themes in [Jetpack Compose][compose].

### Customizing the M2 theme

The [`MdcTheme()`][mdctheme] function will automatically read the host `Context`'s MDC theme and pass them to [`MaterialTheme`][materialtheme] on your behalf, but if you want to customize the generated values, you can do so via the [`createMdcTheme()`][createmdctheme] function:

``` kotlin
val context = LocalContext.current
val layoutDirection = LocalLayoutDirection.current
var (colors, typography, shapes) = createMdcTheme(
    context = context,
    layoutDirection = layoutDirection
)

// Modify colors, typography or shapes as required.
// Then pass them through to MaterialTheme...

MaterialTheme(
    colors = colors ?: MaterialTheme.colors,
    typography = typography ?: MaterialTheme.typography,
    shapes = shapes ?: MaterialTheme.shapes
) {
    // Rest of M2 layout
}
```

### Limitations

There are some known limitations with the implementation at the moment:

* This relies on your `Activity`/`Context` theme extending one of the `Theme.MaterialComponents` themes.
* Text colors are not read from the text appearances by default. You can enable it via the `setTextColors` function parameter.
* Variable fonts are not supported in Compose yet, meaning that the value of `android:fontVariationSettings` are currently ignored.
* MDC `ShapeAppearances` allow setting of different corner families (cut, rounded) on each corner, whereas Compose's [Shapes][shapes] allows only a single corner family for the entire shape. Therefore only the `app:cornerFamily` attribute is read, others (`app:cornerFamilyTopLeft`, etc) are ignored.
* You can modify the resulting `MaterialTheme` in Compose as required, but this _only_ works in Compose. Any changes you make will not be reflected in the `Activity` theme.

---

## Usage

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-material)](https://search.maven.org/search?q=g:com.google.accompanist)

``` groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-themeadapter-material:<version>"
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
[mdctheme]: ../api/themeadapter-material/com.google.accompanist.themeadapter.material/-mdc-theme.html
[createmdctheme]: ../api/themeadapter-material/com.google.accompanist.themeadapter.material/create-mdc-theme.html
[materialtheme]: https://developer.android.com/reference/kotlin/androidx/compose/material/MaterialTheme
[colors]: https://developer.android.com/reference/kotlin/androidx/compose/material/Colors
[typography]: https://developer.android.com/reference/kotlin/androidx/compose/material/Typography
[shapes]: https://developer.android.com/reference/kotlin/androidx/compose/material/Shapes
