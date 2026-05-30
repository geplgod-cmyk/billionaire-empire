package com.billionaire.empire.network

import android.content.Context
import com.billionaire.empire.BuildConfig
import com.billionaire.empire.utils.SecureStorage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    val BASE = BuildConfig.API_BASE
    private val JSON_MT = "application/json".toMediaType()
    @PublishedApi internal val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: String = "") : Result<Nothing>()
        object NoConnection : Result<Nothing>()
    }

    private fun headers(accessToken: String?) = Headers.Builder()
        .add("Content-Type", "application/json")
        .apply { if (!accessToken.isNullOrBlank()) add("Authorization", "Bearer $accessToken") }
        .build()

    fun rawPost(path: String, body: String, token: String? = null): Result<String> {
        return try {
            val req = Request.Builder().url("$BASE$path")
                .headers(headers(token))
                .post(body.toRequestBody(JSON_MT)).build()
            val resp = client.newCall(req).execute()
            val txt  = resp.body?.string() ?: ""
            if (resp.isSuccessful) Result.Success(txt)
            else {
                val code = runCatching {
                    json.parseToJsonElement(txt).jsonObject["code"]?.jsonPrimitive?.content ?: ""
                }.getOrDefault("")
                val err = runCatching {
                    json.parseToJsonElement(txt).jsonObject["error"]?.jsonPrimitive?.content
                }.getOrNull() ?: "Ошибка ${resp.code}"
                Result.Error(err, code)
            }
        } catch (e: IOException) { Result.NoConnection }
    }

    fun rawGet(path: String, token: String? = null): Result<String> {
        return try {
            val req = Request.Builder().url("$BASE$path")
                .headers(headers(token)).get().build()
            val resp = client.newCall(req).execute()
            val txt  = resp.body?.string() ?: ""
            if (resp.isSuccessful) Result.Success(txt)
            else Result.Error("Ошибка ${resp.code}", "")
        } catch (e: IOException) { Result.NoConnection }
    }

    fun checkHealth(): Boolean = try {
        rawGet("/api/health") is Result.Success
    } catch (e: Exception) { false }

    inline fun <reified T> post(path: String, body: String, token: String? = null): Result<T> {
        return when (val r = rawPost(path, body, token)) {
            is Result.Success -> runCatching { Result.Success(json.decodeFromString<T>(r.data)) }
                .getOrElse { Result.Error("Ошибка разбора ответа") }
            is Result.Error   -> r
            is Result.NoConnection -> Result.NoConnection
        }
    }

    inline fun <reified T> get(path: String, token: String? = null): Result<T> {
        return when (val r = rawGet(path, token)) {
            is Result.Success -> runCatching { Result.Success(json.decodeFromString<T>(r.data)) }
                .getOrElse { Result.Error("Ошибка разбора ответа") }
            is Result.Error   -> r
            is Result.NoConnection -> Result.NoConnection
        }
    }
}
