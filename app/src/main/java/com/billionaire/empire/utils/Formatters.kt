package com.billionaire.empire.utils

fun Double.fmtMoney(): String {
    val n = kotlin.math.abs(this)
    return when {
        n < 1_000_000_000.0         -> "$%,.0f".format(this)
        n < 1_000_000_000_000.0     -> "$%,.1fB".format(this/1_000_000_000.0)
        n < 1_000_000_000_000_000.0 -> "$%,.1fT".format(this/1_000_000_000_000.0)
        else                         -> "$%,.1fQ".format(this/1_000_000_000_000_000.0)
    }
}
fun Double.fmtRate(): String = "${fmtMoney()}/сек"
fun Double.fmtPct(): String  = "${if(this>=0)"+" else ""}${"%.1f".format(this)}%"
fun repMultiplier(rep: Int): Double = when { rep>=81->1.0; rep>=61->0.9; rep>=41->0.75; rep>=21->0.55; else->0.35 }
fun repTierName(rep: Int): String   = when { rep>=81->"Элитная"; rep>=61->"Хорошая"; rep>=41->"Нормальная"; rep>=21->"Низкая"; else->"Критическая" }
fun repColor(rep: Int): androidx.compose.ui.graphics.Color = when { rep>=81->com.billionaire.empire.ui.theme.Green; rep>=61->com.billionaire.empire.ui.theme.Blue; rep>=41->com.billionaire.empire.ui.theme.Gold; rep>=21->androidx.compose.ui.graphics.Color(0xFFFF8C00); else->com.billionaire.empire.ui.theme.Red }
