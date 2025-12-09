package com.example.meditox.screens.subscription

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.meditox.models.viewModel.SubscriptionViewModel
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.RazorpayConfig
import com.example.meditox.utils.SubscriptionDataHolder
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.Routes
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import com.example.meditox.ui.theme.primaryGreen
import com.example.meditox.ui.theme.lightGreen
import com.example.meditox.ui.theme.lighterGreen
import com.example.meditox.ui.theme.darkGreen

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: String,
    val duration: String,
    val isPopular: Boolean,
    val features: List<PlanFeature>
)

data class PlanFeature(
    val text: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    var selectedPlan by remember { mutableStateOf("plan_RH7icw3pPxMSSk") }
    var expandedPlan by remember { mutableStateOf<String?>(null) }
    
    val subscriptionResult = subscriptionViewModel.subscriptionResult.collectAsState()
    val phoneNumber = subscriptionViewModel.phoneNumber.collectAsState()
    
    // Get user data from ViewModel instead of direct DataStore calls
    val userData = subscriptionViewModel.userData.collectAsState()
    val shopDetails = subscriptionViewModel.shopDetails.collectAsState()

    // Monitor backend sync status from ViewModel instead of direct DataStore calls
    val backendSyncStatus = subscriptionViewModel.backendSyncStatus.collectAsState()
    val hasActiveSubscription = subscriptionViewModel.hasActiveSubscription.collectAsState()

    // Only navigate after successful backend sync confirmation
    LaunchedEffect(backendSyncStatus.value, hasActiveSubscription.value) {
        if (hasActiveSubscription.value && backendSyncStatus.value) {
            // Small delay to ensure UI is ready
            kotlinx.coroutines.delay(2000)
            Log.d("SubscriptionScreen", "✅ Backend sync confirmed, navigating to success screen")
            navController.navigate(Routes.SUCCEEDED_SUBSCRIPTION) {
                // Remove subscription screen from back stack
                popUpTo(Routes.SUBSCRIPTION) { inclusive = true }
            }
        } else if (hasActiveSubscription.value && !backendSyncStatus.value) {
            Log.w("SubscriptionScreen", "⚠️ Local subscription active but backend sync failed - staying on subscription screen")
        }
    }

    // Define plans list
    val plans = listOf(
        SubscriptionPlan(
            id = "plan_RH7fGQrO7VcWse",
            name = "Starter",
            price = "₹199",
            duration = "month",
            isPopular = false,
            features = listOf(
                PlanFeature("Up to 500 medicines", Icons.Default.Lock),
                PlanFeature("Basic reports", Icons.Default.Lock),
                PlanFeature("Email support", Icons.Default.Email),
                PlanFeature("Stock alerts", Icons.Default.Notifications)
            )
        ),
        SubscriptionPlan(
            id = "plan_RH7icw3pPxMSSk",
            name = "Business",
            price = "₹299",
            duration = "month",
            isPopular = true,
            features = listOf(
                PlanFeature("Unlimited medicines", Icons.Default.Lock),
                PlanFeature("Advanced analytics", Icons.Default.Lock),
                PlanFeature("Priority support", Icons.Default.Lock),
                PlanFeature("Real-time insights", Icons.Default.Lock),
                PlanFeature("Batch management", Icons.Default.Lock),
                PlanFeature("Multi-location support", Icons.Default.LocationOn)
            )
        ),
        SubscriptionPlan(
            id = "plan_RH7jzqh5c5Chgn",
            name = "Enterprise",
            price = "₹399",
            duration = "month",
            isPopular = false,
            features = listOf(
                PlanFeature("Everything in Business", Icons.Default.Lock),
                PlanFeature("Custom integrations", Icons.Default.Lock),
                PlanFeature("24/7 phone support", Icons.Default.Lock),
                PlanFeature("Dedicated account manager", Icons.Default.Lock),
                PlanFeature("Advanced security", Icons.Default.Lock),
                PlanFeature("Custom reports", Icons.Default.Lock),
                PlanFeature("API access", Icons.Default.Lock)
            )
        )
    )

    // Initialize Razorpay Checkout
    val checkout = remember {
        Checkout().apply {
            setKeyID(RazorpayConfig.RAZORPAY_KEY_ID)
            // Set the payment result listener to the activity
            if (activity is PaymentResultListener) {
                Log.d("RazorpayCheckout", "Setting PaymentResultListener")
            } else {
                Log.e("RazorpayCheckout", "Activity does not implement PaymentResultListener")
            }
        }
    }

    // Function to start Razorpay checkout
    fun startRazorpayCheckout(subscriptionResponse: com.example.meditox.models.subscription.SubscriptionResponse, planName: String, planPrice: String) {
        try {
            Log.d("RazorpayCheckout", "Starting subscription payment for plan: $planName")
            Log.d("RazorpayCheckout", "Subscription ID: ${subscriptionResponse.id}")
            Log.d("RazorpayCheckout", "Subscription Status: ${subscriptionResponse.status}")
            
            val options = JSONObject().apply {
                put("name", RazorpayConfig.COMPANY_NAME)
                put("description", "$planName Subscription")
                
                // For subscription payments, only use subscription_id, not amount
                put("subscription_id", subscriptionResponse.id)
                
                // Prefill customer details with actual user data
                val prefill = JSONObject().apply {
                    val user = userData.value
                    put("email", shopDetails.value?.contactEmail) // User model doesn't have email field
                    put("contact", phoneNumber.value ?: user?.phone ?: "")
                    put("name", user?.name ?: "Customer")
                }
                put("prefill", prefill)
                
                // Theme customization
                val theme = JSONObject().apply {
                    put("color", RazorpayConfig.THEME_COLOR)
                }
                put("theme", theme)
                
                // Additional notes for tracking
                val notes = JSONObject().apply {
                    put("subscription_id", subscriptionResponse.id)
                    put("plan_id", subscriptionResponse.planId)
                    put("plan_name", planName)
                    put("app_version", "1.0") // You can get this from BuildConfig
                }
                put("notes", notes)
                
                // Retry settings
                val retry = JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 3)
                }
                put("retry", retry)
                
                // Timeout (in seconds)
                put("timeout", 300)
                
                // Modal configuration
                val modal = JSONObject().apply {
                    put("backdropclose", false)
                    put("escape", false)
                    put("handleback", true)
                }
                put("modal", modal)
            }
            
            Log.d("RazorpayCheckout", "Payment options JSON: $options")
            Log.d("RazorpayCheckout", "Activity type: ${activity.javaClass.simpleName}")
            Log.d("RazorpayCheckout", "Activity implements PaymentResultListener: ${activity is PaymentResultListener}")
            
            // Attempt to open checkout
            Log.d("RazorpayCheckout", "Attempting to open Razorpay checkout...")
            checkout.open(activity, options)
            Log.d("RazorpayCheckout", "Checkout.open() called successfully")
            
        } catch (e: Exception) {
            Log.e("RazorpayCheckout", "Error in checkout", e)
            Toast.makeText(context, "Error starting payment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }




    // Set initial plan ID
    LaunchedEffect(Unit) {
        subscriptionViewModel.setPlanId(selectedPlan)
    }
    
    // Handle subscription result
    LaunchedEffect(subscriptionResult.value) {
        val result = subscriptionResult.value
        Log.d("SubscriptionScreen", "LaunchedEffect triggered. Result type: ${result?.javaClass?.simpleName}")
        Log.d("SubscriptionScreen", "Selected plan: $selectedPlan")
        
        when (result) {
            is ApiResult.Success -> {
                val response = result.data
                Log.d("SubscriptionScreen", "Subscription response: $response")
                Log.d("SubscriptionScreen", "Subscription ID: ${response.id}")
                Log.d("SubscriptionScreen", "Subscription status: ${response.status}")
                Log.d("SubscriptionScreen", "Subscription plan ID: ${response.planId}")
                
                // Find the selected plan details for payment
                val selectedPlanDetails = plans.find { it.id == selectedPlan }
                Log.d("SubscriptionScreen", "Found plan details: $selectedPlanDetails")
                
                if (selectedPlanDetails != null) {
                    // Store subscription and plan data for payment success handling
                    SubscriptionDataHolder.storeSubscriptionData(
                        subscriptionResponse = response,
                        planId = selectedPlanDetails.id,
                        planName = selectedPlanDetails.name,
                        planPrice = selectedPlanDetails.price,
                        planDuration = selectedPlanDetails.duration
                    )
                    
                    Log.d("SubscriptionScreen", "Stored subscription data in holder:")
                    Log.d("SubscriptionScreen", "- Subscription ID: ${response.id}")
                    Log.d("SubscriptionScreen", "- Plan: ${selectedPlanDetails.name}")
                    Log.d("SubscriptionScreen", "- Price: ${selectedPlanDetails.price}")
                    
                    Toast.makeText(
                        context,
                        "Subscription created! Launching payment...",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Add a small delay to ensure UI is ready
                    kotlinx.coroutines.delay(500)
                    
                    // Start Razorpay checkout
                    Log.d("SubscriptionScreen", "About to call startRazorpayCheckout")
                    startRazorpayCheckout(response, selectedPlanDetails.name, selectedPlanDetails.price)
                } else {
                    Log.e("SubscriptionScreen", "Plan details not found for selected plan: $selectedPlan")
                    Toast.makeText(
                        context,
                        "Error: Plan details not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            is ApiResult.Error -> {
                Log.e("SubscriptionScreen", "Subscription error: ${result.message}")
                Toast.makeText(
                    context,
                    "Subscription failed: ${result.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            is ApiResult.Loading -> {
                // Loading state is handled in the button
            }
            null -> {
                // Initial state
            }
        }
    }

    // UI Layout starts here

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar with Business Context
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Meditox Pro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Scale your medicine business",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Help */ }) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Help",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = primaryGreen
            )
        )

        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Business Header with Context
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = lighterGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(lightGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Business",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Join 10,000+ pharmacy owners",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkGreen
                        )
                        Text(
                            text = "Choose a plan that grows with your business needs",
                            fontSize = 14.sp,
                            color = primaryGreen,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Choose Your Plan",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = darkGreen,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Unlock powerful features to manage inventory, boost sales, and grow your pharmacy business efficiently.",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Plans
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(plans.size) { index ->
                    val plan = plans[index]
                    PlanCard(
                        plan = plan,
                        isSelected = selectedPlan == plan.id,
                        isExpanded = expandedPlan == plan.id,
                        onSelect = { 
                            selectedPlan = plan.id
                            subscriptionViewModel.setPlanId(plan.id)
                        },
                        onToggleExpand = {
                            expandedPlan = if (expandedPlan == plan.id) null else plan.id
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Button
            Button(
                onClick = {
                    Log.d("SubscriptionScreen", "Subscribe button clicked for plan: $selectedPlan")
                    subscriptionViewModel.createSubscription(selectedPlan)
                },
                enabled = subscriptionResult.value !is ApiResult.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (subscriptionResult.value is ApiResult.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Payment",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (subscriptionResult.value is ApiResult.Loading) 
                        "Creating Subscription..." 
                    else 
                        "Start ${plans.find { it.id == selectedPlan }?.name} Plan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trust indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Verified",
                    tint = lightGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "30-day money back guarantee",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    isExpanded: Boolean,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) lighterGreen else Color(0xFFFAFAFA)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, lightGreen)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Main card content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radio button and plan info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Custom Radio Button
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) lightGreen else Color.Transparent
                            )
                            .clickable { onSelect() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color.Transparent,
                                        CircleShape
                                    )
                                    .then(
                                        Modifier.background(
                                            Color(0xFFE0E0E0),
                                            CircleShape
                                        )
                                    )
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = plan.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) darkGreen else Color(0xFF333333)
                            )
                            if (plan.isPopular) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            lightGreen,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Most Popular",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = plan.price,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen
                            )
                            Text(
                                text = "/${plan.duration}",
                                fontSize = 16.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Dropdown button
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = lightGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Animated features dropdown
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(200),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(
                    animationSpec = tween(200),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(top =16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        thickness = 1.dp,
                        color = lightGreen.copy(alpha = 0.2f)
                    )

                    plan.features.forEach { feature ->
                        FeatureRow(feature = feature)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureRow(feature: PlanFeature) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = feature.text,
            tint = lightGreen,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = feature.text,
            fontSize = 15.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Normal
        )
    }
}