package spam.blocker.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

/** Matching only; never writes to the contact provider. */
internal object BrazilianLocalContact {
    private val phoneUtil = PhoneNumberUtil.getInstance()
    private val phoneCharacters = Regex("[+0-9() .-]+")

    fun incomingLocalNumber(rawNumber: String): String? {
        if (!phoneCharacters.matches(rawNumber)) return null
        val parsed = try {
            phoneUtil.parse(rawNumber, "BR")
        } catch (_: NumberParseException) {
            return null
        }
        if (parsed.countryCode != 55 || parsed.hasExtension() || !phoneUtil.isValidNumber(parsed)) return null
        if (phoneUtil.getNumberType(parsed) !in setOf(
                PhoneNumberUtil.PhoneNumberType.FIXED_LINE,
                PhoneNumberUtil.PhoneNumberType.MOBILE,
                PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE
            )) return null
        val nationalNumber = parsed.nationalNumber.toString()
        if (nationalNumber.length !in 10..11) return null
        return nationalNumber.drop(2)
    }

    fun matchesSavedLocalNumber(localNumber: String, savedNumber: String?): Boolean {
        if (savedNumber == null || !phoneCharacters.matches(savedNumber) || '+' in savedNumber) return false
        val digits = savedNumber.filter { it in '0'..'9' }
        return digits.length in 8..9 && digits == localNumber
    }
}
