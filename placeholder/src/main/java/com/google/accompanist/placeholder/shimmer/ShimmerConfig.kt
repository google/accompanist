package com.google.accompanist.placeholder.shimmer

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color

data class ShimmerConfig(
    /**
     * indicate the base color for the shimmer
     */
    val contentColor: Color = Color.LightGray.copy(alpha = 0.3f),
    /**
     * indicate the highlight color for the shimmer
     */
    val higLightColor: Color = Color.LightGray.copy(alpha = 0.9f),
    /**
     * indicate how quickly the shimmer's gradient drops-off. A larger value causes a sharper drop-off.
     */
    @FloatRange(from = 0.0, to = 1.0)
    val dropOff: Float = 0.5f,
    /**
     * indicate the intensity of the shimmer. A larger value causes the shimmer to be larger.
     */
    @FloatRange(from = 0.0, to = 1.0)
    val intensity: Float = 0.2f,
    /**
     * indicate the direction of the shimmer's sweep
     */
    val direction: ShimmerDirection = ShimmerDirection.LeftToRight,
    /**
     * indicate the tilt angle of the shimmer in degrees
     */
    val angle: Float = 20f,
    /**
     * indicate how long the shimmering animation takes to do one full sweep.
     */
    val duration: Float = 1000f,
    /**
     * indicate how long to wait for starting the shimmering animation
     */
    val delay: Float = 200f
)