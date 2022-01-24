# WebView wrapper for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-webview)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides a Jetpack Compose wrapper around Android's WebView.

## Usage

To implement this wrapper there are two key APIs which are needed: [`WebView`][api_webview], which is provides the layout, and [`rememberWebViewState(url)`][api_rememberstate] which provides some remembered state including the URL to display.

The basic usage is as follows:

```kotlin
val state by rememberWebViewState("https://example.com")

WebView(
    state
)
```

This will display a WebView in your Compose layout that shows the URL provided.

There is a larger sample in the sample app which can be found [here](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/webview/BasicWebViewSample.kt). This sample also shows how to show a loading state.

### WebView settings including Javascript

By default, javascript is disabled in the WebView. To enable it or any other settings you can use the `onCreated` callback.

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