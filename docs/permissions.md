# Jetpack Compose Permissions

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-permissions)](https://search.maven.org/search?q=g:com.google.accompanist)

A library which provides [Android runtime permissions](https://developer.android.com/guide/topics/permissions/overview) support for Jetpack Compose.

## Usage

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

The following code exercises the [permission request workflow](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions)
and is nice with the user by letting them decide if they don't want to see the rationale again.

```kotlin
@Composable
private fun FeatureThatRequiresCameraPermission(
    navigateToSettingsScreen: () -> Unit
) {
    // When `true`, the permission request must be presented to the user.
    // This could happen when the user opens the screen for the first time.
    var launchPermissionRequest by rememberSaveable { mutableStateOf(false) }

    // Track if the user doesn't want to see the rationale any more.
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    when {
        // If the camera permission is granted, then show screen with the feature enabled
        cameraPermissionState.hasPermission -> {
            Text("Camera permission Granted")
        }
        // If the user denied the permission but a rationale should be shown, explain why the
        // feature is needed by the app and allow the user to be presented with the permission
        // again or to not see the rationale any more
        cameraPermissionState.shouldShowRationale -> {
            if (doNotShowRationale) {
                Text("Feature not available")
            } else {
                Column {
                    Text("The camera is important for this app. Please grant the permission.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Request permission")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { doNotShowRationale = true }) {
                            Text("Don't show rationale again")
                        }
                    }
                }
            }
        }
        // The permission is not granted, the rationale shouldn't be shown to the user, and the
        // permission hasn't been requested previously. Let's update the launchPermissionRequest
        // flag to trigger the `LaunchedEffect` below.
        !cameraPermissionState.permissionRequested -> {
            launchPermissionRequest = true
        }
        // If the criteria above hasn't been met, the user denied the permission. Let's present
        // the user with a FAQ in case they want to know more and send them to the Settings screen
        // to enable it the future there if they want to.
        else -> {
            Column {
                Text(
                    "Camera permission denied. See this FAQ with information about why we " +
                        "need this permission. Please, grant us access on the Settings screen."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = navigateToSettingsScreen) {
                    Text("Open Settings")
                }
            }
        }
    }

    // Trigger a side-effect to request the camera permission if it needs to be presented to the user
    if (launchPermissionRequest) {
        LaunchedEffect(cameraPermissionState) {
            cameraPermissionState.launchPermissionRequest()
            launchPermissionRequest = false
        }
    }
}
```

For more examples, refer to the [samples](https://github.com/google/accompanist/tree/main/sample/src/main/java/com/google/accompanist/sample/permissions).

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
