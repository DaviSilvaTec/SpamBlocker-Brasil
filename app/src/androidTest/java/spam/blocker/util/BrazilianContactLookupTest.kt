package spam.blocker.util

import android.Manifest
import android.content.ContentValues
import android.content.ContentUris
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Local-only fixtures; never modify existing or synced contacts. */
class BrazilianContactLookupTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val ctx = instrumentation.targetContext
    private val rawIds = mutableListOf<Long>()
    private var oldPermission = false

    @Before fun setup() {
        instrumentation.uiAutomation.adoptShellPermissionIdentity(
            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS
        )
        oldPermission = Permission.contacts.isGranted
        Permission.contacts.isGranted = true
    }

    @After fun cleanup() {
        for (id in rawIds) {
            ctx.contentResolver.delete(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, id), null, null)
        }
        Permission.contacts.isGranted = oldPermission
        instrumentation.uiAutomation.dropShellPermissionIdentity()
    }

    private fun addContact(name: String, number: String) {
        val raw = ctx.contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, ContentValues().apply {
            putNull(ContactsContract.RawContacts.ACCOUNT_TYPE)
            putNull(ContactsContract.RawContacts.ACCOUNT_NAME)
            put(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
        })!!
        val id = ContentUris.parseId(raw)
        rawIds.add(id)
        for ((mime, field, value) in listOf(
            Triple(StructuredName.CONTENT_ITEM_TYPE, StructuredName.DISPLAY_NAME, name),
            Triple(Phone.CONTENT_ITEM_TYPE, Phone.NUMBER, number)
        )) {
            ctx.contentResolver.insert(ContactsContract.Data.CONTENT_URI, ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, id)
                put(ContactsContract.Data.MIMETYPE, mime)
                put(field, value)
            })
        }
    }

    @Test fun findsLocalOnlyContactForIncomingBrazilianNumber() {
        addContact("SB Brasil local fixture", "99999-1234")
        assertEquals("SB Brasil local fixture", Contacts.findContactByRawNumber(ctx, "016999991234")?.name)
        assertEquals("SB Brasil local fixture", Contacts.findContactByRawNumber(ctx, "+5516999991234")?.name)
    }

    @Test fun preservesFullNumberLookup() {
        addContact("SB Brasil full fixture", "+551633331234")
        assertEquals("SB Brasil full fixture", Contacts.findContactByRawNumber(ctx, "+551633331234")?.name)
    }

    @Test fun doesNotAllowAnUnknownNumberOrForeignCaller() {
        addContact("SB Brasil negative fixture", "99999-1234")
        assertNull(Contacts.findContactByRawNumber(ctx, "+5516999991235"))
        assertNull(Contacts.findContactByRawNumber(ctx, "+12125551234"))
    }

    @Test fun declinesAmbiguousLocalContacts() {
        addContact("SB Brasil ambiguous one", "99999-1234")
        addContact("SB Brasil ambiguous two", "99999-1234")
        assertNull(Contacts.findContactByRawNumber(ctx, "+5516999991234"))
    }
}
