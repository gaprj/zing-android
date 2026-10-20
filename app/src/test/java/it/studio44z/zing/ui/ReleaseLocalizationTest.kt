package it.studio44z.zing.ui

import org.junit.Assert.assertNotEquals
import org.junit.Test

class ReleaseLocalizationTest {
    @Test fun allNewReleaseUiPhrasesAreLocalizedForEverySupportedLanguage() {
        val phrases = listOf(
            "Automatico (sistema)", "Hai cambiato telefono? Scrivi a studio44z.info@gmail.com per assistenza.",
            "Giorni di fila", "Hai aperto Zing 1 giorno di fila.", "Hai aperto Zing {days} giorni di fila.",
            "Aggiornamento disponibile", "È disponibile una nuova versione di Zing.", "Aggiorna", "Chiudi",
            "Zing Pro attivato il", "Riscatta un codice Zing", "I codici Zing personali sono monouso e devono essere verificati online.",
            "Codice Zing", "Verifica e riscatta codice Zing", "Inserisci un codice Zing.",
            "I codici personalizzati Zing non sono ancora configurati. Contatta studio44z.info@gmail.com.",
            "I codici personalizzati Zing non sono configurati.", "Il servizio codici richiede un indirizzo HTTPS valido.",
            "Codice Zing non valido. Controlla il codice e riprova.", "Questo codice Zing è già stato riscattato.",
            "Questo codice Zing è scaduto.", "Il servizio codici Zing non è disponibile.",
            "Non è stato possibile verificare il codice Zing. Riprova più tardi.",
            "Connessione al servizio codici non riuscita. Controlla la connessione e riprova.",
            "Zing Pro attivo tramite codice Zing.", "Nessun codice Zing attivo su questo dispositivo.",
            "Codice Zing riscattato correttamente.", "Google Play Billing non disponibile.",
            "Impossibile caricare il prodotto Google Play.", "Zing Pro non è ancora attivo in Google Play.",
            "Impossibile verificare gli acquisti Google Play.", "Acquisto non disponibile.", "Acquisto annullato.",
            "Acquisto non completato. Riprova più tardi.", "Google Play ha confermato Zing Pro.",
            "Acquisto riconosciuto; conferma in attesa.", "Connessione a Google Play in corso. Riprova tra poco.",
            "L'offerta Google Play non è configurata correttamente.",
            "Inserisci un codice promozionale Google Play.",
            "Completa il riscatto in Google Play, poi torna qui: controlleremo l'acquisto.",
            "Completa il riscatto in Google Play e poi torna in Zing per aggiornare lo stato Pro.",
            "Non riesco ad aprire Google Play. Aggiorna Play Store e riprova.",
        )
        val languages = listOf("en-US", "es", "fr", "de", "pt")
        languages.forEach { language ->
            phrases.forEach { phrase ->
                assertNotEquals("Missing $language translation for: $phrase", phrase, uiCopy(phrase, language))
            }
        }
    }
}
