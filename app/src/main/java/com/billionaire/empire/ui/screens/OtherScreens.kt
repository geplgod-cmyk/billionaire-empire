package com.billionaire.empire.ui.screens

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.billionaire.empire.network.ApiClient
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.billionaire.empire.ui.components.*
import com.billionaire.empire.ui.theme.*
import com.billionaire.empire.utils.*
import com.billionaire.empire.viewmodel.AppViewModel
import com.billionaire.empire.utils.SecureStorage

// ═══════════════════════════════════════════════════════
// INVEST SCREEN
// ═══════════════════════════════════════════════════════
@Composable
fun InvestScreen(vm: AppViewModel, ctx: Context) {
    var tab by remember{mutableStateOf(0)}
    Column(Modifier.fillMaxSize()){
        Row(Modifier.fillMaxWidth().background(BgCard).padding(12.dp),
            horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf("📈 Акции","🪙 Крипта","📊 Портфель").forEachIndexed{i,lbl->
                Box(Modifier.weight(1f)
                    .background(if(tab==i)GoldGlow else BgSurface2,RoundedCornerShape(8.dp))
                    .border(1.dp,if(tab==i)Gold else Border,RoundedCornerShape(8.dp))
                    .clickable{tab=i}.padding(vertical=8.dp),contentAlignment=Alignment.Center){
                    Text(lbl.split(" ").last(),color=if(tab==i)Gold else TextTert,
                        fontSize=10.sp,fontWeight=FontWeight.Bold)
                }
            }
        }
        when(tab){
            0 -> StocksTab(vm,ctx)
            1 -> CryptosTab(vm,ctx)
            2 -> PortfolioTab(vm)
        }
    }
}

val STOCK_INFO = mapOf(
    "TVI" to Pair("💻","TechVision Inc"),  "GPC" to Pair("⚡","GreenPower Corp"),
    "MCL" to Pair("🔬","MediCore Labs"),   "NAI" to Pair("🤖","NeuralNet AI"),
    "ADC" to Pair("🚗","AutoDrive Co"),    "NVX" to Pair("🏦","NovaxBank"),
    "SPC" to Pair("🚀","StarPath Corp"),   "OXL" to Pair("🛢","OmegaOil Ltd"),
    "MXM" to Pair("📺","MaxMedia Group"),  "CYB" to Pair("🛡","CyberShield Inc")
)
val CRYPTO_INFO = mapOf(
    "BTC" to Pair("🔥","BitCore"),   "MOON" to Pair("🚀","MoonDog"),
    "QX"  to Pair("⚡","QuantumX"),  "NOVA" to Pair("🔗","NovaChain"),
    "ETH2" to Pair("💎","Etherion"), "SOL2" to Pair("☀","SolarCoin"),
    "VT"  to Pair("🌑","VoidToken"), "GLX"  to Pair("🌌","GalaxyCash")
)

@Composable
fun StocksTab(vm: AppViewModel, ctx: Context) {
    var selected by remember{mutableStateOf<String?>(null)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)){
        items(STOCK_INFO.entries.toList(),key={it.key}){(sym,info)->
            val price = vm.stockPrices[sym] ?: 100.0
            val pos = vm.stocks.find{it.symbol==sym}
            AssetCard(info.first,info.second,sym,price,pos?.quantity?.toDouble(),
                pos?.avg_buy_price,onClick={selected=sym})
        }
        item{Spacer(Modifier.height(80.dp))}
    }
    selected?.let{ symNullable ->
        val sym: String = symNullable
        val price = vm.stockPrices[sym] ?: 100.0
        val pos = vm.stocks.find{it.symbol==sym}
        TradeSheet(
            icon = STOCK_INFO[sym]?.first ?: "📈", name=STOCK_INFO[sym]?.second ?: "", symbol=sym,
            price=price, ownedQty=pos?.quantity?.toDouble() ?:0.0, avgBuy=pos?.avg_buy_price ?:0.0,
            cash=vm.displayBalance, isCrypto=false,
            onBuy25={vm.buyStock(ctx,sym,maxOf(1,(vm.displayBalance/price*0.25).toInt()))},
            onBuy50={vm.buyStock(ctx,sym,maxOf(1,(vm.displayBalance/price*0.5).toInt()))},
            onBuyAll={vm.buyStock(ctx,sym,(vm.displayBalance/price).toInt())},
            onSell25={vm.sellStock(ctx,sym,0.25)},
            onSell50={vm.sellStock(ctx,sym,0.5)},
            onSellAll={vm.sellStock(ctx,sym,1.0)},
            onDismiss={selected=null})
    }
}

