# Jetpack Compose Flow Layouts

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-flowlayout)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

Flow layouts adapted from the [Jetpack Compose][compose] alpha versions.

Unlike the standard Row and Column composables, these lay out children in multiple rows/columns if they exceed the available space.

## Usage

``` kotlin
FlowRow {
    // row contents
}

FlowColumn {
    // column contents
}
```

For examples, refer to the [samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/dev/chrisbanes/accompanist/sample/flowlayout).

For more information, visit the documentation: https://google.github.io/accompanist/flowlayout

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-flowlayout:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/chrisbanes/accompanist/accompanist-flowlayout/