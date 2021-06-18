# Lint checks for Permissions for Jetpack Compose

Lint checks for preventing calling `PermissionState.launchPermissionRequest` and
`MultiplePermissionsState.launchMultiplePermissionRequest()` within the Composition as that throws
a runtime exception.

These functions should be called inside a regular lambda or a side-effect but never in the
Composition.

These lint checks will be automatically applied to your project when using
📫 [Permissions](https://google.github.io/accompanist/permissions/).

## Download Permissions for Jetpack Compose

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-permissions:<version>"
}
```
