# Jetpack Navigation Compose Animation

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-navigation-animation)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides [Compose Animation](https://developer.android.com/jetpack/compose/animation) support for [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation).

!!! warning
    The navigation APIs are currently experimental and they could change at any time.
    All of the APIs are marked with the `@ExperimentalAnimationApi` annotation.

## Usage

The `AnimatedNavHost` composable offers a way to add custom transitions to composables in
Navigation Compose via parameters that can be attached to either an individual `composable`
destination, a `navigation` element, or to the `AnimatedNavHost` itself.

Each lambda has an [`AnimatedContentScope<NavBackStackEntry>`](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedContentScope) receiver scope that allows you to use special transitions (such as [`slideIntoContainer`](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedContentScope#slideIntoContainer(androidx.compose.animation.AnimatedContentScope.SlideDirection,androidx.compose.animation.core.FiniteAnimationSpec,kotlin.Function1)) and [`slideOutOfContainer`](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedContentScope#slideOutOfContainer(androidx.compose.animation.AnimatedContentScope.SlideDirection,androidx.compose.animation.core.FiniteAnimationSpec,kotlin.Function1))) and gives you access to the [`initialState`](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedContentScope#initialState()) and [`targetState`](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedContentScope#targetState()) properties that let you customize what transitions are run based on what screen you are transitioning from (the `initialState`) and transitioning to (the `targetState`). 

- `enterTransition` controls what [`EnterTransition`](https://developer.android.com/reference/kotlin/androidx/compose/animation/EnterTransition.html) is run when the `targetState` `NavBackStackEntry` is appearing on the screen.
- `exitTransition` controls what [`ExitTransition`](https://developer.android.com/reference/kotlin/androidx/compose/animation/ExitTransition) is run when the `initialState` `NavBackStackEntry` is disappearing from the screen.
- `popEnterTransition` defaults to `enterTransition`, but can be overridden to provide a separate [`EnterTransition`](https://developer.android.com/reference/kotlin/androidx/compose/animation/EnterTransition.html) when the `targetState` `NavBackStackEntry` is appearing on the screen due to a pop operation (i.e., `popBackStack()`).
- `popExitTransition` defaults to `exitTransition`, but can be overridden to provide a separate [`ExitTransition`](https://developer.android.com/reference/kotlin/androidx/compose/animation/ExitTransition) when the `initialState` `NavBackStackEntry` is disappearing from the screen due to a pop operation (i.e., `popBackStack()`).

For each transition, if a `composable` destination returns `null`, the parent `navigation` element's transition will be used, thus allowing you to set a global set of transitions at the navigation graph level that will apply to every `composable` in that graph. This continues up the hierarchy until you reach the root `AnimatedNavHost`, which controls the global transitions for all destinations and nested graphs that do not specify one.

Note: this means that if a destination wants to instantly jump cut between destinations, it should return [`EnterTransition.None`](https://developer.android.com/reference/kotlin/androidx/compose/animation/EnterTransition#None()) or [`ExitTransition.None`](https://developer.android.com/reference/kotlin/androidx/compose/animation/ExitTransition#None()) to signify that no transition should be run, rather than return `null`.

```kotlin
@Composable
private fun ExperimentalAnimationNav() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController, startDestination = "Blue") {
        composable(
            "Blue",
            enterTransition = {
                when (initialState.destination.route) {
                    "Red" ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "Red" ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
                     else -> null
                }
            },
            popEnterTransition = {
                            when (initialState.destination.route) {
                                "Red" ->
                                    slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
                                else -> null
                            }
                        },
            popExitTransition = {
                when (targetState.destination.route) {
                    "Red" ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
                    else -> null
                }
            }
        ) { BlueScreen(navController) }
        composable(
            "Red",
            enterTransition = {
                when (initialState.destination.route) {
                    "Blue" ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "Blue" ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
                    else -> null
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    "Blue" ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
                    else -> null
                }
            },
            popExitTransition = {
                when (targetState.destination.route) {
                    "Blue" ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
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

## Migration

To migrate from using the Navigation Compose APIs do the following:

* Replace `rememberNavController()` with `rememberAnimatedNavController()`
* Replace `NavHost` with `AnimatedNavHost`
* Replace `import androidx.navigation.compose.navigation` with `import com.google.accompanist.navigation.animation.navigation`
* Replace `import androidx.navigation.compose.composable` with `import com.google.accompanist.navigation.animation.composable`

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-navigation-animation/

For more details see [Animations in Navigation Compose](https://medium.com/androiddevelopers/animations-in-navigation-compose-36d48870776b)
