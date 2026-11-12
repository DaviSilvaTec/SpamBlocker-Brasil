@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package spam.blocker.ui.brasil

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.def.Def
import spam.blocker.ui.isLight
import spam.blocker.util.spf

val LocalSettingLabel = staticCompositionLocalOf { "Opção" }

const val PROTECTION_ROUTE = "home"
val BrasilHeading = FontFamily(*listOf(400,500,600).map { w -> Font(R.font.space_grotesk,weight=FontWeight(w),variationSettings=FontVariation.Settings(FontVariation.weight(w))) }.toTypedArray())
val BrasilBody = FontFamily(*listOf(400,500,600).map { w -> Font(R.font.source_sans,weight=FontWeight(w),variationSettings=FontVariation.Settings(FontVariation.weight(w))) }.toTypedArray())
val BrasilTypography = Typography(
    bodyLarge=androidx.compose.ui.text.TextStyle(fontFamily=BrasilBody,fontSize=16.sp,lineHeight=23.sp),
    bodyMedium=androidx.compose.ui.text.TextStyle(fontFamily=BrasilBody,fontSize=16.sp,lineHeight=22.sp),
    bodySmall=androidx.compose.ui.text.TextStyle(fontFamily=BrasilBody,fontSize=14.sp,lineHeight=19.sp),
    labelLarge=androidx.compose.ui.text.TextStyle(fontFamily=BrasilBody,fontSize=16.sp,fontWeight=FontWeight.SemiBold),
    labelMedium=androidx.compose.ui.text.TextStyle(fontFamily=BrasilBody,fontSize=14.sp),
    titleLarge=androidx.compose.ui.text.TextStyle(fontFamily=BrasilHeading,fontSize=24.sp,fontWeight=FontWeight.SemiBold),
    titleMedium=androidx.compose.ui.text.TextStyle(fontFamily=BrasilHeading,fontSize=19.sp,fontWeight=FontWeight.SemiBold),
    headlineLarge=androidx.compose.ui.text.TextStyle(fontFamily=BrasilHeading,fontSize=32.sp,lineHeight=37.sp,fontWeight=FontWeight.SemiBold)
)
fun brasilColor(key:String, light:Boolean=false): Color = when(key) {
    "background" -> if(light) Color(0xFFF2F6F8) else Color(0xFF101C25)
    "dialogBg" -> if(light) Color.White else Color(0xFF1B2C38)
    "text" -> if(light) Color(0xFF233D4A) else Color(0xFFDCE8EF)
    "info", "enabled", "success" -> if(light) Color(0xFF246B5D) else Color(0xFF8DD9BE)
    "warning" -> if(light) Color(0xFF8F5A0A) else Color(0xFFF0C27B)
    "error" -> if(light) Color(0xFFA83936) else Color(0xFFF19B92)
    "priority", "regexFlags" -> if(light) Color(0xFF506C86) else Color(0xFFB3CADD)
    else -> if(light) Color(0xFF6C7B85) else Color(0xFF92A6B4)
}
fun applyBrasilTheme(light:Boolean) { G.palette.allColors.forEach { it.update(brasilColor(it.key,light)) } }

