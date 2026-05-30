package com.billionaire.empire.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val FILE = "be_secure_v2"
    private const val KEY_ACCESS  = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_LAST_LOGIN = "last_login"
    private const val KEY_SESSION_STARTED = "session_started"
    private const val KEY_USERNAME = "username"
    private const val KEY_DISPLAY_NAME = "display_name"

    private fun prefs(ctx: Context) = EncryptedSharedPreferences.create(
        ctx, FILE,
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(ctx: Context, accessToken: String, refreshToken: String,
                    userId: String, username: String, displayName: String, deviceId: String) {
        prefs(ctx).edit()
            .putString(KEY_ACCESS,   accessToken)
            .putString(KEY_REFRESH,  refreshToken)
            .putString(KEY_USER_ID,  userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_DEVICE_ID, deviceId)
            .putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            .putLong(KEY_SESSION_STARTED, System.currentTimeMillis())
            .apply()
    }

    fun updateAccessToken(ctx: Context, token: String) =
        prefs(ctx).edit().putString(KEY_ACCESS, token).apply()

    fun getAccessToken(ctx: Context)   = prefs(ctx).getString(KEY_ACCESS, null)
    fun getRefreshToken(ctx: Context)  = prefs(ctx).getString(KEY_REFRESH, null)
    fun getUserId(ctx: Context)        = prefs(ctx).getString(KEY_USER_ID, null)
    fun getDeviceId(ctx: Context)      = prefs(ctx).getString(KEY_DEVICE_ID, null)
    fun getUsername(ctx: Context)      = prefs(ctx).getString(KEY_USERNAME, null)
    fun getDisplayName(ctx: Context)   = prefs(ctx).getString(KEY_DISPLAY_NAME, null)
    fun getLastLogin(ctx: Context)     = prefs(ctx).getLong(KEY_LAST_LOGIN, 0L)
    fun getSessionStarted(ctx: Context)= prefs(ctx).getLong(KEY_SESSION_STARTED, 0L)

    fun clearAll(ctx: Context) = prefs(ctx).edit().clear().apply()

    fun hasSession(ctx: Context) = !getRefreshToken(ctx).isNullOrBlank()
}
