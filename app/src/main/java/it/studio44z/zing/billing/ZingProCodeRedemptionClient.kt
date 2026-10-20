package it.studio44z.zing.billing

import android.content.Context
import android.util.Log
import it.studio44z.zing.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class ZingProCodeResult(
    val active: Boolean,
    val activatedAtMillis: Long? = null,
    val messageKey: String,
)

/** Client for the optional first-party code service. No URL means redemption stays safely disabled. */
internal class ZingProCodeRedemptionClient(private val context: Context) {
    private val baseUrl: String = BuildConfig.ZING_PRO_REDEMPTION_BASE_URL.trim().trimEnd('/')
    private val prefs = context.applicationContext.getSharedPreferences("zing_pro_state", Context.MODE_PRIVATE)

    private fun installationId(): String {
        val existing = prefs.getString("installation_id", null)
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString("installation_id", created).apply()
        return created
    }

    suspend fun redeem(rawCode: String): ZingProCodeResult = withContext(Dispatchers.IO) {
        val code = rawCode.trim().uppercase(Locale.ROOT)
        if (code.isBlank()) return@withContext ZingProCodeResult(false, messageKey = "Inserisci un codice Zing.")
        if (baseUrl.isBlank()) return@withContext ZingProCodeResult(false, messageKey = "I codici personalizzati Zing non sono ancora configurati. Contatta studio44z.info@gmail.com.")
        if (!baseUrl.startsWith("https://", ignoreCase = true)) {
            return@withContext ZingProCodeResult(false, messageKey = "Il servizio codici richiede un indirizzo HTTPS valido.")
        }
        request("/api/v1/redeem", JSONObject().put("code", code).put("installation_id", installationId()))
    }

    suspend fun refreshEntitlement(): ZingProCodeResult = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank() || !baseUrl.startsWith("https://", ignoreCase = true)) {
            return@withContext ZingProCodeResult(false, messageKey = "I codici personalizzati Zing non sono configurati.")
        }
        request("/api/v1/entitlement", JSONObject().put("installation_id", installationId()), statusQuery = true)
    }

    private fun request(path: String, payload: JSONObject, statusQuery: Boolean = false): ZingProCodeResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 7000
                readTimeout = 7000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = JSONObject(body)
            val active = json.optBoolean("active", false) && json.optBoolean("ok", true)
            val activatedAt = json.optLong("activated_at_ms", 0L).takeIf { it > 0L }
            if (statusQuery) {
                ZingProCodeResult(active, activatedAt, if (active) "Zing Pro attivo tramite codice Zing." else "Nessun codice Zing attivo su questo dispositivo.")
            } else if (code in 200..299 && active) {
                activatedAt?.let { prefs.edit().putLong("custom_code_activated_at_ms", it).apply() }
                ZingProCodeResult(true, activatedAt, "Codice Zing riscattato correttamente.")
            } else {
                val key = when (json.optString("code")) {
                    "invalid_code" -> "Codice Zing non valido. Controlla il codice e riprova."
                    "already_redeemed" -> "Questo codice Zing è già stato riscattato."
                    "expired" -> "Questo codice Zing è scaduto."
                    "service_disabled" -> "Il servizio codici Zing non è disponibile."
                    else -> "Non è stato possibile verificare il codice Zing. Riprova più tardi."
                }
                ZingProCodeResult(false, messageKey = key)
            }
        } catch (e: Exception) {
            Log.w("ZingProCodes", "Code service request failed", e)
            ZingProCodeResult(false, messageKey = "Connessione al servizio codici non riuscita. Controlla la connessione e riprova.")
        } finally {
            connection?.disconnect()
        }
    }
}
