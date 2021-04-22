![Accompanist logo](docs/header.png)

Accompanist is a group of libraries which aim to supplement [Jetpack Compose][compose] with features which are commonly required by developers, but not yet available.

Currently Accompanist contains:

### 🖼️ Image loading
A number of libraries which integrate popular image loading libraries into Jetpack Compose: [Coil](./coil/) and [Glide](./glide/).

### 📐 [Insets](./insets/)
A library which brings [WindowInsets](https://developer.android.com/reference/kotlin/android/view/WindowInsets) support to Jetpack Compose.

### 🍫 [System UI Controller](./systemuicontroller/)
A library which provides easy-to-use utilities for updating the System UI (status and navigation bars) colors from Jetpack Compose.

### 🎨 [AppCompat Theme Adapter](./appcompat-theme/)
A library that enables reuse of [AppCompat][appcompat] XML themes for theming in [Jetpack Compose][compose].

### 📖 [Pager](./pager/)
A library which provides paging layouts for Jetpack Compose, similar to Android's [`ViewPager`](https://developer.android.com/reference/kotlin/androidx/viewpager/widget/ViewPager).

### 🌊 [Flow layouts](./flowlayout/)
A library that adds a 'flexbox'-like layout to [Jetpack Compose][compose].

### ⬆️ [Swipe To refresh](./swiperefresh/)
A library which provides a layout which provides the swipe-to-refresh UX pattern, similar to Android's SwipeRefreshLayout.

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
