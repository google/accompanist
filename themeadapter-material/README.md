# Material Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-material)](https://search.maven.org/search?q=g:com.google.accompanist)

Material Theme Adapter enables the reuse of [MDC-Android][mdc] Material 2 XML themes, for theming in [Jetpack Compose][compose].

## Usage

This library attempts to bridge the gap between [MDC-Android][mdc] Material 2 XML themes, and themes in [Jetpack Compose][compose],
allowing your composable [`MaterialTheme`][materialtheme] to be based on the `Activity`'s XML theme:

``` kotlin
MdcTheme {
    // MaterialTheme.colors, MaterialTheme.typography and MaterialTheme.shapes
    // will now contain copies of the context's theme
}
```

For more information, visit the documentation: https://google.github.io/accompanist/themeadapter-material

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-themeadapter-material:<version>"
}
```

Snapshots of the development version are available in Sonatype's `snapshots` [repository][snap]. These are updated on every commit.

[mdc]: https://github.com/material-components/material-components-android
[compose]: https://developer.android.com/jetpack/compose
[materialtheme]: https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#materialtheme
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-themeadapter-material/
