package com.example.meditox.screens.subscription

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SubscriptionScreen(navController: NavController) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Your Plan",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF128C7E),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock premium features to grow your medicine business",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        item {
            // Basic Plan - ₹199
            PricingCard(
                title = "Basic",
                price = "₹199",
                duration = "/month",
                features = listOf(
                    PlanFeature("Up to 500 medicines", Icons.Default.CheckCircle),//inventoru
                    PlanFeature("Basic reports", Icons.Default.Create),  //Assessment
                    PlanFeature("Email support", Icons.Default.Email),
                    PlanFeature("Stock alerts", Icons.Default.Notifications),
                    PlanFeature("Basic analytics", Icons.Default.Star)//barchart
                ),
                isPopular = false,
                buttonColor = Color(0xFF128C7E),
                onClick = {
                    Toast.makeText(context, "Basic Plan Selected - ₹199", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            // Premium Plan - ₹299 (Best Seller)
            PricingCard(
                title = "Premium",
                price = "₹299",
                duration = "/month",
                features = listOf(
                    PlanFeature("Unlimited medicines", Icons.Default.CheckCircle),
                    PlanFeature("Advanced reports", Icons.Default.CheckCircle),
                    PlanFeature("Priority support", Icons.Default.CheckCircle),
                    PlanFeature("Real-time analytics", Icons.Default.CheckCircle),
                    PlanFeature("Batch management", Icons.Default.CheckCircle),
                    PlanFeature("Multi-location support", Icons.Default.LocationOn),
                    PlanFeature("Custom branding", Icons.Default.CheckCircle)
                ),
                isPopular = true,
                buttonColor = Color(0xFF128C7E),
                onClick = {
                    Toast.makeText(context, "Premium Plan Selected - ₹299 (Best Seller!)", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            // Pro Plan - ₹399
            PricingCard(
                title = "Professional",
                price = "₹399",
                duration = "/month",
                features = listOf(
                    PlanFeature("Everything in Premium", Icons.Default.CheckCircle),
                    PlanFeature("24/7 phone support", Icons.Default.Phone),
                    PlanFeature("Advanced automations", Icons.Default.CheckCircle),
                    PlanFeature("Custom reports", Icons.Default.CheckCircle),
                    PlanFeature("Team management", Icons.Default.CheckCircle),
                    PlanFeature("White-label solution", Icons.Default.CheckCircle),
                    PlanFeature("Priority feature requests", Icons.Default.Star)
                ),
                isPopular = false,
                buttonColor = Color(0xFF128C7E),
                onClick = {
                    Toast.makeText(context, "Professional Plan Selected - ₹399", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            // Bottom Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF128C7E).copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = Color(0xFF128C7E),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All plans include free trial • Cancel anytime • Secure payments",
                        fontSize = 14.sp,
                        color = Color(0xFF128C7E),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PricingCard(
    title: String,
    price: String,
    duration: String,
    features: List<PlanFeature>,
    isPopular: Boolean,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPopular) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Popular Badge
            if (isPopular) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF128C7E),
                                    Color(0xFF075E54)
                                )
                            )
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Best Seller",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BEST SELLER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Best Seller",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Plan Title
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = price,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF128C7E)
                    )
                    Text(
                        text = duration,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Features List
                features.forEach { feature ->
                    FeatureRow(feature = feature)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subscribe Button
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPopular) {
                            Color(0xFF128C7E)
                        } else {
                            Color(0xFF128C7E).copy(alpha = 0.9f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isPopular) 4.dp else 2.dp
                    )
                ) {
                    Text(
                        text = if (isPopular) "Get Premium" else "Choose Plan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
            tint = Color(0xFF128C7E),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = feature.text,
            fontSize = 15.sp,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
    }
}

data class PlanFeature(
    val text: String,
    val icon: ImageVector
)