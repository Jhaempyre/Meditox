package com.example.meditox.screens.subscription

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SubscriptionHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.example.meditox.ui.theme.primaryGreen
import com.example.meditox.ui.theme.lightGreen
import com.example.meditox.ui.theme.lighterGreen
import com.example.meditox.ui.theme.darkGreen

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
            Toast.makeText(context, "Error loading subscription details", Toast.LENGTH_SHORT).show()
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
                                SubscriptionHelper.cancelSubscription(context)
                                Toast.makeText(
                                    context,
                                    "Subscription cancelled. Refund will be processed within 7 business days.",
                                    Toast.LENGTH_LONG
                                ).show()
                                showCancelDialog = false
                                navController.navigate("subscription")
                            } catch (e: Exception) {
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