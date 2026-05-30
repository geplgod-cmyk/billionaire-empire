package com.billionaire.empire.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.billionaire.empire.ui.components.ToastOverlay
import com.billionaire.empire.ui.theme.*
import com.billionaire.empire.utils.fmtMoney
import com.billionaire.empire.viewmodel.AppViewModel

@Composable
fun MainScreen(vm: AppViewModel, ctx: Context, onLogout: ()->Unit) {
    var tab by remember{mutableStateOf(0)}
    val tabs = listOf("🏠","🏢","📈","💎","👤")
    val labels = listOf("Дашборд","Бизнес","Инвест","Luxury","Профиль")

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize()) {
            // TopBar
            Box(Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(BgCard,BgDeep)))
                .padding(horizontal=20.dp,vertical=14.dp)) {
                Column {
                    Text("ЧИСТЫЙ КАПИТАЛ",color=TextTert,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                    Text(vm.displayBalance.fmtMoney(),
                        style=MaterialTheme.typography.headlineMedium.copy(
                            fontWeight=FontWeight.Bold,
                            brush=Brush.horizontalGradient(listOf(GoldLight,Gold))))
                }
                RepPill(vm.profile.reputation, Modifier.align(Alignment.CenterEnd))
            }

            // Content
            Box(Modifier.weight(1f)) {
                when(tab) {
                    0 -> DashboardScreen(vm,ctx)
                    1 -> BusinessScreen(vm,ctx)
                    2 -> InvestScreen(vm,ctx)
                    3 -> LifestyleScreen(vm,ctx)
                    4 -> ProfileScreen(vm,ctx,onLogout)
                }
            }

            // BottomNav
            Row(Modifier.fillMaxWidth().background(BgCard)
                .border(BorderStroke(1.dp,Border),RoundedCornerShape(0.dp))
                .padding(vertical=4.dp),
                horizontalArrangement=Arrangement.SpaceEvenly) {
                tabs.forEachIndexed{i,icon->
                    Column(Modifier.clickable{tab=i}.padding(horizontal=12.dp,vertical=8.dp),
                        horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(icon,fontSize=22.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(labels[i],color=if(tab==i)Gold else TextTert,
                            fontSize=9.sp,fontWeight=if(tab==i)FontWeight.Bold else FontWeight.Normal)
                        if(tab==i) Box(Modifier.size(4.dp).background(Gold,RoundedCornerShape(2.dp)))
                    }
                }
            }
        }
        Box(Modifier.align(Alignment.TopCenter)) {
            ToastOverlay(vm.toastMsg, onDismiss=vm::clearToast)
        }
    }
}

@Composable
fun RepPill(rep: Int, modifier: Modifier=Modifier) {
    val (bg,tc) = when{rep>=81->Gold.copy(.2f) to Gold;rep>=61->Blue.copy(.2f) to Blue;
        rep>=41->Gold.copy(.1f) to Gold;rep>=21->androidx.compose.ui.graphics.Color(0x30FF8C00) to androidx.compose.ui.graphics.Color(0xFFFF8C00);
        else->Red.copy(.2f) to Red}
    Box(modifier.background(bg,RoundedCornerShape(10.dp))
        .border(1.dp,tc.copy(.4f),RoundedCornerShape(10.dp))
        .padding(horizontal=10.dp,vertical=4.dp)) {
        Text("🎖 $rep",color=tc,fontSize=12.sp,fontWeight=FontWeight.Bold)
    }
}
