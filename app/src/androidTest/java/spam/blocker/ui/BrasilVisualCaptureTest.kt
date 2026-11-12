package spam.blocker.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Rule
import org.junit.Test
import spam.blocker.G
import spam.blocker.db.CallTable
import spam.blocker.db.HistoryRecord
import spam.blocker.def.Def
import spam.blocker.ui.brasil.applyBrasilTheme
import spam.blocker.ui.main.MainActivity
import spam.blocker.util.spf
import java.io.File

/** Only run on the QA emulator: all history entries are fictional local fixtures. */
class BrasilVisualCaptureTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val ids=mutableListOf<Long>()
    @After fun cleanup() {
        compose.runOnIdle {
            ids.forEach { CallTable().deleteById(compose.activity,it) }
            G.callVM.reload(compose.activity)
            applyBrasilTheme(false)
        }
    }
    private fun capture(name:String) {
        compose.waitForIdle()
        val inst=InstrumentationRegistry.getInstrumentation()
        inst.waitForIdleSync()
        android.os.SystemClock.sleep(450)
        val dir=File(compose.activity.getExternalFilesDir(null),"ui-review").apply { mkdirs() }
        File(dir,"$name.png").outputStream().use { inst.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    @Test fun captureFullVisualReview() {
        compose.runOnIdle {
            applyBrasilTheme(false)
            val ctx=compose.activity
            spf.Contact(ctx).isEnabled=true
            spf.Contact(ctx).isStrict=true
            spf.Global(ctx).isGloballyEnabled=true
            spf.Global(ctx).isCallEnabled=true
            G.globallyEnabled.value=true
            G.callEnabled.value=true
            spf.HistoryOptions(ctx).showBlocked=true
            spf.HistoryOptions(ctx).showPassed=true
            spam.blocker.ui.history.HistoryOptions.showHistoryBlocked.value=true
            spam.blocker.ui.history.HistoryOptions.showHistoryPassed.value=true
        }
        compose.onNodeWithTag("nav-home").performClick()
        capture("01-protecao-escuro")
        compose.onNodeWithTag("brasil-home-scroll").performScrollToNode(hasTestTag("test-number"))
        capture("02-protecao-rolagem")
        compose.onNodeWithTag("test-number").performClick()
        capture("03-dialogo-teste")
        androidx.test.espresso.Espresso.pressBack()
        compose.onNodeWithTag("nav-setting").performClick()
        capture("04-ajustes-escuro")
        compose.onNodeWithTag("settings-scroll").performScrollToNode(hasText("Sobre o aplicativo"))
        capture("05-ajustes-fim")
        compose.onNodeWithTag("nav-sms").performClick()
        capture("06-mensagens-vazio")
        compose.runOnIdle {
            val ctx=compose.activity
            repeat(24) { n ->
                val id=-12000L-n
                ids.add(id)
                CallTable().addWithId(ctx,HistoryRecord(id=id,peer="1199000%04d".format(n),cnap=if(n==0) "Clínica de agendamentos e atendimento com nome extenso" else null,
                    time=System.currentTimeMillis()-n*60000L,result=if(n%3==0) Def.RESULT_ALLOWED_BY_DEFAULT else Def.RESULT_BLOCKED_BY_NON_CONTACT,read=true))
            }
            G.callVM.reload(ctx)
        }
        compose.onNodeWithTag("nav-call").performClick()
        capture("07-historico-escuro")
        compose.onNodeWithTag("history-row--12000").performClick()
        capture("08-historico-expandido")
        compose.onNodeWithTag("history-list").performScrollToNode(hasTestTag("history-row--12023"))
        capture("09-historico-fim")
        compose.runOnIdle { applyBrasilTheme(true) }
        compose.onNodeWithTag("nav-home").performClick()
        capture("10-protecao-claro")
        compose.onNodeWithTag("nav-setting").performClick()
        capture("11-ajustes-claro")
        compose.onNodeWithTag("nav-call").performClick()
        capture("12-historico-claro")
    }
}