@Composable
fun CryptosTab(vm: AppViewModel, ctx: Context) {
    var selected by remember{mutableStateOf<String?>(null)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)){
        items(CRYPTO_INFO.entries.toList(),key={it.key}){(sym,info)->
            val price = vm.cryptoPrices[sym] ?: 1.0
            val pos = vm.cryptos.find{it.symbol==sym}
            val priceStr = if(price<0.01)"$${"%.6f".format(price)}" else price.fmtMoney()
            AssetCard(info.first,info.second,sym,price,pos?.quantity,pos?.avg_buy_price,priceStr,onClick={selected=sym})
        }
        item{Spacer(Modifier.height(80.dp))}
    }
    selected?.let{ symNullable ->
        val sym: String = symNullable
        val price = vm.cryptoPrices[sym] ?: 1.0
        val pos = vm.cryptos.find{it.symbol==sym}
        TradeSheet(
            icon=CRYPTO_INFO[sym]?.first ?: "🪙", name=CRYPTO_INFO[sym]?.second ?: "", symbol=sym,
            price=price, ownedQty=pos?.quantity ?:0.0, avgBuy=pos?.avg_buy_price ?:0.0,
            cash=vm.displayBalance, isCrypto=true,
            onBuy25={vm.buyCrypto(ctx,sym,vm.displayBalance*0.25)},
            onBuy50={vm.buyCrypto(ctx,sym,vm.displayBalance*0.5)},
            onBuyAll={vm.buyCrypto(ctx,sym,vm.displayBalance)},
            onSell25={vm.sellCrypto(ctx,sym,0.25)},
            onSell50={vm.sellCrypto(ctx,sym,0.5)},
            onSellAll={vm.sellCrypto(ctx,sym,1.0)},
            onDismiss={selected=null})
    }
}

@Composable
fun AssetCard(icon:String, name:String, sym:String, price:Double, owned:Double?, avgBuy:Double?,
              priceStr:String?=null, onClick:()->Unit) {
    val pStr = priceStr ?: price.fmtMoney()
    Row(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(14.dp))
        .border(1.dp,Border,RoundedCornerShape(14.dp)).clickable(onClick=onClick).padding(14.dp),
        verticalAlignment=Alignment.CenterVertically){
        Box(Modifier.size(48.dp).background(BgSurface2,RoundedCornerShape(12.dp))
            .border(1.dp,Border,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Text(icon,fontSize=22.sp)}
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)){
            Text(name,color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=14.sp)
            Text(sym,color=TextTert,fontSize=11.sp)
            if(owned!=null&&owned>0) {
                val profit = if(avgBuy!=null&&avgBuy>0)((price-avgBuy)/avgBuy*100) else 0.0
                Text("Позиция · ${profit.fmtPct()}",color=if(profit>=0)Green else Red,fontSize=10.sp)
            }
        }
        Text(pStr,color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=14.sp)
    }
}

