# AppCompat Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-appcompat)](https://search.maven.org/search?q=g:com.google.accompanist)

AppCompat Theme Adapter enables the reuse of [AppCompat][appcompat] XML themes, for theming in [Jetpack Compose][compose].

## Usage

This library attempts to bridge the gap between [AppCompat][appcompat] XML themes, and themes in [Jetpack Compose][compose],
allowing your composable [`MaterialTheme`][materialtheme] to be based on the `Activity`'s XML theme:

``` kotlin
AppCompatTheme {
    // MaterialTheme.colors and MaterialTheme.typography
    // will now contain copies of the context's theme
}
```

For more information, visit the documentation: https://google.github.io/accompanist/themeadapter-appcompat

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-themeadapter-appcompat:<version>"
}
```

Snapshots of the development version are available in Sonatype's `snapshots` [repository][snap]. These are updated on every commit.

[appcompat]: https://developer.android.com/jetpack/androidx/releases/appcompat
[compose]: https://developer.android.com/jetpack/compose
[materialtheme]: https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#materialtheme
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-themeadapter-appcompat/
