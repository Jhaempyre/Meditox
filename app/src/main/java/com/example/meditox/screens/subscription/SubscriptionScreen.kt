package com.example.meditox.screens.subscription

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

// Updated Color Palette
val primaryGreen = Color(0xFF2E7D32)
val lightGreen = Color(0xFF4CAF50)
val lighterGreen = Color(0xFFE8F5E9)
val darkGreen = Color(0xFF1B5E20)

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
    var selectedPlan by remember { mutableStateOf("premium") }
    var expandedPlan by remember { mutableStateOf<String?>(null) }

    val plans = listOf(
        SubscriptionPlan(
            id = "basic",
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
            id = "premium",
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
            id = "pro",
            name = "Enterprise",
            price = "₹399",
            duration = "month",
            isPopular = false,
            features = listOf(
                PlanFeature("Everything in Business", Icons.Default.CheckCircle),
                PlanFeature("API integrations", Icons.Default.Lock),
                PlanFeature("24/7 dedicated support", Icons.Default.Phone),
                PlanFeature("Custom automations", Icons.Default.Lock),
                PlanFeature("Advanced reporting", Icons.Default.Lock),
                PlanFeature("Team management", Icons.Default.Lock),
                PlanFeature("White-label solution", Icons.Default.Lock)
            )
        )
    )

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
                        onSelect = { selectedPlan = plan.id },
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
                    val selectedPlanName = plans.find { it.id == selectedPlan }?.name ?: "Unknown"
                    val selectedPlanPrice = plans.find { it.id == selectedPlan }?.price ?: "₹0"
                    Toast.makeText(
                        context,
                        "$selectedPlanName Plan Selected - $selectedPlanPrice",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Payment",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start ${plans.find { it.id == selectedPlan }?.name} Plan",
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