package com.billionaire.empire.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.billionaire.empire.data.models.BusinessTemplate
import com.billionaire.empire.data.models.BusinessPoint
import com.billionaire.empire.data.models.Network
import com.billionaire.empire.ui.components.*
import com.billionaire.empire.ui.theme.*
import com.billionaire.empire.utils.*
import com.billionaire.empire.viewmodel.AppViewModel

@Composable
fun BusinessScreen(vm: AppViewModel, ctx: Context) {
    var tab by remember{mutableStateOf(0)}
    var buyDialog   by remember{mutableStateOf<BusinessTemplate?>(null)}
    var detailPoint by remember{mutableStateOf<BusinessPoint?>(null)}
    var netDialog   by remember{mutableStateOf(false)}
    var netRenameDialog by remember{mutableStateOf<Network?>(null)}

    Column(Modifier.fillMaxSize()) {
        // Sub-tabs
        Row(Modifier.fillMaxWidth().background(BgCard).padding(12.dp),
            horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("🏪 Каталог","💼 Мои","🔗 Сети").forEachIndexed{i,lbl->
                Box(Modifier.weight(1f)
                    .background(if(tab==i)GoldGlow else BgSurface2,RoundedCornerShape(10.dp))
                    .border(1.dp,if(tab==i)Gold else Border,RoundedCornerShape(10.dp))
                    .clickable{tab=i}.padding(vertical=10.dp),contentAlignment=Alignment.Center) {
                    Text(lbl,color=if(tab==i)Gold else TextTert,fontSize=11.sp,fontWeight=FontWeight.Bold)
                }
            }
        }
        when(tab) {
            0 -> CatalogTab(vm,ctx){buyDialog=it}
            1 -> MyPointsTab(vm,ctx){detailPoint=it}
            2 -> NetworksTab(vm,ctx,{netDialog=true},{netRenameDialog=it})
        }
    }

    // Buy dialog
    buyDialog?.let{tmpl->
        var name by remember{mutableStateOf(tmpl.name_ru)}
        AlertDialog(onDismissRequest={buyDialog=null},containerColor=BgSurface,shape=RoundedCornerShape(20.dp),
            title={Text("${tmpl.icon} Открыть ${tmpl.name_ru}",color=TextPrimary,fontWeight=FontWeight.Bold)},
            text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                Text("Цена: ${tmpl.base_price.fmtMoney()}",color=TextSec,fontSize=13.sp)
                Text("Доход: ${tmpl.base_profit.fmtRate()}",color=Green,fontSize=13.sp)
                Divider(color=Border)
                Text("Название точки:",color=TextSec,fontSize=12.sp)
                AuthField(name,{name=it},"Название вашей точки")
            }},
            confirmButton={GoldButton("Открыть!",onClick={vm.buyBusiness(ctx,tmpl.template_id,name);buyDialog=null},enabled=vm.displayBalance>=tmpl.base_price)},
            dismissButton={OutlineButton("Отмена",onClick={buyDialog=null},small=true,color=TextSec)})
    }

    // Detail sheet
    detailPoint?.let{p->
        BusinessDetailSheet(p,vm,ctx,onDismiss={detailPoint=null})
    }

    // Create network dialog
    if(netDialog) {
        CreateNetworkDialog(vm,ctx,onDismiss={netDialog=false})
    }

    // Rename network dialog
    netRenameDialog?.let{net->
        var newName by remember{mutableStateOf(net.name)}
        AlertDialog(onDismissRequest={netRenameDialog=null},containerColor=BgSurface,shape=RoundedCornerShape(20.dp),
            title={Text("✏️ Переименовать сеть",color=TextPrimary,fontWeight=FontWeight.Bold)},
            text={AuthField(newName,{newName=it},"Название сети")},
            confirmButton={GoldButton("Сохранить",onClick={vm.renameNetwork(ctx,net.network_id,newName);netRenameDialog=null})},
            dismissButton={OutlineButton("Отмена",onClick={netRenameDialog=null},small=true,color=TextSec)})
    }
}

