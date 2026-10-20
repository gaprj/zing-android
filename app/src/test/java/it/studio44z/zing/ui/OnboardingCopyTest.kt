package it.studio44z.zing.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class OnboardingCopyTest {
    @Test
    fun allSupportedLanguagesProvideFourCompleteSlides() {
        listOf("it", "en", "es", "fr", "de", "pt").forEach { language ->
            val copy = OnboardingCopy.forLanguage(language)

            assertEquals("$language title count", 4, copy.titles.size)
            assertEquals("$language description count", 4, copy.descriptions.size)
            assertFalse("$language has blank titles", copy.titles.any(String::isBlank))
            assertFalse("$language has blank descriptions", copy.descriptions.any(String::isBlank))
            assertFalse("$language has blank controls", listOf(copy.skip, copy.back, copy.next, copy.start).any(String::isBlank))
            assertFalse("$language has blank feature labels", listOf(
                copy.scan, copy.quantity, copy.expiry, copy.available, copy.missing,
                copy.shoppingItemOne, copy.shoppingItemTwo, copy.reviewTitle,
                copy.reviewDescription, copy.reviewButton,
            ).any(String::isBlank))
        }
    }

    @Test
    fun unknownLanguageFallsBackToItalian() {
        assertEquals(OnboardingCopy.forLanguage("it"), OnboardingCopy.forLanguage("xx"))
    }
}
