# Glide for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-glide)](https://search.maven.org/search?q=g:com.google.accompanist)

This library brings easy-to-use [Painter][painter] which can fetch and display images from external sources, such as network, using the [Glide][glide] image loading library.

<img src="https://github.com/bumptech/glide/blob/master/static/glide_logo.png?raw=true" width="480" alt="Glide logo">

!!! info
    If you're migrating from Accompanist 0.7.x or before, please read the [migration](./migration-glideimage) documentation after reading this document.

## `rememberGlidePainter()`

The primary API is via the [`rememberGlidePainter()`][rememberpainter] function. The simplest usage is like so:

```kotlin 
import androidx.compose.foundation.Image
import com.google.accompanist.glide.rememberGlidePainter

Image(
    painter = rememberGlidePainter("https://picsum.photos/300/300"),
    contentDescription = "My content description",
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
    contentDescription = "My content description",
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
    contentDescription = "My content description",
)
```

## Custom content

Some times you may wish to display some alternative content whilst the image is loading, or an error has occurred. The painter returned from `rememberGlidePainter()` is an instance of [`LoadPainter`][loadpainter], which is stateful and allows you to display different content as required:


``` kotlin
val painter = rememberGlidePainter("https://picsum.photos/300/300")

Box {
    Image(
        painter = painter,
        contentDescription = "My content description",
    )

    if (painter.loadState == ImageLoadState.Loading) {
        // Display a circular progress indicator whilst loading
        CircularProgressIndicator(Modifier.align(Alignment.Center))     
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
    contentDescription = "My content description",
)
```

## GIFs

Accompanist Glide supports GIFs through Glide's own GIF support. There's nothing you need to do, it should just work.

![Example GIF](https://media.giphy.com/media/6oMKugqovQnjW/giphy.gif)

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