package com.billionaire.empire.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.billionaire.empire.ui.components.*
import com.billionaire.empire.ui.theme.*
import com.billionaire.empire.utils.*
import com.billionaire.empire.viewmodel.AppViewModel

@Composable
fun DashboardScreen(vm: AppViewModel, ctx: Context) {
    val haptic = LocalHapticFeedback.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {

            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
                StatCard("ДОХОД/СЕК",  vm.displayPassive.fmtRate(), Green,     Modifier.weight(1f))
                StatCard("ПОРТФЕЛЬ",   vm.finances.total_capital.fmtMoney(), Gold, Modifier.weight(1f))
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
                StatCard("ЗАРАБОТАНО", vm.finances.total_earned.fmtMoney(), TextPrimary, Modifier.weight(1f))
                StatCard("РЕПУТАЦИЯ",  "${vm.profile.reputation}/100 · ${repTierName(vm.profile.reputation)}",
                    repColor(vm.profile.reputation), Modifier.weight(1f))
            }

            // Clicker
            SectionHeader("👆","КЛИКЕР")
            ClickerCard(vm,ctx,haptic)

            // Businesses
            val myPoints = vm.points
            if(myPoints.isNotEmpty()) {
                SectionHeader("🏢","МОИ БИЗНЕСЫ")
                myPoints.take(3).forEach{p->
                    Row(Modifier.fillMaxWidth()
                        .background(BgCard,RoundedCornerShape(12.dp))
                        .border(1.dp,Border,RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalAlignment=Alignment.CenterVertically) {
                        val tmpl = vm.templates.find{t->t.template_id==p.template_id}
                        Text("${tmpl?.icon ?: "🏪"}",fontSize=24.sp)  // fixed
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.custom_name,color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=14.sp)
                            Text("LV ${p.level} · ${tmpl?.name_ru ?: ""}",color=TextTert,fontSize=11.sp)
                        }
                        Column(horizontalAlignment=Alignment.End) {
                            Text(p.current_profit.fmtRate(),color=Green,fontSize=12.sp,fontWeight=FontWeight.Bold)
                            val netMod = vm.networks.find{n->n.network_id==p.network_id}?.profit_multiplier
                            if(netMod!=null) Text("Сеть ×${"%.1f".format(netMod)}",color=Gold,fontSize=10.sp)
                        }
                    }
                }
                if(myPoints.size>3) Text("+ещё ${myPoints.size-3} точек",color=TextTert,fontSize=11.sp,modifier=Modifier.padding(start=4.dp))
            }

            // News
            if(vm.toastMsg==null && vm.profile.reputation<80) {
                SectionHeader("⚠️","ВНИМАНИЕ")
                GameCard(Modifier.fillMaxWidth()) {
                    Text("Ваша репутация ${vm.profile.reputation}/100 (${repTierName(vm.profile.reputation)})",
                        color=repColor(vm.profile.reputation),fontWeight=FontWeight.SemiBold,fontSize=13.sp)
                    Text("Текущий множитель дохода: ×${"%.2f".format(repMultiplier(vm.profile.reputation))}",
                        color=TextSec,fontSize=12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Пожертвуйте или улучшите бизнесы для роста репутации",color=TextTert,fontSize=11.sp)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ClickerCard(vm: AppViewModel, ctx: Context, haptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    var pressed by remember{mutableStateOf(false)}
    val scale by animateFloatAsState(if(pressed)0.88f else 1f,spring(0.4f,600f),label="s")

    Column(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(16.dp))
        .border(1.dp,Border,RoundedCornerShape(16.dp)).padding(16.dp),
        horizontalAlignment=Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            Column { Text("ЗА КЛИК",color=TextTert,fontSize=9.sp,letterSpacing=1.sp,fontWeight=FontWeight.Bold)
                    Text("+$1",color=Gold,fontSize=16.sp,fontWeight=FontWeight.Bold) }
            Column(horizontalAlignment=Alignment.End) {
                Text("КЛИКАМИ ВСЕГО",color=TextTert,fontSize=9.sp,letterSpacing=1.sp,fontWeight=FontWeight.Bold)
                Text(vm.profile.clicker_total.fmtMoney(),color=Green,fontSize=13.sp,fontWeight=FontWeight.Bold) }
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.size(110.dp).scale(scale)
            .background(Brush.radialGradient(listOf(GoldLight.copy(.25f),Gold.copy(.1f),GoldGlow)),RoundedCornerShape(55.dp))
            .border(2.dp,Gold.copy(.6f),RoundedCornerShape(55.dp))
            .clickable{pressed=true;haptic.performHapticFeedback(HapticFeedbackType.LongPress);vm.onTap(ctx)},
            contentAlignment=Alignment.Center) {
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                Text("💰",fontSize=36.sp)
                Text("ТАП",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
            }
        }
        LaunchedEffect(pressed){if(pressed){kotlinx.coroutines.delay(120);pressed=false}}
    }
}
