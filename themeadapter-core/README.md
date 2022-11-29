# Core Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-core)](https://search.maven.org/search?q=g:com.google.accompanist)

Core Theme Adapter includes common utilities that enable the reuse of XML themes, for theming in [Jetpack Compose][compose].

## Usage

This library includes common utilities that enable the reuse of XML themes, for theming in [Jetpack Compose][compose],
allowing composables like [`MaterialTheme`][materialtheme] to be based on the `Activity`'s XML theme.

For more information, visit the documentation: https://google.github.io/accompanist/themeadapter-core

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-themeadapter-core:<version>"
}
```

Snapshots of the development version are available in Sonatype's `snapshots` [repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[materialtheme]: https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#materialtheme
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-themeadapter-core/
