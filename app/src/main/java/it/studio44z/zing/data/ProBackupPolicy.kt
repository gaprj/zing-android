package it.studio44z.zing.data

import java.util.Locale

/** Fields that must never be portable through user-created JSON backups. */
internal object ProBackupPolicy {
    private val exactKeys = setOf(
        "supporterProUnlocked", "supporterProRedeemed", "zingProActivatedAt", "zingProActivatedAtMillis",
        "zingProCode", "zingProCodeRedeemed", "proCode", "proCodes", "proEntitlement",
        "purchaseToken", "billingPurchaseToken", "zingProPurchaseToken", "activationToken",
        "customCodeActivatedAt", "custom_code_activated_at_ms", "google_play_activated_at_ms",
        "installation_id", "zingProActivationDate", "supporterProActivationDate", "zingProActivatedAtMillis",
    )

    fun isSensitiveKey(key: String): Boolean {
        val normalized = key.lowercase(Locale.ROOT).replace("_", "").replace("-", "")
        return key in exactKeys || normalized in setOf(
            "supporterprounlocked", "supporterproredeemed", "zingproactivatedat", "zingproactivatedatmillis",
            "zingprocode", "zingprocoderedeemed", "procode", "procodes", "proentitlement", "purchasetoken",
            "billingpurchasetoken", "zingpropurchasetoken", "activationtoken", "customcodeactivatedat",
            "customcodeactivatedatms", "googleplayactivatedatms", "installationid", "zingproactivationdate",
            "supporterproactivationdate",
        ) || normalized.contains("purchasetoken") || normalized.contains("activationtoken") ||
            (normalized.contains("zingpro") && (normalized.contains("code") || normalized.contains("entitlement") || normalized.contains("activatedat")))
    }
}
