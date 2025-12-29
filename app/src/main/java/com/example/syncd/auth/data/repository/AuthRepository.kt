package com.example.syncd.auth.data.repository

import com.example.syncd.auth.data.model.SendOtpRequest
import com.example.syncd.auth.data.model.SendOtpResponse
import com.example.syncd.auth.data.model.SessionResponse
import com.example.syncd.auth.data.model.SignOutResponse
import com.example.syncd.auth.data.model.VerifyOtpRequest
import com.example.syncd.auth.data.model.VerifyOtpResponse
import com.example.syncd.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthRepository(
    private val httpClient: HttpClient
) {
    suspend fun sendOtp(phoneNumber: String): Result<SendOtpResponse> {
        return runCatching {
            httpClient.post(ApiConfig.Auth.SEND_OTP) {
                setBody(SendOtpRequest(phoneNumber))
            }.body<SendOtpResponse>()
        }
    }
    
    suspend fun verifyOtp(phoneNumber: String, code: String): Result<VerifyOtpResponse> {
        return runCatching {
            httpClient.post(ApiConfig.Auth.VERIFY_OTP) {
                setBody(VerifyOtpRequest(phoneNumber, code))
            }.body<VerifyOtpResponse>()
        }
    }
    
    suspend fun getSession(): Result<SessionResponse> {
        return runCatching {
            httpClient.get(ApiConfig.Auth.SESSION).body<SessionResponse>()
        }
    }
    
    suspend fun signOut(): Result<SignOutResponse> {
        return runCatching {
            httpClient.post(ApiConfig.Auth.SIGN_OUT).body<SignOutResponse>()
        }
    }
}
