package it.studio44z.zing.domain

import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.Period

data class NutritionFacts(
    val energyKcal100g: Double? = null,
    val sugarsG100g: Double? = null,
    val saturatedFatG100g: Double? = null,
    val saltG100g: Double? = null,
    val fiberG100g: Double? = null,
    val proteinG100g: Double? = null,
    val nutriScoreGrade: String? = null,
    val novaGroup: Int? = null,
    val ingredientsText: String? = null,
) {
    val hasAnyNutrients: Boolean
        get() = listOf(energyKcal100g, sugarsG100g, saturatedFatG100g, saltG100g, fiberG100g, proteinG100g).any { it != null && it.isFinite() && it >= 0.0 }
}

data class NutritionProfile(
    val age: Int? = null,
    val birthDate: LocalDate? = null,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val sexForEnergyEstimate: String = "unspecified",
    val trainingDaysPerWeek: Int = 0,
    val focus: String = "balanced",
) {
    val effectiveAge: Int?
        get() = if (birthDate != null) ageFromBirthDate(birthDate) else age

    val isUsableForEnergyEstimate: Boolean
        get() = effectiveAge in 18..100 && weightKg != null && weightKg in 30.0..350.0 && heightCm != null && heightCm in 120.0..230.0 && sexForEnergyEstimate in setOf("female", "male")
}

fun ageFromBirthDate(birthDate: LocalDate, today: LocalDate = LocalDate.now()): Int? {
    if (birthDate.isAfter(today)) return null
    return Period.between(birthDate, today).years.coerceAtLeast(0)
}

data class NutritionAssessment(
    val score: Int?,
    val scoreLabel: String,
    val recommendation: String,
    val estimatedDailyEnergyKcal: Int? = null,
    val kcalShareOfDailyEstimatePercent: Int? = null,
    val officialNutriScoreGrade: String? = null,
    val novaGroup: Int? = null,
    val completenessPercent: Int = 0,
)