@Composable
fun PortfolioTab(vm: AppViewModel) {
    val total = vm.stocks.sumOf{it.quantity*(vm.stockPrices[it.symbol]?:it.avg_buy_price)} +
                vm.cryptos.sumOf{it.quantity*(vm.cryptoPrices[it.symbol]?:it.avg_buy_price)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)){
        item{GameCard(Modifier.fillMaxWidth()){
            Text("СТОИМОСТЬ ПОРТФЕЛЯ",color=TextTert,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
            Text(total.fmtMoney(),color=Gold,fontSize=28.sp,fontWeight=FontWeight.Bold)
        }}
        if(vm.stocks.any{it.quantity>0}){
            item{SectionHeader("📈","АКЦИИ")}
            items(vm.stocks.filter{it.quantity>0},key={it.symbol}){s->
                val price=vm.stockPrices[s.symbol]?:s.avg_buy_price
                val profit=if(s.avg_buy_price>0)((price-s.avg_buy_price)/s.avg_buy_price*100) else 0.0
                Row(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(12.dp))
                    .border(1.dp,Border,RoundedCornerShape(12.dp)).padding(14.dp),
                    verticalAlignment=Alignment.CenterVertically){
                    Text(STOCK_INFO[s.symbol]?.first.orEmpty().ifEmpty { "📈" },fontSize=22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)){
                        Text(s.symbol,color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=13.sp)
                        Text("${s.quantity} акций",color=TextTert,fontSize=11.sp)
                    }
                    Column(horizontalAlignment=Alignment.End){
                        Text((s.quantity*price).fmtMoney(),color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=13.sp)
                        Text(profit.fmtPct(),color=if(profit>=0)Green else Red,fontSize=12.sp)
                    }
                }
            }
        }
        if(vm.cryptos.any{it.quantity>0.0001}){
            item{SectionHeader("🪙","КРИПТА")}
            items(vm.cryptos.filter{it.quantity>0.0001},key={it.symbol}){cr->
                val price=vm.cryptoPrices[cr.symbol]?:cr.avg_buy_price
                val profit=if(cr.avg_buy_price>0)((price-cr.avg_buy_price)/cr.avg_buy_price*100) else 0.0
                Row(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(12.dp))
                    .border(1.dp,Border,RoundedCornerShape(12.dp)).padding(14.dp),
                    verticalAlignment=Alignment.CenterVertically){
                    Text(CRYPTO_INFO[cr.symbol]?.first.orEmpty().ifEmpty { "🪙" },fontSize=22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)){
                        Text(cr.symbol,color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=13.sp)
                        Text("${"%.4f".format(cr.quantity)} ${cr.symbol}",color=TextTert,fontSize=11.sp)
                    }
                    Column(horizontalAlignment=Alignment.End){
                        Text((cr.quantity*price).fmtMoney(),color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=13.sp)
                        Text(profit.fmtPct(),color=if(profit>=0)Green else Red,fontSize=12.sp)
                    }
                }
            }
        }
        item{Spacer(Modifier.height(80.dp))}
    }
}

