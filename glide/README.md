# Glide for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-glide)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

This library brings easy-to-use composable which can fetch and display images from external sources, such as network, using the [Glide][glide] image loading library.

<img src="https://github.com/bumptech/glide/blob/master/static/glide_logo.png?raw=true" width="480" alt="Glide logo">

## `GlideImage()`

The primary API is via the `GlideImage()` functions. There are a number of function versions available.

The simplest usage is like so:

```kotlin 
GlideImage(
    data = "https://picsum.photos/300/300"
)
```

This loads the `data` passed in with [Glide][glide], and then displays the resulting image using the standard `Image` composable.

You can also customize the Glide [`RequestBuilder`](https://bumptech.github.io/glide/javadocs/4110/com/bumptech/glide/RequestBuilder.html) through the `requestBuilder` parameter. This allows usage of things like (but not limited to) transformations:

```kotlin
GlideImage(
    data = "https://picsum.photos/300/300",
    requestBuilder = {
        val options = RequestOptions()
        options.centerCrop()

        apply(options)
    },
)
```

It also provides optional content 'slots', allowing you to provide custom content to be displayed when the request is loading, and/or if the image request failed:

``` kotlin
GlideImage(
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

A `fadeIn: Boolean` parameter has been added to `GlideImage` (default: `false`). When enabled, a default fade-in animation will be used when the image is successfully loaded:

``` kotlin
GlideImage(
    data = "https://picsum.photos/300/300",
    fadeIn = true
)
```

## Custom content

If you need more control over the animation, or you want to provide custom layout for the loaded image, you can use the `content` composable version of `GlideImage`:

``` kotlin
GlideImage(
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

Accompanist Glide supports GIFs through Glide's own GIF support. There's nothing you need to do, it should just work.

![Example GIF](https://media.giphy.com/media/6oMKugqovQnjW/giphy.gif)

## Custom RequestManager

If you wish to provide a default `RequestManager` to use across all of your `GlideImage`
calls, we provide the `AmbientRequestManager` ambient.

You can use it like so:

``` kotlin
val requestManager = Glide.with(...)
    // customize the RequestManager as needed
    .build()

Providers(AmbientRequestManager provides requestManager) {
    // This will automatically use the value of AmbientRequestManager
    GlideImage(
        data = ...
    )
}
```

For more information on ambients, see [here](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Ambient).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/dev.chrisbanes.accompanist/accompanist-glide)](https://search.maven.org/search?q=g:dev.chrisbanes.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-glide:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/chrisbanes/accompanist/accompanist-glide/
[glide]: https://bumptech.github.io/glide/
