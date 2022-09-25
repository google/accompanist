![Accompanist logo](docs/header.png)

Accompanist is a group of libraries that aim to supplement [Jetpack Compose][compose] with features that are commonly required by developers but not yet available.

Accompanist is a labs like environment for new Compose APIs. We use it to help fill known gaps in the Compose toolkit, experiment with new APIs and to gather insight into the development experience of developing a Compose library. The goal of these libraries is to upstream them into the official toolkit, at which point they will be deprecated and removed from Accompanist.

For more details like, why does this library exist? Why is it not part of AndroidX? Will you be releasing more libraries? Check out our [Accompanist FAQ](https://medium.com/p/b55117b02712).

## Compose versions

Each [release](https://github.com/google/accompanist/releases) outlines what version of the Compose UI libraries it depends on. We are currently releasing multiple versions of Accompanist for the different versions of Compose:

<table>
 <tr>
  <td>Compose 1.0 (1.0.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-permissions?versionPrefix=0.20"></td>
 </tr>
 <tr>
  <td>Compose 1.1 (1.1.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-permissions?versionPrefix=0.23"></td>
 </tr>
 <tr>
  <td>Compose UI 1.2 (1.2.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-permissions?versionPrefix=0.25"></td>
 </tr>
  <tr>
  <td>Compose UI 1.3 (1.3.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-permissions"></td>
 </tr>
</table>

For stable versions of Compose, we use the latest *stable* version of the Compose compiler. For non-stable versions (alpha, beta, etc), we use the latest compiler at the time of release.

> :warning: **Ensure you are using the Accompanist version that matches with your Compose UI version**: If you upgrade Accompanist, it will upgrade your Compose libraries version via transitive dependencies.

## Libraries

### 🍫 [System UI Controller](./systemuicontroller/)
A library that provides easy-to-use utilities for recoloring the Android system bars from Jetpack Compose.

### 🎨 [AppCompat Theme Adapter](./appcompat-theme/)
A library that enables the reuse of [AppCompat][appcompat] XML themes for theming in Jetpack Compose.

### 📖 [Pager](./pager/)
A library that provides utilities for building paginated layouts in Jetpack Compose, similar to Android's [ViewPager][viewpager].

### 📫 [Permissions](./permissions/)
A library that provides [Android runtime permissions][runtimepermissions] support for Jetpack Compose.

### ⏳ [Placeholder](./placeholder/)
A library that provides easy-to-use modifiers for displaying a placeholder UI while content is loading.

### 🌊 [Flow Layouts](./flowlayout/)
A library that adds Flexbox-like layout components to Jetpack Compose.

### 🧭✨[Navigation-Animation](./navigation-animation/)
A library which provides [Compose Animation](https://developer.android.com/jetpack/compose/animation) support for Jetpack Navigation Compose.

### 🧭🎨️ [Navigation-Material](./navigation-material/)
A library which provides [Compose Material](https://developer.android.com/jetpack/androidx/releases/compose-material) support, such as modal bottom sheets, for Jetpack Navigation Compose.

### 🖌️ [Drawable Painter](./drawablepainter/)
A library which provides a way to use Android Drawables as Jetpack Compose Painters.

### ⬇️ [Swipe to Refresh](./swiperefresh/)
A library that provides a layout implementing the swipe-to-refresh UX pattern, similar to Android's [SwipeRefreshLayout](https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout).

### 🌏 [Web](./web/)
A wrapper around WebView for basic WebView support in Jetpack Compose.

### 📜 [Adaptive](./adaptive/)
A library providing a collection of utilities for adaptive layouts.

### 📐 [Insets](./insets/) (Deprecated)
See our [Migration Guide](https://google.github.io/accompanist/insets/) for migrating to Insets in Compose.

---

## Future?

Any of the features available in this group of libraries may become obsolete in the future, at which point they will (probably) become deprecated. 

We will aim to provide a migration path (where possible), to whatever supersedes the functionality.

## Snapshots

Snapshots of the current development version of Accompanist are available, which track the latest commit. See [here](docs/using-snapshot-version.md) for more information. 

---

### Why the name?

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

[appcompat]: https://developer.android.com/jetpack/androidx/releases/appcompat
[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/
[mdc]: https://material.io/develop/android/
[windowinsets]: https://developer.android.com/reference/kotlin/android/view/WindowInsets
[viewpager]: https://developer.android.com/reference/kotlin/androidx/viewpager/widget/ViewPager
[runtimepermissions]: https://developer.android.com/guide/topics/permissions/overview
