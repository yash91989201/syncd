package com.example.syncd.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    val phoneNumber: String
)

@Serializable
data class SendOtpResponse(
    val success: Boolean? = null,
    val status: String? = null,
    val message: String? = null
)

@Serializable
data class VerifyOtpRequest(
    val phoneNumber: String,
    val code: String
)

@Serializable
data class VerifyOtpResponse(
    val user: User? = null,
    val session: Session? = null,
    val token: String? = null
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    val image: String? = null,
    val phoneNumber: String? = null,
    val phoneNumberVerified: Boolean? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Session(
    val id: String,
    val userId: String,
    val token: String,
    val expiresAt: String,
    val createdAt: String,
    val updatedAt: String,
    val ipAddress: String? = null,
    val userAgent: String? = null
)

@Serializable
data class SessionResponse(
    val user: User? = null,
    val session: Session? = null
)

@Serializable
data class SignOutResponse(
    val success: Boolean? = null
)
