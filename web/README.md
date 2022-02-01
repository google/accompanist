# WebView wrapper for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-webview)](https://search.maven.org/search?q=g:com.google.accompanist)

For more information, visit the documentation: https://google.github.io/accompanist/webview/

## Basic Usage

To implement this wrapper there are two key APIs which are needed: WebView, which provides the layout, and rememberWebViewState(url) which provides some remembered state including the URL to display.

The basic usage is as follows:
```kotlin
val state by rememberWebViewState("https://example.com")

WebView(state)
```
This will display a WebView in your Compose layout that shows the URL provided.

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-webview:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

  [snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-web/
