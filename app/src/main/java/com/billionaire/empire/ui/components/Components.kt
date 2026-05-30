package com.billionaire.empire.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.billionaire.empire.ui.theme.*

@Composable
fun GoldButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
               enabled: Boolean = true, small: Boolean = false) {
    val bg = if (enabled) Brush.horizontalGradient(listOf(GoldDim,Gold,GoldLight))
             else Brush.horizontalGradient(listOf(BgSurface2,BgSurface2))
    Box(modifier.height(if(small)38.dp else 48.dp).background(bg,RoundedCornerShape(12.dp))
        .clickable(enabled=enabled){onClick()}.padding(horizontal=if(small)14.dp else 20.dp),
        contentAlignment=Alignment.Center) {
        Text(text,color=if(enabled)Color(0xFF0A0700) else TextTert,
            fontWeight=FontWeight.Bold,fontSize=if(small)12.sp else 14.sp)
    }
}

@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                  small: Boolean = false, color: Color = Gold) {
    Box(modifier.height(if(small)38.dp else 48.dp)
        .border(1.dp,color.copy(.5f),RoundedCornerShape(12.dp))
        .background(color.copy(.08f),RoundedCornerShape(12.dp))
        .clickable{onClick()}.padding(horizontal=if(small)14.dp else 20.dp),
        contentAlignment=Alignment.Center) {
        Text(text,color=color,fontWeight=FontWeight.SemiBold,fontSize=if(small)12.sp else 14.sp)
    }
}

@Composable
fun RedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, small: Boolean = false) {
    Box(modifier.height(if(small)38.dp else 48.dp)
        .border(1.dp,Red.copy(.5f),RoundedCornerShape(12.dp))
        .background(Red.copy(.1f),RoundedCornerShape(12.dp))
        .clickable{onClick()}.padding(horizontal=if(small)14.dp else 20.dp),
        contentAlignment=Alignment.Center) {
        Text(text,color=Red,fontWeight=FontWeight.SemiBold,fontSize=if(small)12.sp else 14.sp)
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color = TextPrimary, modifier: Modifier = Modifier) {
    Column(modifier.background(BgSurface2,RoundedCornerShape(12.dp))
        .border(1.dp,Border,RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(label,color=TextTert,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp)
        Spacer(Modifier.height(6.dp))
        Text(value,color=color,fontSize=15.sp,fontWeight=FontWeight.Bold)
    }
}

@Composable
fun SectionHeader(icon: String, label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
        Text(icon,fontSize=14.sp); Spacer(Modifier.width(6.dp))
        Text(label,color=TextTert,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
        Spacer(Modifier.width(8.dp))
        Divider(color=Border,modifier=Modifier.weight(1f),thickness=1.dp)
    }
}

@Composable
fun GoldProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val anim by animateFloatAsState(progress, tween(600), label="prog")
    Box(modifier.height(3.dp).background(Border,RoundedCornerShape(2.dp))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(anim)
            .background(Brush.horizontalGradient(listOf(GoldDim,Gold)),RoundedCornerShape(2.dp)))
    }
}

@Composable
fun Badge(text: String, color: Color = Gold) {
    Box(Modifier.background(color.copy(.15f),RoundedCornerShape(6.dp))
        .border(1.dp,color.copy(.4f),RoundedCornerShape(6.dp))
        .padding(horizontal=7.dp,vertical=2.dp)) {
        Text(text,color=color,fontSize=10.sp,fontWeight=FontWeight.Bold)
    }
}

@Composable
fun ToastOverlay(message: String?, onDismiss: () -> Unit) {
    LaunchedEffect(message) { if(message!=null){kotlinx.coroutines.delay(2500);onDismiss()} }
    AnimatedVisibility(visible=message!=null,
        enter=slideInVertically{-it}+fadeIn(), exit=slideOutVertically{-it}+fadeOut()) {
        Box(Modifier.fillMaxWidth().padding(24.dp,16.dp),contentAlignment=Alignment.Center) {
            Box(Modifier.background(BgSurface,RoundedCornerShape(12.dp))
                .border(1.dp,Border2,RoundedCornerShape(12.dp))
                .padding(20.dp,12.dp)) {
                Text(message?:"",color=TextPrimary,fontSize=13.sp,textAlign=TextAlign.Center)
            }
        }
    }
}

@Composable
fun AuthField(value: String, onValueChange: (String)->Unit, placeholder: String,
              isPassword: Boolean=false, modifier: Modifier=Modifier) {
    var show by remember{mutableStateOf(false)}
    OutlinedTextField(value=value,onValueChange=onValueChange,
        placeholder={Text(placeholder,color=TextTert,fontSize=14.sp)},
        visualTransformation=if(isPassword&&!show) androidx.compose.ui.text.input.PasswordVisualTransformation()
                             else androidx.compose.ui.text.input.VisualTransformation.None,
        trailingIcon=if(isPassword)({IconButton(onClick={show=!show}){Text(if(show)"🙈" else "👁",fontSize=18.sp)}}) else null,
        modifier=modifier.fillMaxWidth(),singleLine=true,
        colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Gold,unfocusedBorderColor=Border,
            cursorColor=Gold,focusedTextColor=TextPrimary,unfocusedTextColor=TextPrimary,
            focusedContainerColor=BgSurface2,unfocusedContainerColor=BgSurface2),
        shape=RoundedCornerShape(12.dp))
}

@Composable
fun GameCard(modifier: Modifier=Modifier, content: @Composable ColumnScope.()->Unit) {
    Column(modifier.background(BgCard,RoundedCornerShape(16.dp))
        .border(1.dp,Border,RoundedCornerShape(16.dp)).padding(16.dp),content=content)
}

@Composable
fun LuxuryToggle(checked: Boolean, onCheckedChange: (Boolean)->Unit) {
    val offset by animateFloatAsState(if(checked)22f else 0f,tween(200),label="tog")
    val bg     by animateColorAsState(if(checked)Green else Border2,tween(200),label="bg")
    Box(Modifier.size(52.dp,30.dp).background(bg,RoundedCornerShape(15.dp))
        .clickable{onCheckedChange(!checked)}.padding(4.dp),
        contentAlignment=Alignment.CenterStart) {
        Box(Modifier.offset(x=offset.dp).size(22.dp).background(TextPrimary,RoundedCornerShape(11.dp)))
    }
}

@Composable
fun ConfirmDialog(title: String, body: String, confirmText: String="Подтвердить",
                  cancelText: String="Отмена", onConfirm: ()->Unit, onDismiss: ()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,containerColor=BgSurface,shape=RoundedCornerShape(20.dp),
        title={Text(title,color=TextPrimary,fontWeight=FontWeight.Bold)},
        text={Text(body,color=TextSec)},
        confirmButton={RedButton(confirmText,onClick=onConfirm,small=true)},
        dismissButton={OutlineButton(cancelText,onClick=onDismiss,small=true,color=TextSec)})
}
