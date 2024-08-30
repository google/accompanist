# Jetpack Navigation Compose Material

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation-material)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides [Compose Material](https://developer.android.com/jetpack/androidx/releases/compose-material) support for [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation).
This features composable bottom sheet destinations.

!!! warning
    **This library is deprecated, with official material-navigation support in [androidx.compose.material.navigation](https://developer.android.com/jetpack/androidx/releases/compose-material#1.7.0-alpha04).** The original documentation is below the migration guide.

## Migration

The official `androidx.compose.material.navigation` version 1.7.0-alpha04+ offers all of the same functionality as Accompanist Navigation Material.

All class names are the same, the only needed changes are import related.

1. Replace dependency `com.google.accompanist:accompanist-navigation-material:<version>` with `androidx.compose.material:material-navigation:<version>`
2. Change import for ModalBottomSheetLayout from `com.google.accompanist.navigation.material.ModalBottomSheetLayout` to `androidx.compose.material.navigation.ModalBottomSheetLayout`
3. Change import for bottomSheet from `com.google.accompanist.navigation.material.bottomSheet` to `androidx.compose.material.navigation.bottomSheet`
4. Change import for rememberBottomSheetNavigator from `com.google.accompanist.navigation.material.rememberBottomSheetNavigator` to `androidx.compose.material.navigation.rememberBottomSheetNavigator`
5. Change import for BottomSheetNavigator from `com.google.accompanist.navigation.material.BottomSheetNavigator` to `androidx.compose.material.navigation.BottomSheetNavigator`
6. Change import for BottomSheetNavigatorSheetState from `com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState` to `androidx.compose.material.navigation.BottomSheetNavigatorSheetState`

# Deprecated Guidance for Accompanist Navigation Material

The following is the deprecated guide for using Navigation Material in Accompanist. Please see above migration section for how to use the `androidx.compose.material.navigation` Material Navigation.

## Usage

### Bottom Sheet Destinations

1. Create a `BottomSheetNavigator` and add it to the `NavController`:

    ```kotlin
    @Composable
    fun MyApp() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navController = rememberNavController(bottomSheetNavigator)
    }
    ```

2. Wrap your `NavHost` in the `ModalBottomSheetLayout` composable that accepts a `BottomSheetNavigator`.

    ```kotlin
    @Composable
    fun MyApp() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navController = rememberNavController(bottomSheetNavigator)
        ModalBottomSheetLayout(bottomSheetNavigator) {
            NavHost(navController, "home") {
               // We'll define our graph here in a bit!
            }
        }
    }
    ```

3. Register a bottom sheet destination

    ```kotlin
    @Composable
    fun MyApp() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navController = rememberNavController(bottomSheetNavigator)
        ModalBottomSheetLayout(bottomSheetNavigator) {
            NavHost(navController, "home") {
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
