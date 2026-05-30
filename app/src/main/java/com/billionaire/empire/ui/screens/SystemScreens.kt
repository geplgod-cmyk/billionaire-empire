package com.billionaire.empire.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.billionaire.empire.ui.components.*
import com.billionaire.empire.ui.theme.*
import com.billionaire.empire.viewmodel.AppViewModel

// ── Splash ────────────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(msg: String) {
    val alpha by rememberInfiniteTransition(label="pulse").animateFloat(
        0.5f,1f, infiniteRepeatable(tween(900),RepeatMode.Reverse),label="a")
    Box(Modifier.fillMaxSize().background(BgDeep),contentAlignment=Alignment.Center) {
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Text("BILLIONAIRE\nEMPIRE",
                style=MaterialTheme.typography.headlineLarge.copy(
                    fontWeight=FontWeight.Bold,
                    brush=Brush.verticalGradient(listOf(GoldLight,Gold,GoldDim)),
                    textAlign=TextAlign.Center, lineHeight=44.sp), fontSize=38.sp)
            Spacer(Modifier.height(24.dp))
            Text(msg,color=TextTert,fontSize=12.sp,letterSpacing=2.sp,modifier=Modifier.alpha(alpha))
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color=Gold,modifier=Modifier.size(24.dp),strokeWidth=2.dp)
        }
    }
}

