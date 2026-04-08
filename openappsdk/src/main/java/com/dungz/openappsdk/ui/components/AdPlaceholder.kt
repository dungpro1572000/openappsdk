package com.dungz.openappsdk.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig

/**
 * Shimmer effect modifier
 */
@Composable
fun Modifier.shimmerEffect(
    baseColor: Color = OpenAppConfig.getAdPlaceholderConfig().shimmerBaseColor,
    highlightColor: Color = OpenAppConfig.getAdPlaceholderConfig().shimmerHighlightColor
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    return this.background(shimmerBrush)
}

/**
 * Native Ad Medium Placeholder (280dp height)
 * Hiển thị skeleton UI giống layout native ad thực
 */
@Composable
fun NativeAdMediumPlaceholder(
    modifier: Modifier = Modifier
) {
    val config = OpenAppConfig.getAdPlaceholderConfig()

    // Check custom placeholder first
    config.nativeMediumPlaceholder?.let {
        it()
        return
    }

    val cornerRadius = config.cornerRadius

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(config.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (config.useShimmer) {
            NativeAdMediumShimmer(cornerRadius = cornerRadius)
        } else if (config.showLoadingIndicator) {
            CircularProgressIndicator(color = config.loadingIndicatorColor)
        }
    }
}

@Composable
private fun NativeAdMediumShimmer(cornerRadius: Dp) {
    val config = OpenAppConfig.getAdPlaceholderConfig()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Header row: Icon + Title + Ad badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Headline
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Advertiser
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
                )
            }
            // Ad badge
            Box(
                modifier = Modifier
                    .size(24.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Media placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Body text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom row: Stars + CTA button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star rating
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
            )
            Spacer(modifier = Modifier.weight(1f))
            // CTA button
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
            )
        }
    }
}

/**
 * Native Ad Small Placeholder (80dp height)
 */
@Composable
fun NativeAdSmallPlaceholder(
    modifier: Modifier = Modifier
) {
    val config = OpenAppConfig.getAdPlaceholderConfig()

    // Check custom placeholder first
    config.nativeSmallPlaceholder?.let {
        it()
        return
    }

    val cornerRadius = config.cornerRadius

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(config.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (config.useShimmer) {
            NativeAdSmallShimmer()
        } else if (config.showLoadingIndicator) {
            CircularProgressIndicator(color = config.loadingIndicatorColor)
        }
    }
}

@Composable
private fun NativeAdSmallShimmer() {
    val config = OpenAppConfig.getAdPlaceholderConfig()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Headline
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Advertiser
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
            )
        }
        // CTA button
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
        )
    }
}

/**
 * Banner Ad Placeholder
 */
@Composable
fun BannerAdPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = 50.dp
) {
    val config = OpenAppConfig.getAdPlaceholderConfig()

    // Check custom placeholder first
    config.bannerPlaceholder?.let {
        it()
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(config.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (config.useShimmer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .shimmerEffect(config.shimmerBaseColor, config.shimmerHighlightColor)
            )
        } else if (config.showLoadingIndicator) {
            CircularProgressIndicator(
                color = config.loadingIndicatorColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// PREVIEWS
@Preview(showBackground = true)
@Composable
private fun NativeAdMediumPlaceholderPreview() {
    NativeAdMediumShimmer(cornerRadius = 12.dp)
}

@Preview(showBackground = true)
@Composable
private fun NativeAdSmallPlaceholderPreview() {
    NativeAdSmallShimmer()
}
