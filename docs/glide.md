# Glide for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-glide)](https://search.maven.org/search?q=g:com.google.accompanist)

This library brings easy-to-use [Painter][painter] which can fetch and display images from external sources, such as network, using the [Glide][glide] image loading library.

<img src="https://github.com/bumptech/glide/blob/master/static/glide_logo.png?raw=true" width="480" alt="Glide logo">

!!! tip
    Unless you have a specific requirement to use Glide, consider using [Coil](coil.md) instead.
    Coil is built upon Kotlin Coroutines which means that it integrates better with
    Jetpack Compose, which also heavily uses [Coroutines](https://developer.android.com/jetpack/compose/kotlin#coroutines).

??? info "Migrating from GlideImage"
    If you're migrating from Accompanist 0.7.x or before, please read the [migration](./migration-glideimage) documentation after reading this document.

## `rememberGlidePainter()`

The primary API is via the [`rememberGlidePainter()`][rememberpainter] function. The simplest usage is like so:

```kotlin 
import androidx.compose.foundation.Image
import com.google.accompanist.glide.rememberGlidePainter

Image(
    painter = rememberGlidePainter("https://picsum.photos/300/300"),
    contentDescription = stringResource(R.string.image_content_desc),
    previewPlaceholder = R.drawable.placeholder,
)
```

This painter loads the data passed in, using [Glide][glide], and then draws the resulting image.

You can also customize the Glide [`RequestBuilder`](https://bumptech.github.io/glide/javadocs/4110/com/bumptech/glide/RequestBuilder.html) through the `requestBuilder` parameter. This allows usage of things like (but not limited to) transformations:

```kotlin
import androidx.compose.foundation.Image
import com.google.accompanist.glide.rememberGlidePainter

Image(
    painter = rememberGlidePainter(
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


A `fadeIn: Boolean` parameter is available on [`rememberGlidePainter()`][rememberpainter] (default: `false`). When enabled, a default fade-in animation will be used when the image is successfully loaded:

``` kotlin
import androidx.compose.foundation.Image
import com.google.accompanist.glide.rememberGlidePainter

Image(
    painter = rememberGlidePainter(
        "https://picsum.photos/300/300",
        fadeIn = true
    ),
    contentDescription = stringResource(R.string.image_content_desc),
)
```

## Custom content

Some times you may wish to display some alternative content whilst the image is loading, or an error has occurred. The painter returned from `rememberGlidePainter()` is an instance of [`LoadPainter`][loadpainter], which is stateful and allows you to display different content as required:


``` kotlin
val painter = rememberGlidePainter("https://picsum.photos/300/300")

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
    painter = rememberGlidePainter(
        request = "https://picsum.photos/300/300",
        previewPlaceholder = R.drawable.placeholder,
    ),
    contentDescription = stringResource(R.string.image_content_desc),
)
```

If the referenced drawable is only used for the purposes of `previewPlaceholder`s, it can be placed in the resources of your `debug` build variant For example: `app/debug/res/drawable/`. This allows the drawable to be only bundled in your debug builds, and not shipped to users of your release build.

## GIFs

Accompanist Glide supports GIFs through Glide's own GIF support. There's nothing you need to do, it should just work.

![Example GIF](https://media.giphy.com/media/6oMKugqovQnjW/giphy.gif)

## Observing load state changes

To observe changes to the load state you can use [`snapshotFlow()`][snapshotflow] to observe changes to `painter.loadState`, and then call your logic as necessary:

``` kotlin
val painter = rememberGlidePainter("https://image.url")

LaunchedEffect(painter) {
    snapshotFlow { painter.loadState }
        .filter { it.isFinalState() }
        .collect { result ->
            // TODO do something with result
        }
}

Image(painter = painter)
```

## Custom RequestManager

If you wish to provide a default [`RequestManager`](https://bumptech.github.io/glide/javadocs/4120/com/bumptech/glide/RequestManager.html) to use across all of your `rememberGlidePainter()`
calls, we provide the [`LocalRequestManager`][local] composition local.

You can use it like so:

``` kotlin
val requestManager = Glide.with(...)
    // customize the RequestManager as needed
    .build()

CompositionLocalProvider(LocalRequestManager provides requestManager) {
    // This will automatically use the value of LocalRequestManager
    Image(
        painter = rememberGlidePainter(...)
    )
}
```

For more information on composition locals, see [here](https://developer.android.com/reference/kotlin/androidx/compose/runtime/CompositionLocal).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-glide)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-glide:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

  [compose]: https://developer.android.com/jetpack/compose
  [snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-glide/
  [glide]: https://bumptech.github.io/glide/
  [rememberpainter]: ../api/glide/glide/com.google.accompanist.glide/remember-glide-painter.html
  [imageloadstate]: ../api/imageloading-core/imageloading-core/com.google.accompanist.imageloading/-image-load-state/index.html
  [loadpainter]: ../api/imageloading-core/imageloading-core/com.google.accompanist.imageloading/-load-painter/index.html
  [local]: ../api/glide/glide/com.google.accompanist.glide/-local-request-manager.html
  [crossfade]: https://developer.android.com/reference/kotlin/androidx/compose/animation/package-summary#crossfade
  [painter]: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/painter/Painter
  [snapshotflow]: https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#snapshotflow