import it.studio44z.zing.domain.IngredientAmount
import it.studio44z.zing.domain.LotAmount
import it.studio44z.zing.domain.QuantityMath
import it.studio44z.zing.domain.NutritionAdvisor
import it.studio44z.zing.domain.ageFromBirthDate
import it.studio44z.zing.domain.NutritionFacts
import it.studio44z.zing.domain.NutritionProfile
import it.studio44z.zing.data.RecipeIngredientRules
import it.studio44z.zing.data.isDueOrExpiringDate
import it.studio44z.zing.data.isExpiredDate
import it.studio44z.zing.data.isExpiringDate
import java.time.LocalDate
import java.time.ZoneId

private fun checkClose(expected: Double, actual: Double, label: String) {
    check(kotlin.math.abs(expected - actual) < 0.0001) { "$label: atteso $expected, ottenuto $actual" }
}

fun main() {
    checkClose(200.0, QuantityMath.scale(100.0, 1, 2)!!, "porzioni 1→2")
    checkClose(300.0, QuantityMath.scale(100.0, 1, 3)!!, "porzioni 1→3")
    check(QuantityMath.scale(null, 1, 3) == null) { "q.b. non deve essere moltiplicato" }
    checkClose(1500.0, QuantityMath.convert(1.5, "kg", "g")!!, "kg→g")
    checkClose(250.0, QuantityMath.convert(0.25, "l", "ml")!!, "l→ml")
    checkClose(28.349523125, QuantityMath.convert(1.0, "oz", "g")!!, "oz→g")
    checkClose(16.0, QuantityMath.convert(1.0, "cup", "tbsp")!!, "cup→tbsp")
    check(QuantityMath.convert(1.0, "kg", "ml") == null) { "conversione massa/volume da rifiutare" }
    checkClose(100.0, QuantityMath.missingQuantity(IngredientAmount("pasta", 300.0, "g"), listOf(LotAmount("Pasta", 200.0, "g")))!!, "quantità mancante")
    check(QuantityMath.namesMatch("pomodori", "pomodoro")) { "alias plurale" }
    val coverage = QuantityMath.coverage(
        listOf(IngredientAmount("Pasta", 300.0, "g"), IngredientAmount("Pomodoro", 100.0, "g")),
        listOf(LotAmount("pasta", 200.0, "g"), LotAmount("pomodori", 100.0, "g")),
    )
    check(coverage.percentage == 83) { "copertura attesa 83%, ottenuto ${coverage.percentage}%" }
    check(!coverage.fullyAvailable && coverage.missing.size == 1) { "ricetta parzialmente disponibile" }
    val today = LocalDate.of(2026, 10, 16)
    val zone = ZoneId.systemDefault()
    val todayMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
    val yesterdayMillis = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    check(!isExpiredDate(todayMillis, today)) { "la data di oggi non deve risultare già scaduta" }
    check(isExpiringDate(todayMillis, 0, today)) { "la scadenza di oggi deve essere evidenziata" }
    check(isExpiredDate(yesterdayMillis, today) && isDueOrExpiringDate(yesterdayMillis, 3, today)) { "una scadenza passata va segnalata come scaduta/da controllare" }
    val expiredLotMillis = LocalDate.now().minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val expiredLots = listOf(LotAmount("Pasta", 500.0, "g", expiredLotMillis))
    checkClose(0.0, QuantityMath.availableQuantity(IngredientAmount("Pasta", 100.0, "g"), expiredLots), "lotto scaduto non disponibile")
    check(!QuantityMath.coverage(listOf(IngredientAmount("Pasta", 100.0, "g")), expiredLots).fullyAvailable) { "la ricetta non deve usare lotti già scaduti" }

    val legacyGnocchi = QuantityMath.normalizeIngredient(IngredientAmount("200g di gnocchi di patate", null, "q.b."))
    check(legacyGnocchi.name == "gnocchi di patate" && legacyGnocchi.quantity == 200.0 && legacyGnocchi.unit == "g") {
        "quantità nel nome degli gnocchi non normalizzata: $legacyGnocchi"
    }
    val legacyCheese = QuantityMath.normalizeIngredient(IngredientAmount("100g di stracchino cremoso", null, "q.b."))
    check(legacyCheese.name == "stracchino cremoso" && legacyCheese.quantity == 100.0 && legacyCheese.unit == "g") {
        "quantità nel nome dello stracchino non normalizzata: $legacyCheese"
    }
    val legacyZucchina = QuantityMath.normalizeIngredient(IngredientAmount("1 zucchina media", null, "q.b."))
    check(legacyZucchina.name == "zucchina media" && legacyZucchina.quantity == 1.0 && legacyZucchina.unit == "pz") {
        "quantità nel nome della zucchina non normalizzata: $legacyZucchina"
    }
    val legacyPancetta = QuantityMath.normalizeIngredient(IngredientAmount("50-75g di pancetta (circa mezza vaschetta)", null, "q.b."))
    check(legacyPancetta.name.startsWith("pancetta") && legacyPancetta.quantity == 62.5 && legacyPancetta.unit == "g") {
        "intervallo di pancetta non normalizzato: $legacyPancetta"
    }
    check(QuantityMath.format(legacyPancetta.quantity, legacyPancetta.unit, legacyPancetta.note) == "50–75 g") {
        "intervallo di pancetta non visualizzato correttamente"
    }
    val scaledRange = legacyPancetta.copy(quantity = QuantityMath.scale(legacyPancetta.quantity, 2, 4))
    check(QuantityMath.format(scaledRange.quantity, scaledRange.unit, scaledRange.note) == "100–150 g") {
        "intervallo non scalato correttamente con le porzioni"
    }
    val salt = QuantityMath.normalizeIngredient(IngredientAmount("Sale e pepe nero q.b.", null, "q.b."))
    check(salt.quantity == null && QuantityMath.format(salt.quantity, salt.unit, salt.note) == "q.b.") {
        "q.b. qualitativo deve restare q.b."
    }
    val insufficientFacts = NutritionAdvisor.assess(NutritionFacts(sugarsG100g = 1.0, saltG100g = 0.1))
    check(insufficientFacts.score == null) { "non deve assegnare un punteggio con meno di tre dati nutrizionali" }
    val missingCoreFacts = NutritionAdvisor.assess(NutritionFacts(energyKcal100g = 120.0, fiberG100g = 4.0, proteinG100g = 8.0))
    check(missingCoreFacts.score == null) { "non deve dare un punteggio se mancano tutti gli indicatori chiave" }
    val birthdayToday = java.time.LocalDate.of(2026, 10, 16)
    check(ageFromBirthDate(java.time.LocalDate.of(2000, 10, 16), birthdayToday) == 26)
    check(ageFromBirthDate(java.time.LocalDate.of(2000, 10, 17), birthdayToday) == 25)
    check(ageFromBirthDate(java.time.LocalDate.of(2030, 1, 1), birthdayToday) == null)

    check(RecipeIngredientRules.isPantryStaple("Salt")) { "il sale va escluso dalla corrispondenza essenziale" }
    check(RecipeIngredientRules.isPantryStaple("sale fino")) { "il sale italiano va escluso dalla corrispondenza essenziale" }
    check(RecipeIngredientRules.isPantryStaple("olio extravergine d'oliva")) { "l'olio italiano va escluso dalla corrispondenza essenziale" }
    check(RecipeIngredientRules.isPantryStaple("mixed herbs")) { "le erbe miste vanno escluse dalla corrispondenza essenziale" }
    check(RecipeIngredientRules.matchesPantryIngredient("tomato", "Tomatoes")) { "plurale tomato non riconosciuto" }
    check(RecipeIngredientRules.matchesPantryIngredient("chicken", "Chicken breast")) { "descrittore di preparazione non riconosciuto" }
    check(!RecipeIngredientRules.matchesPantryIngredient("cheese", "Cheddar cheese")) { "etichetta generica cheese non deve garantire un formaggio specifico" }
    check(RecipeIngredientRules.matchesPantryIngredient("parmesan", "Parmesan cheese")) { "il formaggio specifico deve corrispondere alla variante con suffisso cheese" }
    check(!RecipeIngredientRules.matchesPantryIngredient("tomato", "tomato puree")) { "un prodotto trasformato non deve essere confuso con l'ingrediente intero" }
    check(!RecipeIngredientRules.matchesPantryIngredient("potato", "gnocchi")) { "le patate non devono essere considerate gnocchi" }
    check(!RecipeIngredientRules.isPantryStaple("bell pepper")) { "il peperone è un ingrediente essenziale, non una spezia" }
    check(RecipeIngredientRules.matchesPantryIngredient("bell pepper", "green pepper")) { "il peperone verde non corrisponde al peperone della dispensa" }
    check(RecipeIngredientRules.matchesPantryIngredient("zucchini", "courgettes")) { "zucchini/courgette non riconosciuti come sinonimi" }
    check(RecipeIngredientRules.matchesPantryIngredient("eggplant", "aubergine")) { "eggplant/aubergine non riconosciuti come sinonimi" }
    check(RecipeIngredientRules.matchesPantryIngredient("shrimp", "prawns")) { "shrimp/prawns non riconosciuti come sinonimi" }

    val nutritionProfile = NutritionProfile(age = 28, weightKg = 75.0, heightCm = 178.0, sexForEnergyEstimate = "male", trainingDaysPerWeek = 3)
    check(NutritionAdvisor.estimateDailyEnergyKcal(nutritionProfile) != null) { "stima energetica non calcolata per un profilo valido" }
    val balanced = NutritionAdvisor.assess(NutritionFacts(energyKcal100g = 200.0, sugarsG100g = 12.0, saturatedFatG100g = 2.0, saltG100g = 0.7, fiberG100g = 4.0, proteinG100g = 7.0))
    val lowerSugar = NutritionAdvisor.assess(NutritionFacts(energyKcal100g = 200.0, sugarsG100g = 12.0, saturatedFatG100g = 2.0, saltG100g = 0.7, fiberG100g = 4.0, proteinG100g = 7.0), nutritionProfile.copy(focus = "lower_sugar"))
    val balancedScore = balanced.score ?: error("punteggio di base atteso")
    val lowerSugarScore = lowerSugar.score ?: error("punteggio personalizzato atteso")
    check(lowerSugarScore < balancedScore) { "il focus personale non modifica il punteggio indicativo" }
    println("OK: verifiche dominio superate (porzioni, q.b., conversioni metriche/imperiali, quantità legacy, intervalli, copertura, scadenze e analisi nutrizionale).")
}
