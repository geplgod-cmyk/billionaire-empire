package com.billionaire.empire.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken:  String = "",
    val refreshToken: String = "",
    // Legacy fallback (old server used 'token')
    val token:        String = "",
    val player:       PlayerInfo = PlayerInfo(),
    val deviceId:     String = "",
    val error:        String = ""
) {
    // Use whichever token field is available
    val effectiveAccessToken: String get() = accessToken.ifBlank { token }
}

@Serializable
data class PlayerInfo(
    val id:           String = "",
    val username:     String = "",
    val name:         String = "",
    val display_name: String = ""  // Some endpoints return display_name
) {
    val effectiveName: String get() = name.ifBlank { display_name }
}

@Serializable
data class ServerState(
    val serverTime:        String = "",
    val revision:          Long = 0,
    val player:            PlayerInfo = PlayerInfo(),
    val finances:          Finances = Finances(),
    val profile:           Profile = Profile(),
    val settings:          Settings = Settings(),
    val businessTemplates: List<BusinessTemplate> = emptyList(),
    val businessPoints:    List<BusinessPoint> = emptyList(),
    val networks:          List<Network> = emptyList(),
    val stockPositions:    List<StockPosition> = emptyList(),
    val cryptoPositions:   List<CryptoPosition> = emptyList(),
    val luxuryAssets:      List<LuxuryAsset> = emptyList(),
    val achievements:      List<Achievement> = emptyList()
)

@Serializable
data class Finances(
    val balance:        Double = 10_000.0,
    val total_capital:  Double = 10_000.0,
    val passive_income: Double = 0.0,
    val total_earned:   Double = 0.0,
    val total_spent:    Double = 0.0,
    val revision:       Long   = 0
)

@Serializable
data class Profile(
    val reputation:    Int    = 100,
    val level:         Int    = 1,
    val clicker_total: Double = 0.0,
    val charity_total: Double = 0.0
)

@Serializable
data class Settings(
    val language:              String  = "ru",
    val theme:                 String  = "dark",
    val notifications_enabled: Boolean = false,
    val sound_enabled:         Boolean = true
)

@Serializable
data class BusinessTemplate(
    val template_id: String = "",
    val name_ru:     String = "",
    val name_en:     String = "",
    val icon:        String = "",
    val category:    String = "",
    val base_price:  Double = 0.0,
    val base_profit: Double = 0.0,
    val max_level:   Int    = 8,
    val desc_ru:     String = "",
    val desc_en:     String = "",
    val sort_order:  Int    = 0,
    val upgrades:    List<TemplateUpgrade> = emptyList()
)

@Serializable
data class TemplateUpgrade(
    val upgrade_idx:  Int    = 0,
    val name_ru:      String = "",
    val name_en:      String = "",
    val description:  String = "",
    val cost:         Double = 0.0,
    val profit_bonus: Double = 0.0
)

@Serializable
data class BusinessPoint(
    val point_id:       String = "",
    val template_id:    String = "",
    val custom_name:    String = "",
    val level:          Int    = 1,
    val base_profit:    Double = 0.0,
    val current_profit: Double = 0.0,
    val total_invested: Double = 0.0,
    val network_id:     String? = null,
    val purchase_date:  String = "",
    val upgrades:       List<PointUpgrade> = emptyList()
) {
    val roi: Double get() = if (total_invested > 0) (current_profit * 3600 / total_invested * 100) else 0.0
    val paybackHours: Int get() = if (current_profit > 0) (total_invested / (current_profit * 3600)).toInt() else 0
}

@Serializable
data class PointUpgrade(
    val upgrade_idx:  Int    = 0,
    val cost:         Double = 0.0,
    val profit_bonus: Double = 0.0
)

@Serializable
data class Network(
    val network_id:       String  = "",
    val name:             String  = "",
    val template_id:      String? = null,
    val total_points:     Int     = 0,
    val total_profit:     Double  = 0.0,
    val total_invested:   Double  = 0.0,
    val network_level:    Int     = 1,
    val profit_multiplier:Double  = 1.8,
    val auto_add:         Boolean = false,
    val members:          List<String> = emptyList(),
    val upgrades:         List<NetworkUpgrade> = emptyList()
)

@Serializable
data class NetworkUpgrade(
    val upgrade_type:     String = "",
    val level:            Int    = 1,
    val profit_multiplier:Double = 1.0
)

@Serializable
data class StockPosition(
    val symbol:        String = "",
    val quantity:      Int    = 0,
    val avg_buy_price: Double = 0.0,
    val total_invested:Double = 0.0,
    val first_buy_date:String = ""
)

@Serializable
data class CryptoPosition(
    val symbol:        String = "",
    val quantity:      Double = 0.0,
    val avg_buy_price: Double = 0.0,
    val total_invested:Double = 0.0
)

@Serializable
data class LuxuryAsset(
    val item_id:       String = "",
    val purchase_date: String = ""
)

@Serializable
data class Achievement(
    val achievement_id: String  = "",
    val progress:       Double  = 0.0,
    val unlocked:       Boolean = false,
    val reward_claimed: Boolean = false
)
