# Jetpack Compose Navigation

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides [Compose Animation](https://developer.android.com/jetpack/compose/animation) support for [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation).

!!! warning
    The navigation APIs are currently experimental and they could change at any time.
    All of the APIs are marked with the `@ExperimentalAnimationApi` annotation.

## Usage

### `AnimatedNavHost`

The `AnimatedNavHost` composable offers a way to add custom transitions to composables in
Navigation Compose.

```kotlin
@Composable
private fun ExperimentalAnimationNav() {
    val navController = rememberNavController()
    navController.navigatorProvider += AnimatedComposeNavigator()
    AnimatedNavHost(navController, startDestination = "Blue") {
        composable(
            "Blue",
            enterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Red" ->
                        if (navController.previousBackStackEntry != initial) {
                            slideInHorizontally(
                                initialOffsetX = { -1000 },
                                animationSpec = tween(2000)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { 1000 },
                                animationSpec = tween(2000)
                            )
                        }
                    else -> null
                }
            },
            exitTransition = { initial, target ->
                when (target.destination.route) {
                    "Red" ->
                        if (navController.previousBackStackEntry != initial) {
                            slideOutHorizontally(
                                targetOffsetX = { 1000 },
                                animationSpec = tween(2000)
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { -1000 },
                                animationSpec = tween(2000)
                            )
                        }
                    else -> null
                }
            }
        ) { BlueScreen(navController) }
        composable(
            "Red",
            enterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Blue" ->
                        if (navController.previousBackStackEntry != initial) {
                            slideInHorizontally(
                                initialOffsetX = { -1000 },
                                animationSpec = tween(2000)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { 1000 },
                                animationSpec = tween(2000)
                            )
                        }
                    else -> null
                }
            },
            exitTransition = { initial, target ->
                when (target.destination.route) {
                    "Blue" ->
                        if (navController.previousBackStackEntry != initial) {
                            slideOutHorizontally(
                                targetOffsetX = { 1000 },
                                animationSpec = tween(2000)
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { -1000 },
                                animationSpec = tween(2000)
                            )
                        }
                    else -> null
                }
            }
        ) { RedScreen(navController) }
    }
}
```

For more examples, refer to the [samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/com/google/accompanist/sample/navigation).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-navigation:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-navigation/
