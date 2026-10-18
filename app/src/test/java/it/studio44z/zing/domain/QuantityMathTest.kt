package it.studio44z.zing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuantityMathTest {
    @Test fun scalesRecipeQuantitiesAndPreservesQualitativeIngredients() {
        assertEquals(200.0, QuantityMath.scale(100.0, 1, 2)!!, 0.0001)
        assertEquals(300.0, QuantityMath.scale(100.0, 1, 3)!!, 0.0001)
        assertEquals(null, QuantityMath.scale(null, 1, 3))
    }

    @Test fun convertsOnlyCompatibleUnits() {
        assertEquals(1500.0, QuantityMath.convert(1.5, "kg", "g")!!, 0.0001)
        assertEquals(250.0, QuantityMath.convert(0.25, "l", "ml")!!, 0.0001)
        assertEquals(null, QuantityMath.convert(1.0, "kg", "ml"))
    }

    @Test fun computesPartialMissingAmount() {
        val ingredient = IngredientAmount("Pasta", 300.0, "g")
        val lots = listOf(LotAmount("pasta", 200.0, "g"))
        assertEquals(100.0, QuantityMath.missingQuantity(ingredient, lots)!!, 0.0001)
    }

    @Test fun pluralIngredientsMatchAndQuantitativeCoverageIsPartial() {
        assertTrue(QuantityMath.namesMatch("pomodori", "pomodoro"))
        val result = QuantityMath.coverage(
            listOf(IngredientAmount("Pasta", 300.0, "g"), IngredientAmount("Pomodoro", 100.0, "g")),
            listOf(LotAmount("pasta", 200.0, "g"), LotAmount("pomodori", 100.0, "g")),
        )
        assertEquals(83, result.percentage)
        assertFalse(result.fullyAvailable)
        assertEquals(1, result.missing.size)
    }
    @Test fun translatedIngredientNamesMatchCanonicalPantryIngredients() {
        assertTrue(QuantityMath.namesMatch("Tomatoes", "Pomodori"))
        assertTrue(QuantityMath.namesMatch("Eggs", "Uova"))
        assertTrue(QuantityMath.namesMatch("Flour", "Farina"))
        assertTrue(QuantityMath.namesMatch("Extra-virgin olive oil", "Olio extravergine d'oliva"))
    }

}