// ── Catalog ───────────────────────────────────────────────────────────────────
@Composable
fun CatalogTab(vm: AppViewModel, ctx: Context, onBuy: (BusinessTemplate)->Unit) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)) {
        val early = vm.templates.filter{it.category=="early"}
        val mid   = vm.templates.filter{it.category=="mid"}
        val late  = vm.templates.filter{it.category=="late"}
        item{SectionHeader("☕","НАЧАЛО ПУТИ")}
        items(early,key={it.template_id}){CatalogCard(it,vm.displayBalance,onBuy)}
        item{SectionHeader("🏨","СРЕДНИЙ БИЗНЕС")}
        items(mid,key={it.template_id}){CatalogCard(it,vm.displayBalance,onBuy)}
        item{SectionHeader("🚀","МИРОВАЯ ИМПЕРИЯ")}
        items(late,key={it.template_id}){CatalogCard(it,vm.displayBalance,onBuy)}
        item{Spacer(Modifier.height(80.dp))}
    }
}

@Composable
fun CatalogCard(tmpl: BusinessTemplate, cash: Double, onBuy: (BusinessTemplate)->Unit) {
    val canBuy = cash >= tmpl.base_price
    Row(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(14.dp))
        .border(1.dp,Border,RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(52.dp).background(BgSurface2,RoundedCornerShape(12.dp))
            .border(1.dp,Border,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){
            Text(tmpl.icon,fontSize=24.sp)}
        Column(Modifier.weight(1f)){
            Text(tmpl.name_ru,color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=14.sp)
            Text(tmpl.desc_ru,color=TextTert,fontSize=11.sp)
            Text(tmpl.base_price.fmtMoney(),color=Gold,fontSize=11.sp,fontWeight=FontWeight.SemiBold)
        }
        GoldButton(if(canBuy)"Купить" else "Мало $",onClick={onBuy(tmpl)},enabled=canBuy,small=true)
    }
}

// ── My Points ─────────────────────────────────────────────────────────────────
@Composable
fun MyPointsTab(vm: AppViewModel, ctx: Context, onDetail: (BusinessPoint)->Unit) {
    if(vm.points.isEmpty()) {
        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
            Column(horizontalAlignment=Alignment.CenterHorizontally){
                Text("🏪",fontSize=48.sp);Spacer(Modifier.height(12.dp))
                Text("Нет бизнесов",color=TextSec,fontSize=16.sp,fontWeight=FontWeight.SemiBold)
                Text("Купите в каталоге",color=TextTert,fontSize=13.sp)
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)) {
        val free    = vm.freePoints
        val inNets  = vm.points.filter{it.network_id!=null}
        if(free.isNotEmpty()){item{SectionHeader("📍","СВОБОДНЫЕ ТОЧКИ")}
            items(free,key={it.point_id}){PointCard(it,vm,onDetail)}}
        if(inNets.isNotEmpty()){item{SectionHeader("🔗","В СЕТЯХ")}
            items(inNets,key={it.point_id}){PointCard(it,vm,onDetail)}}
        item{Spacer(Modifier.height(80.dp))}
    }
}

@Composable
fun PointCard(p: BusinessPoint, vm: AppViewModel, onDetail: (BusinessPoint)->Unit) {
    val tmpl = vm.templates.find{it.template_id==p.template_id}
    val net  = vm.networks.find{it.network_id==p.network_id}
    Column(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(14.dp))
        .border(1.dp,if(net!=null)Gold.copy(.4f) else Border,RoundedCornerShape(14.dp))
        .clickable{onDetail(p)}) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(12.dp)){
            Box(Modifier.size(52.dp).background(BgSurface2,RoundedCornerShape(12.dp))
                .border(1.dp,Border,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){
                Text("${tmpl?.icon.orEmpty().ifEmpty { "🏪" }}",fontSize=24.sp)}
            Column(Modifier.weight(1f)){
                Text(p.custom_name,color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=14.sp)
                Text("${tmpl?.name_ru ?: ""}  LV ${p.level}",color=TextTert,fontSize=11.sp)
                Text(p.current_profit.fmtRate(),color=Green,fontSize=12.sp,fontWeight=FontWeight.Bold)
            }
            Column(horizontalAlignment=Alignment.End){
                if(net!=null){Badge("В сети",Gold);Spacer(Modifier.height(4.dp))}
                Badge("×${net?.profit_multiplier?.let{"%.1f".format(it)}?: "1.0"}",if(net!=null)Gold else TextSec)
            }
        }
    }
}

