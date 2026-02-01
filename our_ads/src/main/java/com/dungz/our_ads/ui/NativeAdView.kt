package com.dungz.our_ads.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.dungz.our_ads.R
import com.dungz.our_ads.manager.NativeAdManager
import com.dungz.our_ads.state.RetryConfig

enum class NativeAdSize { SMALL, MEDIUM }

@Composable
fun NativeAdView(
    adHigherId: String,
    adNormalId: String,
    showHigher: Boolean,
    showNormal: Boolean,
    modifier: Modifier = Modifier,
    size: NativeAdSize = NativeAdSize.MEDIUM,
    retryConfig: RetryConfig? = null,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    if (!showHigher && !showNormal) return

    var currentNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasFailed by remember { mutableStateOf(false) }

    LaunchedEffect(adHigherId, adNormalId, showHigher, showNormal) {
        if (NativeAdManager.isReady(adHigherId, adNormalId)) {
            currentNativeAd = NativeAdManager.getNativeAd(adHigherId, adNormalId)
            if (currentNativeAd != null) onAdLoaded()
        } else {
            isLoading = true
            NativeAdManager.loadAd(
                adHigherId, adNormalId, showHigher, showNormal, retryConfig,
                onLoaded = { ad ->
                    currentNativeAd = ad
                    isLoading = false
                    onAdLoaded()
                },
                onFailed = { error ->
                    isLoading = false
                    hasFailed = true
                    onAdFailed(error)
                }
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        val height = if (size == NativeAdSize.SMALL) 80.dp else 280.dp
        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            currentNativeAd != null -> AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val layout = if (size == NativeAdSize.SMALL) {
                        R.layout.native_ad_small
                    } else {
                        R.layout.native_ad_medium
                    }
                    (LayoutInflater.from(ctx).inflate(layout, null) as NativeAdView).also {
                        populateNativeAdView(currentNativeAd!!, it)
                    }
                }
            )
            hasFailed -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("Ad not available", color = Color.Gray)
            }
            else -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(Color.White)
            )
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    (adView.headlineView as? TextView)?.text = nativeAd.headline

    adView.bodyView = adView.findViewById(R.id.ad_body)
    nativeAd.body?.let {
        (adView.bodyView as? TextView)?.apply {
            text = it
            visibility = View.VISIBLE
        }
    }

    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    nativeAd.callToAction?.let {
        (adView.callToActionView as? Button)?.apply {
            text = it
            visibility = View.VISIBLE
        }
    }

    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    nativeAd.icon?.let {
        (adView.iconView as? ImageView)?.setImageDrawable(it.drawable)
    }

    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    nativeAd.starRating?.let {
        (adView.starRatingView as? RatingBar)?.rating = it.toFloat()
    }

    adView.mediaView = adView.findViewById(R.id.ad_media)
    adView.mediaView?.mediaContent = nativeAd.mediaContent

    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
    nativeAd.advertiser?.let {
        (adView.advertiserView as? TextView)?.text = it
    }

    adView.setNativeAd(nativeAd)
}

// PREVIEWS
@Preview(showBackground = true, name = "Native Small - Loading")
@Composable
private fun NativeSmallLoadingPreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true, name = "Native Small - Loaded")
@Composable
private fun NativeSmallLoadedPreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("App Headline", style = MaterialTheme.typography.titleSmall)
                Text("Advertiser", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(12.dp, 6.dp)
            ) {
                Text("Install", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true, name = "Native Medium - Loaded")
@Composable
private fun NativeMediumLoadedPreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("App Headline", style = MaterialTheme.typography.titleSmall)
                    Text("Advertiser", color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFA500))
                        .padding(4.dp, 2.dp)
                ) {
                    Text("Ad", fontSize = 10.sp, color = Color.White)
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text("Media", color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Text("Body text of the native ad.")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("★★★★☆", color = Color(0xFFFFB800))
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp, 8.dp)
                ) {
                    Text("Install", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Native - Failed")
@Composable
private fun NativeFailedPreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("Ad not available", color = Color.Gray)
        }
    }
}
