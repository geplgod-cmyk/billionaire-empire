package com.billionaire.empire.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billionaire.empire.data.models.*
import com.billionaire.empire.network.ApiClient
import com.billionaire.empire.utils.SecureStorage
import kotlinx.coroutines.*
import kotlin.math.*

// ── App state machine ────────────────────────────────────────────────────────
sealed class AppScreen {
    object Splash         : AppScreen()
    object NoConnection   : AppScreen()
    object Login          : AppScreen()
    object SessionExpired : AppScreen()
    object Loading        : AppScreen()
    object Game           : AppScreen()
}

class AppViewModel : ViewModel() {

    var screen       by mutableStateOf<AppScreen>(AppScreen.Splash)
    var loadingMsg   by mutableStateOf("Подключение к серверам...")
    var errorMsg     by mutableStateOf("")
    var toastMsg     by mutableStateOf<String?>(null)

    // ── Server state (single source of truth) ─────────────────────────────────
    var serverState  by mutableStateOf<ServerState?>(null)

    // Derived convenience getters
    val finances:    Finances  get() = serverState?.finances  ?: Finances()
    val profile:     Profile   get() = serverState?.profile   ?: Profile()
    val settings:    Settings  get() = serverState?.settings  ?: Settings()
    val templates:   List<BusinessTemplate> get() = serverState?.businessTemplates ?: emptyList()
    val points:      List<BusinessPoint>    get() = serverState?.businessPoints    ?: emptyList()
    val networks:    List<Network>          get() = serverState?.networks          ?: emptyList()
    val stocks:      List<StockPosition>    get() = serverState?.stockPositions    ?: emptyList()
    val cryptos:     List<CryptoPosition>   get() = serverState?.cryptoPositions   ?: emptyList()
    val luxury:      List<LuxuryAsset>      get() = serverState?.luxuryAssets      ?: emptyList()

    // Points NOT in any network
    val freePoints: List<BusinessPoint> get() = points.filter { it.network_id == null }

    // ── Session ───────────────────────────────────────────────────────────────
    private var accessToken: String = ""
    private var refreshToken: String = ""

    // ── Passive income client-side display (real money from server ticks) ─────
    var displayBalance   by mutableStateOf(0.0)
    var displayPassive   by mutableStateOf(0.0)
    private var lastSyncTime = System.currentTimeMillis()
    private var secondsSinceLastServerTick = 0L

    // Market prices (client-side only, cosmetic)
    val stockPrices  = mutableStateMapOf<String, Double>()
    val cryptoPrices = mutableStateMapOf<String, Double>()

