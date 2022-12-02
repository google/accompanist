# AppCompat Compose Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-appcompat-theme)](https://search.maven.org/search?q=g:com.google.accompanist)

!!! warning
	**This library is deprecated in favor of the new [`themeadapter-appcompat`][themeadapterappcompatlib] artifact.** The migration guide and original documentation is below.

## Migration

Accompanist AppCompat Theme Adapter has moved from the [`appcompat-theme`][appcompatthemelib] artifact to the [`themeadapter-appcompat`][themeadapterappcompatlib] artifact.
The implementation is identical but the dependency and import package have changed.

### Migration steps

1. Change the dependency from `com.google.accompanist:accompanist-appcompat-theme:<version>` to `com.google.accompanist:accompanist-themeadapter-appcompat:<version>`
2. Change any `com.google.accompanist.appcompattheme.*` imports to `com.google.accompanist.themeadapter.appcompat.*`

## Original Docs

A library that enables reuse of [AppCompat][appcompat] XML themes for theming in [Jetpack Compose][compose].

The basis of theming in [Jetpack Compose][compose] is the [`MaterialTheme`][materialtheme] composable, where you provide [`Colors`][colors], [`Shapes`][shapes] and [`Typography`][typography] instances containing your styling parameters:

``` kotlin
MaterialTheme(
    typography = type,
    colors = colors,
    shapes = shapes
) {
    // Surface, Scaffold, etc
}
```

[AppCompat][appcompat] XML themes allow for similar but coarser theming via XML theme attributes, like so:

``` xml
<style name="Theme.MyApp" parent="Theme.AppCompat.DayNight">
    <item name="colorPrimary">@color/purple_500</item>
    <item name="colorAccent">@color/green_200</item>
</style>
```

This library attempts to bridge the gap between [AppCompat][appcompat] XML themes, and themes in [Jetpack Compose][compose], allowing your composable [`MaterialTheme`][materialtheme] to be based on the `Activity`'s XML theme:

``` kotlin
AppCompatTheme {
    // MaterialTheme.colors, MaterialTheme.shapes, MaterialTheme.typography
    // will now contain copies of the context's theme
}
```

This is especially handy when you're migrating an existing app, a fragment (or other UI container) at a time.

!!! caution
    If you are using [Material Design Components](https://material.io/develop/android/) in your app, you should use the
    [MDC Compose Theme Adapter](https://github.com/material-components/material-components-android-compose-theme-adapter)
    instead, as it allows much finer-grained reading of your theme.


### Customizing the theme

The [`AppCompatTheme()`][appcompattheme] function will automatically read the host context's AppCompat theme and pass them to [`MaterialTheme`][materialtheme] on your behalf, but if you want to customize the generated values, you can do so via the [`createAppCompatTheme()`][createappcompattheme] function:

``` kotlin
val context = LocalContext.current
var (colors, type) = context.createAppCompatTheme()

// Modify colors or type as required. Then pass them
// through to MaterialTheme...

MaterialTheme(
    colors = colors,
    typography = type
) {
    // rest of layout
}
```

</details>

## Generated theme

Synthesizing a material theme from a `Theme.AppCompat` theme is not perfect, since `Theme.AppCompat`
does not expose the same level of customization as is available in material theming.
Going through the pillars of material theming:

### Colors

AppCompat has a limited set of top-level color attributes, which means that [`AppCompatTheme()`][appcompattheme]
has to generate/select alternative colors in certain situations. The mapping is currently:

| MaterialTheme color | AppCompat attribute                                            |
|---------------------|-------------------------------------------------------|
| primary             | `colorPrimary`                                          |
| primaryVariant      | `colorPrimaryDark`                                      |
| onPrimary           | Calculated black/white                                |
| secondary           | `colorAccent`                                           |
| secondaryVariant    | `colorAccent`                                           |
| onSecondary         | Calculated black/white                                |
| surface             | Default                                               |
| onSurface           | `android:textColorPrimary`, else calculated black/white |
| background          | `android:colorBackground`                               |
| onBackground        | `android:textColorPrimary`, else calculated black/white |
| error               | `colorError`                                            |
| onError             | Calculated black/white                                |

Where the table says "calculated black/white", this means either black/white, depending on
which provides the greatest contrast against the corresponding background color.

### Typography

AppCompat does not provide any semantic text appearances (such as headline6, body1, etc), and
instead relies on text appearances for specific widgets or use cases. As such, the only thing
we read from an AppCompat theme is the default `app:fontFamily` or `android:fontFamily`.
For example:

``` xml
<style name="Theme.MyApp" parent="Theme.AppCompat">
    <item name="fontFamily">@font/my_font</item>
</style>
```

Compose does not currently support downloadable fonts, so any font referenced from the theme
should from your resources. See [here](https://developer.android.com/guide/topics/resources/font-resource)
for more information.

### Shape

AppCompat has no concept of shape theming, therefore we use the default value from
[`MaterialTheme.shapes`][shapes]. If you wish to provide custom values, use the `shapes` parameter on `AppCompatTheme`.

## Limitations

There are some known limitations with the implementation at the moment:

* This relies on your `Activity`/`Context` theme extending one of the `Theme.AppCompat` themes.
* Variable fonts are not supported in Compose yet, meaning that the value of `android:fontVariationSettings` are currently ignored.
* You can modify the resulting `MaterialTheme` in Compose as required, but this _only_ works in Compose. Any changes you make will not be reflected in the Activity theme.

---

## Usage

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-appcompat-theme)](https://search.maven.org/search?q=g:com.google.accompanist)

``` groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-appcompat-theme:<version>"
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
Copyright 2020 The Android Open Source Project
 
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

 [appcompatthemelib]: ../appcompat-theme
 [themeadapterappcompatlib]: ../themeadapter-appcompat
 [compose]: https://developer.android.com/jetpack/compose
 [appcompat]: https://developer.android.com/jetpack/androidx/releases/appcompat
 [appcompattheme]: ../api/appcompat-theme/appcompat-theme/com.google.accompanist.appcompattheme/-app-compat-theme.html
 [createappcompattheme]: ../api/appcompat-theme/appcompat-theme/com.google.accompanist.appcompattheme/create-app-compat-theme.html
 [materialtheme]: https://developer.android.com/reference/kotlin/androidx/compose/material/MaterialTheme
 [shapes]: https://developer.android.com/reference/kotlin/androidx/compose/material/Shapes
 [colors]: https://developer.android.com/reference/kotlin/androidx/compose/material/Colors
 [typography]: https://developer.android.com/reference/kotlin/androidx/compose/material/Typography