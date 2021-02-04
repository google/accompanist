# Coil for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-coil)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

This library brings easy-to-use composable which can fetch and display images from external sources, such as network, using the [Coil][coil] image loading library.

<img src="https://coil-kt.github.io/coil/logo.svg" width="480" alt="Coil logo">

## `CoilImage()`

The primary API is via the `CoilImage()` functions. There are a number of function versions available.

The simplest usage is like so:

```kotlin 
CoilImage(
    data = "https://picsum.photos/300/300"
)
```

This loads the `data` passed in with [Coil][coil], and then displays the resulting image using the standard `Image` composable.

You can also customize the Coil [`ImageRequest`](https://coil-kt.github.io/coil/image_requests/) through the `requestBuilder` parameter. This allows usage of things like (but not limited to) transformations:

```kotlin
CoilImage(
    data = "https://picsum.photos/300/300",
    requestBuilder = {
        transformations(CircleCropTransformation())
    },
)
```

It also provides optional content 'slots', allowing you to provide custom content to be displayed when the request is loading, and/or if the image request failed:

``` kotlin
CoilImage(
    data = "https://picsum.photos/300/300",
    loading = {
        Box(Modifier.matchParentSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    },
    error = {
        Image(asset = imageResource(R.drawable.ic_error))
    }
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
    data = "https://picsum.photos/300/300",
    fadeIn = true
)
```

## Custom content

If you need more control over the animation, or you want to provide custom layout for the loaded image, you can use the `content` composable version of `CoilImage`:

``` kotlin
CoilImage(
    data = "https://picsum.photos/300/300",
) { imageState ->
    when (imageState) {
        is ImageLoadState.Success -> {
            MaterialLoadingImage(
                result = imageState,
                fadeInEnabled = true,
                fadeInDurationMs = 600,
            )
        }
        is ImageLoadState.Error -> /* TODO */
        ImageLoadState.Loading -> /* TODO */
        ImageLoadState.Empty -> /* TODO */
    }
}
```

## GIFs

Accompanist Coil supports GIFs through Coil's own GIF support. Follow the [setup instructions](https://coil-kt.github.io/coil/gifs/) and it should just work.

## Custom ImageLoader

If you wish to provide a default [`ImageLoader`](https://coil-kt.github.io/coil/image_loaders/) to use across all of your `CoilImage`
calls, we provide the `LocalImageLoader` composition local.

You can use it like so:

``` kotlin
val imageLoader = ImageLoader.Builder(context)
    // customize the ImageLoader as needed
    .build()

Providers(LocalImageLoader provides imageLoader) {
    // This will automatically use the value of LocalImageLoader
    CoilImage(
        data = ...
    )
}
```

For more information on composition locals, see [here](https://developer.android.com/reference/kotlin/androidx/compose/runtime/CompositionLocal).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-coil)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-coil:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

### What's the goal of the library?

Eventually the goal is to upstream all of this functionality back to [Coil][coil]. [Jetpack Compose][compose]'s development is currently moving very fast, which means that there are frequent API changes between releases. For now, it makes sense to keep this as a seperately released library to track the latest Compose release.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/chrisbanes/accompanist/accompanist-coil/
[coil]: https://github.com/coil-kt/coil
