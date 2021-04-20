# Migration from CoilImage

In Accompanist v0.8.0 the Coil library was refactored, moving away from the `CoilImage()` functions, to a new remembered painter provided by [`rememberCoilPainter()`][rememberpainter]. This page details how to migrate, as well as alternatives for function which no longer exists.

## CoilImage is now deprecated
The `CoilImage()` functions still exist (as of v0.8.0) but are now deprecated. They will be removed in a future release of Accompanist (likely v1.0).

### ReplaceWith

An automatic replacement migration has been provided in the deprecation. This allows you to migrate one or all `CoilImage()` calls over to the new API.  This works perfectly in most cases but you may find that the resulting code needs tidying up, YMMV.

## Removed features

The new API is intentionally missing some functionality which was previously available in `CoilImage`. Migrating usages of those features requires manual migration:

### onRequestCompleted()

`CoilImage` previously allowed the passing of a lambda which was invoked whenever a request was completed. This has been removed to simplify the internal state. If you wish to convert state changes into events, you can use [`snapshotFlow()`][snapshotflow] to observe changes to `painter.loadState`, and then call your logic as necessary:

``` kotlin
val painter = rememberCoilPainter("https://image.url")

LaunchedEffect(painter) {
    snapshotFlow { painter.loadState }
        .filter { it.isFinalState() }
        .collect { result ->
            // TODO do something with result
        }
}

Image(painter = painter)
```

### Loading and error content slots

As the main API is now a painter, it can not hold other content itself. Similar to above, this has been done to drastically simplify the layout, and optimize the performance of the resulting layout. See the ['Custom content'](../#custom-content) document for an alternative.


  [rememberpainter]: ../api/coil/coil/com.google.accompanist.coil/remember-coil-painter.html
  [snapshotflow]: https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#snapshotflow