    // ── Startup flow ──────────────────────────────────────────────────────────
    fun startup(ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { loadingMsg = "Проверка соединения..." }

            // Check server reachability
            if (!ApiClient.checkHealth()) {
                withContext(Dispatchers.Main) { screen = AppScreen.NoConnection }
                return@launch
            }

            withContext(Dispatchers.Main) { loadingMsg = "Проверка сессии..." }

            if (!SecureStorage.hasSession(ctx)) {
                withContext(Dispatchers.Main) { screen = AppScreen.Login }
                return@launch
            }

            // Try refresh
            val refresh = SecureStorage.getRefreshToken(ctx) ?: ""
            val r = ApiClient.post<AuthResponse>(
                "/api/auth/refresh",
                """{"refreshToken":"$refresh"}"""
            )
            when (r) {
                is ApiClient.Result.Success -> {
                    val resp = r.data
                    if (resp.error.isNotEmpty()) {
                        withContext(Dispatchers.Main) { screen = AppScreen.SessionExpired }
                        return@launch
                    }
                    accessToken  = resp.accessToken
                    refreshToken = resp.refreshToken
                    SecureStorage.saveSession(ctx, resp.accessToken, resp.refreshToken,
                        resp.player.id, resp.player.username, resp.player.name,
                        SecureStorage.getDeviceId(ctx) ?: resp.deviceId)
                    loadServerState(ctx)
                }
                is ApiClient.Result.Error -> {
                    if (r.code == "SESSION_EXPIRED" || r.code == "TOKEN_EXPIRED") {
                        SecureStorage.clearAll(ctx)
                        withContext(Dispatchers.Main) { screen = AppScreen.SessionExpired }
                    } else {
                        withContext(Dispatchers.Main) { screen = AppScreen.NoConnection }
                    }
                }
                is ApiClient.Result.NoConnection ->
                    withContext(Dispatchers.Main) { screen = AppScreen.NoConnection }
            }
        }
    }

    fun loadServerState(ctx: Context, onDone: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { loadingMsg = "Загрузка данных..." }
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.get<ServerState>("/api/me/state", token)
            withContext(Dispatchers.Main) {
                when (r) {
                    is ApiClient.Result.Success -> {
                        serverState = r.data
                        displayBalance = r.data.finances.balance
                        displayPassive = r.data.finances.passive_income
                        lastSyncTime   = System.currentTimeMillis()
                        screen = AppScreen.Game
                        initMarketPrices()
                        startClientLoops(ctx)
                        onDone?.invoke()
                    }
                    is ApiClient.Result.Error -> {
                        if (r.code == "TOKEN_EXPIRED") {
                            // Try refresh first
                            startup(ctx)
                        } else {
                            screen = AppScreen.NoConnection
                        }
                    }
                    is ApiClient.Result.NoConnection -> screen = AppScreen.NoConnection
                }
            }
        }
    }

    // ── Login / Register ──────────────────────────────────────────────────────
    fun login(ctx: Context, username: String, password: String,
              onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceId = SecureStorage.getDeviceId(ctx) ?: java.util.UUID.randomUUID().toString()
            val body = """{"username":"${username.trim().lowercase()}","password":"$password","device_id":"$deviceId"}"""
            val r = ApiClient.post<AuthResponse>("/api/auth/login", body)
            withContext(Dispatchers.Main) {
                when (r) {
                    is ApiClient.Result.Success -> {
                        val resp = r.data
                        if (resp.error.isNotEmpty()) { onError(resp.error); return@withContext }
                        val at = resp.effectiveAccessToken
                        val rt = resp.refreshToken
                        if (at.isBlank()) { onError("Ошибка авторизации: нет токена"); return@withContext }
                        accessToken  = at
                        refreshToken = rt
                        SecureStorage.saveSession(ctx, at, rt,
                            resp.player.id, resp.player.username, resp.player.name,
                            resp.deviceId.ifBlank { deviceId })
                        loadServerState(ctx)
                    }
                    is ApiClient.Result.Error -> onError(r.message)
                    is ApiClient.Result.NoConnection -> onError("Нет соединения с сервером")
                }
            }
        }
    }

    fun register(ctx: Context, username: String, displayName: String,
                 password: String, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceId = java.util.UUID.randomUUID().toString()
            val body = """{"username":"${username.trim().lowercase()}","display_name":"${displayName.trim()}","password":"$password","device_id":"$deviceId"}"""
            val r = ApiClient.post<AuthResponse>("/api/auth/register", body)
            withContext(Dispatchers.Main) {
                when (r) {
                    is ApiClient.Result.Success -> {
                        val resp = r.data
                        if (resp.error.isNotEmpty()) { onError(resp.error); return@withContext }
                        val at = resp.effectiveAccessToken
                        val rt = resp.refreshToken
                        if (at.isBlank()) { onError("Ошибка регистрации: нет токена"); return@withContext }
                        accessToken  = at
                        refreshToken = rt
                        SecureStorage.saveSession(ctx, at, rt,
                            resp.player.id, resp.player.username, resp.player.name, deviceId)
                        loadServerState(ctx)
                    }
                    is ApiClient.Result.Error -> onError(r.message)
                    is ApiClient.Result.NoConnection -> onError("Нет соединения с сервером")
                }
            }
        }
    }

    fun logout(ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiClient.rawPost("/api/auth/logout",
                """{"refreshToken":"$refreshToken"}""", accessToken)
            withContext(Dispatchers.Main) {
                SecureStorage.clearAll(ctx)
                accessToken  = ""
                refreshToken = ""
                serverState  = null
                screen       = AppScreen.Login
            }
        }
    }

    // ── Client loops (display only — no money creation) ───────────────────────
    private var loopsStarted = false
    private fun startClientLoops(ctx: Context) {
        if (loopsStarted) return
        loopsStarted = true

        // Display balance tick — cosmetic only
        viewModelScope.launch {
            while (screen == AppScreen.Game) {
                delay(1000)
                if (displayPassive > 0) displayBalance += displayPassive
                secondsSinceLastServerTick++
                // Every 5 min send passive tick to server
                if (secondsSinceLastServerTick >= 300) {
                    sendPassiveTick(ctx, secondsSinceLastServerTick)
                    secondsSinceLastServerTick = 0
                }
            }
        }

        // Market simulation (cosmetic prices only)
        viewModelScope.launch {
            while (screen == AppScreen.Game) {
                delay(3000)
                try { tickMarketPrices() } catch(e: Exception) {}
            }
        }

        // Heartbeat every 30s
        viewModelScope.launch {
            while (screen == AppScreen.Game) {
                delay(30_000)
                val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
                ApiClient.rawPost("/api/session/heartbeat", "{}", token)
            }
        }

        // Connection check every 60s
        viewModelScope.launch {
            while (screen == AppScreen.Game) {
                delay(60_000)
                if (!ApiClient.checkHealth()) {
                    withContext(Dispatchers.Main) { screen = AppScreen.NoConnection }
                    break
                }
            }
        }
    }

    private fun sendPassiveTick(ctx: Context, seconds: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/passive-tick", """{"seconds":$seconds}""", token)
            if (r is ApiClient.Result.Success) {
                // Sync display balance with server
                val srv = ApiClient.get<ServerState>("/api/me/state", token)
                if (srv is ApiClient.Result.Success) {
                    withContext(Dispatchers.Main) {
                        serverState    = srv.data
                        displayBalance = srv.data.finances.balance
                        displayPassive = srv.data.finances.passive_income
                    }
                }
            }
        }
    }

    // ── Market prices (cosmetic simulation) ───────────────────────────────────
    private val defaultPrices = mapOf(
        "TVI" to 142.5, "GPC" to 87.2, "MCL" to 234.8, "NAI" to 512.0,
        "ADC" to 67.3,  "NVX" to 198.4,"SPC" to 445.0,  "OXL" to 73.1,
        "MXM" to 156.9, "CYB" to 321.0,
        "BTC" to 42000.0, "MOON" to 0.0042, "QX" to 8800.0, "NOVA" to 142.0,
        "ETH2" to 2800.0, "SOL2" to 185.0, "VT" to 0.00018, "GLX" to 67.0
    )
    private val stockVol  = mapOf("TVI" to 3f,"GPC" to 2f,"MCL" to 2.5f,"NAI" to 5f,
        "ADC" to 3.5f,"NVX" to 1.5f,"SPC" to 6f,"OXL" to 4f,"MXM" to 2.5f,"CYB" to 3f)
    private val cryptoVol = mapOf("BTC" to 8f,"MOON" to 25f,"QX" to 12f,"NOVA" to 15f,
        "ETH2" to 9f,"SOL2" to 18f,"VT" to 40f,"GLX" to 20f)

    private fun initMarketPrices() {
        defaultPrices.forEach { (k,v) -> stockPrices[k] = v; cryptoPrices[k] = v }
    }

    private fun tickMarketPrices() {
        stockVol.forEach  { (sym, vol) -> stockPrices[sym]  = maxOf(0.01, (stockPrices[sym]  ?: defaultPrices[sym] ?: 100.0) * (1 + (Math.random()-0.5)*vol/100)) }
        cryptoVol.forEach { (sym, vol) -> cryptoPrices[sym] = maxOf(0.000001, (cryptoPrices[sym] ?: defaultPrices[sym] ?: 1.0) * (1 + (Math.random()-0.49)*vol/100)) }
    }

    // ── Actions (all go through server) ──────────────────────────────────────
    fun buyBusiness(ctx: Context, templateId: String, customName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val body  = """{"templateId":"$templateId","customName":"${customName.replace("\"","\\\"")}"}"""
            val r = ApiClient.rawPost("/api/actions/business/buy", body, token)
            handleActionResult(ctx, r, "✅ Бизнес открыт!")
        }
    }

    fun upgradePoint(ctx: Context, pointId: String, upgradeIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/business-point/upgrade",
                """{"pointId":"$pointId","upgradeIdx":$upgradeIdx}""", token)
            handleActionResult(ctx, r, "📈 Улучшение куплено!")
        }
    }

    fun renamePoint(ctx: Context, pointId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/business-point/rename",
                """{"pointId":"$pointId","newName":"${newName.replace("\"","\\\"")}"}""", token)
            handleActionResult(ctx, r, "✏️ Название изменено")
        }
    }

    fun createNetwork(ctx: Context, name: String, pointIds: List<String>, autoAdd: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val ids   = pointIds.joinToString(",") { "\"$it\"" }
            val r = ApiClient.rawPost("/api/actions/network/create",
                """{"name":"${name.replace("\"","\\\"")}","pointIds":[$ids],"autoAdd":$autoAdd}""", token)
            handleActionResult(ctx, r, "🔗 Сеть создана!")
        }
    }

    fun renameNetwork(ctx: Context, networkId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/network/rename",
                """{"networkId":"$networkId","newName":"${newName.replace("\"","\\\"")}"}""", token)
            handleActionResult(ctx, r, "✏️ Сеть переименована")
        }
    }

    fun setNetworkAutoAdd(ctx: Context, networkId: String, autoAdd: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            ApiClient.rawPost("/api/actions/network/auto-add",
                """{"networkId":"$networkId","autoAdd":$autoAdd}""", token)
            loadServerState(ctx)
        }
    }

    fun addPointToNetwork(ctx: Context, networkId: String, pointId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/network/add-point",
                """{"networkId":"$networkId","pointId":"$pointId"}""", token)
            handleActionResult(ctx, r, "✅ Точка добавлена в сеть")
        }
    }

    fun removePointFromNetwork(ctx: Context, networkId: String, pointId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/network/remove-point",
                """{"networkId":"$networkId","pointId":"$pointId"}""", token)
            handleActionResult(ctx, r, "✅ Точка убрана из сети")
        }
    }

    fun disbandNetwork(ctx: Context, networkId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/network/disband",
                """{"networkId":"$networkId"}""", token)
            handleActionResult(ctx, r, "❌ Сеть расформирована")
        }
    }

    fun buyStock(ctx: Context, symbol: String, quantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val price = stockPrices[symbol] ?: return@launch
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/investments/stock/buy",
                """{"symbol":"$symbol","quantity":$quantity,"price":$price}""", token)
            handleActionResult(ctx, r, "✅ Куплено $quantity $symbol")
        }
    }

    fun sellStock(ctx: Context, symbol: String, fraction: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val pos = stocks.find { it.symbol == symbol } ?: return@launch
            val qty   = max(1, (pos.quantity * fraction).toInt())
            val price = stockPrices[symbol] ?: return@launch
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/investments/stock/sell",
                """{"symbol":"$symbol","quantity":$qty,"price":$price}""", token)
            handleActionResult(ctx, r, "💰 Продано $qty $symbol")
        }
    }

    fun buyCrypto(ctx: Context, symbol: String, usdAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val price = cryptoPrices[symbol] ?: return@launch
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/investments/crypto/buy",
                """{"symbol":"$symbol","usdAmount":$usdAmount,"price":$price}""", token)
            handleActionResult(ctx, r, "🪙 Куплена крипта $symbol")
        }
    }

    fun sellCrypto(ctx: Context, symbol: String, fraction: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val pos = cryptos.find { it.symbol == symbol } ?: return@launch
            val qty   = pos.quantity * fraction
            val price = cryptoPrices[symbol] ?: return@launch
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/investments/crypto/sell",
                """{"symbol":"$symbol","quantity":$qty,"price":$price}""", token)
            handleActionResult(ctx, r, "💰 Крипта продана")
        }
    }

    fun buyLuxury(ctx: Context, itemId: String, price: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/luxury/buy",
                """{"itemId":"$itemId","price":$price}""", token)
            handleActionResult(ctx, r, "✨ Luxury куплен!")
        }
    }

    fun donate(ctx: Context, amount: Double, repGain: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/charity/donate",
                """{"amount":$amount,"repGain":$repGain}""", token)
            handleActionResult(ctx, r, "❤️ Пожертвовано!")
        }
    }

    fun onTap(ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val r = ApiClient.rawPost("/api/actions/click", """{"amount":1}""", token)
            if (r is ApiClient.Result.Success) {
                withContext(Dispatchers.Main) { displayBalance += 1 }
            }
        }
    }

    fun updateSettings(ctx: Context, language: String? = null, theme: String? = null,
                       notif: Boolean? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = accessToken.ifBlank { SecureStorage.getAccessToken(ctx) ?: "" }
            val parts = buildList {
                language?.let { add("\"language\":\"$it\"") }
                theme?.let    { add("\"theme\":\"$it\"") }
                notif?.let    { add("\"notifications_enabled\":$it") }
            }
            if (parts.isEmpty()) return@launch
            ApiClient.rawPost("/api/actions/settings", "{${parts.joinToString(",")}}", token)
            loadServerState(ctx)
        }
    }

    // ── Shared action handler ─────────────────────────────────────────────────
    private suspend fun handleActionResult(ctx: Context, r: ApiClient.Result<String>, successMsg: String) {
        withContext(Dispatchers.Main) {
            when (r) {
                is ApiClient.Result.Success -> {
                    toast(successMsg)
                    // Reload state to sync with server
                    loadServerState(ctx)
                }
                is ApiClient.Result.Error -> toast("❌ ${r.message}")
                is ApiClient.Result.NoConnection -> {
                    toast("❌ Нет соединения")
                    screen = AppScreen.NoConnection
                }
            }
        }
    }

    fun toast(msg: String) { toastMsg = msg }
    fun clearToast() { toastMsg = null }
}
