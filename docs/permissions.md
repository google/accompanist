# Jetpack Compose Permissions

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-permissions)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides [Android runtime permissions](https://developer.android.com/guide/topics/permissions/overview) support for Jetpack Compose.

!!! warning
    The permission APIs are currently experimental and they could change at any time.
    All of the APIs are marked with the `@ExperimentalPermissionsApi` annotation.

## Usage

### `rememberPermissionState` and `rememberMultiplePermissionsState` APIs

The `rememberPermissionState(permission: String)` API allows you to request a certain permission
to the user and check for the status of the permission.
`rememberMultiplePermissionsState(permissions: List<String>)` offers the same but for multiple
permissions at the same time.

Both APIs expose properties for you to follow the workflow as described in the
[permissions documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).

!!! caution
    The call to the method that requests the permission to the user (e.g. `PermissionState.launchPermissionRequest()`)
    needs to be invoked from a non-composable scope. For example, from a side-effect or from a
    non-composable callback such as a `Button`'s `onClick` lambda.

The following code exercises the [permission request workflow](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FeatureThatRequiresCameraPermission() {

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    if (cameraPermissionState.status.isGranted) {
        Text("Camera permission Granted")
    } else {
        Column {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The camera is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Camera permission required for this feature to be available. " +
                    "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}
```

For more examples, refer to the [samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/com/google/accompanist/sample/permissions).

## Limitations

This permissions wrapper is built on top of the available Android platform APIs. We cannot extend
the platform's capabilities. For example, it's not possible to differentiate between the
_it's the first time requesting the permission_ vs _the user doesn't want to be asked again_
use cases.

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-permissions)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-permissions:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-permissions/
