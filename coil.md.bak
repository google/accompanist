# Jetpack Compose + Coil

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.chrisbanes.accompanist/accompanist-coil/badge.svg)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

This library brings easy-to-use composable which can fetch and display images from external sources, such as network, using the [Coil][coil] image loading library.


## `CoilImage()`

The primary API is via the `CoilImage()` functions. There are a number of function versions available.

The simplest usage is like so:

```kotlin 
CoilImage(
    data = "https://loremflickr.com/300/300"
)
```

This loads the `data` passed in with [Coil][coil], and then displays the resulting image using the standard `Image` composable.

There is also a version of this function which accepts a Coil [`ImageRequest`](https://coil-kt.github.io/coil/image_requests/), allowing full customization of the request. This allows usage of things like (but not limited to) transformations:

```kotlin
CoilImage(
    request = GetRequest.Builder(ContextAmbient.current)
        .data("https://loremflickr.com/300/300")
        .transformations(CircleCropTransformation())
        .build()
)
```

## Fade-in animation

This library has built-in support for animating loaded images in, using a [fade-in animation](https://material.io/archive/guidelines/patterns/loading-images.html).

![](./images/crossfade.gif)

There are two ways to enable the animation:

### `fadeIn` parameter

A `fadeIn: Boolean` parameter has been added to `CoilImage` (default: `false`). When enabled, a default fade-in animation will be used when the image is successfully loaded:

``` kotlin
CoilImage(
    data = "https://random.image",
    fadeIn = true,
)
```

### Custom layout

If you need more control over the animation, you can use the `image` content composable lambda on `CoilImage` to display the result in a `MaterialLoadingImage`:

``` kotlin
CoilImage(
    data = "https://random.image",
) { result ->
    MaterialLoadingImage(
        result = result,
        fadeInDurationMs = 600,
    )
}
```

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

* Compose currently only supports static bitmap images, which means that we need to convert the resulting images to a `Bitmap`. This means that using things like Coil's [GIF support](https://coil-kt.github.io/coil/gifs/) will result in only the first frame being rendered, instead of animating.

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

### What's the goal of the library?

Eventually the goal is to upstream all of this functionality back to [Coil][coil]. [Jetpack Compose][compose]'s development is currently moving very fast, which means that there are frequent API changes between releases. For now, it makes sense to keep this as a seperately released library to track the latest Compose release.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/chrisbanes/accompanist/accompanist-coil/
[coil]: https://github.com/coil-kt/coil
