# Updating & releasing Accompanist

This doc is mostly for maintainers.

## New features & bugfixes
All new features should be uploaded as PRs against the `main` branch. 

Once merged into `main`, they will be automatically merged into the `snapshot` branch.

## Jetpack Compose Snapshots

We publish snapshot versions of Accompanist, which depend on a `SNAPSHOT` versions of Jetpack Compose. These are built from the `snapshot` branch.

### Updating to a newer Compose snapshot

As mentioned above, updating to a new Compose snapshot is done by submitting a new PR against the `snapshot` branch:

``` sh
git checkout snapshot && git pull
# Create branch for PR
git checkout -b update_snapshot
```

Now edit the project to depend on the new Compose SNAPSHOT version:

Edit [`/gradle/libs.versions.toml`](https://github.com/google/accompanist/blob/main/gradle/libs.versions.toml):

Under `[versions]`:

1. Update the `composesnapshot` property to be the snapshot number
2. Ensure that the `compose` property is correct

Make sure the project builds and test pass:
```
./gradlew check
```

Now `git commit` the changes and push to GitHub.

Finally create a PR (with the base branch as `snapshot`) and send for review.

## Releasing

Once the next Jetpack Compose version is out, we're ready to push a new release:

### #1: Merge `snapshot` into `main`

First we merge the `snapshot` branch into `main`:

``` sh
git checkout snapshot && git pull
git checkout main && git pull

# Create branch for PR
git checkout -b main_snapshot_merge

# Merge in the snapshot branch
git merge snapshot
```

### #2: Update dependencies

Edit [`/gradle/libs.versions.toml`](https://github.com/google/accompanist/blob/main/gradle/libs.versions.toml):

Under `[versions]`:

1. Update the `composesnapshot` property to a single character (usually `-`). This disables the snapshot repository.
2. Update the `compose` property to match the new release (i.e. `1.0.0-beta06`)

Make sure the project builds and test pass:
```
./gradlew check
```

Commit the changes.

### #3: Bump the version number

Edit [gradle.properties](https://github.com/google/accompanist/blob/main/gradle.properties):

 * Update the `VERSION_NAME` property and remove the `-SNAPSHOT` suffix.

Commit the changes, using the commit message containing the new version name.

### #4: Push to GitHub

Push the branch to GitHub and create a PR against the `main` branch, and send for review. Once approved and merged, it will be automatically deployed to Maven Central.

### #5: Create release

Once the above PR has been approved and merged, we need to create the GitHub release:

 * Open up the [Releases](https://github.com/google/accompanist/releases) page.
 * At the top you should see a 'Draft' release, auto populated with any PRs since the last release. Click 'Edit'.
 * Make sure that the version number matches what we released (the tool guesses but is not always correct).
 * Double check everything, then press 'Publish release'.

At this point the release is published. This will trigger the docs action to run, which will auto-deploy a new version of the [website](https://google.github.io/accompanist/).

### #6: Prepare the next development version

The current release is now finished, but we need to update the version for the next development version:

Edit [gradle.properties](https://github.com/google/accompanist/blob/main/gradle.properties):

 * Update the `VERSION_NAME` property, by increasing the version number, and adding the `-SNAPSHOT` suffix.
 * Example: released version: `0.3.0`. Update to `0.3.1-SNAPSHOT`

 `git commit` and push to `main`.

Finally, merge all of these changes back to `snapshot`:

```
git checkout snapshot && git pull
git merge main
git push
```