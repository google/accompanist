# Jetpack Compose + Coil

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.chrisbanes.accompanist/accompanist-coil/badge.svg)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

This library brings easy-to-use composable which can fetch and display images from external sources, such as network, using the [Coil][coil] image loading library.

There are currently two composables:

### `CoilImage()`

This loads the `data` passed in with [Coil][coil], and then displays the resulting image using the standard `Image` composable.

```kotlin 
CoilImage(
    data = "https://loremflickr.com/300/300"
)
```

There is also a version of this function which accepts a Coil [`GetRequest`](https://coil-kt.github.io/coil/api/coil-base/coil.request/-get-request/), allowing full customization of the request. This allows usage of things like (but not limited to) transformations:

```kotlin
CoilImage(
    request = GetRequest.Builder(ContextAmbient.current)
        .data("https://loremflickr.com/300/300")
        .transformations(CircleCropTransformation())
        .build()
)
```

### `CoilImageWithCrossfade()`

Very similar to `CoilImage`, but this will run a crossfade transition when the image is first loaded.

![](./images/crossfade.gif)

```kotlin 
CoilImageWithCrossfade(
    data = "https://loremflickr.com/300/300"
)
```

Similarly to `CoilImage`, there is a version of this function which accepts a [`GetRequest`](https://coil-kt.github.io/coil/api/coil-base/coil.request/-get-request/) instead of a `data`.

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-coil:<version>"
}
```

## Limitations

* Compose currently only supports static bitmap images, which means that we need to convert and resulting images to a `Bitmap`. This means that using things like Coil's [SVG support](https://coil-kt.github.io/coil/svgs/) will result in a rasterized bitmap, rather than displaying it as a vector.

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

### What's the goal of the library?

Eventually the goal is to upstream all of this functionality back to [Coil][coil]. [Jetpack Compose][compose]'s development is currently moving very fast, which means that there are frequent API changes between releases. For now, it makes sense to keep this as a seperately released library to track the latest Compose release.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/
[coil]: https://github.com/coil-kt/coil