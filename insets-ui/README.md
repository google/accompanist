# Insets: UI for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-insets)](https://search.maven.org/search?q=g:com.google.accompanist)

Insets for Jetpack Compose takes a lot of the ideas which drove [Insetter][insetter-view] for views, and applies them for use in composables.

## Usage
To setup Insets in your composables, you need to call the `ProvideWindowInsets` function and
wrap your content. This would typically be done near the top level of your composable hierarchy:

``` kotlin
setContent {
  MaterialTheme {
    ProvideWindowInsets {
      // your content
    }
  }
}
```

For more information, visit the documentation: https://google.github.io/accompanist/insets

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-insets:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.


  [snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-insets/