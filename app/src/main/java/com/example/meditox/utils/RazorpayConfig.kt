package com.example.meditox.utils

object RazorpayConfig {
    // TODO: Replace with your actual Razorpay key ID from https://dashboard.razorpay.com/#/app/keys we definetly beed to change it like it should be sent from server in another or last touchup we will do it
    const val RAZORPAY_KEY_ID = "rzp_test_RGS8HzGl0uPLxL"
    
    // For production, use:
    // const val RAZORPAY_KEY_ID = "rzp_live_your_key_id"
    
    const val COMPANY_NAME = "Meditox"
    const val CURRENCY = "INR"
    
    // Theme color for Razorpay checkout (should match your app theme)
    const val THEME_COLOR = "#2E7D32"
}