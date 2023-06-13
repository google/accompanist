# Adaptive utilities for Jetpack Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-adaptive)](https://search.maven.org/search?q=g:com.google.accompanist)

A library providing a collection of utilities for adaptive layouts.

## calculateDisplayFeatures

[`calculateDisplayFeatures(activity)`](../api/adaptive/com.google.accompanist.adaptive/calculate-display-features.html) returns the current list of `DisplayFeature`s,
as reported by the [Jetpack WindowManager library](https://developer.android.com/jetpack/androidx/releases/window).

These contain the list of folds (if any), and can be used to drive components like [`TwoPane`](#TwoPane).

## TwoPane

[`TwoPane`](../api/adaptive/com.google.accompanist.adaptive/-two-pane.html) is a UI component that positions exactly two slots on the screen.

The default positioning of these two slots is driven by a [`TwoPaneStrategy`](../api/adaptive/com.google.accompanist.adaptive/-two-pane-strategy.html),
which can decide to orient the two slots side-by-side horizontally or vertically, and also configure the gap between them.

The built-in [`HorizontalTwoPaneStrategy`](../api/adaptive/com.google.accompanist.adaptive/-horizontal-two-pane-strategy.html) and
[`VerticalTwoPaneStrategy`](../api/adaptive/com.google.accompanist.adaptive/-vertical-two-pane-strategy.html) allow positioning the
slots based on a fixed offset, or as some fraction of the space.

[`TwoPane`](../api/adaptive/com.google.accompanist.adaptive/-two-pane.html) also requires a list of display features (to be retrieved with [`calculateDisplayFeatures`](#calculateDisplayFeatures)),
and optionally a [`FoldAwareConfiguration`](../api/adaptive/com.google.accompanist.adaptive/-fold-aware-configuration.html) to determine which folds to handle automatically.

When there is a fold that intersects with the [`TwoPane`](../api/adaptive/com.google.accompanist.adaptive/-two-pane.html) component that is obscuring or separating,
the [`TwoPane`](../api/adaptive/com.google.accompanist.adaptive/-two-pane.html) will automatically place the slots to avoid the fold.

When there is no fold, the default supplied strategy will be used instead.

## FoldAwareColumn

[`FoldAwareColumn`](../api/adaptive/com.google.accompanist.adaptive/-fold-aware-column.html) is a simplified version of [Column](https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/package-summary#Column(androidx.compose.ui.Modifier,androidx.compose.foundation.layout.Arrangement.Vertical,androidx.compose.ui.Alignment.Horizontal,kotlin.Function1)) that places children in a fold-aware manner.

[`FoldAwareColumn`](../api/adaptive/com.google.accompanist.adaptive/-fold-aware-column.html) requires a list of display features (to be retrieved with [`calculateDisplayFeatures`](#calculatedisplayfeatures)) to determine which folds to handle automatically.

The built-in `foldPadding` parameter is zero, and the values of the vertical padding are used in the layout determine how much space should be left around a fold when placing children.

When there is a horizontal fold that is obscuring or separating, the layout will begin placing children from the top of the available space. If a child is projected to overlap the fold, then its y-coordinate is increased so it will be placed fully below the fold, as will any other remaining children.

When there is no fold, the children will be placed consecutively with no y-coordinate adjustments.

Optionally, children can be modified with the `ignoreFold()` attribute, which means that they will be placed as if no fold is present even if they overlap a fold.

## Download

[![Maven Central](https://img.shields.io/maven-central/v/com.google.accompanist/accompanist-adaptive)](https://search.maven.org/search?q=g:com.google.accompanist)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.google.accompanist:accompanist-adaptive:<version>"
}
```