![Accompanist logo](./images/social.png)

Accompanist is a group of libraries which contains some utilities which I've found myself copying around projects which use [Jetpack Compose][compose]. Currently it contains:

 * 🎨 [Material Design Components theme integration](./mdc-theme/README.md)
 * 🖼️ [Coil image loading composables](./coil/README.md)

[Jetpack Compose][compose] is a fast moving project and I'll be updating these libraries to match the
latest tagged release as quickly as possible. Each [release listing](https://github.com/chrisbanes/accompanist/releases) will outline what version of Compose and the UI libraries it depends on.

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.chrisbanes.accompanist:accompanist-mdc-theme:<version>"
    implementation "dev.chrisbanes.accompanist:accompanist-coil:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

---

#### Why the name?

The library is all about adding some utilities around Compose. Music composing is done by a
composer, and since this library is about supporting composition, the supporting role of an [accompanist](https://en.wikipedia.org/wiki/Accompaniment) felt like a good name.

## Contributions

Please contribute! We will gladly review any pull requests.
Make sure to read the [Contributing](CONTRIBUTING.md) page first though.

## License

```
Copyright 2020 The Android Open Source Project
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/
[mdc]: https://material.io/develop/android/
