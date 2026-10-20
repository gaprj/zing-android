package it.studio44z.zing.ui

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class UsageStreakTest {
    @Test fun firstOpenStartsAtOne() {
        assertEquals(1, calculateNextStreak(0, null, LocalDate.parse("2026-10-19")).days)
    }
    @Test fun multipleOpensSameDayDoNotIncrement() {
        val date = LocalDate.parse("2026-10-19")
        assertEquals(StreakSnapshot(3, date), calculateNextStreak(3, date, date))
    }
    @Test fun nextCalendarDayIncrements() {
        assertEquals(4, calculateNextStreak(3, LocalDate.parse("2026-10-19"), LocalDate.parse("2026-10-20")).days)
    }
    @Test fun skippedDayResetsToOne() {
        assertEquals(1, calculateNextStreak(3, LocalDate.parse("2026-10-19"), LocalDate.parse("2026-10-21")).days)
    }
    @Test fun crossesMonthBoundary() {
        assertEquals(2, calculateNextStreak(1, LocalDate.parse("2026-10-31"), LocalDate.parse("2026-11-01")).days)
    }
    @Test fun crossesYearBoundary() {
        assertEquals(2, calculateNextStreak(1, LocalDate.parse("2026-12-31"), LocalDate.parse("2027-01-01")).days)
    }
    @Test fun backwardsClockDoesNotIncreaseOrReset() {
        val last = LocalDate.parse("2026-10-20")
        assertEquals(StreakSnapshot(3, last), calculateNextStreak(3, last, LocalDate.parse("2026-10-19")))
    }
}