@Composable fun rememberSettingsRevision():Int {
    val ctx=LocalContext.current
    val prefs=remember { spf.SharedPref(ctx).prefs }
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(prefs) {
        val listener=SharedPreferences.OnSharedPreferenceChangeListener { _,_-> revision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return revision
}
fun navigateBrasil(route:String) {
    val vm=G.bottomBarVM
    val previous=vm.tabItems.firstOrNull { it.isSelected.value }?.route
    if(previous==route) return
    previous?.let(vm.onTabLeave)
    vm.tabItems.forEach { it.isSelected.value=it.route==route }
    vm.onTabSelected(route)
}
@Composable fun BrasilPageHeader() {
    val C=G.palette
    val route=G.bottomBarVM.tabItems.firstOrNull { it.isSelected.value }?.route
    val landscape=LocalConfiguration.current.screenHeightDp < 400
    val title=when(route) { Def.CALL_TAB_ROUTE->"Chamadas";Def.SMS_TAB_ROUTE->"Mensagens";Def.SETTING_TAB_ROUTE->"Ajustes";else->"Sua proteção" }
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=if(landscape) 6.dp else 14.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            if(!landscape) Text("SPAMBLOCKER  /  BRASIL",fontSize=11.sp,letterSpacing=1.7.sp,color=C.textGrey.copy(alpha=.75f),fontWeight=FontWeight.SemiBold)
            Text(title,fontFamily=BrasilHeading,fontSize=if(landscape) 22.sp else 27.sp,fontWeight=FontWeight.SemiBold,color=C.textGrey)
        }
        Icon(painterResource(R.drawable.ic_brasil_shield),contentDescription="SpamBlocker Brasil",tint=C.teal200,modifier=Modifier.size(30.dp))
    }
}
@Composable fun BrasilBottomBar() {
    val C=G.palette
    val compact=LocalConfiguration.current.screenWidthDp<360 || LocalDensity.current.fontScale>1.2f
    Surface(color=C.dialogBg,tonalElevation=0.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=8.dp)) {
            G.bottomBarVM.tabItems.forEach { tab->
                val selected=tab.isSelected.value
                Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).testTag("nav-${tab.route}").semantics { this.selected=selected;role=Role.Tab;contentDescription=tab.label }.clickable { navigateBrasil(tab.route) }.padding(vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Box(Modifier.background(if(selected) C.teal200.copy(alpha=.14f) else Color.Transparent,RoundedCornerShape(16.dp)).padding(horizontal=18.dp,vertical=5.dp)) {
                        BadgedBox(badge={tab.badgeText()?.let { Badge(containerColor=C.error,contentColor=C.background) { Text(it,fontSize=11.sp) } }}) {
                            Icon(painterResource(tab.icon),null,Modifier.size(22.dp),tint=if(selected) C.teal200 else C.textGrey.copy(alpha=.75f))
                        }
                    }
                    Text(if(compact && tab.route==Def.SMS_TAB_ROUTE) "SMS" else tab.label,maxLines=1,overflow=TextOverflow.Ellipsis,fontFamily=BrasilBody,fontSize=12.sp,fontWeight=if(selected) FontWeight.SemiBold else FontWeight.Normal,color=if(selected) C.teal200 else C.textGrey.copy(alpha=.75f))
                }
            }
        }
    }
}
@Composable fun BrasilSection(title:String?, collapsed:MutableState<Boolean>?, onToggle:((Boolean)->Unit)?, summary:(@Composable ()->Unit)?, content:@Composable ()->Unit) {
    val C=G.palette
    Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=7.dp).background(C.dialogBg,RoundedCornerShape(20.dp))) {
        if(title!=null) Row(Modifier.fillMaxWidth().heightIn(min=52.dp).then(if(collapsed!=null) Modifier.clickable { collapsed.value=!collapsed.value;onToggle?.invoke(collapsed.value) } else Modifier).padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
            Text(title,Modifier.weight(1f),fontFamily=BrasilHeading,fontWeight=FontWeight.SemiBold,fontSize=17.sp,color=C.textGrey)
            if(collapsed!=null) Text(if(collapsed.value) "+" else "−",fontSize=23.sp,color=C.teal200)
        }
        Column(Modifier.padding(start=16.dp,end=16.dp,bottom=12.dp)) { if(collapsed?.value==true) summary?.invoke() else content() }
    }
}
@Composable fun BrasilEmptyHistory(forSms:Boolean) {
    val C=G.palette
    val compact=LocalConfiguration.current.screenHeightDp<700 || LocalDensity.current.fontScale>1.2f
    Column(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=if(compact) 16.dp else 48.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Icon(painterResource(if(forSms) R.drawable.ic_sms else R.drawable.ic_call),null,Modifier.size(40.dp),tint=C.teal200)
        Spacer(Modifier.height(20.dp))
        Text(if(forSms) "Nenhuma mensagem por aqui" else "Nenhuma chamada por aqui",fontFamily=BrasilHeading,fontSize=if(compact) 18.sp else 21.sp,color=C.textGrey)
        Text("O motivo de cada decisão aparece aqui. Confira os filtros em Exibição.",Modifier.padding(top=10.dp),color=C.textGrey.copy(alpha=.8f),style=MaterialTheme.typography.bodyMedium)
    }
}

@Composable fun BrasilNavigationRail() {
    val C=G.palette
    Surface(color=C.dialogBg,modifier=Modifier.width(76.dp).fillMaxHeight()) {
        Column(Modifier.padding(vertical=4.dp),verticalArrangement=Arrangement.SpaceEvenly,horizontalAlignment=Alignment.CenterHorizontally) {
            G.bottomBarVM.tabItems.forEach { tab ->
                val selected=tab.isSelected.value
                Column(Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("nav-${tab.route}").semantics { this.selected=selected;role=Role.Tab;contentDescription=tab.label }.clickable { navigateBrasil(tab.route) }.padding(vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Icon(painterResource(tab.icon),null,Modifier.background(if(selected) C.teal200.copy(alpha=.15f) else Color.Transparent,RoundedCornerShape(12.dp)).padding(horizontal=12.dp,vertical=3.dp).size(22.dp),tint=if(selected) C.teal200 else C.textGrey)
                    Text(if(tab.route==Def.SMS_TAB_ROUTE) "SMS" else tab.label,fontSize=10.sp,maxLines=1,color=if(selected) C.teal200 else C.textGrey)
                }
            }
        }
    }
}
