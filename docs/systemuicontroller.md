# System UI Controller for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-systemuicontroller)](https://search.maven.org/search?q=g:com.google.accompanist)

System UI Controller provides easy-to-use utilities for updating the System UI bar colors within Jetpack Compose.

## Usage
To control system UI in your composables, you must provide an instance to the `LocalSystemUiController`
composition local. This would typically be done near the top level of your composable hierarchy:

``` kotlin
setContent {
    // Create a controller, and provide it to the LocalSystemUiController
    val controller = androidSystemUiController(LocalView.current)
    CompositionLocalProvider(LocalSystemUiController provides controller) {
        MyHomeScreen()
    }
}
```

Then in your layouts, you can update the system bar colors as necessary like so:

``` kotlin
@Composable
fun MyHomeScreen() {
    // Get the current SystemUiController
    val systemUiController = LocalSystemUiController.current
    val isSystemInDarkTheme = isSystemInDarkTheme()

    SideEffect {
        // Update all ofthe system bar colors to be transparent, and use
        // dark icons if we're in light theme
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !isSystemInDarkTheme
        )

        // setStatusBarsColor() and setNavigationBarsColor() also exist
    }
}
```

## System bar icon colors
The library automatically handles API level differences when running on Android devices. If we look at the example
of status bar icons, Android only natively supports dark icons on API 23+. This library handles this by automatically
altering the requested color with a scrim, to maintain contrast:

![](api-scrim.png)

Similar happens on navigation bar color, which is only available on API 26+.

### Modifying scrim logic

The scrim logic can be modified if needed:

``` kotlin
systemUiController.setStatusBarsColor(
    color = Color.Transparent,
    darkIcons = true
) { requestedColor ->
    // TODO: return a darkened color to be used when the system doesn't
    // natively support dark icons
}
```

## Download
[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-systemuicontroller)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-systemuicontroller:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-systemuicontroller/
