# Picasso for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-picasso)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

This library brings easy-to-use composable which can fetch and display images from external sources, such as network, using the [Picasso][picasso] v2 image loading library.

<img src="https://raw.githubusercontent.com/square/picasso/master/website/static/sample.png" width="400" alt="Picasso sample screenshot">

## `PicassoImage()`

The primary API is via the `PicassoImage()` functions. There are multiple function versions available.

The simplest usage is like so:

```kotlin 
PicassoImage(
    data = "https://picsum.photos/300/300"
)
```

This loads the `data` passed in with [Picasso][Picasso], and then displays the resulting image using the standard `Image` composable.

You can also customize the Picasso [`RequestCreator`](https://square.github.io/picasso/2.x/picasso/com/squareup/picasso/RequestCreator.html) through the `requestBuilder` parameter. This allows usage of things like (but not limited to) transformations:

```kotlin
PicassoImage(
    data = "https://picsum.photos/300/300",
    requestBuilder = {
        rotate(90f)
    }
)
```

It also provides optional content 'slots', allowing you to provide custom content to be displayed when the request is loading, and/or if the image request failed:

``` kotlin
PicassoImage(
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

A `fadeIn: Boolean` parameter is available on `PicassoImage` (default: `false`). When enabled, a default fade-in animation will be used when the image is successfully loaded:

``` kotlin
PicassoImage(
    data = "https://picsum.photos/300/300",
    fadeIn = true
)
```

## Custom content

If you need more control over the animation, or you want to provide custom layout for the loaded image, you can use the `content` composable version of `PicassoImage`:

``` kotlin
PicassoImage(
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

## Custom Picasso

If you wish to provide a default `Picasso` to use across all of your `PicassoImage`
calls, we provide the `LocalPicasso` composition local. 

You can use it like so:

``` kotlin
val picasso = Picasso.Builder(...)
    // Customize as required
    .build()

CompositionLocalProvider(LocalPicasso provides picasso) {
    // This will automatically use the value of LocalPicasso
    PicasoImage(
        data = ...
    )
}
```

For more information on composition locals, see [here](https://developer.android.com/reference/kotlin/androidx/compose/runtime/CompositionLocal).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-picasso)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-picasso:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/chrisbanes/accompanist/accompanist-picasso/
[picasso]: https://square.github.io/picasso/
