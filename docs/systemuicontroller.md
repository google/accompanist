# System UI Controller for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-systemuicontroller)](https://search.maven.org/search?q=g:com.google.accompanist)

!!! warning
    **This library is deprecated, and the API is no longer maintained. We recommend forking the implementation and customising it to your needs.** The original documentation is below.

## Migration
Recommendation: If you were using SystemUIController to go edge-to-edge in your activity and change the system bar colors and system bar icon colors, use the new [Activity.enableEdgeToEdge](https://developer.android.com/reference/androidx/activity/ComponentActivity#(androidx.activity.ComponentActivity).enableEdgeToEdge(androidx.activity.SystemBarStyle,androidx.activity.SystemBarStyle)) method available in androidx.activity 1.8.0-alpha03 and later. This method backports the scrims used on some versions of Android. [This](https://github.com/android/nowinandroid/pull/817) is a sample PR of the migration to the new method and removing the dependency on SystemUIController in Now in Android.

For other usages, migrate to using WindowInsetsControllerCompat or window APIs directly.

## Original Documentation
System UI Controller provides easy-to-use utilities for updating the System UI bar colors within Jetpack Compose.

## Usage
To control the system UI in your composables, you need to get a [`SystemUiController`](../api/systemuicontroller/systemuicontroller/com.google.accompanist.systemuicontroller/-system-ui-controller/) instance. The library provides the [`rememberSystemUiController()`](../api/systemuicontroller/systemuicontroller/com.google.accompanist.systemuicontroller/remember-system-ui-controller.html) function which returns an instance for the current system (currently only Android).

In your layouts you can update the system bar colors like so:

``` kotlin
// Remember a SystemUiController
val systemUiController = rememberSystemUiController()
val useDarkIcons = !isSystemInDarkTheme()

DisposableEffect(systemUiController, useDarkIcons) {
    // Update all of the system bar colors to be transparent, and use
    // dark icons if we're in light theme
    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = useDarkIcons
    )

    // setStatusBarColor() and setNavigationBarColor() also exist

    onDispose {}
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
systemUiController.setStatusBarColor(
    color = Color.Transparent,
    darkIcons = true
) { requestedColor ->
    // TODO: return a darkened color to be used when the system doesn't
    // natively support dark icons
}
```

## Samples

For complete samples, check out the [Insets samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/com/google/accompanist/sample/insets) which all use `SystemUiController` to set transparent system bars.

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