@Composable
fun TradeSheet(icon:String,name:String,symbol:String,price:Double,ownedQty:Double,avgBuy:Double,
               cash:Double,isCrypto:Boolean,onBuy25:()->Unit,onBuy50:()->Unit,onBuyAll:()->Unit,
               onSell25:()->Unit,onSell50:()->Unit,onSellAll:()->Unit,onDismiss:()->Unit) {
    val profitPct = if(avgBuy>0)((price-avgBuy)/avgBuy*100) else 0.0
    val priceStr  = if(price<0.01)"$${"%.6f".format(price)}" else price.fmtMoney()
    Box(Modifier.fillMaxSize().background(BgDeep.copy(.92f)).clickable(onClick=onDismiss)){
        Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            .background(BgCard,RoundedCornerShape(topStart=20.dp,topEnd=20.dp))
            .clickable(enabled=false){}.padding(20.dp)){
            Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){
                Box(Modifier.size(36.dp,4.dp).background(Border2,RoundedCornerShape(2.dp)))}
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment=Alignment.CenterVertically){
                Text(icon,fontSize=32.sp);Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)){Text(name,color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(symbol,color=TextTert,fontSize=12.sp)}
                Text(priceStr,color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=16.sp)
            }
            Spacer(Modifier.height(14.dp))
            if(ownedQty>0.0001){
                val qty = if(isCrypto)"${"%.4f".format(ownedQty)} $symbol" else "${ownedQty.toInt()} акций"
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    StatCard("ПОЗИЦИЯ",qty,Gold,Modifier.weight(1f))
                    StatCard("ПРИБЫЛЬ",profitPct.fmtPct(),if(profitPct>=0)Green else Red,Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Text("ПРОДАТЬ",color=TextTert,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    RedButton("25%",onClick=onSell25,modifier=Modifier.weight(1f),small=true)
                    RedButton("50%",onClick=onSell50,modifier=Modifier.weight(1f),small=true)
                    RedButton("Всё",onClick=onSellAll,modifier=Modifier.weight(1f),small=true)
                }
                Spacer(Modifier.height(12.dp))
            }
            Text("КУПИТЬ",color=TextTert,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                GoldButton("25%",onClick=onBuy25,modifier=Modifier.weight(1f),small=true,enabled=cash>=price)
                GoldButton("50%",onClick=onBuy50,modifier=Modifier.weight(1f),small=true,enabled=cash>=price)
                GoldButton("Всё",onClick=onBuyAll,modifier=Modifier.weight(1f),small=true,enabled=cash>=price)
            }
            Spacer(Modifier.height(8.dp))
            Text("Доступно: ${cash.fmtMoney()}",color=TextTert,fontSize=11.sp)
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════
// LIFESTYLE SCREEN
// ═══════════════════════════════════════════════════════
val LUXURY_ITEMS = listOf(
    listOf("apt" to ("🏢" to Triple("City Apartment","Первая квартира бизнесмена",350_000.0)),
           "villa" to ("🏡" to Triple("Sunset Villa","Роскошная вилла с бассейном",8_000_000.0)),
           "pent" to ("🏙" to Triple("Sky Penthouse","Пентхаус в мегаполисе",45_000_000.0))),
    listOf("falcongt" to ("🚘" to Triple("Falcon GT","Спортивный бизнес-класс",120_000.0)),
           "inferno" to ("🏎" to Triple("Inferno X","Гиперкар",1_800_000.0)),
           "phantom" to ("👑" to Triple("Phantom Emperor","Ультраредкий luxury",12_000_000.0))),
    listOf("pearl" to ("🛥" to Triple("Ocean Pearl","Luxury яхта",12_000_000.0)),
           "neptune" to ("🚢" to Triple("Royal Neptune","Мегаяхта с казино",65_000_000.0))),
    listOf("s1" to ("✈" to Triple("Skyline S1","Лёгкий джет",2_500_000.0)),
           "aether" to ("🛩" to Triple("Aether X5","Бизнес-джет",8_000_000.0))),
    listOf("silver" to ("⌚" to Triple("Silver Chronos","Luxury часы",15_000.0)),
           "diamond" to ("💎" to Triple("Diamond Royal","Часы с бриллиантами",250_000.0)),
           "emperor" to ("👑" to Triple("Emperor Gold","Легендарные часы",3_000_000.0)))
)

@Composable
fun LifestyleScreen(vm: AppViewModel, ctx: Context) {
    val cats = listOf("🏠 Недвижимость","🚗 Авто","🛥 Яхты","✈ Самолёты","⌚ Часы")
    var cat by remember{mutableStateOf(0)}
    Column(Modifier.fillMaxSize()){
        LazyRow(Modifier.fillMaxWidth().background(BgCard),contentPadding=PaddingValues(12.dp),
            horizontalArrangement=Arrangement.spacedBy(6.dp)){
            itemsIndexed(cats){i,lbl->
                Box(Modifier.background(if(cat==i)GoldGlow else BgSurface2,RoundedCornerShape(10.dp))
                    .border(1.dp,if(cat==i)Gold else Border,RoundedCornerShape(10.dp))
                    .clickable{cat=i}.padding(horizontal=14.dp,vertical=9.dp)){
                    Text(lbl,color=if(cat==i)Gold else TextTert,fontSize=11.sp,fontWeight=FontWeight.SemiBold)}
            }
        }
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),
            verticalArrangement=Arrangement.spacedBy(10.dp)){
            val items = LUXURY_ITEMS.getOrElse(cat){emptyList()}
            items(items,key={it.first}){(id,data)->
                val (icon,info)=data;val(name,desc,price)=info
                val owned = vm.luxury.any{it.item_id==id}
                val canBuy = !owned && vm.displayBalance >= price
                Column(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(16.dp))
                    .border(1.dp,if(owned)Gold.copy(.5f) else Border,RoundedCornerShape(16.dp)).padding(16.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Box(Modifier.size(60.dp).background(if(owned)GoldGlow else BgSurface2,RoundedCornerShape(14.dp))
                            .border(1.dp,if(owned)Gold.copy(.6f) else Border,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){
                            Text(icon,fontSize=28.sp)}
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)){
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                                Text(name,color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=15.sp)
                                if(owned)Badge("✓ Куплено",Green)}
                            Text(desc,color=TextTert,fontSize=11.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Text(price.fmtMoney(),color=if(owned)TextTert else TextPrimary,
                            fontWeight=FontWeight.Bold,fontSize=16.sp,modifier=Modifier.weight(1f))
                        if(!owned)GoldButton(if(canBuy)"Купить" else "Мало $",
                            onClick={vm.buyLuxury(ctx,id,price)},enabled=canBuy,small=true)
                    }
                }
            }
            item{Spacer(Modifier.height(80.dp))}
        }
    }
}

