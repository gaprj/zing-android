package it.studio44z.zing.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import it.studio44z.zing.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Consent-aware, opt-in-only entry point for Zing's banner advertising. */
internal class ZingAdsManager(context: Context) {
    private val appContext = context.applicationContext
    private val consentInformation = UserMessagingPlatform.getConsentInformation(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val latestRequestId = AtomicLong(0L)
    private val initializationStarted = AtomicBoolean(false)

    @Volatile
    private var adsOptedIn: Boolean = false

    private val _state = MutableStateFlow(ZingAdsState())
    val state: StateFlow<ZingAdsState> = _state.asStateFlow()

    /**
     * UMP consent information is refreshed on each app launch. The consent form is shown only
     * when the user has enabled the optional banner. A new call invalidates callbacks from an
     * older request (for example, if the user turns the setting back off while it is loading).
     */
    fun refreshConsent(activity: Activity, optedIn: Boolean) {
        adsOptedIn = optedIn
        val requestId = latestRequestId.incrementAndGet()
        _state.update { it.copy(adsOptedIn = optedIn, consentFlowComplete = false) }

        val parameters = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                if (requestId == latestRequestId.get()) {
                    refreshPrivacyOptionsRequirement()
                    if (optedIn && adsOptedIn) {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            if (formError != null) {
                                Log.i(TAG, "UMP consent form completed with message: ${formError.message}")
                            }
                            finishConsentFlow(requestId)
                        }
                    } else {
                        finishConsentFlow(requestId)
                    }
                }
            },
            { requestError ->
                if (requestId == latestRequestId.get()) {
                    Log.w(TAG, "Could not refresh UMP consent information: ${requestError.message}")
                    // UMP keeps the last consent result. Honor it only if canRequestAds() allows it.
                    finishConsentFlow(requestId)
                }
            },
        )
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Could not show UMP privacy options: ${formError.message}")
            }
            refreshPrivacyOptionsRequirement()
            val canRequest = consentInformation.canRequestAds()
            _state.update {
                it.copy(
                    canRequestAds = canRequest,
                    consentFlowComplete = true,
                )
            }
            if (adsOptedIn && canRequest) initializeMobileAdsIfAllowed()
        }
    }

    private fun finishConsentFlow(requestId: Long) {
        if (requestId != latestRequestId.get()) return
        val canRequest = consentInformation.canRequestAds()
        refreshPrivacyOptionsRequirement()
        _state.update {
            it.copy(
                adsOptedIn = adsOptedIn,
                canRequestAds = canRequest,
                consentFlowComplete = true,
            )
        }
        if (adsOptedIn && canRequest) initializeMobileAdsIfAllowed()
    }

    private fun refreshPrivacyOptionsRequirement() {
        val required = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        _state.update { it.copy(privacyOptionsRequired = required) }
    }

    private fun initializeMobileAdsIfAllowed() {
        if (!adsOptedIn || !_state.value.canRequestAds) return
        if (!initializationStarted.compareAndSet(false, true)) return

        scope.launch {
            try {
                // GMA Next-Gen initialization must run on a background thread. Consent is checked first.
                MobileAds.initialize(
                    appContext,
                    InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build(),
                ) {
                    _state.update { it.copy(sdkReady = true) }
                }
            } catch (error: Exception) {
                initializationStarted.set(false)
                Log.e(TAG, "Google Mobile Ads initialization failed", error)
            }
        }
    }

    private companion object {
        const val TAG = "ZingAds"
    }
}

internal data class ZingAdsState(
    val adsOptedIn: Boolean = false,
    val consentFlowComplete: Boolean = false,
    val canRequestAds: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val sdkReady: Boolean = false,
)

internal fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current != null) {
        if (current is Activity) return current
        current = (current as? ContextWrapper)?.baseContext
    }
    return null
}
