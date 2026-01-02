package com.example.syncd.network

object ApiConfig {
    const val BASE_URL = "http://192.168.1.12:3000"
    
    object Auth {
        const val SEND_OTP = "/api/auth/phone-number/send-otp"
        const val VERIFY_OTP = "/api/auth/phone-number/verify"
        const val SESSION = "/api/auth/get-session"
        const val SIGN_OUT = "/api/auth/sign-out"
    }
    
    object Onboarding {
        const val COMPLETE = "/rpc/onboarding/complete"
        const val IS_COMPLETE = "/rpc/onboarding/isComplete"
    }
    
    object DailyLog {
        const val CREATE = "/rpc/dailyLog/create"
        const val UPDATE = "/rpc/dailyLog/update"
        const val LIST = "/rpc/dailyLog/list"
    }
    
    object Cycle {
        const val GET_PHASE_INFO = "/rpc/cycle/getPhaseInfo"
    }
}
