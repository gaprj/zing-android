package it.studio44z.zing.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class GooglePlayBillingManager(context: Context) : PurchasesUpdatedListener {
    private val appContext = context.applicationContext
    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    private val _proUnlocked = MutableStateFlow(false)
    val proUnlocked: StateFlow<Boolean> = _proUnlocked.asStateFlow()

    private val _formattedPrice = MutableStateFlow<String?>(null)
    val formattedPrice: StateFlow<String?> = _formattedPrice.asStateFlow()

    private val _productAvailable = MutableStateFlow(false)
    val productAvailable: StateFlow<Boolean> = _productAvailable.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val entitlementPrefs = appContext.getSharedPreferences("zing_pro_state", Context.MODE_PRIVATE)
    private val _activationTimeMillis = MutableStateFlow(
        entitlementPrefs.getLong("google_play_activated_at_ms", 0L).takeIf { it > 0L },
    )
    /** Historical purchase timestamp, displayed only as metadata and never used to grant Pro. */
    val activationTimeMillis: StateFlow<Long?> = _activationTimeMillis.asStateFlow()

    @Volatile private var connecting = false

    fun start() {
        ensureConnected()
    }

    fun refreshPurchases() {
        if (billingClient.isReady) {
            queryProductDetails()
            queryOwnedPurchases()
        } else {
            ensureConnected()
        }
    }

    fun endConnection() {
        connecting = false
        if (billingClient.isReady) billingClient.endConnection()
    }

    private fun ensureConnected() {
        if (billingClient.isReady) {
            queryProductDetails()
            queryOwnedPurchases()
            return
        }
        if (connecting) return
        connecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connecting = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryOwnedPurchases()
                } else {
                    _productAvailable.value = false
                    _statusMessage.value = "Google Play Billing non disponibile (${billingResult.responseCode})."
                }
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
            }
        })
    }

    private fun productQueryParams(): QueryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

    private fun queryProductDetails() {
        if (!billingClient.isReady) return
        billingClient.queryProductDetailsAsync(productQueryParams()) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _productAvailable.value = false
                _statusMessage.value = "Impossibile caricare il prodotto Google Play (${result.responseCode})."
                return@queryProductDetailsAsync
            }
            val details = queryResult.productDetailsList.firstOrNull { it.productId == PRODUCT_ID }
            val offer = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()
            _formattedPrice.value = offer?.formattedPrice
            _productAvailable.value = details != null && offer != null
            if (details == null || offer == null) {
                _statusMessage.value = "Prodotto non ancora attivo o non disponibile per questo account. Configura $PRODUCT_ID in Play Console e installa la build tramite Google Play testing."
            } else {
                _statusMessage.value = null
            }
        }
    }

    private fun queryOwnedPurchases() {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            } else {
                _statusMessage.value = "Impossibile verificare gli acquisti Google Play (${result.responseCode})."
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) queryOwnedPurchases() else processPurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _statusMessage.value = "Acquisto annullato."
            }
            else -> {
                _statusMessage.value = "Acquisto non completato (${result.responseCode}). Riprova più tardi."
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val ownedPurchase = purchases.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(PRODUCT_ID)
        }
        _proUnlocked.value = ownedPurchase != null
        if (ownedPurchase == null) return

        val purchaseTime = ownedPurchase.purchaseTime.takeIf { it > 0L }
        if (purchaseTime != null) {
            entitlementPrefs.edit().putLong("google_play_activated_at_ms", purchaseTime).apply()
            _activationTimeMillis.value = purchaseTime
        }
        _statusMessage.value = "Google Play ha confermato Zing Pro."
        if (!ownedPurchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(ownedPurchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { acknowledgeResult ->
                if (acknowledgeResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    _statusMessage.value = "Acquisto riconosciuto; conferma Google Play da completare (${acknowledgeResult.responseCode})."
                }
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (!billingClient.isReady) {
            _statusMessage.value = "Connessione a Google Play in corso. Riprova tra poco."
            ensureConnected()
            return
        }

        billingClient.queryProductDetailsAsync(productQueryParams()) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _statusMessage.value = "Prodotto non disponibile (${result.responseCode})."
                return@queryProductDetailsAsync
            }
            val details = queryResult.productDetailsList.firstOrNull { it.productId == PRODUCT_ID }
            val offer = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()
            if (details == null || offer == null) {
                _productAvailable.value = false
                _statusMessage.value = "Zing Pro non è ancora configurato o attivo in Play Console."
                return@queryProductDetailsAsync
            }

            val offerToken = offer.offerToken?.takeIf { it.isNotBlank() }
            if (offerToken == null) {
                _productAvailable.value = false
                _statusMessage.value = "L'offerta Google Play non contiene un token valido. Controlla la configurazione del prodotto in Play Console."
                return@queryProductDetailsAsync
            }

            val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build()
            val launchResult = billingClient.launchBillingFlow(activity, flowParams)
            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _statusMessage.value = "Impossibile avviare l'acquisto (${launchResult.responseCode})."
            }
        }
    }

    fun openPromoRedemption(context: Context, rawCode: String) {
        val code = rawCode.trim()
        if (code.isBlank()) {
            _statusMessage.value = "Inserisci un codice promozionale Google Play."
            return
        }
        val uri = Uri.parse("https://play.google.com/redeem?code=${Uri.encode(code)}")
        val playIntent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.android.vending")
        try {
            launchExternal(context, playIntent)
            _statusMessage.value = "Completa il riscatto in Google Play, poi torna qui: controlleremo l'acquisto."
        } catch (_: Exception) {
            try {
                launchExternal(context, Intent(Intent.ACTION_VIEW, uri))
                _statusMessage.value = "Completa il riscatto in Google Play e poi torna in Zing per aggiornare lo stato Pro."
            } catch (_: Exception) {
                _statusMessage.value = "Non riesco ad aprire Google Play. Aggiorna Play Store e riprova."
            }
        }
    }

    private fun launchExternal(context: Context, intent: Intent) {
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    companion object {
        const val PRODUCT_ID = "zing_pro_lifetime"
    }
}