// ═══════════════════════════════════════════════════════
// PROFILE SCREEN
// ═══════════════════════════════════════════════════════
@Composable
fun ProfileScreen(vm: AppViewModel, ctx: Context, onLogout: ()->Unit) {
    var showLogout by remember{mutableStateOf(false)}
    var showSettings by remember{mutableStateOf(false)}

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
        // Hero
        Box(Modifier.fillMaxWidth()
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(BgCard,BgDeep)))
            .padding(24.dp)){
            // Settings button
            Box(Modifier.align(Alignment.TopEnd).size(36.dp)
                .background(BgSurface2,RoundedCornerShape(18.dp))
                .border(1.dp,Border,RoundedCornerShape(18.dp))
                .clickable{showSettings=true},contentAlignment=Alignment.Center){
                Text("⚙️",fontSize=18.sp)}
            Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){
                Box(Modifier.size(80.dp).background(GoldGlow,RoundedCornerShape(40.dp))
                    .border(2.dp,Gold,RoundedCornerShape(40.dp)),contentAlignment=Alignment.Center){Text("👤",fontSize=36.sp)}
                Spacer(Modifier.height(12.dp))
                Text(vm.serverState?.player?.name ?:"Игрок",color=TextPrimary,fontSize=22.sp,fontWeight=FontWeight.Bold)
                Text(repTierName(vm.profile.reputation),color=Gold,fontSize=12.sp,letterSpacing=2.sp)
            }
        }

        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                StatCard("КАПИТАЛ",vm.displayBalance.fmtMoney(),Gold,Modifier.weight(1f))
                StatCard("ДОХОД/СЕК",vm.displayPassive.fmtRate(),Green,Modifier.weight(1f))
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                StatCard("ЗАРАБОТАНО",vm.finances.total_earned.fmtMoney(),TextPrimary,Modifier.weight(1f))
                StatCard("РЕПУТАЦИЯ","${vm.profile.reputation}/100",repColor(vm.profile.reputation),Modifier.weight(1f))
            }

            // Rep bar
            SectionHeader("🎖","РЕПУТАЦИЯ")
            GameCard(Modifier.fillMaxWidth()){
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                    Text("${vm.profile.reputation}/100",color=Gold,fontSize=16.sp,fontWeight=FontWeight.Bold)
                    Text(repTierName(vm.profile.reputation),color=TextSec,fontSize=12.sp)
                }
                Spacer(Modifier.height(8.dp))
                GoldProgressBar(vm.profile.reputation/100f,Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                val mult=repMultiplier(vm.profile.reputation)
                Text(if(mult>=1.0)"▲ Бонус к доходу: +${((mult-1)*100).toInt()}%"
                     else "▼ Штраф к доходу: ${((mult-1)*100).toInt()}%",
                    color=if(mult>=1.0)Green else Red,fontSize=13.sp,fontWeight=FontWeight.SemiBold)
            }

            // Charity
            SectionHeader("❤️","БЛАГОТВОРИТЕЛЬНОСТЬ")
            GameCard(Modifier.fillMaxWidth()){
                Text("Пожертвовано: ${vm.profile.charity_total.fmtMoney()}",color=Gold,fontSize=14.sp,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                listOf(Triple("🏫 Школа",1_000.0,2),Triple("🏥 Больница",5_000.0,5),
                       Triple("🌍 Помощь стране",25_000.0,15),Triple("🌱 Экофонд",100_000.0,40),
                       Triple("🏆 Глобальный фонд",1_000_000.0,100)).forEach{(lbl,amt,rep)->
                    val can=vm.displayBalance>=amt
                    Row(Modifier.fillMaxWidth().padding(vertical=3.dp)
                        .background(if(can)Green.copy(.07f) else BgSurface2,RoundedCornerShape(10.dp))
                        .border(1.dp,if(can)Green.copy(.35f) else Border,RoundedCornerShape(10.dp))
                        .clickable(enabled=can){vm.donate(ctx,amt,rep)}.padding(12.dp),
                        verticalAlignment=Alignment.CenterVertically){
                        Text(lbl,color=if(can)TextPrimary else TextTert,fontSize=13.sp,modifier=Modifier.weight(1f))
                        Text(amt.fmtMoney(),color=if(can)Green else Red,fontSize=11.sp,fontWeight=FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        Badge("+$rep реп",if(can)Green else TextTert)
                    }
                }
            }

            // Security info
            SectionHeader("🔐","БЕЗОПАСНОСТЬ")
            GameCard(Modifier.fillMaxWidth()){
                val lastLogin = SecureStorage.getLastLogin(ctx)
                val sessionStarted = SecureStorage.getSessionStarted(ctx)
                val username = SecureStorage.getUsername(ctx)
                listOf(
                    "Аккаунт" to (username ?:"—"),
                    "Последний вход" to if(lastLogin>0) java.text.SimpleDateFormat("dd.MM HH:mm",java.util.Locale.getDefault()).format(java.util.Date(lastLogin)) else "—",
                    "Сессия с" to if(sessionStarted>0) java.text.SimpleDateFormat("dd.MM HH:mm",java.util.Locale.getDefault()).format(java.util.Date(sessionStarted)) else "—",
                    "Версия API" to ApiClient.BASE
                ).forEach{(k,v)->
                    Row(Modifier.fillMaxWidth().padding(vertical=3.dp)){
                        Text(k,color=TextTert,fontSize=12.sp,modifier=Modifier.weight(1f))
                        Text(v,color=TextSec,fontSize=12.sp)
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if(showSettings) SettingsSheet(vm,ctx,onLogout={showLogout=true},onDismiss={showSettings=false})
    if(showLogout) ConfirmDialog("Выйти?","Прогресс сохранён на сервере.",
        "Выйти",onConfirm={vm.logout(ctx)},onDismiss={showLogout=false})
}

@Composable
fun SettingsSheet(vm: AppViewModel, ctx: Context, onLogout: ()->Unit, onDismiss: ()->Unit) {
    var lang  by remember{mutableStateOf(vm.settings.language)}
    var notif by remember{mutableStateOf(vm.settings.notifications_enabled)}

    Box(Modifier.fillMaxSize().background(BgDeep.copy(.92f)).clickable(onClick=onDismiss)){
        Column(Modifier.fillMaxWidth().fillMaxHeight(.85f).align(Alignment.BottomCenter)
            .background(BgCard,RoundedCornerShape(topStart=20.dp,topEnd=20.dp))
            .clickable(enabled=false){}.verticalScroll(rememberScrollState())){
            Box(Modifier.fillMaxWidth().padding(top=10.dp),contentAlignment=Alignment.Center){
                Box(Modifier.size(36.dp,4.dp).background(Border2,RoundedCornerShape(2.dp)))}
            Text("⚙️ Настройки",color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=18.sp,modifier=Modifier.padding(16.dp))

            Column(Modifier.padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                SectionHeader("🌍","ЯЗЫК")
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    listOf("ru" to "🇷🇺 Русский","en" to "🇺🇸 English").forEach{(code,lbl)->
                        Box(Modifier.weight(1f).background(if(lang==code)GoldGlow else BgSurface2,RoundedCornerShape(12.dp))
                            .border(1.dp,if(lang==code)Gold else Border,RoundedCornerShape(12.dp))
                            .clickable{lang=code;vm.updateSettings(ctx,language=code)}.padding(vertical=14.dp),
                            contentAlignment=Alignment.Center){
                            Text(lbl,color=if(lang==code)Gold else TextSec,fontWeight=if(lang==code)FontWeight.Bold else FontWeight.Normal,fontSize=13.sp)}
                    }
                }

                SectionHeader("🔔","УВЕДОМЛЕНИЯ")
                GameCard(Modifier.fillMaxWidth()){
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
                        Column(Modifier.weight(1f)){
                            Text("Push-уведомления",color=TextPrimary,fontSize=14.sp,fontWeight=FontWeight.SemiBold)
                            Text(if(notif)"Включены" else "Выключены",color=if(notif)Green else TextTert,fontSize=11.sp)
                        }
                        LuxuryToggle(notif){notif=it;vm.updateSettings(ctx,notif=it)}
                    }
                }

                SectionHeader("🔐","СИСТЕМА РЕПУТАЦИИ")
                GameCard(Modifier.fillMaxWidth()){
                    listOf(Triple("81–100","100% дохода",Green),Triple("61–80","90% дохода",Blue),
                           Triple("41–60","75% дохода",Gold),Triple("21–40","55% дохода",androidx.compose.ui.graphics.Color(0xFFFF8C00)),
                           Triple("0–20","35% дохода",Red)).forEach{(range,inc,color)->
                        Row(Modifier.fillMaxWidth().padding(vertical=3.dp)
                            .background(color.copy(.06f),RoundedCornerShape(8.dp))
                            .border(1.dp,color.copy(.2f),RoundedCornerShape(8.dp))
                            .padding(horizontal=12.dp,vertical=6.dp),
                            horizontalArrangement=Arrangement.SpaceBetween){
                            Text("Реп $range",color=color,fontWeight=FontWeight.Bold,fontSize=12.sp)
                            Text(inc,color=TextSec,fontSize=12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                RedButton("🚪 Выйти из аккаунта",onClick=onLogout,modifier=Modifier.fillMaxWidth())
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