object NutritionAdvisor {
    fun assess(facts: NutritionFacts, profile: NutritionProfile = NutritionProfile()): NutritionAssessment {
        val nutrientValues = listOf(
            facts.energyKcal100g, facts.sugarsG100g, facts.saturatedFatG100g,
            facts.saltG100g, facts.fiberG100g, facts.proteinG100g,
        )
        val knownCount = nutrientValues.count { it != null && it.isFinite() && it >= 0.0 }
        val completeness = (knownCount * 100.0 / nutrientValues.size).roundToInt()
        val energy = facts.energyKcal100g?.takeIf { it.isFinite() && it >= 0.0 }
        val keyRiskValuesKnown = listOf(facts.sugarsG100g, facts.saturatedFatG100g, facts.saltG100g)
            .count { it != null && it.isFinite() && it >= 0.0 }
        if (knownCount < 3 || keyRiskValuesKnown < 2) {
            return NutritionAssessment(
                score = null,
                scoreLabel = "Dati nutrizionali insufficienti",
                recommendation = "Il catalogo non riporta valori nutrizionali sufficienti per stimare il punteggio. Controlla l'etichetta del prodotto.",
                estimatedDailyEnergyKcal = profile.estimatedDailyEnergyKcal(),
                officialNutriScoreGrade = facts.nutriScoreGrade?.takeIf { it.lowercase() in setOf("a", "b", "c", "d", "e") },
                novaGroup = facts.novaGroup?.takeIf { it in 1..4 },
                completenessPercent = completeness,
            )
        }

        var score = 100
        facts.sugarsG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { sugar ->
            score -= when { sugar <= 5.0 -> 0; sugar <= 10.0 -> 5; sugar <= 15.0 -> 12; sugar <= 22.5 -> 20; else -> 28 }
            if (profile.focus == "lower_sugar" && sugar > 5.0) score -= ((sugar - 5.0) * 0.6).roundToInt().coerceAtMost(15)
        }
        facts.saturatedFatG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { sat ->
            score -= when { sat <= 1.5 -> 0; sat <= 5.0 -> 5; sat <= 10.0 -> 12; else -> 20 }
        }
        facts.saltG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { salt ->
            score -= when { salt <= 0.3 -> 0; salt <= 1.0 -> 5; salt <= 1.5 -> 10; else -> 18 }
            if (profile.focus == "lower_salt" && salt > 0.3) score -= ((salt - 0.3) * 4.0).roundToInt().coerceAtMost(12)
        }
        energy?.let { kcal -> score -= when { kcal <= 100 -> 0; kcal <= 250 -> 2; kcal <= 400 -> 5; else -> 10 } }
        facts.fiberG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { fiber -> score += when { fiber >= 6 -> 8; fiber >= 3 -> 4; else -> 0 } }
        facts.proteinG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { protein ->
            score += when { protein >= 10 -> 6; protein >= 5 -> 3; else -> 0 }
            if (profile.focus == "higher_protein") score += if (protein >= 10) 8 else if (protein >= 5) 4 else 0
        }
        score = score.coerceIn(0, 100)
        val scoreLabel = when { score >= 80 -> "Profilo indicativo favorevole"; score >= 60 -> "Profilo indicativo intermedio"; score >= 40 -> "Da confrontare con alternative"; else -> "Valuta porzione e frequenza" }
        val details = buildList {
            facts.sugarsG100g?.let { add("zuccheri ${format(it)} g/100 g") }
            facts.saltG100g?.let { add("sale ${format(it)} g/100 g") }
            facts.proteinG100g?.let { add("proteine ${format(it)} g/100 g") }
            facts.fiberG100g?.let { add("fibre ${format(it)} g/100 g") }
        }
        val focusAdvice = when (profile.focus) {
            "lower_sugar" -> "Focus personale: confronta soprattutto gli zuccheri per 100 g."
            "lower_salt" -> "Focus personale: confronta soprattutto il sale per 100 g."
            "higher_protein" -> "Focus personale: confronta le proteine per porzione e l'equilibrio complessivo."
            else -> "Confronta i valori per 100 g e la porzione che consumi abitualmente."
        }
        val energyEstimate = profile.estimatedDailyEnergyKcal()
        val energyShare = if (energy != null && energyEstimate != null && energyEstimate > 0) (energy * 100.0 / energyEstimate).roundToInt().coerceAtLeast(0) else null
        val recommendation = buildString {
            append(focusAdvice)
            if (details.isNotEmpty()) append(" Dati: ").append(details.joinToString(", ")).append('.')
            if (completeness < 67) append(" I dati online sono parziali: verifica l'etichetta.")
            if (energyShare != null) append(" 100 g apportano circa il $energyShare% della stima energetica giornaliera personale.")
            append(" Il punteggio è indicativo e non è una valutazione medica né il Nutri-Score ufficiale.")
        }
        return NutritionAssessment(
            score = score,
            scoreLabel = scoreLabel,
            recommendation = recommendation,
            estimatedDailyEnergyKcal = energyEstimate,
            kcalShareOfDailyEstimatePercent = energyShare,
            officialNutriScoreGrade = facts.nutriScoreGrade?.lowercase()?.takeIf { it in setOf("a", "b", "c", "d", "e") },
            novaGroup = facts.novaGroup?.takeIf { it in 1..4 },
            completenessPercent = completeness,
        )
    }

    fun estimateDailyEnergyKcal(profile: NutritionProfile): Int? = profile.estimatedDailyEnergyKcal()

    private fun NutritionProfile.estimatedDailyEnergyKcal(): Int? {
        val ageValue = effectiveAge ?: return null
        val weight = weightKg ?: return null
        val height = heightCm ?: return null
        if (ageValue !in 18..100 || weight !in 30.0..350.0 || height !in 120.0..230.0 || sexForEnergyEstimate !in setOf("female", "male")) return null
        val base = 10.0 * weight + 6.25 * height - 5.0 * ageValue + if (sexForEnergyEstimate == "male") 5.0 else -161.0
        val activity = when (trainingDaysPerWeek.coerceIn(0, 7)) {
            0 -> 1.2; 1, 2 -> 1.375; 3, 4 -> 1.55; 5, 6 -> 1.725; else -> 1.9
        }
        return (base * activity).roundToInt().coerceAtLeast(0)
    }

    private fun format(value: Double): String = String.format(java.util.Locale.ROOT, "%.1f", value)
}
