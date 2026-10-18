package it.studio44z.zing.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WikibooksRecipeParserTest {
    private val repository = RecipeDiscoveryRepository()

    @Test
    fun parsesBulletIngredientSectionAndStopsAtPreparation() {
        val wikitext = """
            == Ingredienti ==
            * 150g di burro o margarina
            * 150g zucchero
            * 300g farina
            * 1 dose di lievito
            * 2 tuorli d'uovo
            * (facoltativo) scorza di limone
            == Preparazione ==
            # Cuocere al forno.
        """.trimIndent()

        assertEquals(
            listOf(
                "150g di burro o margarina",
                "150g zucchero",
                "300g farina",
                "1 dose di lievito",
                "2 tuorli d'uovo",
                "(facoltativo) scorza di limone",
            ),
            repository.parseIngredientSection(wikitext),
        )
    }

    @Test
    fun ignoresProseOnlyIngredientSectionsInsteadOfGuessing() {
        val wikitext = """
            == Ingredienti ==
            Per un dolce semplice si scalda olio e vino e si versa sulla farina.
            == Preparazione ==
            # Mescolare e cuocere.
        """.trimIndent()

        assertTrue(repository.parseIngredientSection(wikitext).isEmpty())
    }

    @Test
    fun matchesItalianPluralNamesAndQuantifiedWikitextIngredients() {
        assertTrue(ItalianIngredientMatcher.matches("Pomodori", "5 cucchiai di salsa di pomodoro (concentrato)"))
        assertTrue(ItalianIngredientMatcher.matches("Uova", "2 tuorli d'uovo"))
        assertTrue(ItalianIngredientMatcher.matches("Zucchine", "zucchina fresca tagliata"))
        assertFalse(ItalianIngredientMatcher.matches("Formaggio", "parmigiano reggiano"))
    }
}
