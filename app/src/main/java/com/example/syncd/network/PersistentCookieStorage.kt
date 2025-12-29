package com.example.syncd.network

import android.content.Context
import android.content.SharedPreferences
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderSetCookieHeader
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PersistentCookieStorage(context: Context) : CookiesStorage {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cookies = mutableMapOf<String, Cookie>()
    private val mutex = Mutex()
    
    init {
        loadCookies()
    }
    
    private fun loadCookies() {
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                runCatching {
                    val cookie = parseServerSetCookieHeader(value)
                    cookies[key] = cookie
                }
            }
        }
    }
    
    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val now = GMTDate()
        cookies.values.filter { cookie ->
            !cookie.isExpired(now) && cookie.matches(requestUrl)
        }
    }
    
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = mutex.withLock {
        if (cookie.name.isBlank()) return@withLock
        
        val key = "${cookie.name}@${requestUrl.host}"
        cookies[key] = cookie
        
        prefs.edit()
            .putString(key, renderSetCookieHeader(cookie))
            .apply()
    }
    
    override fun close() {
    }
    
    fun clearCookies() {
        cookies.clear()
        prefs.edit().clear().apply()
    }
    
    private fun Cookie.isExpired(now: GMTDate): Boolean {
        return expires?.let { it.timestamp < now.timestamp } ?: false
    }
    
    private fun Cookie.matches(url: Url): Boolean {
        val domainMatches = domain?.let { d ->
            url.host.endsWith(d) || url.host == d.removePrefix(".")
        } ?: true
        
        val pathMatches = path?.let { p ->
            url.encodedPath.startsWith(p)
        } ?: true
        
        return domainMatches && pathMatches
    }
    
    companion object {
        private const val PREFS_NAME = "ktor_cookies"
    }
}
