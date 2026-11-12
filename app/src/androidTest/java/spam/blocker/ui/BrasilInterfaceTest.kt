package spam.blocker.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.After
import spam.blocker.G
import spam.blocker.db.CallTable
import spam.blocker.db.HistoryRecord
import spam.blocker.def.Def
import spam.blocker.ui.main.MainActivity
import spam.blocker.util.spf

class BrasilInterfaceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val fixtureIds = mutableListOf<Long>()

    @After fun cleanup() {
        compose.runOnIdle {
            fixtureIds.forEach { CallTable().deleteById(compose.activity, it) }
            G.callVM.reload(compose.activity)
        }
    }

    @Test fun protectionNavigationAndNumberTestAreReachable() {
        compose.onNodeWithTag("nav-home").performClick()
        compose.onNodeWithTag("protection-status").assertIsDisplayed()
        compose.onNodeWithTag("brasil-home-scroll").performScrollToNode(hasTestTag("test-number"))
        compose.onNodeWithTag("test-number").performClick()
        compose.onNodeWithText("Teste de Regra").assertIsDisplayed()
        androidx.test.espresso.Espresso.pressBack()
        compose.onNodeWithTag("nav-setting").performClick()
        compose.onNodeWithTag("nav-setting").assertIsSelected()
        compose.onNodeWithTag("settings-scroll").assertIsDisplayed()
    }

    @Test fun selectedTabSurvivesActivityRecreation() {
        compose.onNodeWithTag("nav-home").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("nav-home").assertIsSelected()
        compose.onNodeWithTag("protection-status").assertIsDisplayed()
    }

    @Test fun settingsScrollReachesLastSection() {
        compose.onNodeWithTag("nav-setting").performClick()
        compose.onNodeWithTag("settings-scroll").performScrollToNode(hasText("Sobre o aplicativo"))
        compose.onNodeWithText("Sobre o aplicativo").assertIsDisplayed()
        compose.onNodeWithTag("nav-home").assertIsDisplayed()
    }

    @Test fun historyWithLongContentScrollsPastTwoScreens() {
        compose.runOnIdle {
            val ctx = compose.activity
            spf.HistoryOptions(ctx).showBlocked = true
            spf.HistoryOptions(ctx).showPassed = true
            HistoryOptionsSetup.enable()
            repeat(24) { n ->
                val id = -9000L-n
                fixtureIds.add(id)
                CallTable().addWithId(ctx, HistoryRecord(
                    id=id, peer="1199000%04d".format(n),
                    cnap=if(n==0) "Clínica de atendimento e agendamento com nome extenso" else null,
                    time=System.currentTimeMillis()-n*60000L,
                    result=Def.RESULT_BLOCKED_BY_NON_CONTACT, read=true
                ))
            }
            G.callVM.reload(ctx)
        }
        compose.onNodeWithTag("nav-call").performClick()
        compose.onNodeWithTag("history-list").performScrollToNode(hasTestTag("history-row--9023"))
        compose.onNodeWithTag("history-row--9023").assertIsDisplayed()
        compose.onNodeWithTag("nav-setting").assertIsDisplayed()
    }
}

private object HistoryOptionsSetup {
    fun enable() {
        spam.blocker.ui.history.HistoryOptions.showHistoryBlocked.value=true
        spam.blocker.ui.history.HistoryOptions.showHistoryPassed.value=true
    }
}
