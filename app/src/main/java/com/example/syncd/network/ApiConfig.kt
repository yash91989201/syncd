package com.example.syncd.network

object ApiConfig {
    // TODO: Replace with your actual backend URL
    const val BASE_URL = "http://192.168.1.12:3000"
    
    object Auth {
        const val SEND_OTP = "/api/auth/phone-number/send-otp"
        const val VERIFY_OTP = "/api/auth/phone-number/verify"
        const val SESSION = "/api/auth/get-session"
        const val SIGN_OUT = "/api/auth/sign-out"
    }
}
