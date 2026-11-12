package spam.blocker.ui.brasil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.def.Def
import spam.blocker.ui.setting.TestDialog
import spam.blocker.ui.setting.quick.Contacts
import spam.blocker.util.Permission
import spam.blocker.util.PermissionWrapper
import spam.blocker.util.spf

@Composable fun ProtectionScreen() {
    val ctx=LocalContext.current
    val C=G.palette
    val revision=rememberSettingsRevision()
    val contactPrefs=remember(revision) { spf.Contact(ctx) }
    val contactsAllowed=contactPrefs.isEnabled && Permission.contacts.isGranted
    val active=G.globallyEnabled.value && G.callEnabled.value && Permission.callScreening.isGranted
    val test=remember { mutableStateOf(false) }
    TestDialog(test)
    Column(Modifier.fillMaxSize().testTag("brasil-home-scroll").verticalScroll(rememberScrollState()).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Column(Modifier.fillMaxWidth().background(C.dialogBg,RoundedCornerShape(26.dp)).padding(24.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_brasil_shield),null,Modifier.size(38.dp),tint=C.teal200)
                Spacer(Modifier.width(12.dp))
                Text(if(active) "PROTEÇÃO ATIVA" else "PROTEÇÃO PAUSADA",Modifier.testTag("protection-status"),fontSize=12.sp,letterSpacing=1.1.sp,fontWeight=FontWeight.SemiBold,color=if(active) C.teal200 else C.warning)
            }
            Text("Você decide\nquem pode ligar.",Modifier.padding(top=20.dp,bottom=12.dp),style=MaterialTheme.typography.headlineLarge.copy(fontSize=28.sp,lineHeight=34.sp),color=C.textGrey)
            Text(if(active) "A filtragem de chamadas está ligada. Sua agenda ajuda a reconhecer quem você conhece." else "Ative a filtragem para aplicar suas regras às chamadas recebidas.",color=C.textGrey.copy(alpha=.85f),style=MaterialTheme.typography.bodyMedium)
            if(contactPrefs.isEnabled && !Permission.contacts.isGranted) Text("O acesso à agenda precisa ser liberado para reconhecer seus contatos.",Modifier.padding(top=12.dp),color=C.warning,style=MaterialTheme.typography.bodySmall)
            Button(onClick={
                val prefs=spf.Global(ctx)
                if(active) { prefs.isGloballyEnabled=false;G.globallyEnabled.value=false }
                else G.permissionChain.ask(ctx,listOf(PermissionWrapper(Permission.callScreening))) { granted ->
                    if(granted) { prefs.isGloballyEnabled=true;prefs.isCallEnabled=true;G.globallyEnabled.value=true;G.callEnabled.value=true }
                }
            },modifier=Modifier.fillMaxWidth().padding(top=24.dp).heightIn(min=52.dp).testTag("toggle-protection"),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=C.teal200,contentColor=C.background)) {
                Text(if(active) "Pausar proteção" else "Ativar proteção",fontWeight=FontWeight.SemiBold)
            }
        }
        Column {
            Text("Quem pode ligar",style=MaterialTheme.typography.titleMedium,color=C.textGrey)
            Text(if(active) "As regras abaixo fazem parte da sua filtragem." else "Regras salvas para quando você ativar a proteção.",Modifier.padding(top=4.dp,bottom=12.dp),color=C.textGrey.copy(alpha=.8f),style=MaterialTheme.typography.bodySmall)
            Column(Modifier.fillMaxWidth().background(C.dialogBg,RoundedCornerShape(20.dp)).padding(16.dp)) {
                PolicyLine(R.drawable.ic_contact_square,"Contatos da agenda",if(contactsAllowed) "Permitidos" else "Seguem outras regras",contactsAllowed)
                HorizontalDivider(Modifier.padding(vertical=14.dp),color=C.textGrey.copy(alpha=.12f))
                PolicyLine(R.drawable.ic_call,"Números desconhecidos",if(contactsAllowed && contactPrefs.isStrict) "Bloqueados" else "Seguem outras regras",false)
                Text("Contatos brasileiros também são reconhecidos quando salvos sem DDD. Números completos mantêm o DDD na comparação.",Modifier.padding(top=16.dp),color=C.textGrey.copy(alpha=.8f),style=MaterialTheme.typography.bodySmall)
            }
        }
        Column(Modifier.fillMaxWidth().background(C.dialogBg,RoundedCornerShape(20.dp)).padding(16.dp)) {
            Text("Sua agenda, reconhecida",style=MaterialTheme.typography.titleMedium,color=C.textGrey)
            Text(if(Permission.contacts.isGranted) "Acesso aos contatos disponível neste aparelho." else "Permita acesso aos contatos para reconhecer pessoas da sua agenda.",Modifier.padding(top=6.dp,bottom=8.dp),color=C.textGrey.copy(alpha=.8f),style=MaterialTheme.typography.bodySmall)
            Contacts()
        }
        OutlinedButton(onClick={test.value=true},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp).testTag("test-number"),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.outlinedButtonColors(contentColor=C.teal200)) {
            Icon(painterResource(R.drawable.ic_tube),null,Modifier.size(20.dp));Spacer(Modifier.width(8.dp));Text("Testar um número")
        }
        Column(Modifier.fillMaxWidth().clickable { navigateBrasil(Def.SETTING_TAB_ROUTE) }.background(C.dialogBg,RoundedCornerShape(20.dp)).padding(20.dp)) {
            Text("Suas regras, em detalhes",style=MaterialTheme.typography.titleMedium,color=C.textGrey)
            Text("Exceções, filtros adicionais, aparência e backup.",Modifier.padding(top=6.dp),color=C.textGrey.copy(alpha=.8f),style=MaterialTheme.typography.bodyMedium)
            Text("Abrir ajustes  →",Modifier.padding(top=16.dp),color=C.teal200,fontWeight=FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
    }
}
@Composable private fun PolicyLine(icon:Int,title:String,value:String,positive:Boolean) {
    val C=G.palette
    Row(verticalAlignment=Alignment.CenterVertically) {
        Icon(painterResource(icon),null,Modifier.size(22.dp),tint=C.textGrey.copy(alpha=.8f))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title,color=C.textGrey,fontWeight=FontWeight.SemiBold)
            Text(value,color=if(positive) C.teal200 else C.textGrey.copy(alpha=.8f),style=MaterialTheme.typography.bodySmall)
        }
    }
}
