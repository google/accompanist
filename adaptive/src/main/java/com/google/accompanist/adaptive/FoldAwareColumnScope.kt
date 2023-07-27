/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.accompanist.adaptive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.debugInspectorInfo

/**
 * Copy of [ColumnScope] that excludes the weight Modifier attribute.
 *
 * Also adds a new [ignoreFold] Modifier attribute.
 */
@LayoutScopeMarker
@Immutable
public interface FoldAwareColumnScope {
    /**
     * Ignore the fold when placing this child within the [FoldAwareColumn].
     */
    @Stable
    public fun Modifier.ignoreFold(): Modifier

    /**
     * Align the element horizontally within the [Column]. This alignment will have priority over
     * the [Column]'s `horizontalAlignment` parameter.
     *
     * Example usage:
     * @sample androidx.compose.foundation.layout.samples.SimpleAlignInColumn
     */
    @Stable
    public fun Modifier.align(alignment: Alignment.Horizontal): Modifier

    /**
     * Position the element horizontally such that its [alignmentLine] aligns with sibling elements
     * also configured to [alignBy]. [alignBy] is a form of [align],
     * so both modifiers will not work together if specified for the same layout.
     * Within a [Column], all components with [alignBy] will align horizontally using
     * the specified [VerticalAlignmentLine]s or values provided using the other
     * [alignBy] overload, forming a sibling group.
     * At least one element of the sibling group will be placed as it had [Alignment.Start] align
     * in [Column], and the alignment of the other siblings will be then determined such that
     * the alignment lines coincide. Note that if only one element in a [Column] has the
     * [alignBy] modifier specified the element will be positioned
     * as if it had [Alignment.Start] align.
     *
     * Example usage:
     * @sample androidx.compose.foundation.layout.samples.SimpleRelativeToSiblingsInColumn
     */
    @Stable
    public fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier

    /**
     * Position the element horizontally such that the alignment line for the content as
     * determined by [alignmentLineBlock] aligns with sibling elements also configured to
     * [alignBy]. [alignBy] is a form of [align], so both modifiers
     * will not work together if specified for the same layout.
     * Within a [Column], all components with [alignBy] will align horizontally using
     * the specified [VerticalAlignmentLine]s or values obtained from [alignmentLineBlock],
     * forming a sibling group.
     * At least one element of the sibling group will be placed as it had [Alignment.Start] align
     * in [Column], and the alignment of the other siblings will be then determined such that
     * the alignment lines coincide. Note that if only one element in a [Column] has the
     * [alignBy] modifier specified the element will be positioned
     * as if it had [Alignment.Start] align.
     *
     * Example usage:
     * @sample androidx.compose.foundation.layout.samples.SimpleRelativeToSiblings
     */
    @Stable
    public fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier
}

internal object FoldAwareColumnScopeInstance : FoldAwareColumnScope {
    @Stable
    override fun Modifier.ignoreFold() = this.then(
        IgnoreFoldModifier(
            inspectorInfo = debugInspectorInfo {
                name = "ignoreFold"
                value = true
            }
        )
    )

    @Stable
    override fun Modifier.align(alignment: Alignment.Horizontal) = this.then(
        HorizontalAlignModifier(
            horizontal = alignment,
            inspectorInfo = debugInspectorInfo {
                name = "align"
                value = alignment
            }
        )
    )

    @Stable
    override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine) = this.then(
        SiblingsAlignedModifier.WithAlignmentLine(
            alignmentLine = alignmentLine,
            inspectorInfo = debugInspectorInfo {
                name = "alignBy"
                value = alignmentLine
            }
        )
    )

    @Stable
    override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int) = this.then(
        SiblingsAlignedModifier.WithAlignmentLineBlock(
            block = alignmentLineBlock,
            inspectorInfo = debugInspectorInfo {
                name = "alignBy"
                value = alignmentLineBlock
            }
        )
    )
}
