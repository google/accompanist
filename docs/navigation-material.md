# Jetpack Navigation Compose Material

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation-material)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides [Compose Material](https://developer.android.com/jetpack/androidx/releases/compose-material) support for [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation).
This features composable bottom sheet destinations.

!!! warning
    The navigation APIs are currently experimental and they could change at any time.
    All of the APIs are marked with the `@ExperimentalMaterialNavigationApi` annotation.

## Usage

### Bottom Sheet Destinations

1. Create a `BottomSheetNavigator` and add it to the `NavController`:

    ```kotlin
    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        navController.navigatorProvider += bottomSheetNavigator
    }
    ```

2. Wrap your `NavHost` in the `ModalBottomSheetLayout` composable that accepts a `BottomSheetNavigator`.

    ```kotlin
    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        navController.navigatorProvider += bottomSheetNavigator
        ModalBottomSheetLayout(bottomSheetNavigator) {
            NavHost(navController, Destinations.Home) {
               // We'll define our graph here in a bit!
            }
        }
    }
    ```

3. Register a bottom sheet destination

    ```kotlin
    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        navController.navigatorProvider += bottomSheetNavigator
        ModalBottomSheetLayout(bottomSheetNavigator) {
            NavHost(navController, Destinations.Home) {
               composable(route = "home") {
                   ...
               }
               bottomSheet(route = "sheet") {
                   Text("This is a cool bottom sheet!")
               }
            }
        }
    }
    ```

For more examples, refer to the [samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/com/google/accompanist/sample/navigation/material).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation-material)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-navigation-material:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-navigation-material/
