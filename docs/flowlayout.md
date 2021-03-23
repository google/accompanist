# Jetpack Compose Flow Layouts

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-flowlayout)](https://search.maven.org/search?q=g:com.google.accompanist)

Flow layouts adapted from the versions which were available in [Jetpack Compose][compose] until they were removed.

Unlike the standard `Row` and `Column` composables, these layout children across multiple rows/columns if they exceed the available space.

## Usage

``` kotlin
FlowRow {
    // row contents
}

FlowColumn {
    // column contents
}
```

For examples, refer to the [samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/com/google/accompanist/sample/flowlayout).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-flowlayout)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-flowlayout:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-flowlayout/