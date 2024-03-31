# Core Theme Adapter

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-core)](https://search.maven.org/search?q=g:com.google.accompanist)

!!! warning
    **This library is deprecated, and the API is no longer maintained.** The original documentation is below.

## Migration
Recommendation: Use the [Material Theme Builder](https://m3.material.io/theme-builder) tool, or an alternative design tool, to generate a matching XML and Compose theme implementation for your app. See [Migrating XML themes to Compose](https://developer.android.com/jetpack/compose/designsystems/views-to-compose) to learn more.

You can checkout [Material Design 3 in Compose](https://developer.android.com/jetpack/compose/designsystems/material3#material-theming) to learn more about creating and adding theme to your app using Material Theme Builder.

## Original Documentation

A library that includes common utilities that enable the reuse of XML themes, for theming in [Jetpack Compose][compose].

See the [API][api] for more details.

---

## Usage

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-themeadapter-core)](https://search.maven.org/search?q=g:com.google.accompanist)

``` groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-themeadapter-core:<version>"
}
```

### Library Snapshots

Snapshots of the current development version of this library are available, which track the latest commit. See [here](../using-snapshot-version) for more information on how to use them.

---

## Contributions

Please contribute! We will gladly review any pull requests.
Make sure to read the [Contributing](../contributing) page first though.

## License

```
Copyright 2022 The Android Open Source Project
 
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
[api]: ../api/themeadapter-core
