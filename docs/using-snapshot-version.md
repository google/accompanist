# Using a Snapshot Version of the Library

If you would like to depend on the cutting edge version of the Accompanist
library, you can use the [snapshot versions][snap] that are published to
[Sonatype OSSRH](https://central.sonatype.org/)'s snapshot repository. These are updated on every commit to `main`.

To do so:

```groovy
repositories {
    // ...
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}

dependencies {
    // Check the latest SNAPSHOT version from the link above
    classpath 'com.google.accompanist:accompanist-coil:XXX-SNAPSHOT'
}
```

You might see a number of different versioned snapshots. If we use an example:

* `0.3.0-SNAPSHOT` is a build from the `main` branch, and depends on the latest tagged Jetpack Compose release (i.e. [alpha03](https://developer.android.com/jetpack/androidx/releases/compose#1.0.0-alpha03)).
* `0.3.0.compose-6574163-SNAPSHOT` is a build from the `snapshot` branch. This depends on the [SNAPSHOT build](https://androidx.dev) of Jetpack Compose from build `6574163`. You should only use these if you are using Jetpack Compose snapshot versions (see below).

### Using Jetpack Compose Snapshots

If you're using [`SNAPSHOT`](https://androidx.dev) versions of the `androidx.compose` libraries, you might run into issues with the current stable Accompanist release forcing an older version of those libraries.

We publish snapshot versions of Accompanist which depend on recent Jetpack Compose SNAPSHOT repositories. To find a recent build, look through the [snapshot repository][snap] for any versions in the scheme `x.x.x.compose-YYYY-SNAPSHOT` (for example: `0.3.0.compose-6574163-SNAPSHOT`). The `YYYY` in the scheme is the snapshot build being used from [AndroidX](https://androidx.dev) (from the example: build [`6574163`](https://androidx.dev/snapshots/builds/6574163/artifacts)). You can then use it like so:


``` groovy
repositories {
    // ...
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}

dependencies {
    // Check the latest SNAPSHOT version from the link above
    classpath 'com.google.accompanist:accompanist-coil:XXXX.compose-YYYYY-SNAPSHOT'
}
```

These builds are updated regularly, but there's no guarantee that we will create one for a given snapshot number.

*Note:* you might also see versions in the scheme `x.x.x.ui-YYYY-SNAPSHOT`. These are the same, just using an older suffix.


 [snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/