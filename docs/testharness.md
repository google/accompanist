# Test Harness for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-testharness)](https://search.maven.org/search?q=g:com.google.accompanist)

A library providing a test harness for UI components.

## Background

Device configuration (locale, font size, screen size, folding features, etc.) are device-wide
properties, which makes it hard to automate tests that wants to vary these properties.
One current solution is to run tests across a range of emulators or devices with different
properties, and potentially filter tests to only run when specific conditions are met.
This has the downside of increasing the number of devices to manage, higher complexity of
configuring those devices, and more complicated test suites.

With a Compose-only app, it is less common that the “physical” constraints of the device are
directly used.
Instead, state hoisting encourages isolating such constraints, and providing them to components via
state that is observable via snapshots.
The mechanism to do so is primarily via a set of composition locals, such as `LocalConfiguration`,
`LocalDensity`, and others.
The composition local mechanism provides a layer of indirection that permits overriding these
constraints via those composition local hooks.

## Test Harness

`TestHarness` is an `@Composable` function, which takes a single slot of `@Composable` content.
This content is the `@Composable` UI under test, so standard usage would look like the following:

```kotlin
@Test
fun example() {
    composeTestRule.setContent {
        TestHarness {
            MyComponent()
        }
    }

    // assertions
}
```

When no parameters of `TestHarness` are specified, `TestHarness` has no direct effect, and it would
be equivalent to calling `MyComponent` directly.

Specifying parameters of `TestHarness` results in overriding the default configuration for the
content under-test, and will affect `MyComponent`.

For example, specifying the `fontScale` parameter will change the effective font scale within
the `TestHarness`:

```kotlin
@Test
fun example() {
    composeTestRule.setContent {
        TestHarness(fontScale = 1.5f) {
            Text("Configuration: ${LocalConfiguration.current.fontScale}")
            Text("Density: ${LocalDensity.current.fontScale}")
        }
    }

    composeTestRule.onNodeWithText("Configuration: 1.5").assertExists()
    composeTestRule.onNodeWithText("Density: 1.5").assertExists()
}
```

This allows testing UI for different font scales in a isolated way, without having to directly
configure the device to use a different font scale.

`TestHarness` also takes a `size: DpSize` parameter, to test a Composable at a particular size.

```kotlin
@Test
fun example() {
    composeTestRule.setContent {
        TestHarness(size = DpSize(800.dp, 1000.dp)) {
            MyComponent() // will be rendered at 800dp by 1000dp, even if the window is smaller
        }
    }
}
```

See the full list of parameters and effects below.

## Parameters

The full list of parameters and their effects:

| Parameter                           | Default value                                                                           | Effect                                                                                                             |
|-------------------------------------|-----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `size: DpSize`                      | `DpSize.Unspecified`                                                                    | If specified, overrides `LocalDensity` if needed to give the `DpSize` amount of space to the composable under test |
| `darkMode: Boolean`                 | `isSystemInDarkTheme()`                                                                 | Overrides `LocalConfiguration.current.uiMode`                                                                      |
| `fontScale: Float`                  | `LocalDensity.current.fontScale`                                                        | Overrides `LocalDensity.current.fontScale` and `LocalConfiguration.current.fontScale`                              |
| `fontWeightAdjustment: Int?`        | `LocalConfiguration.current.fontWeightAdjustment` on API 31 and above, otherwise `null` | Overrides `LocalConfiguration.current.fontWeightAdjustment` on API 31 and above and not-null                       |
| `locales: LocaleListCompat`         | `ConfigurationCompat.getLocales(LocalConfiguration.current)`                            | Overrides `LocalConfiguration.current.locales`                                                                     |
| `layoutDirection: LayoutDirection?` | `null` (which uses the resulting locale layout direction)                               | Overrides `LocalLayoutDirection.current` and `LocalConfiguration.current.screenLayout`                             |

## Implementation

`TestHarness` works by overriding a set of composition locals provided to the content under test.

The full list of composition locals that may be overridden by various parameters are:

- `LocalConfiguration`
- `LocalContext`
- `LocalLayoutDirection`
- `LocalDensity`
- `LocalFontFamilyResolver`

Any composable that depends on these composition locals should be testable via the test harness,
because they will pull the overridden configuration information from them.
This includes configuration-specific resources, because these are pulled from `LocalContext`.

Testing a composable at a smaller size than the real screen space available is straightforward, but
testing a composable at a larger size than the real screen space available is not. This is because
the library and the testing APIs are sensitive to whether or not a composable is actually rendered
within the window of the application.

As a solution, `TestHarness` will override the `LocalDensity` to shrink the content as necessary
for all of the specified `size: DpSize` to be displayed at once in the window space that is
available. This results in the composable under test believing it has the specified space to work
with, even if that is larger than the window of the application.

## Limitations

The test harness is simulating alternate configurations and sizes, so it does not exactly represent
what a user would see on a real device.
For that reason, the platform edges where Composables interact with the system more is where the
test harness may break down and have issues.
An incomplete list includes: dialogs (due to different `Window` instances), insets, soft keyboard
interactions, and interop with `View`s.
The density overriding when specifying a specific size to test a composable at also means that UI
might be rendered in atypical ways, especially at the extreme of rendering a very large desktop-size
UI on a small portrait phone.
The mechanism that the test harness uses is also not suitable for production code: in production,
the default configuration as specified by the user and the system should be used.

The mechanism that the test harness uses to override the configuration (`ContextThemeWrapper`) is
not fully supported by layoutlib. In particular, alternate resources are available just by using
`TestHarness`.

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-testharness)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-testharness:<version>"
}
```