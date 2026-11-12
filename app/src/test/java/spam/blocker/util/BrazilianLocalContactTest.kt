package spam.blocker.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BrazilianLocalContactTest {
    @Test
    fun `accepts Brazilian mobile with international DDD and trunk formats`() {
        for (number in listOf("+55 (16) 99999-1234", "16999991234", "016999991234", "02116999991234")) {
            assertEquals("999991234", BrazilianLocalContact.incomingLocalNumber(number), number)
        }
    }

    @Test
    fun `accepts Brazilian landline`() {
        assertEquals("33331234", BrazilianLocalContact.incomingLocalNumber("+55 16 3333-1234"))
    }

    @Test
    fun `rejects foreign incomplete service invalid and extension inputs`() {
        for (number in listOf("+1 212 555 1234", "999991234", "190", "08001234567", "*123#", "tel:+5516999991234", "+5516999991234;123", "00000000000", "+55 16 99999-1234 ext 2")) {
            assertNull(BrazilianLocalContact.incomingLocalNumber(number), number)
        }
    }

    @Test
    fun `matches only a complete locally saved subscriber number`() {
        assertTrue(BrazilianLocalContact.matchesSavedLocalNumber("999991234", "99999-1234"))
        assertTrue(BrazilianLocalContact.matchesSavedLocalNumber("33331234", "3333 1234"))
        for (saved in listOf(null, "11999991234", "+55 16 99999-1234", "016999991234", "999991235", "99991234", "+999991234", "999991234 ext 2")) {
            assertFalse(BrazilianLocalContact.matchesSavedLocalNumber("999991234", saved), saved)
        }
    }
}
