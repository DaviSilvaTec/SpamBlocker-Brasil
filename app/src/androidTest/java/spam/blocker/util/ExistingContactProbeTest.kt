package spam.blocker.util

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Explicit on-device diagnostic. Reads one supplied number; never writes contacts. */
class ExistingContactProbeTest {
    @Test fun recognizesExistingContact() {
        val number = InstrumentationRegistry.getArguments().getString("probeNumber")
        assumeTrue("Optional probeNumber was not supplied", !number.isNullOrBlank())
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull("Supplied incoming number must resolve to an existing contact",
            Contacts.findContactByRawNumber(ctx, number!!))
    }
}
