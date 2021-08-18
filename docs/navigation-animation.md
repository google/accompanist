# Jetpack Navigation Compose Animation

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation-animation)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides [Compose Animation](https://developer.android.com/jetpack/compose/animation) support for [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation).

!!! warning
    The navigation APIs are currently experimental and they could change at any time.
    All of the APIs are marked with the `@ExperimentalAnimationApi` annotation.

## Usage

The `AnimatedNavHost` composable offers a way to add custom transitions to composables in
Navigation Compose.

```kotlin
@Composable
private fun ExperimentalAnimationNav() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController, startDestination = "Blue") {
        composable(
            "Blue",
            enterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Red" ->
                        slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = { _, target ->
                when (target.destination.route) {
                    "Red" ->
                        slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700))
                     else -> null
                }
            },
            popEnterTransition = { initial, _ ->
                            when (initial.destination.route) {
                                "Red" ->
                                    slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700))
                                else -> null
                            }
                        },
            popExitTransition = { _, target ->
                when (target.destination.route) {
                    "Red" ->
                        slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700))
                    else -> null
                }
            }
        ) { BlueScreen(navController) }
        composable(
            "Red",
            enterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Blue" ->
                        slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = { _, target ->
                when (target.destination.route) {
                    "Blue" ->
                        slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700))
                    else -> null
                }
            },
            popEnterTransition = { initial, _ ->
                when (initial.destination.route) {
                    "Blue" ->
                        slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700))
                    else -> null
                }
            },
            popExitTransition = { _, target ->
                when (target.destination.route) {
                    "Blue" ->
                        slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700))
                    else -> null
                }
            }
        ) { RedScreen(navController) }
    }
}
```

For more examples, refer to the [samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/com/google/accompanist/sample/navigation/animation).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation-animation)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-navigation-animation:<version>"
}
```

## Migrating to Accompanist Navigation Animation

To migrate from using the Navigation Compose APIs do the following:

* Replace `rememberNavController()` with `rememberAnimatedNavController()`
* Replace `NavHost` with `AnimatedNavHost`
* Replace `import androidx.navigation.compose.navigation` with `import com.google.accompanist.navigation.animation.navigation`
* Replace `import androidx.navigation.compose.composable` with `import com.google.accompanist.navigation.animation.composable`

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-navigation-animation/

For more details see [Animations in Navigation Compose](https://medium.com/androiddevelopers/animations-in-navigation-compose-36d48870776b)
