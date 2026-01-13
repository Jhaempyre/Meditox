package com.example.meditox.screens.subscription

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.subscription.toSubscriptionDetails
import com.example.meditox.models.viewModel.OtpViewModel
import com.example.meditox.models.viewModel.SubscriptionViewModel
import com.example.meditox.ui.theme.darkGreen
import com.example.meditox.ui.theme.lighterGreen
import com.example.meditox.ui.theme.primaryGreen
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreDebugger
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SubscriptionHelper
import com.example.meditox.utils.SubscriptionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Additional colors for subscription status
val successGreen = Color(0xFF4CAF50)
val warningOrange = Color(0xFFFF9800)
val dangerRed = Color(0xFFF44336)

// Data class for subscription summary (local to this screen)
data class SubscriptionSummary(
    val planName: String = "No Plan",
    val amount: String = "₹0",
    val status: String = "No Subscription",
    val startDate: String = "N/A",
    val endDate: String = "N/A",
    val daysRemaining: Int = 0,
    val isActive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucceededSubscriptionScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for subscription details
    var subscriptionSummary by remember { mutableStateOf<SubscriptionSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isRefundEligible by remember { mutableStateOf(false) }
    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    val otpViewModel : OtpViewModel = viewModel()
    val subscriptionId by subscriptionViewModel
        .subscriptionId
        .collectAsState(initial = null)
        val subscriptionDetailResult = otpViewModel.subscriptionDetailResult.collectAsState().value


    // Load subscription details
    LaunchedEffect(Unit) {
        try {
            val helperSummary = SubscriptionHelper.getSubscriptionSummary(context)
            // Convert to local SubscriptionSummary
            subscriptionSummary = SubscriptionSummary(
                planName = helperSummary.planName,
                amount = helperSummary.amount,
                status = helperSummary.status,
                startDate = helperSummary.startDate,
                endDate = helperSummary.endDate,
                daysRemaining = helperSummary.daysRemaining,
                isActive = helperSummary.isActive
            )
            
            // Calculate refund eligibility
            val details = SubscriptionHelper.getSubscriptionDetails(context).first()
            if (details != null) {
                val daysSinceStart = (System.currentTimeMillis() - details.startDate) / (24 * 60 * 60 * 1000)
                isRefundEligible = daysSinceStart <= 7 && details.isActive
            }
            
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            print(e.message)
            Toast.makeText(context, "Error loading subscription details", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(subscriptionDetailResult) {
        when(val response =subscriptionDetailResult ){
            is ApiResult.Success->{
                val response = response.data
                val body = response.body()
                Log.d(this::class.java.simpleName, "Subscription details received: $body")
                if(response.isSuccessful && body?.success == true && body.data != null){
                    try{
                        Log.d(this::class.java.simpleName,"The successfull response is getting here ")
                        val subscriptionApiData = response.body()?.data
                        Log.d(this::class.java.simpleName, "Subscription API data: ${subscriptionApiData.toString()}")
                        if (subscriptionApiData != null) {
                            val subscriptionData = subscriptionApiData.toSubscriptionDetails()
                            val isActive = subscriptionData.isActive
                            if(isActive){
                                DataStoreManager.saveSubscriptionDetails(context, subscriptionData)
                                DataStoreManager.setBackendSyncStatus(context,true);
                                Log.d(this::class.java.simpleName, "Subscription details saved successfully")
                            }
                            else{
                                DataStoreManager.setBackendSyncStatus(context,false)
                                DataStoreManager.clearSubscriptionData(context)
                            }
                            Log.d(this::class.java.simpleName, "Subscription details saved successfully")
                        } else {
                            Log.w(this::class.java.simpleName, "No subscription data available")
                        }
                        Log.d(this::class.java.simpleName, "Setup complete, navigating to dashboard")

                        // Wait a bit to ensure all DataStore writes complete
                        delay(200)

                        // Verify data is saved before navigating
                        DataStoreDebugger.logAllStates(context, "SettingThingsUp-BeforeNavigate")

                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        otpViewModel.resetgetSubscriptionDetails()


                    }
                    catch(e: Exception){
                        Log.e(this::class.java.simpleName, "Error saving subscription details: ${e.message}", e)
                        // Navigate to dashboard anyway as subscription is optional
                        Log.d(this::class.java.simpleName, "Navigating to dashboard despite subscription error")

                        // Wait a bit to ensure all DataStore writes complete
                        delay(200)

                        // Verify data is saved
                        DataStoreDebugger.logAllStates(context, "SettingThingsUp-ErrorCase")

                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }

                        otpViewModel.resetgetSubscriptionDetails()
                    }
                }else {
                        Log.w(this::class.java.simpleName, "Subscription not found or failed to fetch")
                        // Navigate to dashboard anyway as subscription might not exist yet
                        Log.d("SettingThingsUp", "Navigating to dashboard without subscription")

                        // Wait a bit to ensure all DataStore writes complete
                        delay(200)
                        DataStoreManager.setBackendSyncStatus(context,false)
                        // Verify data is saved
                        DataStoreDebugger.logAllStates(context, "SettingThingsUp-NoSubscription")

                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                }
                otpViewModel.resetgetSubscriptionDetails()
            }
            is ApiResult.Error->{
                Log.e(this::class.java.simpleName, "Error fetching subscription: ${response.message}")
                // Navigate to dashboard anyway as subscription might not exist yet
                Log.d(this::class.java.simpleName, "Navigating to dashboard despite subscription error")

                // Wait a bit to ensure all DataStore writes complete
                delay(200)

                // Verify data is saved
                DataStoreDebugger.logAllStates(context, "SettingThingsUp-SubscriptionError")
                DataStoreManager.setBackendSyncStatus(context,false)
                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }

                otpViewModel.resetgetSubscriptionDetails()

            }
            else->{}
        }



    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "My Subscription",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
                IconButton(onClick = { 
                    // Refresh subscription data
                    scope.launch {
                        isLoading = true
                        try {
                            val helperSummary = SubscriptionHelper.getSubscriptionSummary(context)
                            subscriptionSummary = SubscriptionSummary(
                                planName = helperSummary.planName,
                                amount = helperSummary.amount,
                                status = helperSummary.status,
                                startDate = helperSummary.startDate,
                                endDate = helperSummary.endDate,
                                daysRemaining = helperSummary.daysRemaining,
                                isActive = helperSummary.isActive
                            )
                        } catch (e: Exception) {
                            print(e.message)
                            Toast.makeText(context, "Error refreshing data", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = primaryGreen
            )
        )

        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = primaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Loading subscription details...",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // Success Header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = lighterGreen),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(successGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Active Subscription",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkGreen,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "You're all set with premium features!",
                                fontSize = 16.sp,
                                color = primaryGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                item {
                    // Plan Details Card
                    subscriptionSummary?.let { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Plan Details",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = darkGreen
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                DetailRow(
                                    icon = Icons.Default.Star,
                                    label = "Current Plan",
                                    value = summary.planName,
                                    color = primaryGreen
                                )
                                
                                DetailRow(
                                    icon = Icons.Default.Star,
                                    label = "Amount Paid",
                                    value = summary.amount,
                                    color = successGreen
                                )
                                
                                DetailRow(
                                    icon = Icons.Default.DateRange,
                                    label = "Started On",
                                    value = summary.startDate,
                                    color = Color.Gray
                                )
                                
                                DetailRow(
                                    icon = Icons.Default.Build,
                                    label = "Valid Until",
                                    value = summary.endDate,
                                    color = if (summary.daysRemaining <= 7) warningOrange else primaryGreen
                                )
                                
                                DetailRow(
                                    icon = Icons.Default.Info,
                                    label = "Days Remaining",
                                    value = "${summary.daysRemaining} days",
                                    color = when {
                                        summary.daysRemaining <= 3 -> dangerRed
                                        summary.daysRemaining <= 7 -> warningOrange
                                        else -> successGreen
                                    }
                                )
                                
                                DetailRow(
                                    icon = Icons.Default.Info,
                                    label = "Status",
                                    value = summary.status,
                                    color = when (summary.status.lowercase()) {
                                        "active" -> successGreen
                                        "expired" -> dangerRed
                                        "cancelled" -> warningOrange
                                        else -> Color.Gray
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    // Current Features Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Active Features",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkGreen
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Get features based on plan name
                            val features = getFeaturesByPlan(subscriptionSummary?.planName ?: "")
                            
                            features.forEach { feature ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Included",
                                        tint = successGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = feature,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Action Buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Upgrade/Change Plan Button
                        Button(
                            onClick = {
                                navController.navigate("subscription")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Upgrade",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upgrade Plan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Cancel Subscription Button (only if refund eligible)
                        if (isRefundEligible) {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = dangerRed
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Cancel",
                                    tint = dangerRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cancel & Refund",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = dangerRed
                                )
                            }
                        }
                    }
                }

                item {
                    // Help & Support Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = lighterGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Help",
                                    tint = primaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Need Help?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = darkGreen
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Contact our support team for any subscription-related queries or assistance.",
                                fontSize = 14.sp,
                                color = primaryGreen,
                                lineHeight = 20.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { 
                                        Toast.makeText(context, "Opening email support...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = primaryGreen
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = primaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Email", fontSize = 14.sp)
                                }
                                
                                OutlinedButton(
                                    onClick = { 
                                        Toast.makeText(context, "Opening phone support...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = primaryGreen
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone",
                                        tint = primaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cancel Subscription Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = "Cancel Subscription",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel your subscription? You will receive a full refund within 7 business days.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                Log.d("successcreen",subscriptionId.toString())
                                if(subscriptionId!=null) {
                                    Log.d("successscreen ", "trying thing inside")
                                    val cancelResponse = SubscriptionManager.cancelSubscriptionImmediately(
                                        context,
                                        subscriptionId ?: ""
                                    )
                                    Log.d("succesceded screen ", cancelResponse.toString())

                                    if(cancelResponse){
                                        Toast.makeText(context, "Subscription cancelled", Toast.LENGTH_SHORT).show()
                                        Log.d("succeded screen1",otpViewModel.userId.toString())
                                        Log.d("succeded screen2",otpViewModel.userId.value.toString())
                                        otpViewModel.getSubscriptionDetails(otpViewModel.userId.value.toString())
                                    }


                                    Toast.makeText(
                                        context,
                                        "Subscription cancelled. Refund will be processed within 7 business days.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showCancelDialog = false
                                    navController.navigate("subscription")
                                }else{
                                    Toast.makeText(context, "Error cancelling subscription", Toast.LENGTH_SHORT).show()
                                    showCancelDialog = false
                                }
                            } catch (e: Exception) {
                                print(e.message)
                                Toast.makeText(context, "Error cancelling subscription", Toast.LENGTH_SHORT).show()
                            }
                        }


                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dangerRed)
                ) {
                    Text("Cancel Subscription")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Subscription", color = primaryGreen)
                }
            }
        )
    }
}


@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

fun getFeaturesByPlan(planName: String): List<String> {
    return when (planName.lowercase()) {
        "starter" -> listOf(
            "Up to 500 medicines",
            "Basic reports",
            "Email support",
            "Stock alerts"
        )
        "business" -> listOf(
            "Unlimited medicines",
            "Advanced analytics",
            "Priority support",
            "Real-time insights",
            "Batch management",
            "Multi-location support"
        )
        "enterprise" -> listOf(
            "Everything in Business",
            "Custom integrations",
            "24/7 phone support",
            "Dedicated account manager",
            "Advanced security",
            "Custom reports",
            "API access"
        )
        else -> listOf(
            "Basic medicine tracking",
            "Standard reporting",
            "Email support"
        )
    }
}