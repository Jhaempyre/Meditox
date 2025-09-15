#!/bin/bash

echo "🔍 Testing Razorpay Checkout Issue"
echo "=================================="

# Build and install the app
echo "📱 Building and installing app..."
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo ""
echo "🚀 Starting app and monitoring logs..."
echo "Look for these key log messages:"
echo "  - 'Subscribe button clicked for plan:' (Button click)"
echo "  - 'LaunchedEffect triggered' (Effect execution)"
echo "  - 'Subscription created! Launching payment...' (Toast message)"
echo "  - 'Starting subscription payment for plan:' (Checkout start)"
echo "  - 'Attempting to open Razorpay checkout...' (Checkout attempt)"
echo "  - 'Checkout.open() called successfully' (Checkout success)"
echo ""

# Start the app
adb shell am start -n com.example.meditox/.MainActivity

echo "🔍 Monitoring logs (Press Ctrl+C to stop)..."
adb logcat -v time | grep -E "(SubscriptionScreen|SubscriptionViewModel|RazorpayCheckout|MainActivity.*Payment)"