// ── No Connection ─────────────────────────────────────────────────────────────
@Composable
fun NoConnectionScreen(onRetry: ()->Unit) {
    Box(Modifier.fillMaxSize().background(BgDeep),contentAlignment=Alignment.Center) {
        Column(Modifier.padding(32.dp),horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text("📡",fontSize=56.sp)
            Text("Нестабильное соединение",color=TextPrimary,fontSize=20.sp,
                fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
            Text("Для игры требуется подключение\nк серверам Billionaire Empire.",
                color=TextSec,fontSize=14.sp,textAlign=TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            GoldButton("🔄 Повторить",onClick=onRetry,modifier=Modifier.fillMaxWidth())
        }
    }
}

// ── Session Expired ───────────────────────────────────────────────────────────
@Composable
fun SessionExpiredScreen(onLogin: ()->Unit) {
    Box(Modifier.fillMaxSize().background(BgDeep),contentAlignment=Alignment.Center) {
        Column(Modifier.padding(32.dp),horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text("🔐",fontSize=56.sp)
            Text("Сессия истекла",color=TextPrimary,fontSize=20.sp,fontWeight=FontWeight.Bold)
            Text("Войдите снова.",color=TextSec,fontSize=14.sp)
            Spacer(Modifier.height(8.dp))
            GoldButton("⚡ Войти снова",onClick=onLogin,modifier=Modifier.fillMaxWidth())
        }
    }
}

// ── Auth ──────────────────────────────────────────────────────────────────────
@Composable
fun AuthScreen(vm: AppViewModel, ctx: Context) {
    var tab        by remember{mutableStateOf(0)}
    var lang       by remember{mutableStateOf("ru")}
    var loginUser  by remember{mutableStateOf("")}
    var loginPass  by remember{mutableStateOf("")}
    var regName    by remember{mutableStateOf("")}
    var regUser    by remember{mutableStateOf("")}
    var regPass    by remember{mutableStateOf("")}
    var regPass2   by remember{mutableStateOf("")}
    var loading    by remember{mutableStateOf(false)}
    var error      by remember{mutableStateOf<String?>(null)}

    Box(Modifier.fillMaxSize().background(BgDeep),contentAlignment=Alignment.Center) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .imePadding().padding(horizontal=24.dp).padding(top=24.dp,bottom=40.dp),
            horizontalAlignment=Alignment.CenterHorizontally) {

            Row(Modifier.align(Alignment.End),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("RU","EN").forEach { code ->
                    Box(Modifier
                        .background(if(lang==code) GoldGlow else BgSurface2,RoundedCornerShape(8.dp))
                        .border(1.dp,if(lang==code) Gold else Border,RoundedCornerShape(8.dp))
                        .clickable{lang=code}.padding(horizontal=12.dp,vertical=6.dp)) {
                        Text(code,color=if(lang==code) Gold else TextSec,fontSize=12.sp,fontWeight=FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            Text("BILLIONAIRE\nEMPIRE",style=MaterialTheme.typography.headlineLarge.copy(
                fontWeight=FontWeight.Bold,brush=Brush.verticalGradient(listOf(GoldLight,Gold,GoldDim)),
                textAlign=TextAlign.Center,lineHeight=40.sp),fontSize=34.sp)
            Spacer(Modifier.height(6.dp))
            Text(if(lang=="ru")"Путь к триллионам" else "Rise to Trillions",
                color=TextTert,fontSize=12.sp,letterSpacing=3.sp)
            Spacer(Modifier.height(28.dp))

            Column(Modifier.fillMaxWidth()
                .background(BgCard,RoundedCornerShape(20.dp))
                .border(1.dp,Border,RoundedCornerShape(20.dp)).padding(20.dp)) {

                // Tabs — key(lang) forces recompose on lang change
                key(lang) {
                Row(Modifier.fillMaxWidth().background(BgSurface2,RoundedCornerShape(10.dp)).padding(3.dp)) {
                    listOf(if(lang=="ru")"Войти" else "Login",
                           if(lang=="ru")"Регистрация" else "Register").forEachIndexed{i,lbl->
                        Box(Modifier.weight(1f)
                            .background(if(tab==i)GoldGlow else androidx.compose.ui.graphics.Color.Transparent,RoundedCornerShape(8.dp))
                            .clickable{tab=i;error=null}.padding(vertical=10.dp),
                            contentAlignment=Alignment.Center) {
                            Text(lbl,color=if(tab==i) Gold else TextTert,
                                fontWeight=FontWeight.Bold,fontSize=13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Error
                if(error!=null) {
                    Box(Modifier.fillMaxWidth()
                        .background(Red.copy(.12f),RoundedCornerShape(10.dp))
                        .border(1.dp,Red.copy(.4f),RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Text(error ?:"",color=Red,fontSize=13.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if(tab==0) {
                    Text(if(lang=="ru")"ЛОГИН" else "USERNAME",color=TextTert,fontSize=10.sp,
                        fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                    Spacer(Modifier.height(6.dp))
                    AuthField(loginUser,{loginUser=it},if(lang=="ru")"Ваш логин" else "Username")
                    Spacer(Modifier.height(12.dp))
                    Text(if(lang=="ru")"ПАРОЛЬ" else "PASSWORD",color=TextTert,fontSize=10.sp,
                        fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                    Spacer(Modifier.height(6.dp))
                    AuthField(loginPass,{loginPass=it},if(lang=="ru")"Ваш пароль" else "Password",isPassword=true)
                    Spacer(Modifier.height(20.dp))
                    GoldButton(if(lang=="ru")"⚡ ВОЙТИ" else "⚡ LOGIN",
                        onClick={
                            if(!loading){
                                loading=true
                                error=null
                                vm.login(ctx,loginUser.trim(),loginPass){msg->
                                    loading=false
                                    error=msg
                                }
                            }
                        },modifier=Modifier.fillMaxWidth(),
                        enabled=!loading&&loginUser.isNotBlank()&&loginPass.isNotBlank())
                } else {
                    Text(if(lang=="ru")"ИМЯ ИГРОКА" else "PLAYER NAME",color=TextTert,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                    Spacer(Modifier.height(6.dp))
                    AuthField(regName,{regName=it},if(lang=="ru")"Как вас называть?" else "Your display name")
                    Spacer(Modifier.height(12.dp))
                    Text(if(lang=="ru")"ЛОГИН" else "USERNAME",color=TextTert,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                    Spacer(Modifier.height(6.dp))
                    AuthField(regUser,{regUser=it},if(lang=="ru")"Только латиница и цифры" else "Latin letters & digits only")
                    Spacer(Modifier.height(12.dp))
                    Text(if(lang=="ru")"ПАРОЛЬ" else "PASSWORD",color=TextTert,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                    Spacer(Modifier.height(6.dp))
                    AuthField(regPass,{regPass=it},if(lang=="ru")"Минимум 6 символов" else "Min 6 characters",isPassword=true)
                    Spacer(Modifier.height(12.dp))
                    Text(if(lang=="ru")"ПОВТОР ПАРОЛЯ" else "REPEAT PASSWORD",color=TextTert,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
                    Spacer(Modifier.height(6.dp))
                    AuthField(regPass2,{regPass2=it},if(lang=="ru")"Повторите пароль" else "Repeat password",isPassword=true)
                    Spacer(Modifier.height(12.dp))
                    if(regPass.isNotEmpty()&&regPass2.isNotEmpty()&&regPass!=regPass2)
                        Text(if(lang=="ru")"Пароли не совпадают" else "Passwords don't match",color=Red,fontSize=12.sp)
                    Spacer(Modifier.height(8.dp))
                    GoldButton(if(lang=="ru")"🚀 СОЗДАТЬ АККАУНТ" else "🚀 CREATE ACCOUNT",
                        onClick={
                            if(!loading){loading=true;error=null
                                vm.register(ctx,regUser,regName,regPass){msg->loading=false;error=msg}}
                        },modifier=Modifier.fillMaxWidth(),
                        enabled=!loading&&regPass==regPass2&&regPass.length>=6&&regName.isNotBlank()&&regUser.isNotBlank())
                }
                if(loading){Spacer(Modifier.height(16.dp));LinearProgressIndicator(Modifier.fillMaxWidth(),color=Gold,trackColor=BgSurface2)}
                } // end key(lang)
            }
        }
    }
}