// ── Business Detail Sheet ─────────────────────────────────────────────────────
@Composable
fun BusinessDetailSheet(p: BusinessPoint, vm: AppViewModel, ctx: Context, onDismiss: ()->Unit) {
    var showRename by remember{mutableStateOf(false)}
    var newName    by remember{mutableStateOf(p.custom_name)}
    val tmpl = vm.templates.find{it.template_id==p.template_id}
    val boughtIdx = p.upgrades.map{it.upgrade_idx}.toSet()

    Box(Modifier.fillMaxSize().background(BgDeep.copy(.92f)).clickable(onClick=onDismiss)){
        Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            .background(BgCard,RoundedCornerShape(topStart=20.dp,topEnd=20.dp))
            .clickable(enabled=false){}) {
            // Handle + X
            Row(Modifier.fillMaxWidth().padding(top=10.dp,start=16.dp,end=16.dp),
                horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                Box(Modifier.weight(1f));
                Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){
                    Box(Modifier.size(36.dp,4.dp).background(Border2,RoundedCornerShape(2.dp)))}
                Box(Modifier.size(32.dp).background(BgSurface2,RoundedCornerShape(16.dp))
                    .border(1.dp,Border,RoundedCornerShape(16.dp)).clickable(onClick=onDismiss),
                    contentAlignment=Alignment.Center){Text("✕",color=TextSec,fontSize=14.sp,fontWeight=FontWeight.Bold)}
            }

            Column(Modifier.fillMaxHeight(.9f).verticalScroll(rememberScrollState()).padding(20.dp)){
                // Header
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
                    Box(Modifier.size(56.dp).background(BgSurface2,RoundedCornerShape(14.dp))
                        .border(1.dp,Border,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){
                        Text("${tmpl?.icon.orEmpty().ifEmpty { "🏪" }}",fontSize=28.sp)}
                    Column(Modifier.weight(1f)){
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            Text(p.custom_name,color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=17.sp)
                            Box(Modifier.background(BgSurface2,RoundedCornerShape(8.dp)).border(1.dp,Border,RoundedCornerShape(8.dp))
                                .clickable{showRename=true}.padding(horizontal=8.dp,vertical=3.dp)){
                                Text("✏️",fontSize=14.sp)}
                        }
                        Text("${tmpl?.name_ru ?: ""}",color=TextTert,fontSize=11.sp)
                    }
                    Badge("LV ${p.level}/${tmpl?.max_level ?:8}",Gold)
                }
                Spacer(Modifier.height(16.dp))

                // Stats
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    StatCard("ДОХОД/СЕК",p.current_profit.fmtRate(),Green,Modifier.weight(1f))
                    StatCard("ВЛОЖЕНО",p.total_invested.fmtMoney(),TextPrimary,Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    StatCard("ROI","${"%.1f".format(p.roi)}%",if(p.roi>=100)Green else if(p.roi>=50)Gold else TextPrimary,Modifier.weight(1f))
                    StatCard("ОКУПАЕМОСТЬ","${p.paybackHours}ч",Blue,Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                val net = vm.networks.find{it.network_id==p.network_id}
                if(net!=null) StatCard("В СЕТИ","${net.name} · ×${"%.1f".format(net.profit_multiplier)}",Gold,Modifier.fillMaxWidth())
                Spacer(Modifier.height(14.dp))

                // Upgrades
                Text("УЛУЧШЕНИЯ",color=TextTert,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                Spacer(Modifier.height(8.dp))
                tmpl?.upgrades?.forEachIndexed{i,upg->
                    val bought = boughtIdx.contains(i)
                    val canBuy = !bought && vm.displayBalance >= upg.cost
                    Row(Modifier.fillMaxWidth().padding(bottom=6.dp)
                        .background(if(bought)Green.copy(.06f) else BgSurface2,RoundedCornerShape(12.dp))
                        .border(1.dp,if(bought)Green.copy(.3f) else Border,RoundedCornerShape(12.dp))
                        .padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                        Box(Modifier.size(24.dp).background(if(bought)Green else BgSurface2,RoundedCornerShape(12.dp))
                            .border(1.dp,if(bought)Green else Border,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){
                            Text(if(bought)"✓" else "${i+1}",fontSize=10.sp,color=if(bought)BgDeep else TextTert,fontWeight=FontWeight.Bold)}
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)){
                            Text(upg.name_ru,color=TextPrimary,fontSize=13.sp,fontWeight=FontWeight.SemiBold)
                            Text(upg.description,color=TextTert,fontSize=10.sp)
                        }
                        if(bought) Text("✓",color=Green,fontSize=14.sp,fontWeight=FontWeight.Bold)
                        else GoldButton(upg.cost.fmtMoney(),onClick={vm.upgradePoint(ctx,p.point_id,i)},enabled=canBuy,small=true)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    OutlineButton("Закрыть",onClick=onDismiss,small=true,color=TextSec)
                    GoldButton("Купить ещё",onClick={vm.buyBusiness(ctx,p.template_id,p.custom_name+" 2")},modifier=Modifier.weight(1f))
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if(showRename) {
        AlertDialog(onDismissRequest={showRename=false},containerColor=BgSurface,shape=RoundedCornerShape(20.dp),
            title={Text("✏️ Переименовать",color=TextPrimary,fontWeight=FontWeight.Bold)},
            text={AuthField(newName,{newName=it},"Новое название")},
            confirmButton={GoldButton("Сохранить",onClick={vm.renamePoint(ctx,p.point_id,newName);showRename=false})},
            dismissButton={OutlineButton("Отмена",onClick={showRename=false},small=true,color=TextSec)})
    }
}

// ── Networks Tab ──────────────────────────────────────────────────────────────
@Composable
fun NetworksTab(vm: AppViewModel, ctx: Context, onCreate: ()->Unit, onRename: (Network)->Unit) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)){
        if(vm.networks.isEmpty()){
            item{GameCard(Modifier.fillMaxWidth()){
                Text("🔗 Сетей нет",color=TextSec,fontSize=14.sp,fontWeight=FontWeight.SemiBold)
                Text("Объединяйте точки в сети для множительного дохода",color=TextTert,fontSize=12.sp)
            }}
        } else {
            items(vm.networks,key={it.network_id}){net->NetworkCard(net,vm,ctx,onRename)}
        }
        item{SectionHeader("➕","СОЗДАТЬ СЕТЬ")}
        item{
            if(vm.freePoints.isEmpty())
                Text("Нет свободных точек для создания сети",color=TextTert,fontSize=12.sp,modifier=Modifier.padding(8.dp))
            else GoldButton("+ Создать сеть из свободных точек",onClick=onCreate,modifier=Modifier.fillMaxWidth())
        }
        item{Spacer(Modifier.height(80.dp))}
    }
}

@Composable
fun NetworkCard(net: Network, vm: AppViewModel, ctx: Context, onRename: (Network)->Unit) {
    var showAutoAdd by remember{mutableStateOf(net.auto_add)}
    var showAddPoint by remember{mutableStateOf(false)}

    Column(Modifier.fillMaxWidth().background(BgCard,RoundedCornerShape(14.dp))
        .border(1.dp,Gold.copy(.3f),RoundedCornerShape(14.dp))) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Text(net.name,color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=15.sp)
                    Box(Modifier.background(BgSurface2,RoundedCornerShape(8.dp)).border(1.dp,Border,RoundedCornerShape(8.dp))
                        .clickable{onRename(net)}.padding(horizontal=8.dp,vertical=3.dp)){Text("✏️",fontSize=12.sp)}
                }
                Text("${net.total_points} точек · ×${"%.1f".format(net.profit_multiplier)} доход",color=Gold,fontSize=11.sp)
                Text(net.total_profit.fmtRate(),color=Green,fontSize=12.sp,fontWeight=FontWeight.Bold)
            }
            RedButton("Расформировать",onClick={vm.disbandNetwork(ctx,net.network_id)},small=true)
        }
        // Auto-add toggle
        Row(Modifier.padding(horizontal=14.dp).padding(bottom=10.dp),
            verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
            LuxuryToggle(showAutoAdd,{
                showAutoAdd=it
                vm.setNetworkAutoAdd(ctx,net.network_id,it)
            })
            Text("Автодобавление новых точек",color=TextSec,fontSize=12.sp)
        }
        // Members
        val memberPoints = vm.points.filter{net.members.contains(it.point_id)}
        memberPoints.forEach{p->
            Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=4.dp),
                verticalAlignment=Alignment.CenterVertically){
                val tmpl = vm.templates.find{t->t.template_id==p.template_id}
                Text("${tmpl?.icon.orEmpty().ifEmpty { "🏪" }}",fontSize=16.sp)
                Spacer(Modifier.width(8.dp))
                Text(p.custom_name,color=TextSec,fontSize=12.sp,modifier=Modifier.weight(1f))
                Text(p.current_profit.fmtRate(),color=Green,fontSize=11.sp)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.background(Red.copy(.1f),RoundedCornerShape(8.dp))
                    .border(1.dp,Red.copy(.3f),RoundedCornerShape(8.dp))
                    .clickable{vm.removePointFromNetwork(ctx,net.network_id,p.point_id)}
                    .padding(horizontal=8.dp,vertical=3.dp)){Text("Убрать",color=Red,fontSize=10.sp)}
            }
        }
        Spacer(Modifier.height(8.dp))
        // Add point
        if(vm.freePoints.isNotEmpty()){
            OutlineButton("+ Добавить точку",onClick={showAddPoint=true},
                modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp).padding(bottom=10.dp),small=true)
        }
    }

    if(showAddPoint){
        AlertDialog(onDismissRequest={showAddPoint=false},containerColor=BgSurface,shape=RoundedCornerShape(20.dp),
            title={Text("Добавить точку в сеть",color=TextPrimary,fontWeight=FontWeight.Bold)},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
                    vm.freePoints.forEach{p->
                        val tmpl=vm.templates.find{t->t.template_id==p.template_id}
                        Row(Modifier.fillMaxWidth()
                            .background(BgSurface2,RoundedCornerShape(10.dp))
                            .border(1.dp,Border,RoundedCornerShape(10.dp))
                            .clickable{vm.addPointToNetwork(ctx,net.network_id,p.point_id);showAddPoint=false}
                            .padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                            Text("${tmpl?.icon.orEmpty().ifEmpty { "🏪" }}",fontSize=20.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)){
                                Text(p.custom_name,color=TextPrimary,fontSize=13.sp)
                                Text(p.current_profit.fmtRate(),color=Green,fontSize=11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton={},
            dismissButton={OutlineButton("Отмена",onClick={showAddPoint=false},small=true,color=TextSec)})
    }
}

@Composable
fun CreateNetworkDialog(vm: AppViewModel, ctx: Context, onDismiss: ()->Unit) {
    var name    by remember{mutableStateOf("")}
    var autoAdd by remember{mutableStateOf(false)}
    var selected by remember{mutableStateOf<Set<String>>(emptySet())}

    AlertDialog(onDismissRequest=onDismiss,containerColor=BgSurface,shape=RoundedCornerShape(20.dp),
        title={Text("🔗 Создать сеть",color=TextPrimary,fontWeight=FontWeight.Bold)},
        text={
            Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Text("Название сети:",color=TextSec,fontSize=12.sp)
                AuthField(name,{name=it},"Например: Coffee Empire")
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    Checkbox(checked=autoAdd,onCheckedChange={autoAdd=it},colors=CheckboxDefaults.colors(checkedColor=Gold))
                    Text("Автодобавление новых точек",color=TextSec,fontSize=12.sp)
                }
                Text("Выберите точки:",color=TextSec,fontSize=12.sp)
                vm.freePoints.forEach{p->
                    val tmpl=vm.templates.find{t->t.template_id==p.template_id}
                    val isSel=selected.contains(p.point_id)
                    Row(Modifier.fillMaxWidth()
                        .background(if(isSel)GoldGlow else BgSurface2,RoundedCornerShape(10.dp))
                        .border(1.dp,if(isSel)Gold else Border,RoundedCornerShape(10.dp))
                        .clickable{selected=if(isSel)selected-p.point_id else selected+p.point_id}
                        .padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                        Checkbox(checked=isSel,onCheckedChange={selected=if(isSel)selected-p.point_id else selected+p.point_id},
                            colors=CheckboxDefaults.colors(checkedColor=Gold))
                        Text("${tmpl?.icon.orEmpty().ifEmpty { "🏪" }}",fontSize=18.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)){
                            Text(p.custom_name,color=if(isSel)Gold else TextPrimary,fontSize=13.sp)
                            Text(p.current_profit.fmtRate(),color=Green,fontSize=11.sp)
                        }
                    }
                }
            }
        },
        confirmButton={GoldButton("Создать!",onClick={
            if(name.isNotBlank()&&selected.isNotEmpty())
                vm.createNetwork(ctx,name,selected.toList(),autoAdd)
            onDismiss()
        },enabled=name.isNotBlank()&&selected.size>=1)},
        dismissButton={OutlineButton("Отмена",onClick=onDismiss,small=true,color=TextSec)})
}
