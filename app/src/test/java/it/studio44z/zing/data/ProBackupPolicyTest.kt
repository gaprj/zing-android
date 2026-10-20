package it.studio44z.zing.data

import com.google.gson.JsonParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProBackupPolicyTest {
    @Test fun entitlementAndActivationFieldsAreNeverPortable() {
        listOf(
            "supporterProUnlocked", "supporterProRedeemed", "zingProActivatedAt",
            "zingProActivatedAtMillis", "zingProCode", "proEntitlement", "purchaseToken",
            "google_play_activated_at_ms", "custom_code_activated_at_ms", "activationToken",
        ).forEach { assertTrue("$it must be filtered", ProBackupPolicy.isSensitiveKey(it)) }
    }

    @Test fun attackerAddedCaseAndSeparatorVariantsAreFiltered() {
        listOf("SUPPORTER_PRO_UNLOCKED", "Zing-Pro-Code", "somePurchaseToken", "CUSTOM_activation_token")
            .forEach { assertTrue("$it must be filtered", ProBackupPolicy.isSensitiveKey(it)) }
    }

    @Test fun manuallyAddedJsonProFieldsAreExcludedFromRestoredPreferences() {
        val attackerJson = """{
            "supporterProUnlocked": true,
            "zingProActivatedAt": 1792440000000,
            "purchaseToken": "fake-token",
            "zingProCode": "FAKE-PRO-CODE",
            "language": "it"
        }"""
        val json = JsonParser.parseString(attackerJson).asJsonObject
        val eligiblePreferences = json.entrySet().filterNot { ProBackupPolicy.isSensitiveKey(it.key) }
        val keys = eligiblePreferences.map { it.key }.toSet()

        assertFalse("import must ignore manually added Pro flags", "supporterProUnlocked" in keys)
        assertFalse("import must ignore manually added activation dates", "zingProActivatedAt" in keys)
        assertFalse("import must ignore purchase tokens", "purchaseToken" in keys)
        assertFalse("import must ignore custom codes", "zingProCode" in keys)
        assertTrue("non-Pro preferences can still be restored", "language" in keys)
    }

    @Test fun normalPreferencesRemainBackupEligible() {
        listOf("language", "dateFormat", "defaultUnit", "nutritionWeightKg", "supporterAccentTheme", "streakDays")
            .forEach { assertFalse("$it should not be filtered", ProBackupPolicy.isSensitiveKey(it)) }
    }
}
