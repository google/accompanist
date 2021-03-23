# Migration from dev.chrisbanes.accompanist

In March 2021, the Accompanist project moved from github.com/chrisbanes/accompanist to github.com/google/accompanist. At the same time we migrated the libraries over to the new package name, and Maven group ID.

The following methods below are available for your information only, but may help if you need to migrate from the old package name. 

!!! warning
    Use these at your own risk, but they have worked on multiple projects from my testing. It's a good idea to make sure that you've made a backup or committed any changes before running these.

## Android Studio

You can use the [Replace in Path](https://www.jetbrains.com/help/idea/finding-and-replacing-text-in-project.html#replace_search_string_in_project) pane (⇧⌘R on Mac) in Android Studio to do a project-wide search and replace.

![Android Studio Replace in Path pane](studio.png)

- Find query: `dev.chrisbanes.accompanist`
- Replace string: `com.google.accompanist`
- _Optional:_ Set the file mask to `*.kt` so that only Kotlin files are searched.

## YOLO commands

These commands while automatically replace any imports and Gradle dependencies for the project in the current directory.

### MacOS

``` bash
find . -type f \( -name '*.kt' -or -name '*.gradle*' \) \
    -exec sed -i '' 's/dev\.chrisbanes\.accompanist/com\.google\.accompanist/' {} \;
```

### Linux

``` bash
find . -type f \( -name '*.kt' -or -name '*.gradle*' \) \
    -exec sed -i 's/dev\.chrisbanes\.accompanist/com\.google\.accompanist/' {} \;
```
