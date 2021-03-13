# AppCompat Compose Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-appcompat-theme)](https://search.maven.org/search?q=g:com.google.accompanist)

AppCompat Compose Theme Adapter enables reuse of [AppCompat][appcompat] XML themes, for theming in [Jetpack Compose][compose].

## Usage
This library attempts to bridge the gap between [AppCompat][appcompat] XML themes, and themes in [Jetpack Compose][compose],
 allowing your composable [`MaterialTheme`][materialtheme] to be based on the `Activity`'s XML theme:

``` kotlin
AppCompatTheme {
    // MaterialTheme.colors, MaterialTheme.shapes, MaterialTheme.typography
    // will now contain copies of the context's theme
}
```

For more information, visit the documentation: https://google.github.io/accompanist/appcompat-theme

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-appcompat-theme:<version>"
}
```

Snapshots of the development version are available in Sonatype's `snapshots` [repository][snap]. These are updated on every commit.

  [compose]: https://developer.android.com/jetpack/compose
  [appcompat]: https://developer.android.com/jetpack/androidx/releases/appcompat
  [snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/google/accompanist/accompanist-appcompat-theme/