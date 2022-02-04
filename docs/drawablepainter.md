# Drawable Painter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-drawablepainter)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides a way to use Android [drawables](https://developer.android.com/guide/topics/resources/drawable-resource) as Jetpack Compose [Painters](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/painter/Painter).

This library attempts to support most Drawable configuration, as well as [Animatable](https://developer.android.com/reference/android/graphics/drawable/Animatable) drawables, such as [AnimatedVectorDrawable](https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable).

## Usage

``` kotlin
@Composable
fun DrawDrawable() {
    val drawable = AppCompatResources.getDrawable(LocalContext.current, R.drawable.rectangle)

    Image(
        painter = rememberDrawablePainter(drawable = drawable),
        contentDescription = "content description",
    )
}
```

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-drawablepainter)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-drawablepainter:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-drawablepainter/
