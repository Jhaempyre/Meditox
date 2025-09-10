package com.example.meditox.screens.addrelease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AddReleaseScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF128C7E)
                )

                IconButton(
                    onClick = { navController.navigate("inventory_history") }
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = "History",
                        tint = Color(0xFF128C7E)
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    text = "Quick Add",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate("quick_add") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = "Bulk Update",
                    icon = Icons.Default.Refresh,
                    onClick = { navController.navigate("bulk_update") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            ActionCard(
                title = "Add New Medicine",
                description = "Add new medicine to inventory with details",
                icon = Icons.Default.Add,
                onClick = { navController.navigate("add_new_medicine") }
            )
        }

        item {
            ActionCard(
                title = "Release Medicine",
                description = "Release medicine from stock for orders",
                icon = Icons.Default.Create,
                onClick = { navController.navigate("release_medicine") }
            )
        }

        item {
            ActionCard(
                title = "Update Stock",
                description = "Update existing medicine stock quantities",
                icon = Icons.Default.Edit,
                onClick = { navController.navigate("update_stock") }
            )
        }

        item {
            ActionCard(
                title = "Batch Management",
                description = "Manage medicine batches and expiry dates",
                icon = Icons.Default.AccountBox,
                onClick = { navController.navigate("batch_management") }
            )
        }

        // Recent Activity Section
        item {
            Text(
                text = "Recent Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(5) { index ->
            RecentActivityItem(
                action = if (index % 2 == 0) "Added" else "Updated",
                medicine = "Medicine ${index + 1}",
                quantity = "+${(10..50).random()}",
                time = "${(1..12).random()}h ago"
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF128C7E).copy(alpha = 0.1f),
            contentColor = Color(0xFF128C7E)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF128C7E).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF128C7E),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RecentActivityItem(
    action: String,
    medicine: String,
    quantity: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$action $medicine",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = quantity,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (action == "Added") Color.Green else Color(0xFF128C7E)
            )
        }
    }
}