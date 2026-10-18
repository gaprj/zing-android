package it.studio44z.zing.ui

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import it.studio44z.zing.BuildConfig

/** A single anchored adaptive banner. This composable is only shown after explicit opt-in and UMP consent checks. */
@Composable
internal fun ZingBannerAd(activity: Activity, modifier: Modifier = Modifier) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val adWidthDp = (screenWidthDp - 16).coerceAtLeast(1)
    val adView = remember(activity, adWidthDp) { AdView(activity) }

    LaunchedEffect(adView, adWidthDp) {
        val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, adWidthDp)
        val unitId = if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else BuildConfig.ADMOB_BANNER_AD_UNIT_ID
        val request = BannerAdRequest.Builder(unitId, adSize).build()
        adView.loadAd(
            request,
            object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    Log.d(TAG, "Opt-in banner loaded.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Opt-in banner failed to load: $adError")
                }
            },
        )
    }

    DisposableEffect(adView) {
        onDispose {
            (adView.parent as? ViewGroup)?.removeView(adView)
            adView.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.wrapContentSize(),
        )
    }
}

/** Google's dedicated test banner. Release builds use the real AdMob unit configured in Gradle. */
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
private const val TAG = "ZingBannerAd"
