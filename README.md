![Accompanist logo](docs/header.png)

Accompanist is a group of libraries that aim to supplement [Jetpack Compose][compose] with features that are commonly required by developers but not yet available.

Currently, Accompanist contains:

### 📐 [Insets](./insets/)
A library that brings [WindowInsets][windowinsets] support to Jetpack Compose.

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

---

## Updates

[Jetpack Compose][compose] is a fast-moving project and we aim to keep these these libraries up-to-date with the
latest tagged release on Compose as quickly as possible. Each [release](https://github.com/google/accompanist/releases)  outlines what version of the Compose libraries it depends on.

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