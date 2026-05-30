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
import androidx.compose.ui.graphics.Color
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
    var tab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        // Sub tabs
        Row(
            Modifier.fillMaxWidth().background(BgCard).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("📊 Дашборд", "📰 Новости", "❤️ Донат").forEachIndexed { i, lbl ->
                Box(
                    Modifier.weight(1f)
                        .background(if (tab == i) GoldGlow else BgSurface2, RoundedCornerShape(8.dp))
                        .border(1.dp, if (tab == i) Gold else Border, RoundedCornerShape(8.dp))
                        .clickable { tab = i }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(lbl.split(" ").last(), color = if (tab == i) Gold else TextTert,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        when (tab) {
            0 -> MainDashTab(vm, ctx, haptic)
            1 -> NewsTab(vm)
            2 -> CharityTab(vm, ctx)
        }
    }
}

@Composable
fun MainDashTab(vm: AppViewModel, ctx: Context,
                haptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    var clickTotal by remember { mutableStateOf(vm.profile.clicker_total) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Stats
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("ДОХОД/СЕК", vm.displayPassive.fmtRate(), Green, Modifier.weight(1f))
                StatCard("БАЛАНС", vm.displayBalance.fmtMoney(), Gold, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("ЗАРАБОТАНО", vm.finances.total_earned.fmtMoney(), TextPrimary, Modifier.weight(1f))
                StatCard("РЕПУТАЦИЯ", "${vm.profile.reputation}/100", repColor(vm.profile.reputation), Modifier.weight(1f))
            }

            // Clicker
            SectionHeader("👆", "КЛИКЕР")
            ClickerCard(vm, ctx, haptic, clickTotal) { clickTotal += 1 }

            // My businesses
            val myPoints = vm.points
            if (myPoints.isNotEmpty()) {
                SectionHeader("🏢", "МОИ БИЗНЕСЫ")
                myPoints.take(4).forEach { p ->
                    val tmpl = vm.templates.find { it.template_id == p.template_id }
                    Row(
                        Modifier.fillMaxWidth()
                            .background(BgCard, RoundedCornerShape(12.dp))
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${tmpl?.icon ?: "🏪"}", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.custom_name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${tmpl?.name_ru ?: ""} · LV ${p.level}", color = TextTert, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(p.current_profit.fmtRate(), color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val net = vm.networks.find { n -> n.network_id == p.network_id }
                            if (net != null) Text("Сеть ×${"%.1f".format(net.profit_multiplier)}", color = Gold, fontSize = 9.sp)
                        }
                    }
                }
                if (myPoints.size > 4) Text("+ ещё ${myPoints.size - 4} бизнесов", color = TextTert, fontSize = 11.sp)
            } else {
                GameCard(Modifier.fillMaxWidth()) {
                    Text("🏪 Нет бизнесов", color = TextSec, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Купите во вкладке Бизнес", color = TextTert, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun NewsTab(vm: AppViewModel) {
    val news = listOf(
        "📈 Рынок акций растёт" to "pos",
        "🪙 Биткоин обновил максимум" to "pos",
        "⚠️ Регуляторы ужесточают правила" to "neg",
        "🌱 ESG-инвестиции набирают популярность" to "pos",
        "💼 Крупные сделки M&A на рынке" to "neu",
        "🏦 Центробанк повысил ставку" to "neg",
        "🚀 Технологический сектор на подъёме" to "pos",
        "🛢 Нефть дорожает на фоне кризиса" to "neu"
    )

    LazyColumnNews(news)
}

@Composable
fun LazyColumnNews(news: List<Pair<String, String>>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("📰", "НОВОСТИ РЫНКА")
            news.forEach { (text, type) ->
                val (bg, border) = when (type) {
                    "pos" -> Green.copy(.08f) to Green.copy(.3f)
                    "neg" -> Red.copy(.08f)   to Red.copy(.3f)
                    else  -> BgCard          to Border
                }
                Row(
                    Modifier.fillMaxWidth()
                        .background(bg, RoundedCornerShape(10.dp))
                        .border(1.dp, border, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text, color = TextPrimary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun CharityTab(vm: AppViewModel, ctx: Context) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("❤️", "БЛАГОТВОРИТЕЛЬНОСТЬ")
            GameCard(Modifier.fillMaxWidth()) {
                Text("Жертвуйте — получайте репутацию", color = TextSec, fontSize = 13.sp)
                Text("Пожертвовано всего: ${vm.profile.charity_total.fmtMoney()}", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            listOf(
                Triple("🏫 Школа",              1_000.0,     2),
                Triple("🏥 Больница",            5_000.0,     5),
                Triple("🌍 Помощь стране",       25_000.0,    15),
                Triple("🌱 Экофонд",             100_000.0,   40),
                Triple("🏆 Глобальный фонд",     1_000_000.0, 100)
            ).forEach { (label, amt, rep) ->
                val can = vm.displayBalance >= amt
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (can) Green.copy(.07f) else BgSurface2, RoundedCornerShape(12.dp))
                        .border(1.dp, if (can) Green.copy(.35f) else Border, RoundedCornerShape(12.dp))
                        .clickable(enabled = can) { vm.donate(ctx, amt, rep) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = if (can) TextPrimary else TextTert, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(amt.fmtMoney(), color = if (can) Green else Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Badge("+$rep реп", if (can) Green else TextTert)
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ClickerCard(vm: AppViewModel, ctx: Context,
                haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
                clickTotal: Double, onTap: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, spring(0.4f, 600f), label = "s")

    Column(
        Modifier.fillMaxWidth()
            .background(BgCard, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ЗА КЛИК", color = TextTert, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text("+$1", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("КЛИКАМИ ЗАРАБОТАНО", color = TextTert, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text(vm.profile.clicker_total.fmtMoney(), color = Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier.size(110.dp).scale(scale)
                .background(
                    Brush.radialGradient(listOf(GoldLight.copy(.25f), Gold.copy(.1f), GoldGlow)),
                    RoundedCornerShape(55.dp)
                )
                .border(2.dp, Gold.copy(.6f), RoundedCornerShape(55.dp))
                .clickable {
                    pressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.onTap(ctx)
                    onTap()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💰", fontSize = 36.sp)
                Text("ТАП", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
        LaunchedEffect(pressed) { if (pressed) { kotlinx.coroutines.delay(120); pressed = false } }
        Spacer(Modifier.height(8.dp))
        Text("Нажимай и зарабатывай!", color = TextTert, fontSize = 11.sp)
    }
}
