# WebView wrapper for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-webview)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides a Jetpack Compose wrapper around Android's WebView.

!!! warning
    **This library is deprecated, and the API is no longer maintained. We recommend forking the implementation and customising it to your needs.** The original documentation is below.

## Usage

To implement this wrapper there are two key APIs which are needed: [`WebView`](../api/web/com.google.accompanist.web/-web-view.html), which is provides the layout, and [`rememberWebViewState(url)`](../api/web/com.google.accompanist.web/remember-web-view-state.html) which provides some remembered state including the URL to display.

The basic usage is as follows:

```kotlin
val state = rememberWebViewState("https://example.com")

WebView(
    state
)
```

This will display a WebView in your Compose layout that shows the URL provided.

There is a larger sample in the sample app which can be found [here](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/webview/BasicWebViewSample.kt). This sample also shows how to show a loading state.

### WebView settings including JavaScript

By default, JavaScript is disabled in the WebView. To enable it or any other settings you can use the `onCreated` callback.

```kotlin
WebView(
    state = webViewState,
    onCreated = { it.settings.javaScriptEnabled = true }
)
```

### Capturing back presses

By default the WebView will capture back presses/swipes when relevant and navigate the WebView back. This can be disabled via the parameter on 
the Composable.

```kotlin
WebView(
    ...
    captureBackPresses = false
)
```

### Using a subclass of WebView

If you want to use a subclass of `WebView`, or simply require more control over its instantiation, you can provide a factory.

```kotlin
WebView(
    ...
    factory = { context -> CustomWebView(context) }
)
```

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-webview)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-webview:<version>"
}
```
