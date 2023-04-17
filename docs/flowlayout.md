# Jetpack Compose Flow Layouts

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-flowlayout)](https://search.maven.org/search?q=g:com.google.accompanist)

Flow Layouts in Accompanist is now deprecated. Please see the migration guide below to begin using 
Flow Layouts in Androidx.

The official `androidx.compose.foundation` FlowLayouts support is very similar to accompanist/flowlayouts, with a few changes.

It is most similar to `Row` and `Column` and shares similar modifiers and the scopes. 
Unlike the standard `Row` and `Column` composables, these layout children across multiple 
rows/columns if they exceed the available space.

## Usage
  
``` kotlin
FlowRow {
    // row contents
}

FlowColumn {
    // column contents
}
```

## Migration Guide to the official FlowLayouts

1. Replace import packages to point to Androidx.Compose
``` kotlin
import androidx.compose.foundation.layout.FlowColumn
```
  
``` kotlin
import androidx.compose.foundation.layout.FlowRow
```
  
For `FlowColumn`:  
2. Replace Modifier `mainAxisAlignment` with `verticalArrangement`  
3. Replace Modifier `crossAxisAlignment` with `horizontalAlignment`
  
For `FlowRow`  
4. `mainAxisAlignment` is now `horizontalArrangement`  
5. `crossAxisAlignment` is now `verticalAlignment`  

``` kotlin
FlowColumn(
    modifier = Modifier,
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.Start,
    content = { // columns }
) 
```
  
``` kotlin
FlowRow(
    modifier = Modifier,
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.Top,
    content = { // rows }
) 
```
  
6. Replace `mainAxisSpacing` with `VerticalArrangement.spacedBy(50.dp)` in `FlowColumn` and `HorizontalArrangement.spacedBy(50.dp)` in `FlowRow`
``` kotlin
FlowColumn(
    verticalArrangement = VerticalArrangement.spacedBy(50.dp),
    content = { // columns }
)
```
  
``` kotlin
FlowRow(
    horizontalArrangement = HorizontalArrangement.spacedBy(50.dp),
    content = { // rows }
)
```
  
7. `crossAxisSpacing` with `VerticalArrangement.spacedBy(50.dp)` in `FlowRow` and `HorizontalArrangement.spacedBy(50.dp)` in `FlowColumn`

``` kotlin
FlowRow(
    verticalArrangement = VerticalArrangement.spacedBy(50.dp),
    content = { // columns }
)
```
  
``` kotlin
FlowColumn(
    horizontalArrangement = HorizontalArrangement.spacedBy(50.dp),
    content = { // rows }
)
```
  
8. `lastLineMainAxisAlignment` is currently not supported in Compose Flow Layouts.

### New Features: 
#### Add weights to each child
To scale an item based on the size of its parent and the space available, adding weights are perfect. 
Adding a weight in `FlowRow` and `FlowColumn` is different than in `Row` and `Column`

In `FlowLayout` it is based on the number of items placed on the row it falls on and their weights. 
First we check to see if an item can fit in the current row or column based on its intrinsic size. 
If it fits and has a weight, its final size is grown based on the available space and the number of items 
with weights placed on the row or column it falls on. 

Because of the nature of `FlowLayouts` an item only grows and does not reduce in size. Its width in `FlowRow`
or height in `FlowColumn` determines it minimum width or height, and then grows based on its weight
and its available space, and the other items that fall on its row and column and their respective weights.

If it cannot fit based on its intrinsic minimum size, then it is placed in the next row and column. 
Once all the number of items that can fit the new row and column is calculated, 
then its final width and size is calculated 

``` kotlin
FlowRow()
    { repeat(20) { Box(Modifier.size(20.dp).weight(1f, true) } }

```

#### Create a maximum number of items in row or column
You may choose to limit the number of items that appear in each row in `FlowRow` or column in `FlowColumn`
This can be configured using `maxItemsInEachRow` or `maxItemsInEachColumn`: 
``` kotlin
FlowRow(maxItemsInEachRow = 3)
    { repeat(10) { Box(Modifier.size(20.dp).weight(1f, true) } }
```

## Examples

For examples, refer to the [Flow Row samples](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/foundation/foundation-layout/samples/src/main/java/androidx/compose/foundation/layout/samples/FlowRowSample.kt) 
and the [Flow Column samples](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/foundation/foundation-layout/samples/src/main/java/androidx/compose/foundation/layout/samples/FlowColumnSamples.kt).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-flowlayout)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-flowlayout:<version>"
}
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap]. These are updated on every commit.

[compose]: https://developer.android.com/jetpack/compose
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/google/accompanist/accompanist-flowlayout/