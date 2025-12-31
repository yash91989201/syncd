package com.example.syncd.di

import android.util.Log
import com.example.syncd.network.ApiConfig
import com.example.syncd.network.PersistentCookieStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }
    
    single { PersistentCookieStorage(androidContext()) }
    
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            
            install(HttpCookies) {
                storage = get<PersistentCookieStorage>()
            }
            
            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorHttp", message)
                    }
                }
            }
            
            defaultRequest {
                url(ApiConfig.BASE_URL)
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Origin, "http://com.example.syncd")
            }
        }
    }
}
