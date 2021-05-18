# Coil for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-coil)](https://search.maven.org/search?q=g:com.google.accompanist)

This library provides easy-to-use [Painter][painter] which can fetch and display images from external sources, such as network, using the [Coil][coil] image loading library.

<img src="https://coil-kt.github.io/coil/logo.svg" width="480" alt="Coil logo">

??? info "Migrating from CoilImage"
    If you're migrating from Accompanist 0.7.x or before, please read the [migration](./migration-coilimage) documentation after reading this document.

## `rememberCoilPainter()`

The primary API is via the [`rememberCoilPainter()`][rememberpainter] function. The simplest usage is like so:

```kotlin 
import androidx.compose.foundation.Image
import com.google.accompanist.coil.rememberCoilPainter

Image(
    painter = rememberCoilPainter("https://picsum.photos/300/300"),
    contentDescription = stringResource(R.string.image_content_desc),
)
```

This painter loads the data passed in, using [Coil][coil], and then draws the resulting image.

You can also customize the Coil [`ImageRequest`](https://coil-kt.github.io/coil/image_requests/) through the `requestBuilder` parameter. This allows usage of things like (but not limited to) transformations:

```kotlin
import androidx.compose.foundation.Image
import com.google.accompanist.coil.rememberCoilPainter

Image(
    painter = rememberCoilPainter(
        request = "https://picsum.photos/300/300",
        requestBuilder = {
            transformations(CircleCropTransformation())
        },
    ),
    contentDescription = stringResource(R.string.image_content_desc),
)
```

## Fade-in animation

This library has built-in support for animating loaded images in, using a [fade-in animation](https://material.io/archive/guidelines/patterns/loading-images.html).

![](crossfade.gif)


A `fadeIn: Boolean` parameter is available on [`rememberCoilPainter()`][rememberpainter] (default: `false`). When enabled, a default fade-in animation will be used when the image is successfully loaded:

``` kotlin
import androidx.compose.foundation.Image
import com.google.accompanist.coil.rememberCoilPainter

Image(
    painter = rememberCoilPainter(
        "https://picsum.photos/300/300",
        fadeIn = true
    ),
    contentDescription = stringResource(R.string.image_content_desc),
)
```

## Custom content

Some times you may wish to display alternative content whilst the image is loading, or an error has occurred. The painter returned from `rememberCoilPainter()` is an instance of [`LoadPainter`][loadpainter], which is stateful and allows you to display different content as required:


``` kotlin
val painter = rememberCoilPainter("https://picsum.photos/300/300")

Box {
    Image(
        painter = painter,
        contentDescription = stringResource(R.string.image_content_desc),
    )

    when (painter.loadState) {
        is ImageLoadState.Loading -> {
            // Display a circular progress indicator whilst loading
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        is ImageLoadState.Error -> {
            // If you wish to display some content if the request fails
        }
    }
}
```

[`ImageLoadState`][imageloadstate] has a number of different states, so tweak your logic to suit. You could also use a [`Crossfade()`][crossfade] or any other custom animation.

## Previews

To support Android Studio [Composable Previews](https://developer.android.com/jetpack/compose/tooling), you can provide a drawable resource ID via the `previewPlaceholder` parameter. That drawable will then be displayed when your content is displayed as a preview:

```kotlin
Image(
    painter = rememberCoilPainter(
        request = "https://picsum.photos/300/300",
        previewPlaceholder = R.drawable.placeholder,
    ),
    contentDescription = stringResource(R.string.image_content_desc),
)
```

If the referenced drawable is only used for the purposes of `previewPlaceholder`s, it can be placed in the resources of your `debug` build variant For example: `app/debug/res/drawable/`. This allows the drawable to be only bundled in your debug builds, and not shipped to users of your release build.

## GIFs

Accompanist Coil supports GIFs through Coil's own GIF support. Follow the [setup instructions](https://coil-kt.github.io/coil/gifs/) and it should just work.

## Observing load state changes

To observe changes to the load state you can use [`snapshotFlow()`][snapshotflow] to observe changes to `painter.loadState`, and then call your logic as necessary:

``` kotlin
val painter = rememberCoilPainter("https://image.url")

LaunchedEffect(painter) {
    snapshotFlow { painter.loadState }
        .filter { it.isFinalState() }
        .collect { result ->
            // TODO do something with result
        }
}

Image(painter = painter)
```

## Custom ImageLoader

If you wish to provide a default [`ImageLoader`](https://coil-kt.github.io/coil/image_loaders/) to use across all of your `rememberCoilPainter()`
calls, we provide the [`LocalImageLoader`][local] composition local.

You can use it like so:

``` kotlin
val imageLoader = ImageLoader.Builder(context)
    // customize the ImageLoader as needed
    .build()

CompositionLocalProvider(LocalImageLoader provides imageLoader) {
    // This will automatically use the value of LocalImageLoader
    Image(
        painter = rememberCoilPainter(...)
    )
}
```

For more information on composition locals, see [here](https://developer.android.com/reference/kotlin/androidx/compose/runtime/CompositionLocal).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-coil)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-coil:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

  [compose]: https://developer.android.com/jetpack/compose
  [snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-coil/
  [coil]: https://github.com/coil-kt/coil
  [rememberpainter]: ../api/coil/coil/com.google.accompanist.coil/remember-coil-painter.html
  [imageloadstate]: ../api/imageloading-core/imageloading-core/com.google.accompanist.imageloading/-image-load-state/index.html
  [loadpainter]: ../api/imageloading-core/imageloading-core/com.google.accompanist.imageloading/-load-painter/index.html
  [local]: ../api/coil/coil/com.google.accompanist.coil/-local-image-loader.html
  [crossfade]: https://developer.android.com/reference/kotlin/androidx/compose/animation/package-summary#crossfade
  [painter]: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/painter/Painter
  [snapshotflow]: https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#snapshotflow
